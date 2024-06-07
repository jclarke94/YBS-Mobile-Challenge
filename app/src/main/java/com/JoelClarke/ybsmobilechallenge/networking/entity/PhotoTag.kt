package com.JoelClarke.ybsmobilechallenge.networking.entity

import com.google.gson.annotations.SerializedName

class PhotoTag(
    var tag : Array<PhotoTagDetails>?
) {

    class PhotoTagDetails(
        var id : String,
        @SerializedName("_content")
        var content : String
    ){}
}