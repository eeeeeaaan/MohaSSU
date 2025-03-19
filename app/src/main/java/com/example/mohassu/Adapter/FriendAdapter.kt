package com.example.mohassu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mohassu.R
import com.example.mohassu.databinding.ViewFriendBinding
import com.example.mohassu.model.Friend

class FriendAdapter(
    private val context: Context,
    private val onFriendClick: (Friend) -> Unit
) : ListAdapter<Friend, FriendAdapter.FriendViewHolder>(FriendDiffCallback()) {

    private var originalList: List<Friend> = emptyList() // 전체 친구 목록
    private var filteredList: List<Friend> = emptyList() // 필터링된 친구 목록

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ViewFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = getItem(position)
        holder.bind(friend)
    }

    inner class FriendViewHolder(private val binding: ViewFriendBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: Friend) {
            binding.nicknameText.text = friend.nickname

            // 상태 메시지 설정
            binding.stateText.text = if (!friend.statusMessage.isNullOrEmpty()) {
                friend.statusMessage
            } else {
                "상태 메시지가 없어요!"
            }

            // 현재 수업 정보 설정
            friend.currentScheduleClass?.let { currentClass ->
                binding.statePlace.text = "${currentClass.classPlace}에서 ${currentClass.classTitle} 수업 중!!!"
                binding.stateTime.text = "${currentClass.startTime.hour}시 ${currentClass.startTime.minute}분 부터 " +
                        "${currentClass.endTime.hour}시 ${currentClass.endTime.minute}분 까지"
            } ?: run {
                binding.statePlace.text = "지금은 수업 중이 아닌디요??"
                binding.stateTime.text = "친구한테 연락해봐요!"
            }

            // 프로필 이미지 로드 (Glide 사용)
            Glide.with(context)
                .load(friend.photoUrl)
                .placeholder(R.drawable.img_logo)
                .error(R.drawable.img_logo)
                .into(binding.profileImage2)

            // 아이템 클릭 리스너 설정
            binding.root.setOnClickListener {
                onFriendClick(friend)
            }
        }
    }

    /**
     * DiffUtil을 사용하여 리스트 변경 최적화
     */
    class FriendDiffCallback : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * 🔥 검색 기능 추가 (최적화된 필터링)
     */
    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter { it.nickname.contains(query, ignoreCase = true) }
        }
        submitList(filteredList)
    }

    /**
     * 🔥 setData() 추가 (전체 목록 업데이트 시 사용)
     */
    fun setData(newFriendList: List<Friend>) {
        originalList = newFriendList
        filter("") // 초기화 후 전체 리스트 표시
    }
}
