package com.pachatary.presentation.common.injection.scheduler

import io.reactivex.Scheduler

class SchedulerProvider(private val subscriberScheduler: Scheduler,
                        private val observerScheduler: Scheduler) {

    fun subscriber() = subscriberScheduler
    fun observer() = observerScheduler
}
