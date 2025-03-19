package com.example.mohassu.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.mohassu.R
import com.example.mohassu.databinding.ViewPromiseBinding
import com.example.mohassu.model.Promise
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.*

class PromiseAdapter(
    private val context: Context,
    private val currentUserId: String,
    private val onEditClick: (Promise) -> Unit,
    private val onItemClick: (Promise) -> Unit
) : ListAdapter<Promise, PromiseAdapter.PromiseViewHolder>(PromiseDiffCallback()) {

    companion object {
        private const val TAG = "PromiseAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromiseViewHolder {
        val binding = ViewPromiseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PromiseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromiseViewHolder, position: Int) {
        val promise = getItem(position)
        holder.bind(promise)
    }

    inner class PromiseViewHolder(private val binding: ViewPromiseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(promise: Promise) {
            Log.d(TAG, "Binding Promise ID: ${promise.id}")

            // 프로필 이미지 설정
            Glide.with(context)
                .load(promise.hostProfileImageUrl)
                .circleCrop()
                .placeholder(R.drawable.img_basic_profile)
                .error(R.drawable.img_basic_profile)
                .into(binding.imgProfile)

            // 약속 제목 설정
            binding.tvPromiseTitle.text = when {
                promise.host?.id == currentUserId -> "내가 만든 약속"
                promise.participants.size <= 1 -> "${promise.hostNickname}님과의 약속"
                else -> "${promise.hostNickname}님 외 ${promise.participants.size - 1}명과의 약속"
            }

            // 약속 장소 설정
            val location = promise.location
            val geofenceName = location?.let { getGeofenceName(it) } ?: "지도 위 마커 장소에서"
            binding.tvPromiseLocation.text = "${geofenceName}에서"

            // 시간 포맷팅
            binding.tvPromiseTime.text = formatTimestamp(promise.time)

            // 지도 이미지 로드
            val staticMapUrl = generateStaticMapUrl(promise.location, promise.promiseType)
            if (staticMapUrl.isNotEmpty()) {
                val headers = LazyHeaders.Builder()
                    .addHeader("X-NCP-APIGW-API-KEY-ID", "fs9rplnpjv")
                    .addHeader("X-NCP-APIGW-API-KEY", "nkBj5b39jqUnW0OVh6IKylxZvBqpd0XUz8GvlkeC")
                    .build()
                Glide.with(context)
                    .load(GlideUrl(staticMapUrl, headers))
                    .into(binding.mapView)
            } else {
                binding.mapView.setImageResource(R.drawable.background_banner_white)
            }

            // 아이템 클릭 리스너 설정
            binding.root.setOnClickListener {
                onItemClick(promise)
            }

            // 수정 버튼 설정
            if (promise.host?.id == currentUserId) {
                binding.editPromiseButton.visibility = View.VISIBLE
                binding.editPromiseButton.setOnClickListener {
                    onEditClick(promise)
                }
            } else {
                binding.editPromiseButton.visibility = View.GONE
            }
        }
    }
    class PromiseDiffCallback : DiffUtil.ItemCallback<Promise>() {
        override fun areItemsTheSame(oldItem: Promise, newItem: Promise): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Promise, newItem: Promise): Boolean {
            return oldItem == newItem
        }
    }
    private fun generateStaticMapUrl(location: GeoPoint?, promiseType: String): String {
        if (location == null) return ""

        val latitude = location.latitude
        val longitude = location.longitude
        val zoom = 16
        val width = 400
        val height = 300
        val markerIcon = getMarkerIconUrl(promiseType)

        return "https://naveropenapi.apigw.ntruss.com/map-static/v2/raster?" +
                "center=$longitude,$latitude&level=$zoom&w=$width&h=$height" +
                "&markers=type:e|anchor=center|$markerIcon|pos:$longitude%$latitude"
    }
    private fun getMarkerIconUrl(promiseType: String): String {
        return when (promiseType) {
            //원래 firebase 연동 되어있던 것
            "밥약속" -> "icon:URL_TO_MEAL_MARKER"
            "술약속" -> "icon:URL_TO_DRINK_MARKER"
            "공부약속" -> "icon:URL_TO_STUDY_MARKER"
            else -> "icon:URL_TO_DEFAULT_MARKER"
        }
    }

    private fun getGeofenceName(location: GeoPoint): String {
        // 특정 위치 범위 확인 (예시)
        return "특정 장소 이름" // 실제 구현 필요
    }

    /**
     * 타임스탬프를 조건에 맞게 포맷팅
     */
    private fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return "시간 정보 없음"

        val date = timestamp.toDate()
        val now = Date()

        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dayFormat.format(now)
        val promiseDay = dayFormat.format(date)

        return when {
            today == promiseDay -> "오늘 ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}에 만나요"
            isTomorrow(now, date) -> "내일 ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}에 만나요"
            else -> SimpleDateFormat("MM월 dd일 HH:mm", Locale.getDefault()).format(date) + "에 만나요"
        }
    }

    /**
     * 내일인지 확인
     */
    private fun isTomorrow(now: Date, date: Date): Boolean {
        val calNow = Calendar.getInstance().apply { time = now; add(Calendar.DAY_OF_YEAR, 1) }
        val calDate = Calendar.getInstance().apply { time = date }
        return calNow.get(Calendar.YEAR) == calDate.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calDate.get(Calendar.DAY_OF_YEAR)
    }
}