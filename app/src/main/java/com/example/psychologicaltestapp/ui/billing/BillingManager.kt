package com.example.psychologicaltestapp.ui.billing

import android.app.Activity
import com.android.billingclient.api.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.functions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BillingManager(
    private val activity: Activity,
    private val db: com.google.firebase.firestore.FirebaseFirestore = Firebase.firestore,
) : PurchasesUpdatedListener {

    private val billingClient: BillingClient = BillingClient.newBuilder(activity)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    /** Conecta y espera a que Billing esté listo */
    suspend fun connect() {
        if (billingClient.isReady) return
        suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    cont.resume(Unit)
                }
                override fun onBillingServiceDisconnected() { /* la lib reintenta sola */ }
            })
        }
    }

    /** Consulta productos (INAPP + SUBS) definidos en Play Console */
    suspend fun queryProducts(): Map<String, ProductDetails> {
        val inappIds = listOf("credits_10", "daypass_24h", "test_beck")
        val subsIds  = listOf("sub_premium_monthly")

        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inappIds.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it).setProductType(BillingClient.ProductType.INAPP).build()
            }).build()

        val subsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subsIds.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it).setProductType(BillingClient.ProductType.SUBS).build()
            }).build()

        val inapp = billingClient.queryProductDetails(inAppParams).productDetailsList ?: emptyList()
        val subs  = billingClient.queryProductDetails(subsParams).productDetailsList ?: emptyList()
        return (inapp + subs).associateBy { it.productId }
    }

    /** Lanza el flujo de compra */
    fun launchPurchase(productDetails: ProductDetails) {
        val offerToken: String? = if (productDetails.productType == BillingClient.ProductType.SUBS)
            productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        else null

        val pdParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .apply { if (offerToken != null) setOfferToken(offerToken) }
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(pdParams))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    /** Callback de compras */
    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode != BillingClient.BillingResponseCode.OK || purchases == null) return
        purchases.forEach { handlePurchase(it) }
    }

    private fun isSubscription(p: Purchase) = p.products.any { it.startsWith("sub_") }

    /** Mapea compra -> documento pending en Firestore */
    private fun mapPurchaseToPendingDoc(p: Purchase): Map<String, Any?> {
        val productId = p.products.firstOrNull().orEmpty()
        val base = mutableMapOf<String, Any?>(
            "userId" to FirebaseAuth.getInstance().currentUser?.uid,
            "platform" to "play",
            "purchaseToken" to p.purchaseToken,
            "createdAt" to FieldValue.serverTimestamp(),
            "status" to "pending"
        )
        when (productId) {
            "credits_10"          -> base += mapOf("type" to "credits", "quantity" to 10)
            "daypass_24h"         -> base += mapOf("type" to "daypass", "dayPassHours" to 24)
            "test_beck"           -> base += mapOf("type" to "test", "testId" to "beck")
            "sub_premium_monthly" -> base += mapOf("type" to "subscription", "plan" to "premium_monthly")
            else                  -> base += mapOf("type" to "credits", "quantity" to 1, "productId" to productId)
        }
        return base
    }

    /** INAPP: consume para permitir recompras del mismo ítem */
    private fun consumeIfInapp(p: Purchase) {
        if (isSubscription(p)) return
        val params = ConsumeParams.newBuilder().setPurchaseToken(p.purchaseToken).build()
        billingClient.consumeAsync(params) { _, _ -> }
    }

    /** SUBS/no-consumibles: acknowledge */
    private fun acknowledgeIfNeeded(p: Purchase) {
        if (p.purchaseState == Purchase.PurchaseState.PURCHASED && !p.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()
            billingClient.acknowledgePurchase(params) { }
        }
    }

    /** Flujo tras compra: acknowledge/consume + doc pending + Cloud Function */
    private fun handlePurchase(purchase: Purchase) {
        if (isSubscription(purchase)) acknowledgeIfNeeded(purchase) else consumeIfInapp(purchase)

        val data = mapPurchaseToPendingDoc(purchase)
        db.collection("purchases")
            .add(data)
            .addOnSuccessListener { doc: DocumentReference ->
                Firebase.functions
                    .getHttpsCallable("purchaseApply")
                    .call(mapOf("purchaseId" to doc.id))
                    .addOnSuccessListener { /* refresca UI */ }
                    .addOnFailureListener { /* queda en pending para reintentar */ }
            }
    }
}
