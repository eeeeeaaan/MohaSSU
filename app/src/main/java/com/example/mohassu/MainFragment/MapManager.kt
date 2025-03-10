package com.example.mohassu.MainFragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.mohassu.R
import com.example.mohassu.ViewModel.MainHomeViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.model.mutation.Overlay
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.bumptech.glide.request.transition.Transition


class MapManager(
    private val fragment: Fragment,
    private val naverMap: NaverMap,
    private val locationSource: FusedLocationSource?,
    private val requestPermissionLauncher: ActivityResultLauncher<String>,
    private val viewModel: MainHomeViewModel
) {

    init {
        setupMap()
    }

    private fun setupMap() {
        naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(0.0, 0.0)))
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isZoomControlEnabled = false

        when {
            ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> {
                naverMap.locationTrackingMode = LocationTrackingMode.Follow
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        setupCameraChangeListener()
        setupMapClickListener()

        //
        initializeMyMarker()
        setupLocationChangeListener()
    }

    private fun setupCameraChangeListener() {
        naverMap.addOnCameraChangeListener { reason, _ ->
            if (reason == CameraUpdate.REASON_GESTURE) {
                viewModel.setCameraMovedByUser(true)
                viewModel.setMyMarkerClicked(false)
                viewModel.setFriendMarkerClicked(false)

                if (viewModel.isFocusMode.value == true) {
                    (fragment as MainHomeFragment1).resetMarkerFocusMode()
                }

                removeOverlayViews()
            }
        }
    }

    private fun setupMapClickListener() {
        naverMap.onMapClickListener = NaverMap.OnMapClickListener { _, _ ->
            if (viewModel.isEditTextClicked.value == false) {
                viewModel.setMyMarkerClicked(false)
                viewModel.setFriendMarkerClicked(false)

                if (viewModel.isFocusMode.value == true) {
                    (fragment as MainHomeFragment1).resetMarkerFocusMode()
                }

                removeOverlayViews()
            }
        }
    }

    private fun removeOverlayViews() {
        val removableViewIds = listOf(
            R.id.dialog_edit_message,
            R.id.dialog_text_message,
            R.id.fragment_status_banner,
            R.id.dialog_show_profile
        )

        val possibleContainers = listOfNotNull(
            fragment.requireActivity().findViewById<ViewGroup>(R.id.fragment_map),
            fragment.requireActivity().findViewById<ViewGroup>(R.id.dialog_text_message),
            fragment.requireActivity().findViewById<ViewGroup>(R.id.dialog_edit_message),// 다이얼로그 전체 루트
            fragment.requireActivity().findViewById<ViewGroup>(R.id.fragment_status_banner),
            fragment.requireActivity().findViewById<ViewGroup>(R.id.dialog_show_profile)
        )


        possibleContainers.forEach { container ->
            removableViewIds.forEach { id ->
                container.findViewById<View>(id)?.let { container.removeView(it) }
            }
        }
    }



    private fun setupLocationChangeListener() {
        naverMap.addOnLocationChangeListener { location: Location ->
            if (locationMarker == null) {
                Log.d("MapManager", "locationMarker is not initialized yet")
                return@addOnLocationChangeListener
            }

            // 서버 연동
//            updateUserLocationToFirestore(location)
//            loadUserLocationFromFirestore()
            naverMap.locationOverlay.isVisible = false // 오버레이 비활성화
        }
    }

    private fun initializeMyMarker() {
        val sharedPreferences = fragment.requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val photoUrl = sharedPreferences.getString("photoUrl", "") ?: ""

        val myMarkerView = LayoutInflater.from(fragment.requireContext()).inflate(R.layout.view_marker_my, null)
        val myProfile = myMarkerView.findViewById<ImageView>(R.id.my_marker_image)

        if (photoUrl.isNotEmpty()) {
            Glide.with(fragment)
                .load(Uri.parse(photoUrl))
                .circleCrop()
                .placeholder(R.drawable.img_basic_profile)
                .error(R.drawable.img_basic_profile)
                .into(object : CustomTarget<Drawable?>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        myProfile.setImageDrawable(resource)
                        addMarkerToMap(myMarkerView)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        } else {
            myProfile.setImageResource(R.drawable.img_basic_profile)
            addMarkerToMap(myMarkerView)
        }
    }

    private fun addMarkerToMap(myMarkerView: View) {
        val myMarkerBitmap = convertViewToBitmap(myMarkerView)

        locationMarker = Marker().apply {
            position = naverMap.locationOverlay.position
            icon = OverlayImage.fromBitmap(myMarkerBitmap)
            width = dpToPx(60)
            height = dpToPx(70)
            map = naverMap

            onClickListener = Overlay.OnClickListener {
                handleMarkerClick()
                true
            }
        }

    }

    private fun dpToPx(dp: Int): Int {
        val density = fragment.requireContext().resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun convertViewToBitmap(view: View): Bitmap {
        val width = dpToPx(120)
        val height = dpToPx(140)

        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    private fun handleMarkerClick() {
        val location = locationMarker!!.position
        val update = CameraUpdate.scrollAndZoomTo(location, 20.0).animate(CameraAnimation.Easing)
        naverMap.moveCamera(update)
        viewModel.setMyMarkerClicked(true)
        viewModel.setFriendMarkerClicked(false)

        val mapContainer = fragment.requireActivity().findViewById<ViewGroup>(R.id.fragment_map)
        val myBalloonView = LayoutInflater.from(fragment.requireContext()).inflate(R.layout.dialog_edit_message, mapContainer, false)
        mapContainer.addView(myBalloonView)

        val markerMessageEditText = myBalloonView.findViewById<EditText>(R.id.markerMyMessage)

        db.collection("users")
            .document(viewModel.currentUser!!.uid)
            .get()
            .addOnSuccessListener { statusMsg: DocumentSnapshot ->
                statusMsg.getString("statusMessage")?.let {
                    markerMessageEditText.setText(it)
                }
            }
            .addOnFailureListener { e -> Log.w("MapManager", "Error getting document", e) }

        markerMessageEditText.setOnEditorActionListener { _, actionId, _ ->
            viewModel.setEditTextClicked(true)
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val statusMessage = markerMessageEditText.text.toString().trim()
                if (statusMessage.isNotEmpty()) {
                    db.collection("users")
                        .document(viewModel.currentUser!!.uid)
                        .update("statusMessage", statusMessage)
                        .addOnSuccessListener {
                            Toast.makeText(fragment.requireContext(), "상태 메시지가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                }
                val imm = fragment.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(markerMessageEditText.windowToken, 0)
                markerMessageEditText.clearFocus()
                return@setOnEditorActionListener true
            }
            false
        }
    }


}
