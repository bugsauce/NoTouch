package com.notouch.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * While the touch lock is active, this service watches for the system UI
 * (notification shade, quick settings, recents) coming to the front and
 * immediately dispatches a "back" action to close it again. This is a
 * best-effort defense — apps without Device Owner / root access cannot
 * fully block system gestures on stock Android, but this stops the shade
 * or quick settings from staying open and being usable.
 */
class NoTouchAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !LockState.isActive) return

        val pkg = event.packageName?.toString() ?: return

        // "com.android.systemui" owns the notification shade, quick
        // settings panel, recents/overview screen, and volume panel.
        if (pkg == "com.android.systemui") {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    override fun onInterrupt() {
        // No-op
    }
}
