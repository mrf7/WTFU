package com.mfriend.wtfu.models

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual abstract class ViewModel actual constructor() {
    //    actual val viewModelScope: CoroutineScope = MainScope()
    protected actual open fun onCleared() {
    }
}