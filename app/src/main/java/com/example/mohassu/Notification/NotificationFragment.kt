package com.example.mohassu.Notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mohassu.Adapter.NotificationAdapter
import com.example.mohassu.NavigationStartLoginAndSignupActivity.MyFirebaseMessagingService.Companion.setNotificationAdapter
import com.example.mohassu.R
import com.example.mohassu.databinding.FragmentMainNotificationBinding

class NotificationFragment : Fragment() {
    private var _binding: FragmentMainNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private var notificationList: MutableList<NotificationItem> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 초기화
        recyclerView = binding.notificationRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Adapter 초기화
        adapter = NotificationAdapter(requireContext(), notificationList)
        recyclerView.adapter = adapter

        // FirebaseMessagingService에 Adapter 설정
        setNotificationAdapter(adapter)

        // 예제 데이터 추가 (테스트용)
        loadDummyNotifications()
    }

    private fun loadDummyNotifications() {
        notificationList.add(NotificationItem("User1", "https://example.com/image1.jpg", "친구 요청", 10, 0))
        notificationList.add(NotificationItem("User2", "https://example.com/image2.jpg", "약속 요청", 5, 1))

        adapter.notifyDataSetChanged() // 데이터 갱신
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}
