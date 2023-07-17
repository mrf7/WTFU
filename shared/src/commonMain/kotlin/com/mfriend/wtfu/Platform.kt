package com.mfriend.wtfu

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform