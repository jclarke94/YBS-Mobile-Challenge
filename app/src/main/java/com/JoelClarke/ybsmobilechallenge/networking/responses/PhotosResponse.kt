package com.JoelClarke.ybsmobilechallenge.networking.responses

import com.JoelClarke.ybsmobilechallenge.networking.entity.Photo

class PhotosResponse: BaseResponse() {
    val photos : PhotosData? = null

    class PhotosData {
        val page : Int = 0
        val pages : Int = 0
        val perpage : Int = 0
        val total : Int = 0
        val photo : Array<Photo>? = null
    }
}