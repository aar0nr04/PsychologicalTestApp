import android.os.Parcel
import android.os.Parcelable

data class Psychologist(
    val id: String,
    val name: String,
    val specialty: String,
    val location: String,
    val phone: String,
    val email: String,
    val description: String,
    val imageUrl: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(specialty)
        parcel.writeString(location)
        parcel.writeString(phone)
        parcel.writeString(email)
    }


    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Psychologist> {
        override fun createFromParcel(parcel: Parcel): Psychologist {
            return Psychologist(parcel)
        }

        override fun newArray(size: Int): Array<Psychologist?> {
            return arrayOfNulls(size)
        }
    }
}