package com.example.services

import android.accessibilityservice.AccessibilityService
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.eventTypeToString
import com.example.accesslock.ui.ScreenAuthenticationActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* We need to configured this service in a tag <service> on the AndroidManifest.xml, and then setup other
configurations for the service in another xml file. Ideally I could create a more detailed readme for
this configuration, but who knows if I will actually do this.
This is a good base to what you need to configure a service:
https://developer.android.com/guide/topics/ui/accessibility/service
 */

// To create a new service, we need to extend the class to the desired service
class AccessLockAccessibilityService : AccessibilityService() {
    // Companion objects are essentially static constants
    companion object {
        // Constant used for logs
        private const val TAG = "AccessLockService"
        /* Originally the list of packages was passed as an argument to this class, but that's not
        allowed when configuring the service on the AndroidManifest.xml, so for now this is just a
        static list to test if it works. Need to figure it out another way to dynamically set this up.
         */

        val packageNames = listOf(
            "com.google.android.deskclock",
            "com.google.android.calendar",
            "com.android.chrome",
            "com.google.android.youtube",
            "com.example.accesslock"
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packName = event.packageName?.toString()
        Log.d(TAG, "AccessibilityEvent: Type = ${eventTypeToString(event.eventType)}, Package = $packName")

        // This is the event that is triggered when an app if brought to the foreground
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()

            /* If the packageName is not-null this `let` function just creates a block to manipulate
            the variable with `it`. And if this value is within the packages allowed on the companion
            variable above, do what you need to do, which right now is nothing.
             */
            packageName?.let {
//                Log.d(TAG, "PACKAGE NAME IS $it")
                if (packageNames.contains(it)) {
//                    Log.d(TAG, "Needs to lock app Launched: $it")
                    showLockScreenOverlay()
                }
            }
        }
    }

//    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())

    private fun showLockScreenOverlay() {
        /* This checks if the variable is true or false. If its true, it means that the authentication
        screen is in the foreground, so it will not try to call the activity that renders the screen
        again. If its false, it will start the activity for the authentication screen.
         */
        if (!ScreenAuthenticationActivity.isAuthenticationScreenVisible.get()) {
            handler.post {
                if (!ScreenAuthenticationActivity.isAuthenticationScreenVisible.get()) {
                    val intent = Intent(this@AccessLockAccessibilityService, ScreenAuthenticationActivity::class.java)

                    /* FLAG_ACTIVITY_NEW_TASK: when you start an activity from a service, the activity needs
                    a task, so the activity might try to join the task of the app that you tried to open, which
                    is not nice. This flag creates a new task for the ScreenAuthenticationActivity. A task
                    meaning a stack of activities, kind of a history of the workflow of activities you accessed
                    in an app.
                    FLAG_ACTIVITY_CLEAR_TASK: If we already have an instance of ScreenAuthenticationActivity,
                    we bring that forward and clear the other activities that were on top of it, so when you
                    trigger the dismiss or go back action, you don't have to go back several times.
                    FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS: Since this is just supposed to be a authentication
                    screen, it should not appear in the recent list of apps opened.
                     */
                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    )


//                    val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val pendingIntent = PendingIntent.getActivity(this@AccessLockAccessibilityService, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//                    } else {
//                        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//                    }

                    handler.postDelayed({
                    try {
                        pendingIntent.send()
                    } catch (e: Exception) {
                        Log.e(TAG, "PendingIntent canceled", e)
                    }
//                    startActivity(intent)
                    }, 200)

                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service interrupted")
    }

    override fun onDestroy() {
    }
}