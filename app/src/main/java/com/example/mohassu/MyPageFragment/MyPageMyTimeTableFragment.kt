package com.example.mohassu.MyPageFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mohassu.R
import com.example.mohassu.databinding.FragmentMypageMytimetableBinding
import com.github.tlaabs.timetableview.TimetableView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyPageMyTimeTableFragment : Fragment() {

    companion object {
        private const val TIMETABLE_KEY = "timetable"
    }

    private var _binding: FragmentMypageMytimetableBinding? = null
    private val binding get() = _binding!!
    private lateinit var timetable: TimetableView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageMytimetableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timetable = binding.viewTimeTable.timetable
        loadTimetable()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEditClass.setOnClickListener {
            findNavController().navigate(R.id.actionEditClass)
        }
    }

    private fun loadTimetable() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val timeTableData = documentSnapshot.getString("timetableData")
                    if (!timeTableData.isNullOrEmpty()) {
                        timetable.load(timeTableData)
                        Toast.makeText(requireContext(), "저장된 시간표를 불러왔습니다.", Toast.LENGTH_SHORT).show()
                        Log.d("Firestore", "TimeTableData: $timeTableData")
                    } else {
                        Log.d("Firestore", "timeTableData 필드가 없습니다.")
                    }
                } else {
                    Log.d("Firestore", "해당 문서가 존재하지 않습니다.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "timeTableData 가져오기 실패: ${e.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}