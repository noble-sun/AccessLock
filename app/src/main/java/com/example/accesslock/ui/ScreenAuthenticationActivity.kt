package com.example.accesslock.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.accesslock.ui.theme.AccessLockTheme
import java.util.concurrent.atomic.AtomicBoolean

class ScreenAuthenticationActivity : ComponentActivity() {
    /* registerForActivityResult starts an activity for result. What this mean is that when this
    variable starts an activity, it expects a response back. In this case the response its expecting
    is the permission to overlay apps or not. The argument passed to this function is basically saying
    to start a generic activity.

    If the response is that we can overlay apps, we display the authentication screen.
     */
    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Settings.canDrawOverlays(this)) {
                showAuthenticationOverlay()
            } else {
                Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    companion object {
        /* AtomicBoolean is an implementation to try to avoid race conditions on multithreaded situations.
        So if more then on process try to change the value at the same time, magic happens and only
        one will be able to update the value, and then the other process will access the updated value.
         */
        var isAuthenticationScreenVisible = AtomicBoolean(false)
    }

    override fun onResume() {
        super.onResume()
        /* When this activity is opened, it means that we need and will show the authentication screen,
        so we define this variable as true
         */
        isAuthenticationScreenVisible.set(true)
    }

    override fun onPause() {
        super.onPause()
        /* If this activity is not in the foreground anymore, the authentication screen is not being
        shown, so we set this as false
         */
        isAuthenticationScreenVisible.set(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccessLockTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                }
            }
        }
        checkOverlayPermission()
    }

    private fun checkOverlayPermission() {
        /* Here we check the permission again because the user might have removed the permission it
        had granted before by going to the setings manually.
         */
        if (!Settings.canDrawOverlays(this)) {
            /* If we do not have permission to overlay, we set the intent to the overlay permission
            setting, and the `data` says to the system what package is requesting this permission.

            We launch the intent using `overlayPermissionLauncher`, so we expect a response that will
            be handled inside the lambda define with the variable at the beginning.
             */
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:com.example.accesslock")
            overlayPermissionLauncher.launch(intent)
        } else {
            showAuthenticationOverlay()
        }
    }

    private fun showAuthenticationOverlay() {
        Log.d("ScreenAuthenticationActivityLog", "WILL SHOW AUTHENTICATION SCREEN")
    }
}
