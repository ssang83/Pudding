package com.enliple.pudding.commons.data

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * 단일 카테고리 아이템
 */
data class CategoryItem(val recordId: Long?,
                        @Expose @SerializedName("categoryId") val categoryId: String,
                        @Expose @SerializedName("categoryName") val categoryName: String,
                        @Expose @SerializedName("categoryRgb") val categoryRgb: String,
                        @Expose @SerializedName("categoryHex") val categoryHex: String,
                        @Expose @SerializedName("categoryImage") val categoryImage: String) : Parcelable {

    companion object CREATOR : Parcelable.Creator<CategoryItem> {
        override fun createFromParcel(parcel: Parcel): CategoryItem {
            return CategoryItem(parcel)
        }

        override fun newArray(size: Int): Array<CategoryItem?> {
            return arrayOfNulls(size)
        }
    }

    constructor(parcel: Parcel) : this(
            parcel.readValue(Long::class.java.classLoader) as? Long,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeValue(recordId)
        dest?.writeString(categoryId)
        dest?.writeString(categoryName)
        dest?.writeString(categoryRgb)
        dest?.writeString(categoryHex)
        dest?.writeString(categoryImage)
    }

    override fun describeContents(): Int = 0

    override fun toString(): String {
        return "[CategoryItem] categoryId:$categoryId, categoryName:$categoryName, categoryImage:$categoryImage"
    }
}