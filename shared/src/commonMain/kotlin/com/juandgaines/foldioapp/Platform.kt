package com.juandgaines.foldioapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform