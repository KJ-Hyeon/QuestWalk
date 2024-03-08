package com.hapataka.questwalk

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import coil.load
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.hapataka.questwalk.databinding.FragmentResultBinding
import com.hapataka.questwalk.domain.entity.HistoryEntity
import com.hapataka.questwalk.ui.quest.QuestData
import com.hapataka.questwalk.ui.result.ResultViewModel
import com.hapataka.questwalk.util.BaseFragment
import okhttp3.internal.notify


class ResultFragment : BaseFragment<FragmentResultBinding>(FragmentResultBinding::inflate),
    OnMapReadyCallback {
    private val resultViewModel: ResultViewModel by viewModels()
    private var userId: String? = null
    private var keyword: String? = null
    private lateinit var googleMap: GoogleMap
//    private lateinit var result: HistoryEntity.ResultEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString("userId")
            keyword = it.getString("keyword")
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getInfo()
        dataObserve()

        binding.fragMap.onCreate(savedInstanceState)
        binding.fragMap.getMapAsync(this)
    }

    private fun dataObserve() {
        with(resultViewModel) {
            resultItem.observe(viewLifecycleOwner) {
                if (::googleMap.isInitialized) {
                    MapsInitializer.initialize(requireContext())
                    updateLocation(googleMap, it)
                }
                initViews(it)
                resultViewModel.getQuestByKeyword(it.quest)
            }
            questItem.observe(viewLifecycleOwner) {
                initImageViews(it)
            }
            completeRate.observe(viewLifecycleOwner) {
                binding.tvCompleteRate.text = "$it"
            }
        }
    }

    private fun getInfo() {
        if (userId != null && keyword != null) {
            resultViewModel.getResultHistory(userId!!, keyword!!)
        }
    }

    private fun initViews(result: HistoryEntity.ResultEntity) {
        with(binding) {
            if (result.questImg != null) {
                Log.d("ResultFragment:","Image Not Null!!!!!")
                ivQuestImage.load(result.questImg)
                tvAdvTime.text = result.time.toString()
                tvAdvDistance.text = "${result.distance}"
                tvTotalSteps.text = "${result.step}"
                tvCalories.text = "Zero"
                tvQuestKeyword.text = result.quest
            } else {
                Log.d("ResultFragment:","Image Null!!!!!")
                ivQuestImage.load(R.drawable.image_fail)
                tvAdvTime.text = result.time.toString()
                tvAdvDistance.text = "${result.distance}"
                tvTotalSteps.text = "${result.step}"
                tvCalories.text = "Zero"
                tvQuestKeyword.text = result.quest
            }
        }
//        this.result =result
//        if (::googleMap.isInitialized) {
//            MapsInitializer.initialize(this.requireContext())
//            updateLocation(googleMap, result)
//        }
    }

    private fun initImageViews(questItem: QuestData) {
        val imageList = listOf(
            binding.ivImage1,
            binding.ivImage2,
            binding.ivImage3,
            binding.ivImage4
        )

        imageList.forEach { it.load(R.drawable.image_empty) }

        questItem.successItems.reversed().take(4).forEachIndexed { index, successItem ->
            imageList[index].load(successItem.imageUrl){
                crossfade(true)
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        MapsInitializer.initialize(this.requireContext())
//        updateLocation(googleMap, result)
    }

    private fun updateLocation(p0: GoogleMap, result: HistoryEntity.ResultEntity) {
        Log.d("check", result.toString())
        var preLocation : Pair<Float, Float>? = null
        val resultLati = result.locations?.map { it.first } ?: listOf()
        val resultLongi = result.locations?.map {it.second} ?: listOf()
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
            LatLngBounds(
                LatLng(resultLati.minOf{it}.toDouble(),resultLongi.minOf{it}.toDouble()),
                LatLng(resultLati.maxOf { it }.toDouble(), resultLongi.maxOf { it }.toDouble()),
            ), 100
        )
        p0.animateCamera(cameraUpdate)

        for (location in resultLati.zip(resultLongi)){
            Log.d("위치정보",  "위도: ${location.first.toDouble()} 경도: ${location.second.toDouble()}")
            if(preLocation!=null){
                Log.d("check", "${location.first.toDouble()} ${location.second.toDouble()} ${preLocation?.first?.toDouble()} ${preLocation?.second?.toDouble()}")
                var polyline = p0.addPolyline(
                    PolylineOptions()
                        .clickable(true)
                        .add(
                            LatLng(preLocation!!.first.toDouble(), preLocation!!.second.toDouble()),
                            LatLng(location.first.toDouble(), location.second.toDouble())
                        )
                )
                polyline.width = 15.0F
                polyline.color = Color.BLACK
                polyline.jointType = JointType.ROUND
                polyline.startCap=RoundCap()
                polyline.endCap= RoundCap()
            }
            preLocation=location
        }
    }
}