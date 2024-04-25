package com.mfriend.wtfu.models

import androidx.lifecycle.ViewModel as AndroidXViewModel

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual abstract class ViewModel actual constructor() : AndroidXViewModel() {
    //    actual val viewModelScope: CoroutineScope = super.viewModelScope
    actual override fun onCleared() {
        super.onCleared()
    }
}