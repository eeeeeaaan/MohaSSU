//package com.example.mohassu.MainFragment
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.graphics.PointF
//import android.graphics.drawable.Drawable
//import android.location.Location
//import android.net.Uri
//import android.os.Bundle
//import android.util.Log
//import android.view.KeyEvent
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.inputmethod.EditorInfo
//import android.view.inputmethod.InputMethodManager
//import android.widget.EditText
//import android.widget.FrameLayout
//import android.widget.ImageButton
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.core.app.ActivityCompat
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.replace
//import androidx.navigation.Navigation.findNavController
//import androidx.navigation.fragment.findNavController
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.target.CustomTarget
//import com.bumptech.glide.request.transition.Transition
//import com.example.mohassu.CheckAndEditPromiseFragment.PromiseEditDialogFragment
//import com.example.mohassu.CheckProfileAndTimeTableFragment.CheckProfileBottomSheetFragment
//import com.example.mohassu.Constants.Constants
//import com.example.mohassu.R
//import com.example.mohassu.databinding.FragmentMainHomeBinding
//import com.google.android.gms.location.LocationServices
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.firestore.DocumentSnapshot
//import com.google.firebase.firestore.FieldValue
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.FirebaseFirestoreException
//import com.google.firebase.firestore.GeoPoint
//import com.google.firebase.firestore.QuerySnapshot
//import com.naver.maps.geometry.LatLng
//import com.naver.maps.map.CameraAnimation
//import com.naver.maps.map.CameraUpdate
//import com.naver.maps.map.LocationTrackingMode
//import com.naver.maps.map.MapFragment
//import com.naver.maps.map.NaverMap
//import com.naver.maps.map.NaverMap.OnMapClickListener
//import com.naver.maps.map.OnMapReadyCallback
//import com.naver.maps.map.overlay.Marker
//import com.naver.maps.map.overlay.Overlay
//import com.naver.maps.map.overlay.OverlayImage
//import com.naver.maps.map.util.FusedLocationSource
//
//class MainHomeFragment : Fragment(), OnMapReadyCallback {
//    private var _binding: FragmentMainHomeBinding? = null
//    private val binding get() = _binding!!
//
//
//    private var naverMap: NaverMap? = null
//    private var locationSource: FusedLocationSource? = null
//    private var isCameraMovedByUser = false
//    private var isMyMarkerClicked = false // 마커 클릭 상태 추적 변수
//    private var isFriendMarkerClicked = false
//    private var isFocusMode = false
//    private var isEditTextClicked = false
//    private val isPlaceFound = false
//
//    var auth: FirebaseAuth = FirebaseAuth.getInstance()
//    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
//    var currentUser: FirebaseUser? = auth.currentUser
//
//    // ActivityResultLauncher for permission requests
//    private val requestPermissionLauncher = registerForActivityResult<String, Boolean>(
//        RequestPermission()
//    ) { isGranted: Boolean ->
//        if (isGranted) {
//            if (naverMap != null) {
//                naverMap!!.locationTrackingMode = LocationTrackingMode.Follow
//            }
//        } else {
//            Toast.makeText(requireContext(), "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentMainHomeBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        Toast.makeText(requireContext(), "지도를 불러오는 중입니다... \n화면을 누르지 마십시오", Toast.LENGTH_LONG)
//            .show()
//        // Initialize MapFragment
//        var mapFragment = childFragmentManager.findFragmentById(R.id.fragment_map) as MapFragment?
//        if (mapFragment == null) {
//            mapFragment = MapFragment.newInstance()
//            childFragmentManager.beginTransaction().add(R.id.fragment_map, mapFragment).commit()
//        }
//        mapFragment!!.getMapAsync(this)
//
//        // Initialize LocationSource
//        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
//
//        val geofencingClient = LocationServices.getGeofencingClient(requireContext())
//
//        // Custom button to center on current location
//        myLocationButton = view.findViewById(R.id.btnNowLocation)
//        if (myLocationButton != null) {
//            myLocationButton!!.setOnClickListener { v: View? ->
//                isCameraMovedByUser = false // 자동 중심 이동 다시 활성화
//                val currentPosition =
//                    naverMap!!.locationOverlay.position
//                if (currentPosition != null) {
//                    // Move camera to the current position
//                    naverMap!!.moveCamera(CameraUpdate.scrollTo(currentPosition))
//                    Log.d("", "") // 추가
//                } else { // 예외처리 생략 가능
//                    Toast.makeText(requireContext(), "현재 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        // NavController 초기화
//        val navController = findNavController()
//
//        // 다음 프레그먼트를 클릭 시 다음 Fragment로 이동
//        // 알림 페이지 이동
//        // 알림 페이지 이동
//        binding.btnNotification.isFocusable = false
//        binding.btnNotification.setOnClickListener {
//            navController.navigate(R.id.actionNotification)
//        }
//
//// 약속 리스트 페이지 이동
//        binding.btnPromiseList.isFocusable = false
//        binding.btnPromiseList.setOnClickListener {
//            navController.navigate(R.id.actionPromiseList)
//        }
//
//// 친구 리스트 페이지 이동
//        binding.btnFriendList.isFocusable = false
//        binding.btnFriendList.setOnClickListener {
//            navController.navigate(R.id.actionFriendList)
//        }
//
//// 약속 추가 페이지 이동
//        binding.btnAddPlan.isFocusable = false
//        binding.btnAddPlan.setOnClickListener {
//            navController.navigate(R.id.actionAddPlan)
//        }
//
//// 마이페이지 이동
//        binding.btnMyPage.isFocusable = false
//        binding.btnMyPage.setOnClickListener {
//            navController.navigate(R.id.actionMyPage)
//        }
//
//    }
//
//    override fun onMapReady(naverMap: NaverMap) {
//        this.naverMap = naverMap
//
//        // 현재 위치 불러오기 전 초기 화면을 보이지 않는 위치(바다)로 설정
//        val initialUpdate = CameraUpdate.scrollTo(LatLng(0.0, 0.0))
//        naverMap.moveCamera(initialUpdate)
//
//        // 위치 정보 가져오기
//        naverMap.locationSource = locationSource
//
//        // +- 줌컨트롤 버튼 비활성화
//        naverMap.uiSettings.isZoomControlEnabled = false
//
//
//        // 위치 요청 수락 시 트래킹모드 가동, 거부 시 다시 묻기
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            naverMap.locationTrackingMode = LocationTrackingMode.Follow // 트래킹 모드 설정 후 나중에 오버레이 비활성화
//        } else {
//            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//        }
//
//        // 지도 드래그 이벤트 설정
//        naverMap.addOnCameraChangeListener { reason: Int, animated: Boolean ->
//            if (reason == CameraUpdate.REASON_GESTURE) {
//                isCameraMovedByUser = true // 사용자가 화면을 이동했을 때 플래그 설정
//                isMyMarkerClicked = false // 사용자가 화면을 이동하면 마커 클릭 상태 해제
//                isFriendMarkerClicked = false
//
//                if (isFocusMode) {
//                    resetMarkerFocusMode()
//                }
//                val mapContainer = requireActivity().findViewById<FrameLayout>(R.id.fragment_map)
//                val myBalloonView =
//                    mapContainer.findViewById<View>(R.id.dialog_edit_message) // ID로 찾기
//                if (myBalloonView != null) {
//                    mapContainer.removeView(myBalloonView) // 내 말풍선 제거
//                }
//                val friendBalloonView =
//                    mapContainer.findViewById<View>(R.id.dialog_text_message) // ID로 찾기
//                if (friendBalloonView != null) {
//                    mapContainer.removeView(friendBalloonView) // 친구 말풍선 제거
//                }
//                val bannerView =
//                    mapContainer.findViewById<View>(R.id.fragment_status_banner) // ID로 찾기
//                if (bannerView != null) {
//                    mapContainer.removeView(bannerView) // 친구 상태 배너 제거
//                }
//                val profileButton =
//                    mapContainer.findViewById<View>(R.id.dialog_show_profile) // ID로 찾기
//                if (profileButton != null) {
//                    mapContainer.removeView(profileButton) // 친구 프로필 확인 버튼 제거
//                }
//            }
//        }
//

// 이쪽 아직 안함
//        // 내 Marker 초기화
//        initializeMyMarker()
//
//        // 친구 Marker 초기화 및 위치 갱신
//        loadFriendMarkers()
//
//        // 약속 Marker 갱신
//        loadPromisesFromFirestore()
//

// 여기서부턴 다시 함
//        // 지도 클릭 이벤트 설정 (말풍선 닫기)
//        naverMap.onMapClickListener = OnMapClickListener { point: PointF?, coord: LatLng? ->
//            if (!isEditTextClicked) {
//                isMyMarkerClicked = false // 사용자가 화면을 클릭하면 마커 클릭 상태 해제
//                isFriendMarkerClicked = false
//                if (isFocusMode) {
//                    resetMarkerFocusMode()
//                }
//                val mapContainer = requireActivity().findViewById<FrameLayout>(R.id.fragment_map)
//                val myBalloonView =
//                    mapContainer.findViewById<View>(R.id.dialog_edit_message) // ID로 찾기
//                if (myBalloonView != null) {
//                    mapContainer.removeView(myBalloonView) // 말풍선 제거
//                }
//                val friendBalloonView =
//                    mapContainer.findViewById<View>(R.id.dialog_text_message) // ID로 찾기
//                if (friendBalloonView != null) {
//                    mapContainer.removeView(myBalloonView) // 말풍선 제거
//                }
//                val bannerView =
//                    mapContainer.findViewById<View>(R.id.fragment_status_banner) // ID로 찾기
//                if (bannerView != null) {
//                    mapContainer.removeView(bannerView) // 말풍선 제거
//                }
//                val profileButton =
//                    mapContainer.findViewById<View>(R.id.dialog_show_profile) // ID로 찾기
//                if (profileButton != null) {
//                    mapContainer.removeView(profileButton) // 말풍선 제거
//                }
//            }
//        }
//
//
//        // 위치 변화 업데이트
//        naverMap.addOnLocationChangeListener { location: Location ->
//            if (locationMarker == null) {
//                // locationMarker가 초기화되지 않았다면 초기화 기다리기
//                return@addOnLocationChangeListener
//            }
//            updateUserLocationToFirestore(location)
//            loadUserLocationFromFirestore()
//            naverMap.locationOverlay.isVisible = false // 오버레이 비활성화
//        }
//    }
//
//    private fun initializeMyMarker() {
//        val hide = CameraUpdate.scrollAndZoomTo(LatLng(0.0, 0.0), 20.0)
//            .animate(CameraAnimation.Easing)
//        naverMap!!.moveCamera(hide) // 가끔씩 naverMap 로딩 버그로 인해 위치 업데이트 이후 보이게 설정
//
//        // XML 레이아웃을 Inflate
//        val myMarkerView =
//            LayoutInflater.from(requireContext()).inflate(R.layout.view_marker_my, null)
//        val myProfile = myMarkerView.findViewById<ImageView>(R.id.my_marker_image)
//
//        // SharedPreferences 인스턴스 가져오기
//        val sharedPreferences = activity!!.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
//
//        // 저장된 데이터 불러오기
//        val photoUrl = sharedPreferences.getString("photoUrl", "")!! // 기본값은 빈 문자열
//        Log.d("mohassu:marker", "마커에 쓰일 프로필 url : $photoUrl")
//
//        if (photoUrl != null && !photoUrl.isEmpty()) {
//            // Glide를 사용하여 이미지 로드
//            Glide.with(this)
//                .load(Uri.parse(photoUrl)) // 프로필 이미지 URI
//                .circleCrop()
//                .placeholder(R.drawable.img_basic_profile)
//                .error(R.drawable.img_basic_profile)
//                .into(object : CustomTarget<Drawable?>() {
//                    override fun onResourceReady(
//                        resource: Drawable,
//                        transition: Transition<in Drawable>?
//                    ) {
//                        myProfile.setImageDrawable(resource)
//                        Log.d("mohassu:marker", "마커에 쓰일 프로필 uri 로드 완료: " + Uri.parse(photoUrl))
//
//                        // 이미지가 로드된 후 Bitmap 변환
//                        val myMarkerBitmap = convertViewToBitmap(myMarkerView)
//                        if (myMarkerBitmap != null) {
//                            // Marker 객체 생성
//                            locationMarker = Marker()
//                            locationMarker!!.position = naverMap!!.locationOverlay.position
//                            locationMarker!!.icon =
//                                OverlayImage.fromBitmap(myMarkerBitmap) // 마커 이미지 설정
//                            locationMarker!!.width = dpToPx(60) // 마커 크기 조정 (dp를 px로 변환)
//                            locationMarker!!.height = dpToPx(70)
//                            locationMarker!!.map = naverMap // 지도에 마커 추가
//
//                            // Marker 클릭 리스너 설정
//                            locationMarker!!.onClickListener =
//                                Overlay.OnClickListener { overlay: Overlay? ->
//                                    if (naverMap == null) { // 테스트 필요
//                                        Toast.makeText(
//                                            requireContext(),
//                                            "지도를 불러오는 중입니다. 잠시만 기다려주세요.",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                        return@setOnClickListener true
//                                    }
//                                    // 다른 버튼 안 보이게
//                                    showMarkerFocusMode()
//
//                                    val location = locationMarker!!.position
//
//                                    // 현재 위치 가져오기
//                                    val update = CameraUpdate.scrollAndZoomTo(location, 20.0)
//                                        .animate(CameraAnimation.Easing)
//                                    naverMap!!.moveCamera(update)
//                                    isMyMarkerClicked = true
//                                    isFriendMarkerClicked = false
//
//                                    val mapContainer =
//                                        requireActivity().findViewById<FrameLayout>(R.id.fragment_map)
//
//                                    // 말풍선 View 인플레이트
//                                    val myBalloonView = LayoutInflater.from(requireContext())
//                                        .inflate(R.layout.dialog_edit_message, mapContainer, false)
//                                    mapContainer.addView(myBalloonView) // 말풍선 추가
//
//                                    // EditText 참조 가져오기
//                                    val markerMessageEditText =
//                                        myBalloonView.findViewById<EditText>(R.id.markerMyMessage)
//
//                                    db.collection("users")
//                                        .document(currentUser!!.uid) // 사용자 ID
//                                        .get()
//                                        .addOnSuccessListener { statusMsg: DocumentSnapshot ->
//                                            if (statusMsg.exists()) {
//                                                // 상태 메시지가 이미 저장되어 있으면 EditText에 띄우기
//                                                val storedMessage =
//                                                    statusMsg.getString("statusMessage")
//                                                if (storedMessage != null && !storedMessage.isEmpty()) {
//                                                    markerMessageEditText.setText(storedMessage) // 기존 메시지 띄우기
//                                                }
//                                            }
//                                        }
//                                        .addOnFailureListener { e: Exception? ->
//                                            Log.w(
//                                                "mohassu:marker",
//                                                "Error getting document",
//                                                e
//                                            )
//                                        }
//
//                                    markerMessageEditText.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
//                                        isEditTextClicked = true
//                                        if (actionId == EditorInfo.IME_ACTION_DONE) {
//                                            val statusMessage =
//                                                markerMessageEditText.text.toString()
//                                                    .trim { it <= ' ' }
//
//                                            if (!statusMessage.isEmpty()) {
//                                                db.collection("users")
//                                                    .document(currentUser!!.uid) // 사용자 ID
//                                                    .update("statusMessage", statusMessage)
//                                                    .addOnSuccessListener { aVoid: Void? ->
//                                                        // 저장 성공 시 처리 (예: 메시지 표시)
//                                                        Toast.makeText(
//                                                            requireContext(),
//                                                            "상태 메시지가 업데이트되었습니다.",
//                                                            Toast.LENGTH_SHORT
//                                                        ).show()
//                                                    }
//                                            }
//
//                                            val imm =
//                                                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                                            if (imm != null) {
//                                                imm.hideSoftInputFromWindow(view!!.windowToken, 0)
//                                                markerMessageEditText.clearFocus() // 포커스 해제하여 깜빡임 끄기
//                                            }
//                                            return@setOnEditorActionListener true
//                                        }
//                                        false
//                                    }
//                                    true // 클릭 이벤트 소비
//                                }
//                        } else {
//                            Log.e("mohassu:marker", "Bitmap 변환 실패.")
//                        }
//                    }
//
//                    override fun onLoadCleared(placeholder: Drawable?) {
//                        // 필요한 경우 처리
//                    }
//
//                    override fun onLoadFailed(errorDrawable: Drawable?) {
//                        super.onLoadFailed(errorDrawable)
//                        myProfile.setImageDrawable(errorDrawable)
//                        Log.d("mohassu:marker", "마커에 쓰일 프로필 이미지 로드 실패")
//
//                        // Bitmap 변환 및 마커 생성 (에러 이미지 사용)
//                        val myMarkerBitmap = convertViewToBitmap(myMarkerView)
//                        if (myMarkerBitmap != null) {
//                            locationMarker = Marker()
//                            locationMarker!!.position = naverMap!!.locationOverlay.position
//                            locationMarker!!.icon =
//                                OverlayImage.fromBitmap(myMarkerBitmap) // 마커 이미지 설정
//                            locationMarker!!.width = dpToPx(60) // 마커 크기 조정
//                            locationMarker!!.height = dpToPx(70)
//                            locationMarker!!.map = naverMap // 지도에 마커 추가
//                            Log.d("mohassu:marker", "내 프로필 사진 불러오지 못함")
//                        } else {
//                            Log.e("mohassu:marker", "Bitmap 변환 실패 (onLoadFailed).")
//                        }
//                    }
//                })
//        } else {
//            // photoUrl이 비어 있을 때의 처리: 기본 이미지 설정
//            myProfile.setImageResource(R.drawable.img_basic_profile)
//            Log.d("mohassu:marker", "내 프로필 사진 불러오지 못함")
//
//            // Bitmap 변환 및 마커 생성 (기본 이미지 사용)
//            val myMarkerBitmap = convertViewToBitmap(myMarkerView)
//            if (myMarkerBitmap != null) {
//                locationMarker = Marker()
//                locationMarker!!.position = naverMap!!.locationOverlay.position
//                locationMarker!!.icon = OverlayImage.fromBitmap(myMarkerBitmap) // 마커 이미지 설정
//                locationMarker!!.width = dpToPx(60) // 마커 크기 조정
//                locationMarker!!.height = dpToPx(70)
//                locationMarker!!.map = naverMap // 지도에 마커 추가
//
//                locationMarker!!.onClickListener =
//                    Overlay.OnClickListener { overlay: Overlay? ->
//                        if (naverMap == null) { // 테스트 필요
//                            Toast.makeText(
//                                requireContext(),
//                                "지도를 불러오는 중입니다. 잠시만 기다려주세요.",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            return@setOnClickListener true
//                        }
//                        // 다른 버튼 안 보이게
//                        showMarkerFocusMode()
//
//                        val location = locationMarker!!.position
//
//                        // 현재 위치 가져오기
//                        val update = CameraUpdate.scrollAndZoomTo(location, 20.0)
//                            .animate(CameraAnimation.Easing)
//                        naverMap!!.moveCamera(update)
//                        isMyMarkerClicked = true
//                        isFriendMarkerClicked = false
//
//                        val mapContainer =
//                            requireActivity().findViewById<FrameLayout>(R.id.fragment_map)
//
//                        // 말풍선 View 인플레이트
//                        val myBalloonView = LayoutInflater.from(requireContext())
//                            .inflate(R.layout.dialog_edit_message, mapContainer, false)
//                        mapContainer.addView(myBalloonView) // 말풍선 추가
//
//                        // EditText 참조 가져오기
//                        val markerMessageEditText =
//                            myBalloonView.findViewById<EditText>(R.id.markerMyMessage)
//
//                        db.collection("users")
//                            .document(currentUser!!.uid) // 사용자 ID
//                            .get()
//                            .addOnSuccessListener { statusMsg: DocumentSnapshot ->
//                                if (statusMsg.exists()) {
//                                    // 상태 메시지가 이미 저장되어 있으면 EditText에 띄우기
//                                    val storedMessage = statusMsg.getString("statusMessage")
//                                    if (storedMessage != null && !storedMessage.isEmpty()) {
//                                        markerMessageEditText.setText(storedMessage) // 기존 메시지 띄우기
//                                    }
//                                }
//                            }
//                            .addOnFailureListener { e: Exception? ->
//                                Log.w("mohassu:marker", "Error getting document", e)
//                            }
//
//                        markerMessageEditText.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
//                            isEditTextClicked = true
//                            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                                val statusMessage =
//                                    markerMessageEditText.text.toString().trim { it <= ' ' }
//
//                                if (!statusMessage.isEmpty()) {
//                                    db.collection("users")
//                                        .document(currentUser!!.uid) // 사용자 ID
//                                        .update("statusMessage", statusMessage)
//                                        .addOnSuccessListener { aVoid: Void? ->
//                                            // 저장 성공 시 처리 (예: 메시지 표시)
//                                            Toast.makeText(
//                                                requireContext(),
//                                                "상태 메시지가 업데이트되었습니다.",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        }
//                                }
//
//                                val imm =
//                                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                                if (imm != null) {
//                                    imm.hideSoftInputFromWindow(view!!.windowToken, 0)
//                                    markerMessageEditText.clearFocus() // 포커스 해제하여 깜빡임 끄기
//                                }
//                                return@setOnEditorActionListener true
//                            }
//                            false
//                        }
//                        true // 클릭 이벤트 소비
//                    }
//            } else {
//                Log.e("mohassu:marker", "Bitmap 변환 실패 (기본 이미지).")
//            }
//        }
//    }
//
//    // dp를 px로 변환하는 메서드 추가
//    private fun dpToPx(dp: Int): Int {
//        val density = resources.displayMetrics.density
//        return Math.round(dp.toFloat() * density)
//    }
//
//    private fun updateUserLocationToFirestore(location: Location) {
//        if (currentUser == null) return  // 사용자 인증되지 않은 경우
//
//
//        val uid = currentUser!!.uid // 사용자 고유 ID (UID)
//
//        //GeoPoint로 location 저장
//        val geoPoint = GeoPoint(location.latitude, location.longitude)
//
//        // Firestore에 저장할 데이터
//        val locationData: MutableMap<String, Any> = HashMap()
//        locationData["location"] = geoPoint
//        locationData["timestamp"] =
//            FieldValue.serverTimestamp() // 서버의 타임스탬프 추가
//
//        db.collection("users")
//            .document(uid)
//            .collection("location")
//            .document("currentLocation")
//            .set(locationData) // 🔥 Firestore에 데이터 저장
//            .addOnSuccessListener { aVoid: Void? ->
//                Log.d(
//                    "TAG",
//                    "Location updated in Firestore"
//                )
//            }
//            .addOnFailureListener { e: Exception? ->
//                Log.w(
//                    "TAG",
//                    "Failed to update location",
//                    e
//                )
//            }
//    }
//
//    private fun loadUserLocationFromFirestore() {
//        if (currentUser == null) return  // 사용자 인증되지 않은 경우
//
//
//        val uid = currentUser!!.uid
//
//        db.collection("users")
//            .document(uid)
//            .collection("location")
//            .document("currentLocation")
//            .addSnapshotListener { snapshot: DocumentSnapshot?, error: FirebaseFirestoreException? ->
//                if (error != null) {
//                    Log.w("TAG", "Listen failed.", error)
//                    return@addSnapshotListener
//                }
//                if (snapshot != null && snapshot.exists()) {
//                    val newGeopoint = snapshot.getGeoPoint("location")
//
//                    val newLocation = LatLng(
//                        newGeopoint!!.latitude, newGeopoint.longitude
//                    )
//
//                    // 사용자 위치 업데이트
//                    locationMarker!!.position = newLocation
//
//                    // 마커 클릭 상태 또는 초기 화면에서 카메라 이동
//                    if (isMyMarkerClicked) {
//                        val update = CameraUpdate.scrollTo(newLocation)
//                            .animate(CameraAnimation.Easing) // 줌 레벨 17.0
//                        naverMap!!.moveCamera(update)
//                    } else if (!isCameraMovedByUser && !isFriendMarkerClicked) {
//                        val update = CameraUpdate.scrollAndZoomTo(newLocation, 17.0)
//                            .animate(CameraAnimation.Easing)
//                        naverMap!!.moveCamera(update)
//                    }
//
//
//                    val view = view
//                    tvBuildingName = view!!.findViewById(R.id.tvBuildingName)
//
//                    loadFromGeofencing(newLocation)
//                }
//            }
//    }
//
//
//    private fun loadFromGeofencing(location: LatLng) {
//        for (place in Constants.PLACES) {
//            val results = FloatArray(1)
//            Location.distanceBetween(
//                location.latitude, location.longitude,
//                place.location.latitude, place.location.longitude,
//                results
//            )
//
//            if (results[0] <= place.radius) {
//                val buildingName = place.name
//                tvBuildingName!!.text = buildingName + "에 있어요."
//                if (!isFocusMode) {
//                    //tvBuildingName.setVisibility(View.VISIBLE);
//                }
//                db.collection("users")
//                    .document(currentUser!!.uid)
//                    .update("place", buildingName) // Firestore에 장소명 저장
//                    .addOnSuccessListener { aVoid: Void? ->
//                        Log.d(
//                            "TAG",
//                            "Location updated in Firestore"
//                        )
//                    }
//                    .addOnFailureListener { e: Exception? ->
//                        Log.w(
//                            "TAG",
//                            "Failed to update location",
//                            e
//                        )
//                    }
//                break // 반경 내 첫 번째 장소를 찾으면 종료
//            }
//        }
//        tvBuildingName!!.visibility = View.GONE // 반경 내 장소가 없을 경우
//
//        if (!isPlaceFound) {
//            db.collection("users")
//                .document(currentUser!!.uid)
//                .update("place", "지도 위 장소")
//                .addOnSuccessListener { aVoid: Void? ->
//                    Log.d(
//                        "TAG",
//                        "No place found, updated to '건물없음'"
//                    )
//                }
//                .addOnFailureListener { e: Exception? ->
//                    Log.w(
//                        "TAG",
//                        "Failed to update location to '건물없음'",
//                        e
//                    )
//                }
//        }
//    }
//
//    // Firestore에서 친구 데이터 가져오기
//    private fun loadFriendMarkers() {
//        // XML 레이아웃을 Inflate
//
//        val friendMarkerView =
//            LayoutInflater.from(requireContext()).inflate(R.layout.view_marker_friend, null)
//        val friendProfile = friendMarkerView.findViewById<ImageView>(R.id.your_marker_image)
//
//        if (currentUser != null) {
//            val uid = currentUser!!.uid
//
//            db.collection("users").document(uid)
//                .collection("friends")
//                .get()
//                .addOnSuccessListener { querySnapshot: QuerySnapshot ->
//                    if (querySnapshot.isEmpty) {
//                        // 친구 데이터가 없을 경우 처리
//                        Toast.makeText(requireContext(), "친구를 추가해보세요!", Toast.LENGTH_SHORT).show()
//                        return@addOnSuccessListener
//                    }
//                    for (document in querySnapshot) {
//                        val name = document.getString("name")
//                        val nickname = document.getString("nickname")
//                        val class_name = document.getString("class_name")
//                        val place = document.getString("place")
//                        val startTime = document.getString("startTime")
//                        val endTime = document.getString("endTime")
//                        val photoUrl = document.getString("photoUrl")
//                        val location = document.getGeoPoint("location")
//                        val statusMessage = document.getString("statusMessage")
//
//                        // 마커 클릭 시 친구 ID 전달
//                        val friendId = document.id
//
//                        if (location == null) {
//                            Log.e("mohassu:marker", "친구의 위치 정보가 없습니다: $friendId")
//                            continue  // 위치 정보가 없는 친구는 건너뜀
//                        }
//
//                        if (photoUrl != null && !photoUrl.isEmpty()) {
//                            // Glide를 사용하여 이미지 로드
//                            Glide.with(this)
//                                .load(Uri.parse(photoUrl)) // 프로필 이미지 URI
//                                .circleCrop()
//                                .placeholder(R.drawable.img_basic_profile)
//                                .error(R.drawable.img_basic_profile)
//                                .into(object : CustomTarget<Drawable?>() {
//                                    override fun onResourceReady(
//                                        resource: Drawable,
//                                        transition: Transition<in Drawable>?
//                                    ) {
//                                        friendProfile.setImageDrawable(resource)
//                                        Log.d(
//                                            "mohassu:marker",
//                                            "친구 마커 이미지 로드 완료: " + Uri.parse(photoUrl)
//                                        )
//
//                                        // 이미지가 로드된 후 Bitmap 변환
//                                        val friendMarkerBitmap =
//                                            convertViewToBitmap(friendMarkerView)
//                                        if (friendMarkerBitmap != null) {
//                                            // Marker 객체 생성
//                                            val friendMarker = Marker()
//                                            friendMarker.position =
//                                                LatLng(
//                                                    location.latitude,
//                                                    location.longitude
//                                                )
//                                            friendMarker.icon =
//                                                OverlayImage.fromBitmap(friendMarkerBitmap) // 마커 이미지 설정
//                                            friendMarker.width = dpToPx(60) // 마커 크기 조정 (dp를 px로 변환)
//                                            friendMarker.height = dpToPx(70)
//                                            friendMarker.map = naverMap // 지도에 마커 추가
//
//                                            // 마커 클릭 이벤트 설정
//                                            friendMarker.onClickListener =
//                                                Overlay.OnClickListener { overlay: Overlay? ->
//                                                    if (naverMap == null || view == null) {
//                                                        Toast.makeText(
//                                                            requireContext(),
//                                                            "지도가 아직 초기화되지 않았습니다.",
//                                                            Toast.LENGTH_SHORT
//                                                        ).show()
//                                                        return@setOnClickListener true // 이벤트 소비
//                                                    }
//                                                    // 다른 버튼 안 보이게
//                                                    showMarkerFocusMode()
//
//                                                    // 친구 위치로 카메라 업데이트
//                                                    val update =
//                                                        CameraUpdate.scrollAndZoomTo(
//                                                            friendMarker.position,
//                                                            20.0
//                                                        )
//                                                            .animate(CameraAnimation.Easing)
//                                                    naverMap!!.moveCamera(update)
//                                                    isFriendMarkerClicked = true
//                                                    isMyMarkerClicked = false
//
//                                                    val mapContainer =
//                                                        requireActivity().findViewById<FrameLayout>(
//                                                            R.id.fragment_map
//                                                        )
//
//                                                    // 말풍선 View 인플레이트
//                                                    val friendBalloonView =
//                                                        LayoutInflater.from(requireContext())
//                                                            .inflate(
//                                                                R.layout.dialog_text_message,
//                                                                mapContainer,
//                                                                false
//                                                            )
//                                                    val friendBalloonText =
//                                                        friendBalloonView.findViewById<TextView>(
//                                                            R.id.markerFriendMessage
//                                                        )
//                                                    friendBalloonText.text =
//                                                        statusMessage ?: "상태 메시지 없음"
//                                                    mapContainer.addView(friendBalloonView) // 말풍선 추가
//
//                                                    // 배너 View 인플레이트
//                                                    val bannerView =
//                                                        LayoutInflater.from(requireContext())
//                                                            .inflate(
//                                                                R.layout.fragment_status_banner,
//                                                                mapContainer,
//                                                                false
//                                                            )
//                                                    mapContainer.addView(bannerView)
//
//                                                    // UI 업데이트 (구현 필요)
//                                                    updateStatusBanner(
//                                                        place,
//                                                        class_name,
//                                                        startTime,
//                                                        endTime
//                                                    )
//
//                                                    // 프로필버튼 View 인플레이트
//                                                    val profileButton =
//                                                        LayoutInflater.from(requireContext())
//                                                            .inflate(
//                                                                R.layout.dialog_show_profile,
//                                                                mapContainer,
//                                                                false
//                                                            )
//                                                    mapContainer.addView(profileButton)
//
//                                                    // 클릭 이벤트 설정
//                                                    // 프로필 보기 버튼 클릭 이벤트
//                                                    profileButton.findViewById<View>(R.id.showProfileButton)
//                                                        .setOnClickListener { v: View? ->
//                                                            // CheckProfileBottomSheetFragment 호출
//                                                            val bottomSheet =
//                                                                CheckProfileBottomSheetFragment.newInstanceWithFriendId(
//                                                                    friendId
//                                                                )
//                                                            bottomSheet.show(
//                                                                parentFragmentManager,
//                                                                "CheckProfileBottomSheetFragment"
//                                                            )
//                                                        }
//
//                                                    true // 클릭 이벤트 소비
//                                                }
//                                        } else {
//                                            Log.e(
//                                                "mohassu:marker",
//                                                "Bitmap 변환 실패 for friend: $friendId"
//                                            )
//                                        }
//                                    }
//
//                                    override fun onLoadCleared(placeholder: Drawable?) {
//                                        // 필요한 경우 처리
//                                    }
//
//                                    override fun onLoadFailed(errorDrawable: Drawable?) {
//                                        super.onLoadFailed(errorDrawable)
//                                        friendProfile.setImageDrawable(errorDrawable)
//                                        Log.d(
//                                            "mohassu:marker",
//                                            "친구 마커 이미지 로드 실패: $friendId"
//                                        )
//
//                                        // Bitmap 변환 및 마커 생성 (에러 이미지 사용)
//                                        val friendMarkerBitmap =
//                                            convertViewToBitmap(friendMarkerView)
//                                        if (friendMarkerBitmap != null) {
//                                            val friendMarker = Marker()
//                                            friendMarker.position =
//                                                LatLng(
//                                                    location.latitude,
//                                                    location.longitude
//                                                )
//                                            friendMarker.icon =
//                                                OverlayImage.fromBitmap(friendMarkerBitmap) // 마커 이미지 설정
//                                            friendMarker.width = dpToPx(60) // 마커 크기 조정
//                                            friendMarker.height = dpToPx(70)
//                                            friendMarker.map = naverMap // 지도에 마커 추가
//                                            Log.d(
//                                                "mohassu:marker",
//                                                "친구 마커 생성 완료 (에러 이미지 사용): $friendId"
//                                            )
//
//                                            // 마커 클릭 이벤트 설정
//                                            friendMarker.onClickListener =
//                                                Overlay.OnClickListener { overlay: Overlay? ->
//                                                    if (naverMap == null || view == null) {
//                                                        Toast.makeText(
//                                                            requireContext(),
//                                                            "지도가 아직 초기화되지 않았습니다.",
//                                                            Toast.LENGTH_SHORT
//                                                        ).show()
//                                                        return@setOnClickListener true // 이벤트 소비
//                                                    }
//                                                    // 다른 버튼 안 보이게
//                                                    showMarkerFocusMode()
//
//                                                    // 친구 위치로 카메라 업데이트
//                                                    val update =
//                                                        CameraUpdate.scrollAndZoomTo(
//                                                            friendMarker.position,
//                                                            20.0
//                                                        )
//                                                            .animate(CameraAnimation.Easing)
//                                                    naverMap!!.moveCamera(update)
//                                                    isFriendMarkerClicked = true
//                                                    isMyMarkerClicked = false
//
//                                                    val mapContainer =
//                                                        requireActivity().findViewById<FrameLayout>(
//                                                            R.id.fragment_map
//                                                        )
//
//                                                    // 말풍선 View 인플레이트
//                                                    val friendBalloonView =
//                                                        LayoutInflater.from(requireContext())
//                                                            .inflate(
//                                                                R.layout.dialog_text_message,
//                                                                mapContainer,
//                                                                false
//                                                            )
//                                                    val friendBalloonText =
//                                                        friendBalloonView.findViewById<TextView>(
//                                                            R.id.markerFriendMessage
//                                                        )
//                                                    friendBalloonText.text =
//                                                        statusMessage ?: "상태 메시지 없음"
//                                                    mapContainer.addView(friendBalloonView) // 말풍선 추가
//
//                                                    // 배너 View 인플레이트
//                                                    //                                                        View bannerView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_status_banner, mapContainer, false);
//                                                    //                                                        mapContainer.addView(bannerView);
//
//                                                    // UI 업데이트 (구현 필요)
//                                                    updateStatusBanner(
//                                                        place,
//                                                        class_name,
//                                                        startTime,
//                                                        endTime
//                                                    )
//
//                                                    // 프로필버튼 View 인플레이트
//                                                    val profileButton =
//                                                        LayoutInflater.from(requireContext())
//                                                            .inflate(
//                                                                R.layout.dialog_show_profile,
//                                                                mapContainer,
//                                                                false
//                                                            )
//                                                    mapContainer.addView(profileButton)
//
//                                                    // 클릭 이벤트 설정
//                                                    // 프로필 보기 버튼 클릭 이벤트
//                                                    profileButton.findViewById<View>(R.id.showProfileButton)
//                                                        .setOnClickListener { v: View? ->
//                                                            // CheckProfileBottomSheetFragment 호출
//                                                            val bottomSheet =
//                                                                CheckProfileBottomSheetFragment.newInstanceWithFriendId(
//                                                                    friendId
//                                                                )
//                                                            bottomSheet.show(
//                                                                parentFragmentManager,
//                                                                "CheckProfileBottomSheetFragment"
//                                                            )
//                                                        }
//
//                                                    true // 클릭 이벤트 소비
//                                                }
//                                        } else {
//                                            Log.e(
//                                                "mohassu:marker",
//                                                "Bitmap 변환 실패 (onLoadFailed) for friend: $friendId"
//                                            )
//                                        }
//                                    }
//                                })
//                        } else {
//                            Log.e("mohassu:marker", "Bitmap 변환 실패 for friend: $friendId")
//                        }
//                    }
//                }
//                .addOnFailureListener { e: Exception? ->
//                    Log.e("mohassu:marker", "친구 데이터 로드 실패", e)
//                    Toast.makeText(requireContext(), "친구 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT)
//                        .show()
//                }
//        }
//    }
//
//    // 상태 배너 업데이트 메서드
//    private fun updateStatusBanner(
//        place: String?,
//        class_name: String?,
//        startTime: String?,
//        endTime: String?
//    ) {
//        val view = view ?: return
//
//        val placeInfo = view.findViewById<TextView>(R.id.placeInfo)
//        val classInfo = view.findViewById<TextView>(R.id.classInfo)
//        val stTimeInfo = view.findViewById<TextView>(R.id.startTimeInfo)
//        val endTimeInfo = view.findViewById<TextView>(R.id.endTimeInfo)
//
//        // Firestore 데이터로 텍스트 업데이트
//        placeInfo.text = place ?: "#PLACE"
//        //classInfo.setText(class_name != null ? class_name : "#CLASS");
//        //stTimeInfo.setText(startTime != null ? startTime : "#st_time");
//        //endTimeInfo.setText(endTime != null ? endTime : "#end_time");
//    }
//
//
//    // convertViewToBitmap 메서드 (기존과 동일)
//    private fun convertViewToBitmap(view: View): Bitmap {
//        // dp 단위를 픽셀 단위로 변환하여 설정
//        val width = dpToPx(120) // RelativeLayout의 너비
//        val height = dpToPx(140) // RelativeLayout의 높이
//        view.measure(
//            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
//            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
//        )
//        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
//
//        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        view.draw(canvas)
//
//        return bitmap
//    }
//
//
//    private fun loadPromisesFromFirestore() {
//        db.collection("promises")
//            .get()
//            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
//                for (document in queryDocumentSnapshots) {
//                    val geoPoint = document.getGeoPoint("location")
//                    if (geoPoint != null) {
//                        val latitude = geoPoint.latitude
//                        val longitude = geoPoint.longitude
//                        val promiseId = document.id
//                        addMarkerOnMap(promiseId, latitude, longitude)
//                    } else {
//                        Log.w(
//                            "HomeFragment",
//                            "GeoPoint가 null입니다. Document ID: " + document.id
//                        )
//                    }
//                }
//            }
//            .addOnFailureListener { e: Exception? ->
//                Log.e(
//                    "HomeFragment",
//                    "Firestore에서 약속 정보를 가져오는 데 실패했습니다.",
//                    e
//                )
//            }
//    }
//
//    private fun addMarkerOnMap(promiseId: String, latitude: Double, longitude: Double) {
//        val marker = Marker()
//        marker.position = LatLng(latitude, longitude)
//        marker.icon = OverlayImage.fromResource(R.drawable.ic_promise_marker) // 마커 이미지 설정
//        marker.width = 120 // 마커 크기 조정
//        marker.height = 140
//
//        // 마커에 tag로 promiseId 저장
//        marker.tag = promiseId
//
//        // 마커 클릭 리스너 추가
//        marker.onClickListener =
//            Overlay.OnClickListener { overlay: Overlay? ->
//                val promiseIdClicked = marker.tag as String?
//                if (promiseIdClicked != null) Log.d("Promise2", promiseIdClicked)
//                else Log.d("Promise2", "fuc")
//                val args = Bundle()
//                args.putString("promiseId", promiseIdClicked)
//
//                val bottomSheetFragment = PromiseEditDialogFragment()
//                bottomSheetFragment.arguments = args
//                bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
//
//                true // 클릭 이벤트 소비
//            }
//
//        marker.map = naverMap
//    }

//
//}
