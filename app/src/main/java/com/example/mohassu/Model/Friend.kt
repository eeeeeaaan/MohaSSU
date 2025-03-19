package com.example.mohassu.model

import com.example.mohassu.Model.ScheduleClass
import java.io.Serializable

data class Friend(
    var uid: String = "",
    var name: String = "",
    var nickname: String = "",
    var email: String = "",
    var statusMessage: String = "",
    var timeTableJSON: String = "",
    var photoUrl: String = "",
    var currentScheduleClass: ScheduleClass? = null, // 현재 수업 정보 (nullable로 처리)
    var isChecked: Boolean = false // 선택 여부
) : Serializable
