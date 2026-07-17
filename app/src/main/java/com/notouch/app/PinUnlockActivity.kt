package com.notouch.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PinUnlockActivity : AppCompatActivity() {

    private var enteredPin = StringBuilder()
    private var unlocked = false
    private lateinit var pinDisplay: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_unlock)

        pinDisplay = findViewById(R.id.pinDisplay)

        val digitButtons = mapOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2",
            R.id.btn3 to "3", R.id.btn4 to "4", R.id.btn5 to "5",
            R.id.btn6 to "6", R.id.btn7 to "7", R.id.btn8 to "8",
            R.id.btn9 to "9"
        )

        for ((id, digit) in digitButtons) {
            findViewById<Button>(id).setOnClickListener {
                if (enteredPin.length < 6) {
                    enteredPin.append(digit)
                    updateDisplay()
                }
            }
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            enteredPin.clear()
            updateDisplay()
        }

        findViewById<Button>(R.id.btnEnter).setOnClickListener {
            checkPin()
        }
    }

    private fun updateDisplay() {
        pinDisplay.text = "\u2022".repeat(enteredPin.length)
    }

    private fun checkPin() {
        val storedPin = Prefs.getPin(this)
        if (enteredPin.toString() == storedPin) {
            unlocked = true
            stopService(Intent(this, OverlayService::class.java))
            finish()
        } else {
            Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
            enteredPin.clear()
            updateDisplay()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // If we're leaving without a successful unlock, put the touch
        // lock and floating button back on screen.
        if (!unlocked) {
            OverlayService.instance?.restoreOverlaysAfterUnlock()
        }
    }
}
