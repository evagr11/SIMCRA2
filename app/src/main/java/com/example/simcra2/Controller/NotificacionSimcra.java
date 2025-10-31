package com.example.simcra2.Controller;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.simcra2.Model.Alerta;
import com.example.simcra2.R;

public class NotificacionSimcra {

    public static void lanzar(Context context, Alerta alerta) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "canal_alertas")
                .setSmallIcon(R.mipmap.ic_alert)
                .setContentTitle(alerta.getTitular())
                .setContentText(alerta.getResumen())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(alerta.getId(), builder.build());
    }
}