package com.ctonew.composemodular

import android.app.Application
import com.ctonew.composemodular.data.work.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ModularComposeApp : Application() {
    
    @Inject
    lateinit var workScheduler: WorkScheduler

    override fun onCreate() {
        super.onCreate()
        workScheduler.schedulePeriodicSyncJobs()
    }
}
