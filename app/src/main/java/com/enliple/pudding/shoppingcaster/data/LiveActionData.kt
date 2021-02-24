package com.enliple.pudding.shoppingcaster.data

import com.google.gson.annotations.SerializedName

/**
 * Created by Kim Joonsung on 2019-05-10.
 */
data class LiveUserJoinData(@SerializedName("nickName") var nickName: List<String> = arrayListOf(),
                            @SerializedName("userId") var userId: List<String> = arrayListOf())

data class LiveUserActionData(@SerializedName("cartUserId") var cartUserId: String = "",
                              @SerializedName("cartUserNick") var cartUserNick: String = "",
                              @SerializedName("buyUserId") var buyUserId: String = "",
                              @SerializedName("buyUserNick") var buyUserNick: String = "",
                              @SerializedName("gubun") var gubun: String = "")
