package com.weclont.shellgo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainApplication.setServiceContext(this)
        setContent {
            ShellGOApp()
        }
    }

    override fun onDestroy() {
        startService(Intent(this, MainService::class.java))
        super.onDestroy()
    }

}
