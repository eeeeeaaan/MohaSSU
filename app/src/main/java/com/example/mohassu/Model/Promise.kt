package com.example.mohassu.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

data class Promise(
    var id: String = "", // 약속 ID
    var host: DocumentReference? = null, // 약속 생성자의 Firestore 참조
    var location: GeoPoint? = null, // 약속 장소 (위도, 경도)
    var time: Timestamp? = null, // 약속 시간
    var description: String = "", // 약속 설명
    var promiseType: String = "", // 약속 유형 (밥약속, 술약속, 공부약속 등)
    var participants: List<DocumentReference> = emptyList(), // 참여자 목록 (Firestore의 user 문서 참조 리스트)
    var hostNickname: String = "", // 호스트 닉네임
    var hostProfileImageUrl: String = "" // 호스트 프로필 이미지 URL
) : Serializable
