package de.lotto24case

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform