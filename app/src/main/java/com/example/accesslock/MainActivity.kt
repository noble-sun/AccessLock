package com.example.accesslock

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.accesslock.ui.theme.AccessLockTheme
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityManager


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccessLockTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { TopAppBar() }
                ) { innerPadding ->
                    /* To monitor events that check if the an app is launched or in the foreground
                    we need access to the accessibility services, and that is a system wide permission
                    that the user need to allow manually. This will check if the the permission is
                    granted to the app or not, and if its not, bring a popup message that will prompt
                    the user to the Accessibility setting to grant permission.
                    */

                    /* The Accessibility Service is a multi file configuration in the app, starting
                    on the class AccessLockAccessibilityService
                     */
                    if (!isAccessibilityServiceEnabled(this)) {
                        showAccessibilityDialog()
                    } else {
                        // If the app already has permission, list all the apps installed.

                        /* When you grant permission to the app and go back from the system settings
                        to the app, the list will not render, not sure why, and not going to fix now.
                        Just close the app and open it again =)
                         */
                        ListInstalledApps(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }

    private fun showAccessibilityDialog() {

        AlertDialog.Builder(this)
            .setTitle("Accessibility Service Permission")
            .setMessage("Please allow AccessLock to use Accessibility Settings to use the app.")
            .setPositiveButton("Enable") { _, _ ->
                // This will redirect to user to the Accessibility Settings
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Not Now") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false) // Prevent user from dismissing by touching outside
            .show()
    }

    // This checks if the app has the permission
    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (enabledService in enabledServices) {
            if (enabledService.id.contains("com.example.accesslock")) {
                return true
            }
        }
        return false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar() {
    CenterAlignedTopAppBar(
        title = { Text("AccessLock", maxLines = 1)},
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
        }
    )
}

@Composable
fun ListInstalledApps(modifier: Modifier) {
    val context = LocalContext.current

    // Right now this is magic to me, do not fully understand
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null)
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

    val resolvedInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.queryIntentActivities(
            mainIntent,
            PackageManager.ResolveInfoFlags.of(0L)
        )
    } else {
        pm.queryIntentActivities(mainIntent, 0)
    }

    resolvedInfos.sortBy { it.activityInfo.applicationInfo.loadLabel(pm).toString() }

    LazyColumn(modifier = modifier){
        items(resolvedInfos) { app ->
            var checked by remember { mutableStateOf(true)}

            Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                Row() {

                    Switch(checked = checked, onCheckedChange = { checked = it })

                    val bitmap = drawableToBitmap(app.activityInfo.loadIcon(pm)).asImageBitmap()
                    Image(bitmap = bitmap, contentDescription = "app icon", modifier = Modifier.size(48.dp).padding(8.dp))
                    Text(
                        text = app.activityInfo.applicationInfo.loadLabel(pm).toString(),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }

}

//another magic that converts a drawable resource to a bitmap that can be used on the Image component
fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }

    val width = drawable.intrinsicWidth.coerceAtLeast(1)
    val height = drawable.intrinsicHeight.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Surface(color = Color.Green) {
        Text(
            text = "Hello $name!",
            modifier = modifier.padding(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AccessLockTheme {
        Greeting("there")
    }
}