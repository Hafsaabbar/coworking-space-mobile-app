package com.example.espacecoworking.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.example.espacecoworking.R;
import com.example.espacecoworking.activities.MainActivity;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationUtils {

    // Ce compteur garantit un ID unique pour chaque notification.
    private final static AtomicInteger c = new AtomicInteger(0);

    public static void showBookingConfirmationNotification(Context context, String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // On génère un ID unique pour éviter les conflits qui peuvent causer des crashs.
        int uniqueId = c.incrementAndGet();

        // On utilise cet ID unique pour le PendingIntent.
        PendingIntent pendingIntent = PendingIntent.getActivity(context, uniqueId, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_arrow_back) // Utilisation d'une icône qui existe
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Et on utilise le même ID unique pour la notification.
            notificationManager.notify(uniqueId, notificationBuilder.build());
        }
    }
}
