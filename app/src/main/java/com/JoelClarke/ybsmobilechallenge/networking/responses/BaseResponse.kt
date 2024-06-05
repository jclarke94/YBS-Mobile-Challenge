package com.JoelClarke.ybsmobilechallenge.networking.responses

import com.google.gson.annotations.SerializedName

open class BaseResponse {
    var responseCode : Int = 200
    var success : Boolean = false
    @SerializedName("message")
    var errorMessage : String? = null
}