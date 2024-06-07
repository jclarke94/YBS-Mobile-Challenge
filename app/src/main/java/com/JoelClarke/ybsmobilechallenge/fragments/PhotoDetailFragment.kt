package com.JoelClarke.ybsmobilechallenge.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.JoelClarke.ybsmobilechallenge.MainActivity
import com.JoelClarke.ybsmobilechallenge.databinding.FragmentPhotoDetailBinding
import com.JoelClarke.ybsmobilechallenge.networking.NetworkManager
import com.JoelClarke.ybsmobilechallenge.networking.Networking
import com.JoelClarke.ybsmobilechallenge.networking.entity.PhotoTag
import com.JoelClarke.ybsmobilechallenge.networking.responses.PhotoDetailsResponse
import com.JoelClarke.ybsmobilechallenge.util.EndpointsUtil
import com.JoelClarke.ybsmobilechallenge.util.ListUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat
import java.util.Date

class PhotoDetailFragment : Fragment() {

    companion object {
        const val TAG = "_photoDetailFragment"

        const val ARGS_PHOTO_ID = "_photoId"
        const val ARG_URL = "_photoUrl"

        const val NET_GET_INFO = "_getInfoNetworkTag"

        fun newInstance(
            id : String,
            url : String
        ) : PhotoDetailFragment {
            var args = Bundle()
            args.putString(ARGS_PHOTO_ID, id)
            args.putString(ARG_URL, url)

            var fragment = PhotoDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val TAG_LONG = 20
    private val TAG_XLONG = 30
    private val TAG_MEDIUM = 10
    private val TAG_SHORT = 6

    private lateinit var binding : FragmentPhotoDetailBinding
    private val detailsViewModel : PhotoDetailViewModel by viewModels()

    private var networkingListener: NetworkManager.NetworkRequestListener<Any>? = null

    private lateinit var adapter : TagsAdapter
    private lateinit var layoutManager: GridLayoutManager

    private var spanCount : Int = 12


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentPhotoDetailBinding.inflate(inflater)
        val v = binding.root

        arguments?.let {
            detailsViewModel.photoId = it.getString(ARGS_PHOTO_ID)
            detailsViewModel.photoUrl = it.getString(ARG_URL)
        }

        binding.ibBack.setOnClickListener {
            (activity as MainActivity).onBackPressed()
        }

        adapter = TagsAdapter()
        layoutManager = GridLayoutManager(requireContext(), spanCount)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                var tagLength = detailsViewModel.tags[position].content.length

                return if (tagLength > TAG_XLONG) {
                    12
                } else if (tagLength > TAG_LONG) {
                    8
                } else if (tagLength > TAG_MEDIUM) {
                    6
                } else if (tagLength > TAG_SHORT) {
                    4
                } else {
                    3
                }
            }

        }
        binding.rvTags.adapter = adapter
        binding.rvTags.layoutManager = layoutManager

        Glide.with(requireContext())
            .load(detailsViewModel.photoUrl)
            .apply(RequestOptions().transform(FitCenter()))
            .into(binding.ivImage)

        setupObservers()
        detailsViewModel.photoId?.let {
            getPhotoDetails()
        }

        return v
    }

    override fun onStart() {
        super.onStart()

        networkingListener = object : NetworkManager.NetworkRequestListener<Any>() {
            override fun onComplete(request: NetworkManager.NetworkRequest<Any>) {
                when(request.tag) {
                    NET_GET_INFO -> {
                        detailsViewModel.photoDetailResponse.value = request.response as PhotoDetailsResponse?
                    }
                }

                // Make sure you finish a request so that it can be cleared from cache
                NetworkManager.getInstance()?.finish(request)
            }
        }

        NetworkManager.getInstance()?.addListener(arrayOf(
            NET_GET_INFO
        ), networkingListener!!)

        val lastPhotosResponse = NetworkManager.getInstance()?.getLatest(NET_GET_INFO)
        if (lastPhotosResponse != null) {
            detailsViewModel.photoDetailResponse.value = lastPhotosResponse.response as PhotoDetailsResponse?
            NetworkManager.getInstance()?.finish(lastPhotosResponse)
        }
    }

    override fun onStop() {
        super.onStop()

        networkingListener?.let {
            NetworkManager.getInstance()?.removeListener(
                arrayOf(
                    NET_GET_INFO
                ), it
            )
        }
    }

    fun setupObservers() {
        detailsViewModel.networkInFlight.removeObservers(viewLifecycleOwner)
        detailsViewModel.networkInFlight.observe(viewLifecycleOwner) {
            if (it) {
                (activity as MainActivity).showBlocker()
            } else {
                (activity as MainActivity).hideBlocker()
            }
        }

        detailsViewModel.photoDetailResponse.removeObservers(viewLifecycleOwner)
        detailsViewModel.photoDetailResponse.observe(viewLifecycleOwner) {
            detailsViewModel.networkInFlight.postValue(false)

            if (it != null && it.stat == EndpointsUtil.STAT_OK) {
                processPhotoDetails(it)
            } else {
                (activity as MainActivity).showError("Something went wrong and could not retrieve the photo details")
            }
        }
    }

    fun processPhotoDetails(response : PhotoDetailsResponse) {
        response.photo?.title?.content?.let {
            binding.tvTitle.text = it
        }

        response.photo?.description?.content?.let {
            binding.tvDescription.text = it
        }

        response.photo?.dates?.taken?.let {
            binding.tvDate.text = detailsViewModel.serverDateToDisplay(it)
        }

        detailsViewModel.tags.clear()
        response.photo?.tags?.tag?.let {
            detailsViewModel.tags.addAll(it)
            adapter.notifyDataSetChanged()
        }


    }

    fun getPhotoDetails() {
        if (detailsViewModel.networkInFlight.value == true) return
        detailsViewModel.networkInFlight.postValue(true)


        NetworkManager.getInstance()?.enqueue(
            NetworkManager.NetworkRequest(
                NET_GET_INFO,
                object : NetworkManager.NetworkRunnable<Any?>() {
                    override fun run(networkManager: NetworkManager, id: String): Any? {

                        return Networking.getInstance()?.fetchPhotoDetails(detailsViewModel.photoId!!)
                    }
                }))
    }

    inner class TagsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == ListUtil.TYPE_TAG) {
                ListUtil.createTagViewHolder(layoutInflater, parent)
            } else {
                ListUtil.DummyViewHolder(View(requireContext()))
            }
        }

        override fun getItemCount(): Int {
            return detailsViewModel.tags.size
        }

        override fun getItemViewType(position: Int): Int {
            super.getItemViewType(position)
            return ListUtil.TYPE_TAG
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder.itemViewType == ListUtil.TYPE_TAG) {
                var tHolder = holder as ListUtil.TagViewHolder
                tHolder.binding.tvTag.text = detailsViewModel.tags[position].content
            }
        }

    }

    class PhotoDetailViewModel : ViewModel() {
        var photoId : String? = null
        var photoUrl : String? = null

        val tags = mutableListOf<PhotoTag.PhotoTagDetails>()

        val networkInFlight = MutableLiveData<Boolean>()
        var photoDetailResponse = MutableLiveData<PhotoDetailsResponse>()

        var dateFormat = SimpleDateFormat("HH:mm d MMM yyyy")
        var serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        fun serverDateToDisplay(sDate : String) : String {
            var date : Date? = serverDateFormat.parse(sDate)
            return dateFormat.format(date)
        }

    }
}