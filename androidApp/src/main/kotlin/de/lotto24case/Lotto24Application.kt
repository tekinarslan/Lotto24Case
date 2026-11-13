package de.lotto24case

import android.app.Application
import de.lotto24case.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class Lotto24Application : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger(Level.ERROR)
            androidContext(this@Lotto24Application)
        }
    }
}
