package com.example.psychologicaltestapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAiApi {

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    fun getChatCompletion(@Body request: ChatRequest): Call<ChatResponse>
}
