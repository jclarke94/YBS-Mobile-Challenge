package com.JoelClarke.ybsmobilechallenge.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.JoelClarke.ybsmobilechallenge.MainActivity
import com.JoelClarke.ybsmobilechallenge.R
import com.JoelClarke.ybsmobilechallenge.databinding.FragmentUserPhotosBinding
import com.JoelClarke.ybsmobilechallenge.navigator.Navigator
import com.JoelClarke.ybsmobilechallenge.networking.NetworkManager
import com.JoelClarke.ybsmobilechallenge.networking.Networking
import com.JoelClarke.ybsmobilechallenge.networking.responses.PhotosResponse
import com.JoelClarke.ybsmobilechallenge.networking.responses.UserProfileResponse
import com.JoelClarke.ybsmobilechallenge.util.BuddyConUtil
import com.JoelClarke.ybsmobilechallenge.util.EndpointsUtil
import com.JoelClarke.ybsmobilechallenge.util.ListItem
import com.JoelClarke.ybsmobilechallenge.util.ListUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat
import java.util.Date


class UserPhotosFragment : Fragment() {

    companion object {
        const val TAG = "_userPhotosFragment"

        const val ARG_USER_ID = "_userID"

        const val NET_USER_INFO = "_userInfoNetTag"
        const val NET_USER_PHOTOS = "_userPhotos"

        fun newInstance(
            userId : String
        ) : UserPhotosFragment {
            var args = Bundle()
            args.putString(ARG_USER_ID, userId)

            var fragment = UserPhotosFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding : FragmentUserPhotosBinding
    private val userViewModel : UserViewModel by viewModels()

    private lateinit var adapter : PhotosAdapter
    private lateinit var layoutManager: GridLayoutManager

    private var networkingListener: NetworkManager.NetworkRequestListener<Any>? = null

    private var spanCount : Int = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentUserPhotosBinding.inflate(inflater)
        val v = binding.root

        arguments?.let {
            userViewModel.userId = it.getString(ARG_USER_ID)
        }

        binding.ibBack.setOnClickListener {
            (activity as MainActivity).onBackPressed()
        }

        adapter = PhotosAdapter()
        layoutManager = GridLayoutManager(requireContext(), spanCount)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = userViewModel.items[position].getListItemType()
                return when (viewType) {
                    ListUtil.TYPE_USER_PHOTO_PORTRAIT -> {
                        1
                    }

                    else -> {
                        spanCount
                    }
                }
            }

        }

        binding.rvItems.adapter = adapter
        binding.rvItems.layoutManager = layoutManager

        setupObservers()
        getUserInfo()

        return v
    }

    override fun onStart() {
        super.onStart()

        networkingListener = object : NetworkManager.NetworkRequestListener<Any>() {
            override fun onComplete(request: NetworkManager.NetworkRequest<Any>) {
                when(request.tag) {
                    NET_USER_INFO -> {
                        userViewModel.userInfoResponse.value = request.response as UserProfileResponse?
                    }
                    NET_USER_PHOTOS -> {
                        userViewModel.userPhotosResponse.value = request.response as PhotosResponse?
                    }
                }

                // Make sure you finish a request so that it can be cleared from cache
                NetworkManager.getInstance()?.finish(request)
            }
        }

        NetworkManager.getInstance()?.addListener(arrayOf(
            NET_USER_INFO,
            NET_USER_PHOTOS
        ), networkingListener!!)

        val lastPhotosResponse = NetworkManager.getInstance()?.getLatest(NET_USER_PHOTOS)
        if (lastPhotosResponse != null) {
            userViewModel.userPhotosResponse.value = lastPhotosResponse.response as PhotosResponse?
            NetworkManager.getInstance()?.finish(lastPhotosResponse)
        }

        val lastUserInfoResponse = NetworkManager.getInstance()?.getLatest(NET_USER_INFO)
        if (lastUserInfoResponse != null) {
            userViewModel.userInfoResponse.value = lastUserInfoResponse.response as UserProfileResponse?
            NetworkManager.getInstance()?.finish(lastUserInfoResponse)
        }
    }

    override fun onStop() {
        super.onStop()

        networkingListener?.let {
            NetworkManager.getInstance()?.removeListener(
                arrayOf(
                    NET_USER_INFO,
                    NET_USER_PHOTOS
                ), it
            )
        }
    }

    fun setupObservers() {
        userViewModel.networkInFlight.removeObservers(viewLifecycleOwner)
        userViewModel.networkInFlight.observe(viewLifecycleOwner) {
            if (it) {
                (activity as MainActivity).showBlocker()
            } else {
                (activity as MainActivity).hideBlocker()
            }
        }

        userViewModel.userInfoResponse.removeObservers(viewLifecycleOwner)
        userViewModel.userInfoResponse.observe(viewLifecycleOwner) { response ->
            userViewModel.networkInFlight.value = false
            if (response != null && response.stat == EndpointsUtil.STAT_OK) {
                processUserInfo(response)
            } else {
                (activity as MainActivity).showError("Something went wrong and could not retrieve user details")
            }
        }

        userViewModel.userPhotosResponse.removeObservers(viewLifecycleOwner)
        userViewModel.userPhotosResponse.observe(viewLifecycleOwner) { response ->
            userViewModel.networkInFlight.value = false
            if (response != null && response.stat == EndpointsUtil.STAT_OK) {
                processUserPhotos(response)
            } else {
                (activity as MainActivity).showError("Something went wrong and could not retrieve user photos")
            }
        }
    }

    private fun processUserInfo(response : UserProfileResponse) {
        Log.d(TAG, "processUserInfo. Network in flight: ${userViewModel.networkInFlight.value}")
        response.profile?.getFullName()?.let {
            binding.tvName.text = it
        }

        response.profile?.joinDate?.let {
            var date = Date()
            date.time = it * 1000
            var dateFormat = SimpleDateFormat("d MMM yyyy")
            binding.tvDateJoined.text = getString(R.string.user_joined, dateFormat.format(date))
        }

        response.profile?.city?.let {
            binding.tvCity.text = it
        }

        response.profile?.country?.let {
            binding.tvCountry.text = it
        }

        response.profile?.description?.let {
            binding.tvDescription.text = it
        }

        getUserPhotos()
    }

    private fun processUserPhotos(response : PhotosResponse) {
        if (response.photos?.page == 1) {
            userViewModel.items.clear()

            response.photos?.photo?.let {
                for (photo in it) {
                    if (userViewModel.iconFarm < 0 || userViewModel.iconServer.isNullOrEmpty()) {
                        photo.iconFarm?.let {
                            userViewModel.iconFarm = it
                        }

                        photo.iconServer?.let {
                            userViewModel.iconServer = it
                        }

                        userViewModel.userId?.let {
                            Glide.with(requireContext())
                                .load(BuddyConUtil.getBuddyConURL(
                                    userViewModel.iconFarm,
                                    userViewModel.iconServer,
                                    it,
                                    requireContext()
                                )).diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(binding.ivUserImage)
                        }

                    }

                    var imageHeight = 0
                    photo.height_l?.let {
                        imageHeight = it
                    }
                    var imageWidth = 0
                    photo.width_l?.let {
                        imageWidth = it
                    }
                    if (imageWidth > imageHeight) {
                        userViewModel.items.add(ListUtil.UserPhotoItem(photo))
                    } else {
                        userViewModel.items.add(ListUtil.UserPhotoPortraitItem(photo))
                    }

                }
            }

            adapter.notifyDataSetChanged()
        }
    }

    private fun getUserInfo() {
        if (userViewModel.networkInFlight.value == true) return
        userViewModel.networkInFlight.postValue(true)


        NetworkManager.getInstance()?.enqueue(
            NetworkManager.NetworkRequest(
                NET_USER_INFO,
                object : NetworkManager.NetworkRunnable<Any?>() {
                    override fun run(networkManager: NetworkManager, id: String): Any? {

                        return Networking.getInstance()?.fetchUserInfo(userViewModel.userId!!)
                    }
                }))
    }

    private fun getUserPhotos() {
        Log.d(TAG, "GET USER PHOTOS")
        if (userViewModel.networkInFlight.value == true) {
            Log.d(TAG, "GET USER PHOTOS stopped because network in flight")
            return
        }
        userViewModel.networkInFlight.postValue(true)


        NetworkManager.getInstance()?.enqueue(
            NetworkManager.NetworkRequest(
                NET_USER_PHOTOS,
                object : NetworkManager.NetworkRunnable<Any?>() {
                    override fun run(networkManager: NetworkManager, id: String): Any? {

                        return Networking.getInstance()?.fetchUserPhotos(userViewModel.userId!!)
                    }
                }))
    }

    inner class PhotosAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            when (viewType) {
                ListUtil.TYPE_USER_PHOTO -> {
                    return ListUtil.createUserPhotoViewHolder(layoutInflater, parent)
                }
                ListUtil.TYPE_USER_PHOTO_PORTRAIT -> {
                    return ListUtil.createUserPhotoPortraitViewHolder(layoutInflater, parent)
                }
                ListUtil.TYPE_LOADING -> {
                    return ListUtil.createLoadingViewHolder(layoutInflater, parent)
                }
                ListUtil.TYPE_SPACER -> {
                    return ListUtil.createSpacerViewHolder(layoutInflater, parent)
                }
            }
            return ListUtil.DummyViewHolder(View(requireContext()))
        }

        override fun getItemCount(): Int {
            return userViewModel.items.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                ListUtil.TYPE_USER_PHOTO -> {
                    val item = userViewModel.items[position] as ListUtil.UserPhotoItem
                    val pHolder = holder as ListUtil.UserPhotoViewHolder

                    item.photo?.url_l?.let {
                        Glide.with(requireContext())
                            .load(it)
                            .apply(RequestOptions().transform(CenterCrop()))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(pHolder.binding.ivImage)
                    }

                    pHolder.itemView.setOnClickListener {
                        //todo navigate to photo details
                    }
                }
                ListUtil.TYPE_USER_PHOTO_PORTRAIT -> {
                    val item = userViewModel.items[position] as ListUtil.UserPhotoPortraitItem
                    val pHolder = holder as ListUtil.UserPhotoPortraitViewHolder

                    item.photo?.url_l?.let {
                        Glide.with(requireContext())
                            .load(it)
                            .apply(RequestOptions().transform(CenterCrop()))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(pHolder.binding.ivImage)
                    }

                    pHolder.itemView.setOnClickListener {
                        Navigator.with(activity as MainActivity)
                            .backstackTag(TAG)
                            .fragment(PhotoDetailFragment.newInstance(
                                item.photo.id,
                                item.photo.url_l
                            )).transitionPreset(Navigator.TRANSITION_PRESET_SLIDE)
                            .navigate()
                    }
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            super.getItemViewType(position)
            return userViewModel.items[position].getListItemType()
        }

    }

    class UserViewModel : ViewModel() {
        var userId : String? = null
        var iconFarm : Int = -1
        var iconServer : String? = null


        var items = mutableListOf<ListItem>()

        val networkInFlight = MutableLiveData<Boolean>()
        val userPhotosResponse = MutableLiveData<PhotosResponse>()
        val userInfoResponse = MutableLiveData<UserProfileResponse>()
    }
}