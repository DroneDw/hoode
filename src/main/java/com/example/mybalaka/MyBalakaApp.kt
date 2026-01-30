package com.example.mybalaka

import android.app.Application
import com.example.mybalaka.cloudinary.CloudinaryManager

class MyBalakaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CloudinaryManager.init(this)
    }
}
