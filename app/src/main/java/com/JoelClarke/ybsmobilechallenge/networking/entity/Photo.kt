package com.JoelClarke.ybsmobilechallenge.networking.entity

import com.google.gson.annotations.SerializedName

class Photo(
    val id : String,
    val owner : String,
    val title : String,
    val url_l : String,
    val ownerName : String,
    val tags : String?,
    @SerializedName("farm")
    val iconFarm : Int?,
    @SerializedName("server")
    val iconServer : String?,
    val height_l : Int?,
    val width_l : Int?
) {
}