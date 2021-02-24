package com.enliple.pudding.shoppingcaster.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Kim Joonsung on 2018-12-18.
 */
data class OLinkProductItem(@Expose @SerializedName("shopName") val shopName: String,
                            @Expose @SerializedName("sc_code") val scCode: String,
                            @Expose @SerializedName("site_url") val siteUrl: String)