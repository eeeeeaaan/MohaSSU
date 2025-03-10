package com.example.mohassu.MyPageFragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mohassu.R
import com.example.mohassu.databinding.FragmentMypageProfileEdit1Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MyPageProfileEdit1DetailFragment : Fragment() {

    companion object {
        private const val TAG = "mohassu:mypage_edit_profile"
    }

    private var _binding: FragmentMypageProfileEdit1Binding? = null
    private val binding get() = _binding!!
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageProfileEdit1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        binding.btnBack.setOnClickListener { navController.navigateUp() }

        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val nickName = sharedPreferences.getString("nickName", "") ?: ""
        val name = sharedPreferences.getString("name", "") ?: ""
        val birthDate = sharedPreferences.getString("birthDate", "") ?: ""

        binding.etNickname.setText(nickName)
        binding.etName.setText(name)
        setDatePicker(birthDate)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnNext.setOnClickListener {
            saveUserProfile()
            navController.navigate(R.id.actionNextToEditProfile2)
        }
    }

    private fun setDatePicker(birthDate: String) {
        val calendar = Calendar.getInstance()
        if (birthDate.isNotEmpty()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val date = sdf.parse(birthDate)
                date?.let {
                    calendar.time = it
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        binding.dpSpinner.updateDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun saveUserProfile() {
        val nickname = binding.etNickname.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val day = binding.dpSpinner.dayOfMonth
        val month = binding.dpSpinner.month + 1 // Month is 0-based in DatePicker
        val year = binding.dpSpinner.year
        val birthDate = "$year-$month-$day"

        if (nickname.isEmpty() || name.isEmpty()) {
            Toast.makeText(requireContext(), "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("name", name)
            putString("nickName", nickname)
            putString("birthDate", birthDate)
            apply()
        }
        Log.d(TAG, "로컬에 수정 정보 저장 완료")

        val uid = mAuth.currentUser?.uid ?: return

        val userProfile = mapOf(
            "nickname" to nickname,
            "name" to name,
            "birthDate" to birthDate
        )

        db.collection("users").document(uid)
            .set(userProfile)
            .addOnSuccessListener {
                Log.d(TAG, "프로필 정보가 업데이트되었습니다.")
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Firestore 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Firestore Error", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
