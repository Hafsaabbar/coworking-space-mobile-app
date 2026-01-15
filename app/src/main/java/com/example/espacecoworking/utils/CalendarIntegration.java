package com.example.espacecoworking.utils;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;

public class CalendarIntegration {

    public static void addBookingToCalendar(Context context, String title, String description, long beginTime, long endTime) {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                .putExtra(CalendarContract.Events.ALL_DAY, false);
        context.startActivity(intent);
    }
}
