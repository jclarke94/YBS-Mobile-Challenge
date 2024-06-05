package com.JoelClarke.ybsmobilechallenge.networking

import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.collection.ArrayMap
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.collections.ArrayList

/**
 * Network Manager
 * v1.0-k
 *
 * NetworkManager is a queue-based asynchronous Network utility designed to centralise the calling
 * surface of network requests inside an application.
 *
 * The core concepts behind NetworkManager are to solve common issues faced by Android developers
 * when performing networking calls:
 *
 * Network Manager abstracts away a network request from any calling party and internalises it as a
 * request that can be broadcast to multiple entities. This can prevent issues where an Activity or
 * Fragment has requested some data but has been destroyed before the async response has returned.
 * This is done through two mechanisms:
 *
 * First, the Network Manager does not accept direct callbacks for a given request. Instead each
 * Network Request must be tagged with a (hopefully) unique identifier. Parties interested in
 * receiving the Network Response can register their interest by adding a Network Manager Listener
 * to Network Manager against a given tag. When a Network Request responds and has a tag which a
 * listener is registered-against, that Listener will be triggered. As a result, if one Fragment
 * instance calls for a Network Request but is replaced by a different instance, that new instance
 * will still receive the callback and can take action appropriately.
 *
 * Secondly, Network Manager can build a cache of recent network request responses, identified by
 * their specified tag. If a calling party misses the network response callback for some reason
 * (i.e. the UI was not in a state to receive the callback, for example the Fragment was being re-
 * created at the exact moment the response comes back) that calling party can explicitly check to
 * see if there has been a Network Request cached and take action. ** It is important that when
 * receiving a response, the response is "finished" by passing it to the Network Manager finish()
 * function so that cache can be cleaned up more efficiently. **
 *
 * Network Manager also acts as a serial queue. Network Manager will only run a single enqueued
 * Network Request at a time. This may or may not be advantageous to your application's
 * requirements. If you require Network Requests to run immediately, you can also use Network
 * Manager and take advantage of the callback and caching mechanisms by using the dispatch()
 * function which will execute the Networking Request on a new thread.
 *
 * Network Thread is Networking Library agnostic, it allows for any Networking library to be
 * utilised. However, this means that you will need to provide the Networking code yourself.
 * Network Manager provides no opinion as to how you run the request, only that you provide a
 * response that can work with the network caching and callback features.
 *
 */

class NetworkManager(context: Context) {

    private val TAG = "NETWORK_MANAGER";

    private val queue : BlockingQueue<NetworkRequest<Any>> = LinkedBlockingQueue<NetworkRequest<Any>>()
    private var listeners : ArrayMap<String, ArrayList<NetworkRequestListener<Any>>> = ArrayMap()

    private var cache : ArrayMap<String, ArrayList<NetworkRequest<Any>>> = ArrayMap()

    private var handler : Handler? = null

    var haltQueue = false

    init {
        handler = Handler(context.mainLooper)
        sleep()
    }

    private fun processQueue() {
        if (haltQueue) {
            // If the breaks are on, do not process queue. Sleep instead.
            Log.d("NETWORK-MANAGER", "Queue has been halted. Sleeping...")
            sleep()
            return
        }

        //Log.d(TAG, "Processing queue...")
        var processed = false

        if (queue.size > 0) {
            Log.d(TAG, "Have item! Executing...")
            val request = queue.poll()
            if (request != null) {
                processed = true;
                executeRequest(request)
            }
        }

        // Attempt to clean up cache now
        cleanupCache()

        if (!processed){
            //Log.d(TAG, "Nothing to execute. Sleeping...")
            // If we didn't find anything, try again in a little bit.
            // Do NOT processQueue again as we'll stack-overflow, we're on Main thread
            sleep()
        }
    }

    private fun executeRequest(request : NetworkRequest<Any>) {
//        Log.d(TAG, "Executing request: " + request.id)
        Thread(Runnable {
            request.response = request.runnable?.run(this, request.id!!)

            handler?.post(Runnable {
//                Log.d(TAG, "Executed!")
                request.hasRun = true
                request.timeRan = System.currentTimeMillis()

                handleCache(request)
                triggerListeners(request)

                processQueue() // Process queue immediately, it will sleep if empty
            })
        }).start()
    }

    /**
     * Adds completed Requests to the cache, so long they can be cached
     */
    private fun handleCache(request : NetworkRequest<Any>) {
        if (request.timeToLive > 0 || request.timeToLive == -1L) {
            if (cache.containsKey(request.tag)) {
                cache.get(request.tag)?.add(request)
            } else {
                val initialList = ArrayList<NetworkRequest<Any>>()
                initialList.add(request)

                cache.put(request.tag, initialList)
            }
        }
    }

    /**
     * Cleans up items in the cache. If items have passed their TTL, they are removed from cache
     */
    private fun cleanupCache() {
        val time = System.currentTimeMillis()
        for (key in cache.keys) {
            val cacheList = cache.get(key)
            if (cacheList != null) {
                for (i in (cacheList.size - 1) downTo 0) {
                    val item = cacheList.get(i)
                    if (item.timeToLive != -1L) {
                        val expires = item.timeRan + item.timeToLive
                        if (time > expires) {
                            cacheList.removeAt(i)
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the oldest Network Request held in cache for a given tag
     */
    fun getOldest(tag : String) : NetworkRequest<Any>? {
        if (cache.containsKey(tag)) {
            val requests = cache.get(tag)
            if (requests != null) {
                if (requests.size > 0) {
                    return requests.get(requests.size - 1)
                }
            }
        }
        return null
    }

    /**
     * Get the latest Network Request held in cache for a given tag
     */
    fun getLatest(tag : String) : NetworkRequest<Any>? {
        if (cache.containsKey(tag)) {
            val requests = cache.get(tag)
            if (requests != null) {
                if (requests.size > 0) {
                    return requests.get(0)
                }
            }
        }
        return null
    }

    /**
     * Get all Network Requests held in cache for a given tag
     */
    fun getAll(tag : String) : ArrayList<NetworkRequest<Any>>? {
        if (cache.containsKey(tag)) {
            val requests = cache.get(tag)
            if (requests != null) {
                return requests
            }
        }
        return ArrayList()
    }

    /**
     * Enqueues a Network Request
     *
     * @return String   The ID of the request
     */
    fun enqueue(request : NetworkRequest<Any>) : String {
//        Log.d(TAG, "Enqueuing request...")
        val id = generateId()
        request.id = id;
        queue.put(request)
//        Log.d(TAG, "Enqueued request with ID: " + id)
        return id
    }

    /**
     * Immediately dispatches a Network Request, avoiding the queue
     *
     * @return String       The ID of the request
     */
    fun dispatch(request : NetworkRequest<Any>) : String {
        val id = generateId()
        request.id = id
        executeRequest(request)
        return id
    }

    /**
     * Cancels a Network Request if it is in the queue.
     *
     * This does NOT cancel any Network Request currently in flight
     *
     */
    fun cancel(id : String) {
        queue.forEach({ it : NetworkRequest<Any> ->
            if (it.id == id) {
                queue.remove(it)
            }
        })
    }

    /**
     * Cancels all network requests of a specified Tag
     */
    fun cancelAll(tag : String) {
        queue.forEach({
            if (it.tag == tag) {
                queue.remove(it)
            }
        })
    }

    fun notifyProgress(tag : String, id : String, progress : Long, total : Long) {
        Log.d(TAG, "Notify Progress: " + id + " :: " + progress + " :: " + total)
        handler?.post {
            if (listeners.containsKey(tag)) {
                var listenerList = listeners.get(tag)
                if (listenerList != null) {
                    if (listenerList.size > 0) {
                        for (i in (listenerList.size - 1) downTo 0) {
                            try {
                                listenerList.get(i).onProgress(id, progress, total)
                            } catch (e : Exception) {
                                e.printStackTrace()
                                // Do not remove listener here
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateId() : String {
        return UUID.randomUUID().toString()
    }

    /**
     * Triggers Network Request Listeners
     */
    private fun triggerListeners(request : NetworkRequest<Any>) {
        Log.d(TAG, "Triggering listeners for request: " + request.id + " :: Tag: " + request.tag)
        if (listeners.containsKey(request.tag)) {
            Log.d(TAG, "Have listeners registered for tag: " + request.tag)
            val listenerList = listeners.get(request.tag)
            if (listenerList != null) {
                Log.d(TAG, "ArrayList is not NULL")
                if (listenerList.size > 0) {
                    Log.d(TAG, "Array list was not empty...")
                    for (i in (listenerList.size - 1) downTo 0) {
                        try {
                            listenerList.get(i).onComplete(request)
                            Log.d(TAG, "Triggered listener!")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            listenerList.removeAt(i)
                        }
                    }
                }
            }
        }
    }

    fun addListener(tag : String, listener : NetworkRequestListener<Any>) {
        var createEntry = true;

        if (listeners.containsKey(tag)) {
            val listenerList = listeners.get(tag)
            if (listenerList !=  null) {
                listenerList.add(listener)
                createEntry = false
            }
        }

        if (createEntry) {
            val newListenerList = ArrayList<NetworkRequestListener<Any>>()
            newListenerList.add(listener)
            listeners.put(tag, newListenerList)
        }
    }

    fun addListener(tags : Array<String>, listener : NetworkRequestListener<Any>) {
        for (tag in tags) {
            addListener(tag, listener)
        }
    }

    fun removeListener(tag : String, listener : NetworkRequestListener<Any>) {
        if (listeners.containsKey(tag)) {
            if (listeners.get(tag) != null) {
                listeners.get(tag)?.remove(listener)
            }
        }
    }

    fun removeListener(tags : Array<String>, listener : NetworkRequestListener<Any>) {
        for (tag in tags) {
            removeListener(tag, listener)
        }
    }

    /**
     * Notifies a request by the tag and id is finished and needs to be cleaned up
     *
     * Method should be called once a cached network request has been finished with
     */
    fun finish(tag : String, id : String?) {
        if (cache.contains(tag)) {
            val requests = cache.get(tag);
            if (requests != null) {
                if (requests.size > 0) {
                    for (i in (requests.size - 1) downTo 0) {
                        if (requests.get(i).id == id || id == null) {
                            requests.removeAt(i)
                            cache.put(tag, requests)
                            break
                        }
                    }
                }
            }
        }
    }

    /**
     * Notifies all requests by a given tag
     *
     * Method should be called once a cached network request has been finished with
     */
    fun finishAll(tag : String) {
        finish(tag, null)
    }

    /**
     * Notifies a request is finished and needs to be cleaned up
     *
     * Method should be called once a cached network request has been finished with
     */
    fun finish(request : NetworkRequest<Any>) {
        finish(request.tag!!, request.id!!)
    }

    /**
     * Waits an arbitrary amount of time before attempting to process the queue
     */
    private fun sleep() {
        //Log.d(TAG, "NetworkManager loop sleeping. See you in 500ms...")
        handler?.postDelayed(Runnable {
            //Log.d(TAG, "NetworkManager awoke, processing queue...")
            processQueue()
        }, 500)
    }

    // Singleton
    companion object {
        @Volatile private var INSTANCE : NetworkManager? = null

        fun initialise(context : Context) : NetworkManager {
            val instance = INSTANCE
            if (instance != null) {
                return instance
            }

            return synchronized(this) {
                val instance2 = instance
                if (instance2 != null) {
                    instance2
                } else {
                    val newInstance = NetworkManager(context)
                    INSTANCE = newInstance
                    newInstance
                }
            }
        }

        fun getInstance() : NetworkManager? {
            return synchronized(this) {
                INSTANCE
            }
        }
    }

    abstract class NetworkRequestListener<T> {
        abstract fun onComplete(request : NetworkRequest<T>)
        open fun onProgress(id : String, progress : Long, total : Long) {
            // Stub
        }
    }

    class NetworkRequest<T>(var tag: String?, var runnable: NetworkRunnable<T?>?) {
        var id : String? = null
        var response : Any? = null
        var timeToLive : Long = -1
        var timeRan : Long = -1
        var hasRun : Boolean = false

        class Builder<T>(tag : String?, runnable : NetworkRunnable<T?>?) {
            val instance : NetworkRequest<T?> = NetworkRequest(tag, runnable)

            fun timeToLive(ttl : Long) : Builder<T> {
                instance.timeToLive = ttl
                return this
            }

            fun build() : NetworkRequest<T?> {
                return instance
            }
        }
    }

    abstract class NetworkRunnable<out T> {
        abstract fun run(networkManager: NetworkManager, id : String) : T
    }

}