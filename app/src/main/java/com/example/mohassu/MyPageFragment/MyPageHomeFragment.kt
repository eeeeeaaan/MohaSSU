package com.example.mohassu.MyPageFragment


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mohassu.NavigationStartLoginAndSignupActivity
import com.example.mohassu.R
import com.example.mohassu.databinding.FragmentMypageMainBinding
import com.google.firebase.auth.FirebaseAuth

class MyPageHomeFragment : Fragment() {

    private var _binding: FragmentMypageMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val photoUrl = sharedPreferences.getString("photoUrl", null)
        val nickName = sharedPreferences.getString("nickName", null)
        val email = sharedPreferences.getString("email", null)
        val name = sharedPreferences.getString("name", null)
        val birthDate = sharedPreferences.getString("birthDate", null)

        // UI 업데이트
        binding.greetingText.text = "$nickName 님 반갑습니다!"
        binding.userId.text = email
        binding.usernickName.text = nickName
        binding.userName.text = name
        binding.userBirth.text = birthDate

        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .placeholder(R.drawable.img_basic_profile)
                .error(R.drawable.img_basic_profile)
                .into(binding.profileImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.img_basic_profile)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.logoutText.setOnClickListener {
            sharedPreferences.edit().clear().apply()
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(activity, NavigationStartLoginAndSignupActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
            Toast.makeText(activity, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 버튼 클릭 시 프래그먼트 이동
        binding.btnProfileEdit.setOnClickListener {
            findNavController().navigate(R.id.actionProfileEdit)
        }

        binding.btnSchedule.setOnClickListener {
            findNavController().navigate(R.id.actionSchedule)
        }

        binding.btnNotification.setOnClickListener {
            findNavController().navigate(R.id.actionSettingNotification)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
