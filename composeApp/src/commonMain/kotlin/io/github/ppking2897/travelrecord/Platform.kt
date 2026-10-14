package io.github.ppking2897.travelrecord

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform