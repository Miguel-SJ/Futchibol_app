package com.example.futchibol_app

import android.app.Application
import android.content.Context

class FutbolApp : Application() {

    companion object {
        lateinit var instance: FutbolApp
            private set

        val context: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
