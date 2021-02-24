package com.enliple.pudding.shoppingcaster.data

import com.google.gson.annotations.SerializedName

/**
 * 방송간 판매할 상품 리스트 아이템
 */
data class LiveStoreItem(@SerializedName("shopKey") val shopKey: String,
                         @SerializedName("strShopName") val strShopName: String,
                         @SerializedName("strImageUrl") val strImageUrl: String)