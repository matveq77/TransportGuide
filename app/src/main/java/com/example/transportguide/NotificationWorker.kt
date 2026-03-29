package com.example.transportguide

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.annotation.SuppressLint

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    @SuppressLint("MissingPermission")
    override fun doWork(): Result {
        val builder = NotificationCompat.Builder(applicationContext, "transport_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Напоминание о маршруте")
            .setContentText("Пора проверить расписание транспорта!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(1, builder.build())
        }
        return Result.success()
    }
}