package com.JoelClarke.ybsmobilechallenge.networking.entity

import com.google.gson.annotations.SerializedName

class PhotoDetail(
    val id : String,
    val title : InfoString,
    val description : InfoString,
    val dates : PhotoDates,
    val tags : PhotoTag?
) {
}