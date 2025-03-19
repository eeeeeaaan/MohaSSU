package com.example.mohassu.Model

import java.io.Serializable

data class ScheduleClass(// Getter and Setter methods
    var classTitle: String,
    var classPlace: String,
    var professorName: String, // 0 = 일요일, 1 = 월요일, ...
    var day: Int,
    var startTime: Time,
    var endTime: Time
) :
    Serializable 