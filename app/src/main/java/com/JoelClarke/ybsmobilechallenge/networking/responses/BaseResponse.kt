package com.JoelClarke.ybsmobilechallenge.networking.responses

import com.google.gson.annotations.SerializedName

open class BaseResponse {
    var code : Int = 200
    var stat : String? = null
    @SerializedName("message")
    var errorMessage : String? = null
}