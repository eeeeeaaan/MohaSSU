package com.example.mohassu.MyPageFragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mohassu.DialogFragment.ClassAddDialogFragment
import com.example.mohassu.DialogFragment.ClassEditOrDeleteDialogFragment
import com.example.mohassu.R
import com.example.mohassu.databinding.FragmentMypageMytimetableEditBinding
import com.github.tlaabs.timetableview.Schedule
import com.github.tlaabs.timetableview.Time
import com.github.tlaabs.timetableview.TimetableView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyPageMyTimeTableEditFragment : Fragment() {

    companion object {
        private const val PREFS_NAME = "TimetablePrefs"
        private const val TIMETABLE_KEY = "timetable"
    }

    private var _binding: FragmentMypageMytimetableEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var timetable: TimetableView
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageMytimetableEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        timetable = binding.viewTimeTable.timetable
        loadTimetable()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAddClass.setOnClickListener {
            val classAddDialogFragment = ClassAddDialogFragment()
            classAddDialogFragment.setOnClassAddedListener { className, classPlace, day, startHour, startMinute, endHour, endMinute ->
                if (className.isEmpty() || classPlace.isEmpty() || startHour > endHour || (startHour == endHour && startMinute >= endMinute)) {
                    Toast.makeText(requireContext(), "올바른 수업 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    addScheduleToTimetable(className, classPlace, day, startHour, startMinute, endHour, endMinute)
                }
            }
            classAddDialogFragment.show(requireActivity().supportFragmentManager, "ClassAddDialog")
        }

        timetable.setOnStickerSelectEventListener { idx, schedules ->
            val classEditOrDeleteDialogFragment = ClassEditOrDeleteDialogFragment.newInstance(schedules[0])
            classEditOrDeleteDialogFragment.setOnClassEditOrDeleteListener(object : ClassEditOrDeleteDialogFragment.OnClassEditOrDeleteListener {
                override fun onEdit(editedSchedule: Schedule) {
                    timetable.edit(idx, arrayListOf(editedSchedule))
                    Toast.makeText(requireContext(), "수업 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                }

                override fun onDelete() {
                    timetable.remove(idx)
                    Toast.makeText(requireContext(), "수업 정보가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
            })
            classEditOrDeleteDialogFragment.show(requireActivity().supportFragmentManager, "ClassEditDialog")
        }

        binding.btnSave.setOnClickListener {
            saveTimetable()
            saveTimetableToFirestore()
            findNavController().navigate(R.id.actionSaveMyClass)
        }
    }

    private fun loadTimetable() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString(TIMETABLE_KEY, null)?.let { timetable.load(it) }
    }

    private fun saveTimetable() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(TIMETABLE_KEY, timetable.createSaveData()).apply()
        Toast.makeText(requireContext(), "로컬에 시간표가 저장되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun saveTimetableToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val json = timetable.createSaveData()

        db.collection("users").document(userId)
            .update("timetableData", json)
            .addOnSuccessListener { Log.d("Firestore", "timeTableData 필드 업데이트 성공") }
            .addOnFailureListener { Log.e("Firestore", "timeTableData 업데이트 실패: ${it.message}") }

        db.collection("users").document(userId).collection("timetable").get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEach { it.reference.delete() }
                timetable.allSchedulesInStickers.forEach { schedule ->
                    val scheduleMap = hashMapOf(
                        "classTitle" to schedule.classTitle,
                        "classPlace" to schedule.classPlace,
                        "professorName" to schedule.professorName,
                        "day" to schedule.day,
                        "startTime" to mapOf("hour" to schedule.startTime.hour, "minute" to schedule.startTime.minute),
                        "endTime" to mapOf("hour" to schedule.endTime.hour, "minute" to schedule.endTime.minute)
                    )
                    db.collection("users").document(userId).collection("timeTable")
                        .add(scheduleMap)
                        .addOnSuccessListener { Log.d("Firestore", "새로운 timeTable 문서 추가 성공") }
                        .addOnFailureListener { Log.e("Firestore", "새로운 timeTable 문서 추가 실패: ${it.message}") }
                }
            }
            .addOnFailureListener { Log.e("Firestore", "timeTable 문서 삭제 실패: ${it.message}") }
    }

    private fun addScheduleToTimetable(className: String, classPlace: String, day: Int, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        val schedule = Schedule().apply {
            this.classTitle = className
            this.classPlace = classPlace
            this.day = day
            this.startTime = Time(startHour, startMinute)
            this.endTime = Time(endHour, endMinute)
        }
        timetable.add(arrayListOf(schedule))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
