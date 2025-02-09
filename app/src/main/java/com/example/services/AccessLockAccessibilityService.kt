package com.example.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.eventTypeToString

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
            "com.android.settings",
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
                Log.d(TAG, "PACKAGE NAME IS $it")
                if (packageNames.contains(it)) {
                    Log.d(TAG, "Needs to lock app Launched: $it")
                    showLockScreenOverlay()
                }
            }
        }
    }

    private fun showLockScreenOverlay() {
        Log.d(TAG, "SHOW AUTHENTICATION SCREEN")
    }

    override fun onInterrupt() {}
}