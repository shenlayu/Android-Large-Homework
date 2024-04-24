package com.example.simplenote

import android.app.Application
import com.example.simplenote.data.AppContainer
import com.example.simplenote.data.AppDataContainer

class NoteApplication : Application(){

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}