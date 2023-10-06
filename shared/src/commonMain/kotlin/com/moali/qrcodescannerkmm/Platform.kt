package com.moali.qrcodescannerkmm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform