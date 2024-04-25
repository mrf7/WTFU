package com.mfriend.wtfu.models

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect abstract class ViewModel() {
    //    protected val viewModelScope: CoroutineScope
    protected open fun onCleared()
}
