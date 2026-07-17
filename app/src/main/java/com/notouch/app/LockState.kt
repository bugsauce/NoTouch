package com.notouch.app

/**
 * Simple in-memory flag shared between OverlayService and
 * NoTouchAccessibilityService (both run in the same process).
 */
object LockState {
    @Volatile
    var isActive: Boolean = false
}
