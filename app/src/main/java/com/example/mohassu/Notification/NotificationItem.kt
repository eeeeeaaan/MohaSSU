package com.example.mohassu.Notification

//class NotificationItem(// 사용자 이름
//    @JvmField val userName: String, // Getter와 Setter
//    @JvmField val profileImageUrl: String, // 프로필 이미지 URL
//    // 액션 타입 (예: 약속, 친구 요청)
//    @JvmField val actionType: String, // 몇 분 전
//    @JvmField val timeAgo: Long, // 알림 메시지
//    var status: Int
//)


data class NotificationItem(
    val userName: String,
    val profileImageUrl: String,
    val actionType: String,
    val timeAgo: Long,
    var status: Int
)