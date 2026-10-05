package com.dynamicdevelopers.reslink

import android.app.Application
import android.util.Log
import com.dynamicdevelopers.reslink.data.ApiFactory
import com.dynamicdevelopers.reslink.data.ResLinkApi
import com.dynamicdevelopers.reslink.data.SessionStore
import com.dynamicdevelopers.reslink.data.UpdatePushTokenRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResLinkApp : Application() {
    lateinit var session: SessionStore
        private set
    lateinit var api: ResLinkApi
        private set

    override fun onCreate() {
        super.onCreate()
        session = SessionStore(this)
        api = ApiFactory.create(session)
        syncPushToken()
    }

    // Builds without google-services.json have no Firebase app, and getInstance() would crash.
    fun syncPushToken() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            Log.i("FCM", "Firebase is not configured; push notifications are off")
            return
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            if (token.isNotBlank()) {
                uploadPushToken(token)
            }
        }
    }

    fun uploadPushToken(token: String) {
        if (!session.isLoggedIn()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.updatePushToken(UpdatePushTokenRequest(token))
                if (!response.isSuccessful) {
                    Log.w("FCM", "Push token upload failed: ${response.code()}")
                } else {
                    Log.d("FCM", "Push token uploaded successfully")
                }
            } catch (e: Exception) {
                Log.w("FCM", "Failed to upload push token", e)
            }
        }
    }
}
