package com.example.mohassu.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mohassu.Adapter.NotificationAdapter.NotificationViewHolder
import com.example.mohassu.Notification.NotificationItem
import com.example.mohassu.R
import java.util.concurrent.TimeUnit

class NotificationAdapter(
    private val context: Context,
    private val notificationList: MutableList<NotificationItem>
) :
    RecyclerView.Adapter<NotificationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        // 아이템 뷰를 인플레이트하여 뷰 홀더 반환
        val view = LayoutInflater.from(context).inflate(R.layout.view_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList[position]

        val userName = notification.userName
        // 알림 데이터 바인딩
        if (notification.actionType == "addFr") {
            holder.message.text = userName + "과 친구가 되었어요!"
        } else if (notification.actionType == "newPr") {
            holder.message.text = userName + "와 새로운 약속이 생성되었어요"
        }

        val currentTime = System.currentTimeMillis()
        holder.timeAgo.text = getTimeAgo(currentTime - notification.timeAgo)

        // 프로필 이미지 로딩 (Glide 사용)
        Glide.with(context)
            .load(notification.profileImageUrl) // URL에서 이미지 로드
            .circleCrop() // 원형으로 잘라서 표시
            .placeholder(R.drawable.img_basic_profile) // 기본 이미지
            .error(R.drawable.img_logo) // 오류시 기본 이미지
            .into(holder.profileImage) // ImageView에 이미지 설정
    }

    fun getTimeAgo(timeDifference: Long): String {
        // 시간 차이를 분으로 변환

        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference)
        val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)
        val days = TimeUnit.MILLISECONDS.toDays(timeDifference)

        return if (days > 0) {
            days.toString() + "일 전"
        } else if (hours > 0) {
            hours.toString() + "시간 전"
        } else if (minutes > 0) {
            minutes.toString() + "분 전"
        } else {
            "방금 전"
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    // 새 알림 추가 메서드
    fun addNotification(notificationItem: NotificationItem) {
        notificationList.add(0, notificationItem) // 새 알림을 리스트의 맨 앞에 추가
        notifyItemInserted(0) // RecyclerView 갱신
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 레이아웃에서 필요한 뷰 연결
        var profileImage: ImageView = itemView.findViewById(R.id.profile_image2)
        var message: TextView = itemView.findViewById(R.id.notification_text)
        var timeAgo: TextView = itemView.findViewById(R.id.timestamp)
    }
}