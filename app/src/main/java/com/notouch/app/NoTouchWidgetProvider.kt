package com.notouch.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews
import android.widget.Toast

class NoTouchWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_START_LOCK = "com.notouch.app.ACTION_START_LOCK"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_notouch)

            val clickIntent = Intent(context, NoTouchWidgetProvider::class.java).apply {
                action = ACTION_START_LOCK
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                widgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetStartButton, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_START_LOCK) {
            startLock(context)
        }
    }

    private fun startLock(context: Context) {
        val pin = Prefs.getPin(context)
        if (pin.length < 4) {
            Toast.makeText(context, "Open NoTouch first and set a PIN", Toast.LENGTH_LONG).show()
            openApp(context)
            return
        }

        if (!Settings.canDrawOverlays(context)) {
            Toast.makeText(context, "Open NoTouch and grant the overlay permission first", Toast.LENGTH_LONG).show()
            openApp(context)
            return
        }

        val serviceIntent = Intent(context, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        Toast.makeText(context, "Touch lock active", Toast.LENGTH_SHORT).show()
    }

    private fun openApp(context: Context) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(launchIntent)
    }
}
