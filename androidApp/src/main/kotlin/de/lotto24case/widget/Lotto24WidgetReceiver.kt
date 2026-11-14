package de.lotto24case.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * BroadcastReceiver binding [Lotto24GlanceWidget] to the Android Home Screen system.
 */
class Lotto24WidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = Lotto24GlanceWidget()
}
