package com.enliple.pudding.shoppingcaster.data

import com.google.gson.annotations.SerializedName

/**
 * 방송간 판매할 상품 리스트 아이템
 */
data class LiveProductsItem(@SerializedName("idx") val idx: String,
                            @SerializedName("sc_code") val sc_code: String,
                            @SerializedName("sitename") val sitename: String,
                            @SerializedName("pcode") val pcode: String,
                            @SerializedName("image1") val image1: String,
                            @SerializedName("width") val width: String,
                            @SerializedName("height") val height: String,
                            @SerializedName("title") val title: String,
                            @SerializedName("orgprice") val orgprice: String,
                            @SerializedName("price") val price: String,
                            @SerializedName("shoptree_yn") val shoptree_yn: String)