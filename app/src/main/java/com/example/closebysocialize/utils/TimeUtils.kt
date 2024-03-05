package com.example.closebysocialize.utils

import android.content.Context
import com.example.closebysocialize.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {
    fun formatTimestamp(context: Context, date: Date?): String {
        date ?: return context.getString(R.string.date_unknown)
        val now = Calendar.getInstance()
        val messageDate = Calendar.getInstance().apply { time = date }
        val formatterTime = SimpleDateFormat("HH:mm", Locale.getDefault())

        if (messageDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            messageDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
        ) {
            return formatterTime.format(date)
        }

        val yesterday = now.apply { add(Calendar.DAY_OF_YEAR, -1) }
        if (messageDate.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            messageDate.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
        ) {
            return context.getString(R.string.yesterday_with_time, formatterTime.format(date))
        }

        val aWeekAgo = now.apply { add(Calendar.DAY_OF_YEAR, -7) }
        if (messageDate.after(aWeekAgo)) {
            return getDayName(context, messageDate.get(Calendar.DAY_OF_WEEK))
        }

        val formatterDate =
            SimpleDateFormat(context.getString(R.string.date_time_format), Locale.getDefault())
        return formatterDate.format(date)
    }

    private fun getDayName(context: Context, dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.MONDAY -> context.getString(R.string.monday)
            Calendar.TUESDAY -> context.getString(R.string.tuesday)
            Calendar.WEDNESDAY -> context.getString(R.string.wednesday)
            Calendar.THURSDAY -> context.getString(R.string.thursday)
            Calendar.FRIDAY -> context.getString(R.string.friday)
            Calendar.SATURDAY -> context.getString(R.string.saturday)
            Calendar.SUNDAY -> context.getString(R.string.sunday)
            else -> ""
        }
    }
}