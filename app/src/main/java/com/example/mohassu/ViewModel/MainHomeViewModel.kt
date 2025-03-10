package com.example.mohassu.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainHomeViewModel : ViewModel() {

    private val _isCameraMovedByUser = MutableLiveData(false)
    val isCameraMovedByUser: LiveData<Boolean> get() = _isCameraMovedByUser

    private val _isMyMarkerClicked = MutableLiveData(false)
    val isMyMarkerClicked: LiveData<Boolean> get() = _isMyMarkerClicked

    private val _isFriendMarkerClicked = MutableLiveData(false)
    val isFriendMarkerClicked: LiveData<Boolean> get() = _isFriendMarkerClicked

    private val _isFocusMode = MutableLiveData(false)
    val isFocusMode: LiveData<Boolean> get() = _isFocusMode

    private val _isEditTextClicked = MutableLiveData(false)
    val isEditTextClicked: LiveData<Boolean> get() = _isEditTextClicked

    fun setCameraMovedByUser(value: Boolean) {
        _isCameraMovedByUser.value = value
    }

    fun setMyMarkerClicked(value: Boolean) {
        _isMyMarkerClicked.value = value
    }

    fun setFriendMarkerClicked(value: Boolean) {
        _isFriendMarkerClicked.value = value
    }

    fun setFocusMode(value: Boolean) {
        _isFocusMode.value = value
    }

    fun setEditTextClicked(value: Boolean) {
        _isEditTextClicked.value = value
    }
}
