package com.example.mohassu.MainFragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mohassu.R
import com.example.mohassu.ViewModel.MainHomeViewModel
import com.example.mohassu.databinding.FragmentMainHomeBinding
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource

class MainHomeFragment: Fragment(), OnMapReadyCallback {

    private lateinit var viewModel: MainHomeViewModel
    private lateinit var mapManager: MapManager
    // 뷰바인딩
    private var _binding : FragmentMainHomeBinding? = null
    private val binding get() = _binding!!

    //네이버 지도
    private var naverMap: NaverMap? = null
    private var locationSource : FusedLocationSource? = null

    //
    private var isCameraMovedByUser = false
    private var isMyMarkerClicked = false
    private var isFriendMarkerClicked = false
    private var isFocusMode = false
    private var isEditTextClicked = false
    private val isPlaceFound = false

    // 인증 관련
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUser: FirebaseUser? get() = auth.currentUser
    private var locationMarker: Marker? = null


    //권한 허용 관련
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            naverMap?.locationTrackingMode = LocationTrackingMode.Follow
        } else {
            Toast.makeText(requireContext(), "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setting 초기화
        initSetting()

        // 화면 넘김
        initNavigator()

        mapManager.setOnMarkerFocusModeChangeListener { isFocused ->
            if (isFocused) {
                showMarkerFocusMode()
            } else {
                resetMarkerFocusMode()
            }
        }

    }

    override fun onMapReady(naverMap: NaverMap){
        this.naverMap = naverMap

        mapManager = MapManager(this,naverMap, locationSource, requestPermissionLauncher, viewModel)
    }

    private fun initSetting(){

        // initialize mapfragment
        var mapFragment = childFragmentManager.findFragmentById(R.id.fragment_map)as? MapFragment

        if(mapFragment == null){
            mapFragment = MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.fragment_map, it).commit()
            }
        }
        mapFragment?.getMapAsync(this)

        //initialize locationSource
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        val geofencingClient = LocationServices.getGeofencingClient(requireContext())

        // Custom Button to center on current Location
        customButton()

    }

    private fun initNavigator(){
        //navController 초기화
        val navController = findNavController()

        // 알림 페이지 이동
        binding.btnNotification.isFocusable = false
        binding.btnNotification.setOnClickListener {
            navController.navigate(R.id.actionNotification)
        }

        // 약속 리스트 페이지 이동
        binding.btnPromiseList.isFocusable = false
        binding.btnPromiseList.setOnClickListener {
            navController.navigate(R.id.actionPromiseList)
        }

        // 친구 리스트 페이지 이동
        binding.btnFriendList.isFocusable = false
        binding.btnFriendList.setOnClickListener {
            navController.navigate(R.id.actionFriendList)
        }

        // 약속 추가 페이지 이동
        binding.btnAddPlan.isFocusable = false
        binding.btnAddPlan.setOnClickListener {
            navController.navigate(R.id.actionAddPlan)
        }

        // 마이페이지 이동
        binding.btnMyPage.isFocusable = false
        binding.btnMyPage.setOnClickListener {
            navController.navigate(R.id.actionMyPage)
        }
    }

    private fun customButton(){
        binding.btnNowLocation.setOnClickListener{
            isCameraMovedByUser = false
            val currentPosition = naverMap?.locationOverlay?.position
            if(currentPosition != null){
                naverMap?.moveCamera(CameraUpdate.scrollTo(currentPosition))
            } else {
                Toast.makeText(requireContext(), "현재 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun resetMarkerFocusMode() {
        isFocusMode = false

        // 보여줄 버튼 및 뷰 리스트
        val editDialogView = requireActivity().findViewById<ViewGroup>(R.id.dialog_edit_message)
        val textMessageView = requireActivity().findViewById<ViewGroup>(R.id.dialog_text_message)
        val statusBannerView = requireActivity().findViewById<ViewGroup>(R.id.fragment_status_banner)
        val showProfileView = requireActivity().findViewById<ViewGroup>(R.id.dialog_show_profile)

        val visibleViews = listOf(
            binding.btnNotification,
            binding.btnPromiseList,
            binding.btnMyPage,
            binding.btnNowLocation,
            binding.btnAddPlan,
            editDialogView,
            textMessageView,
            statusBannerView,
            showProfileView
        )

        // 리스트를 순회하며 visibility 설정
        visibleViews.forEach { it.visibility = View.VISIBLE }
    }

    fun showMarkerFocusMode() {
        isFocusMode = true

        // 다이얼로그 및 프래그먼트의 뷰 가져오기
        val editDialogView = requireActivity().findViewById<ViewGroup>(R.id.dialog_edit_message)
        val textMessageView = requireActivity().findViewById<ViewGroup>(R.id.dialog_text_message)
        val statusBannerView = requireActivity().findViewById<ViewGroup>(R.id.fragment_status_banner)
        val showProfileView = requireActivity().findViewById<ViewGroup>(R.id.dialog_show_profile)

        // 숨길 버튼 및 뷰 리스트 (기존 바인딩 요소 + 다이얼로그 요소)
        val hiddenViews = listOf(
            binding.btnNotification,
            binding.btnPromiseList,
            binding.btnMyPage,
            binding.btnNowLocation,
            binding.btnAddPlan,
            editDialogView,
            textMessageView,
            statusBannerView,
            showProfileView
        )

        // 리스트를 순회하며 visibility 설정 (다이얼로그 포함)
        hiddenViews.forEach { it?.visibility = View.GONE }
    }


//    private fun dpToPx(dp: Int): Int {
//        return (dp * resources.displayMetrics.density).toInt()
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


}