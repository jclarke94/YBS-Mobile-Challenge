package com.JoelClarke.ybsmobilechallenge.util

import android.content.Context
import com.JoelClarke.ybsmobilechallenge.R

class BuddyConUtil {

    companion object {
        const val BUDDYCON_URL = "https://farm{icon-farm}.staticflickr.com/{icon-server}/buddyicons/{nsid}.jpg"
        const val BUDDYCON_BACKUP = "https://www.flickr.com/images/buddyicon.gif"

        fun getBuddyConURL(
            iconFarm : Int?,
            iconServer : String?,
            nsid : String,
            context : Context
        ) : String {
            iconFarm?.let { farm ->
                iconServer?.let { server ->
                    if (farm > 0 && server.isNotEmpty() && nsid.isNotEmpty()) {
//                        var url = BUDDYCON_URL
//                        url.replace("{icon-farm}", iconFarm.toString())
//                        url.replace("{icon-server}", iconServer)
//                        url.replace("{nsid}", nsid)

                        return context.getString(R.string.buddy_url, farm, server, nsid)
                    }
                }
            }
            return BUDDYCON_BACKUP
        }
    }

}