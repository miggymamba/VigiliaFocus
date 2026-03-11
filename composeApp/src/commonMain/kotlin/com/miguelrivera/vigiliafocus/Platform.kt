package com.miguelrivera.vigiliafocus

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform