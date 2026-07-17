package com.notouch.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var pinInput: EditText
    private lateinit var keepScreenOnSwitch: SwitchMaterial
    private lateinit var statusText: TextView

    private val overlayPermissionRequestCode = 1001
    private val accessibilityRequestCode = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pinInput = findViewById(R.id.pinInput)
        keepScreenOnSwitch = findViewById(R.id.keepScreenOnSwitch)
        statusText = findViewById(R.id.statusText)
        val startButton = findViewById<Button>(R.id.startButton)
        val accessibilityButton = findViewById<Button>(R.id.accessibilityButton)

        pinInput.setText(Prefs.getPin(this))
        keepScreenOnSwitch.isChecked = Prefs.getKeepScreenOn(this)

        accessibilityButton.setOnClickListener {
            if (isAccessibilityServiceEnabled()) {
                Toast.makeText(this, "Gesture blocking is already enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Find and enable \"NoTouch\" in the list, then come back",
                    Toast.LENGTH_LONG
                ).show()
                @Suppress("DEPRECATION")
                startActivityForResult(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                    accessibilityRequestCode
                )
            }
        }

        startButton.setOnClickListener {
            val pin = pinInput.text.toString()
            if (pin.length < 4) {
                Toast.makeText(this, "Please enter a PIN with at least 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Prefs.savePin(this, pin)
            Prefs.setKeepScreenOn(this, keepScreenOnSwitch.isChecked)

            if (!Settings.canDrawOverlays(this)) {
                statusText.text = "Grant 'display over other apps' permission, then press Start again."
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                @Suppress("DEPRECATION")
                startActivityForResult(intent, overlayPermissionRequestCode)
                return@setOnClickListener
            }

            startTouchLock()
        }
    }

    private fun startTouchLock() {
        val serviceIntent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        statusText.text = if (isAccessibilityServiceEnabled()) {
            "Touch lock active with gesture blocking. Tap the floating lock button to unlock."
        } else {
            "Touch lock active. Tap the floating lock button to unlock.\n(Gesture blocking is off — enable it above for stronger locking.)"
        }
        moveTaskToBack(true)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponent = "$packageName/${NoTouchAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(":").any { it.equals(expectedComponent, ignoreCase = true) }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            overlayPermissionRequestCode -> {
                statusText.text = if (Settings.canDrawOverlays(this)) {
                    "Permission granted. Press Start to begin."
                } else {
                    "Overlay permission is required to use touch lock."
                }
            }
            accessibilityRequestCode -> {
                statusText.text = if (isAccessibilityServiceEnabled()) {
                    "Gesture blocking enabled."
                } else {
                    "Gesture blocking is off. You can still use basic touch lock."
                }
            }
        }
    }
}
