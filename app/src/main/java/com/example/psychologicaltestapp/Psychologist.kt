import android.os.Parcel
import android.os.Parcelable

data class Psychologist(
    val id: String,
    val name: String,
    val specialty: String,
    val imageUrl: String,
    val description: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
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
        parcel.writeString(imageUrl)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Psychologist> {
        override fun createFromParcel(parcel: Parcel): Psychologist {
            return Psychologist(parcel)
        }

        override fun newArray(size: Int): Array<Psychologist?> {
            return arrayOfNulls(size)
        }
    }
}