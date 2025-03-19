package com.example.mohassu.Model

import com.naver.maps.geometry.LatLng

data class PlaceInfo(
    val name: String,
    val location: LatLng,
    val radius: Float
)