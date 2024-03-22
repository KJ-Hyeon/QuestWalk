package com.hapataka.questwalk.data.fusedlocation.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.hapataka.questwalk.domain.entity.LocationEntity
import com.hapataka.questwalk.domain.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationRepositoryImpl(context: Context) : LocationRepository {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }
    private val request by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000).build()
    }
    private lateinit var locationCallback: LocationCallback
    private var prevLocation: Location? = null
    private val geocoder = Geocoder(context)

    @SuppressLint("MissingPermission")
    override fun startRequest(callback: (LocationEntity) -> Unit) {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val currentLocation = result.locations.last()
                val moveDistance =
                    currentLocation.distanceTo(
                        if (prevLocation != null) prevLocation!! else currentLocation
                    )

                prevLocation = result.lastLocation

                if (currentLocation.hasAccuracy().not()) {
                    return
                }

                if (currentLocation.accuracy > 30) {
                    return
                }

                if (currentLocation.accuracy * 1.5 < moveDistance) {
                    return
                }

                callback(
                    LocationEntity(
                        Pair(
                            currentLocation.latitude.toFloat(),
                            currentLocation.longitude.toFloat()
                        ),
                        moveDistance
                    )
                )
            }
        }.also { locationCallback = it }
        client.requestLocationUpdates(request, locationCallback, Looper.myLooper()!!)
    }

    override fun finishRequest() {
        client.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrent(): LocationEntity = withContext(Dispatchers.IO) {
        val location = client.lastLocation.await()
        val latitude = location.latitude.toFloat()
        val longitude = location.longitude.toFloat()
        val distance = location.distanceTo(
            if (prevLocation != null) prevLocation!! else location
        )

        LocationEntity(
            Pair(latitude, longitude),
            distance
        )

    }

    @SuppressLint("MissingPermission")
    override suspend fun getAddress() = withContext(Dispatchers.IO) {
//        try {
//            val location = client.lastLocation.await()
//            val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
//            if (!addressList.isNullOrEmpty()) {
//                addressList[0]
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
        val location = client.lastLocation.await()
        val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        addressList?.get(0)?.getAddressLine(0) ?: "No Address"
    }
}