package com.JoelClarke.ybsmobilechallenge.networking

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import androidx.collection.ArrayMap
import com.JoelClarke.ybsmobilechallenge.BuildConfig
import com.JoelClarke.ybsmobilechallenge.networking.responses.BaseResponse
import com.JoelClarke.ybsmobilechallenge.networking.responses.PhotosResponse
import com.JoelClarke.ybsmobilechallenge.util.ACTION_STATUS_CODE
import com.JoelClarke.ybsmobilechallenge.util.EndpointsUtil
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.BufferedSink
import java.net.HttpURLConnection
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
//import com.JoelClarke.ybsmobilechallenge.BuildConfig

/**
 *  Networking singleton
 *
 *  Features a collection of helpful boilerplate that will get you communicating with the internet
 *  in no time at all.
 *
 *
 */
class Networking(private val context : Context) {

    private val BASE_URL = BuildConfig.BASE_URL

    private val okHttpClient : OkHttpClient = OkHttpClient.Builder().callTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()
    private val gson = Gson()
    private var handler : Handler? = null

    // If your system relies on authentication, you can store an authentication token here
    private var authenticationToken : String? = null

    init {
        handler = Handler(context.mainLooper)
    }

    /**
     * Sets the current Authentication Token
     */
    fun setAuthenticationToken(token : String?) {
        authenticationToken = token
    }

    /**
     * Gets Google from Network
     */
    fun getGoogle() : Response? {
        return performNetworking("https://www.google.com/", null)
    }

    /**
     * test connection to the api to get photos
     */
    fun testPhotos() : PhotosResponse? {

        var uri = Uri.Builder()
            .scheme("https")
            .authority(BASE_URL)
            .appendPath("services")
            .appendPath("rest/")
            .appendQueryParameter("method", EndpointsUtil.GET_RECENT)
            .appendQueryParameter("api_key", BuildConfig.API_KEY)
            .appendQueryParameter("extras", "url_l")
            .appendQueryParameter("format", "json")
            .build()


        return performBasicNetworking(uri.toString(), null, PhotosResponse::class)
    }

    fun getRecent() : PhotosResponse? {

        var uri = Uri.Builder()
            .scheme("https")
            .authority(BASE_URL)
            .appendPath("services")
            .appendPath("rest/")
            .appendQueryParameter("method", EndpointsUtil.GET_RECENT)
            .appendQueryParameter("api_key", BuildConfig.API_KEY)
            .appendQueryParameter("extras", "url_l")
            .appendQueryParameter("format", "json")
            .build()


        return performBasicNetworking(uri.toString(), null, PhotosResponse::class)
    }

    // ------------------------------
    //  INTERNAL
    // ------------------------------

    /**
     * Returns a path relative to the current BASE_URL
     */
    fun url(suffix : String) : String {
        return BASE_URL + suffix
    }

    /**
     * Determines whether or not a given URL relates to the current BASE_URL
     */
    fun isMainUrl(uri : Uri?) : Boolean {
        if (uri != null) {
            val mainUri = Uri.parse(BASE_URL)
            return mainUri.host == uri.host
        }
        return false
    }

    /**
     * Performs a common networking process. Requests a URL, receives the response and performs a
     * GSON parse to the given class.
     */
    private fun <T : BaseResponse> performBasicNetworking(url : String, body : RequestBody?, outClass : KClass<T>) : T? {
        try {
            val requestCall = performNetworking(url, body)
            val out = gson.fromJson(requestCall?.body?.string(), outClass.java)
            out.responseCode = requestCall?.code ?: HttpURLConnection.HTTP_OK
            return out
        } catch (exception : Exception) {
            exception.printStackTrace()
        }
        return null
    }

    /**
     * Performs networking and returns a OkHttp Response
     */
    fun performNetworking(url : String, body : RequestBody?, attemptRetry : Boolean = true, cookies : String? = null) : Response? {
        val rc = performNetworkingForCall(url, body, attemptRetry, cookies)
        if (rc != null) {
            return rc.response
        }
        return null
    }

    /**
     * Performs Networking and returns the Response and Call, useful for file uploads
     */
    private fun performNetworkingForCall(url : String, requestBody : RequestBody?, attemptRetry : Boolean = true, cookies : String? = null) : RespAndCall? {
        val builder = Request.Builder().url(url)

        if (requestBody != null) {
            builder.post(requestBody)
        }

        var isMainUrl = false
        try {
            val uri = Uri.parse(url)
            isMainUrl = isMainUrl(uri)
        } catch (e : Exception) {
            e.printStackTrace()
        }

        // If we have an Authentication Token, pass it with the request
//        if (isMainUrl && this.authenticationToken != null) {
//            builder.addHeader("Authorization", "Bearer " + this.authenticationToken)
//        }

        if (cookies != null) {
            builder.addHeader("Cookie", cookies)
        }




        try {
            val request = builder.build()
            val call = okHttpClient.newCall(request)
            val response : Response = call.execute()

            // TODO If your application has the ability to refresh tokens, you may use this code path to refresh the token now
            if (isMainUrl && attemptRetry && response.code == HttpURLConnection.HTTP_UNAUTHORIZED && authenticationToken != null) {
                val result = attemptTokenRenewal()
                if (result) {
                    return performNetworkingForCall(url, requestBody, false)
                }
            }

            // Broadcast the response status code
            broadcastStatusCode(context, url, response.code)

            return RespAndCall(response, call)
        } catch (ex : Exception) {
            ex.printStackTrace()
        }

        return null
    }

    /**
     * Do not run on Main Thread!
     *
     * Attempts to renew the current Customer Token in a seemless manner to the API request
     */
    private fun attemptTokenRenewal() : Boolean {
        var out = false

        NetworkManager.getInstance()?.haltQueue = true

        try {
            //todo authToken needed?
        } catch (e : Exception) {
            e.printStackTrace()
        }

        NetworkManager.getInstance()?.haltQueue = false

        return out
    }

    data class RespAndCall(val response : Response, val call : Call)

    companion object {
        @Volatile private var INSTANCE : Networking? = null

        /**
         * Initialises the Networking singleton. Since we are requesting a Context, make sure you
         * are initialising the singleton with the Application Context inside the Application Context
         * to avoid memory leaks.
         */
        fun initialise(context : Context) : Networking {
            val instance = INSTANCE
            if (instance != null) {
                return instance
            }

            return synchronized(this) {
                val instance2 = instance
                if (instance2 != null) {
                    instance2
                } else {
                    val newInstance = Networking(context)
                    INSTANCE = newInstance
                    newInstance
                }
            }
        }

        /**
         * Gets the current instance of the singleton
         */
        fun getInstance() : Networking? {
            return synchronized(this) {
                INSTANCE
            }
        }

        /**
         * Broadcasts a Request Status Code to parties that want to hear them
         */
        fun broadcastStatusCode(context : Context, url : String, statusCode : Int) {
            val statusCodeIntent = Intent(ACTION_STATUS_CODE)
            statusCodeIntent.putExtra("url", url)
            statusCodeIntent.putExtra("code", statusCode)
            statusCodeIntent.setPackage(BuildConfig.APPLICATION_ID)
            context.sendBroadcast(statusCodeIntent)
        }
    }

    /**
     * A JSON request body. Outputs a request formatted in application/json format.
     */
    class JsonBody(val jsonContent : String) : RequestBody() {
        override fun contentType(): MediaType? {
            return "application/json; charset=utf-8".toMediaTypeOrNull()
        }

        override fun writeTo(sink: BufferedSink) {
            sink.writeString(jsonContent, Charset.defaultCharset())
        }

        class Builder() {
            private val gson = Gson()
            private val map = ArrayMap<String, Any?>()

            /**
             * Add a property to the JSON body
             */
            fun add(key : String, data : Any?) : Builder {
                map.put(key, data)
                return this
            }

            /**
             * Builds the JSON string to send
             */
            fun build() : JsonBody {
                var out = "{}"

                try {
                    out = gson.toJson(map)
                } catch (e : Exception) {
                    e.printStackTrace()
                }

                return JsonBody(out)
            }
        }
    }

}