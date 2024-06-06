package com.JoelClarke.ybsmobilechallenge.util

class EndpointsUtil {
    companion object {
        const val STAT_OK = "ok"
        const val STAT_FAIL = "fail"

        const val TEST = "flickr.test.echo"
        const val GET_RECENT = "flickr.photos.getRecent"
        const val GET_SEARCH = "flickr.photos.search"
        const val GET_INFO = "flickr.photos.getInfo"

        const val BUDDYCON_URL = "https://farm{icon-farm}.staticflickr.com/{icon-server}/buddyicons/{nsid}.jpg"
        const val BUDDYCON_BACKUP = "https://www.flickr.com/images/buddyicon.gif"
    }
}