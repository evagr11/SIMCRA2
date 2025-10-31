package com.example.simcra2.Controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.simcra2.Model.Alerta;
import com.example.simcra2.R;

import java.util.List;

public class AlertaReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int idAlerta = intent.getIntExtra("id_alerta", -1);
        if (idAlerta == -1) return;

        Alerta alerta = buscarAlertaPorId(context, idAlerta);
        if (alerta == null) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "canal_alertas")
                .setSmallIcon(R.mipmap.ic_alert)
                .setContentTitle(alerta.getTitular())
                .setContentText(alerta.getResumen())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        try {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
                    context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                            == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(alerta.getId(), builder.build());
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private Alerta buscarAlertaPorId(Context context, int id) {
        List<Alerta> alertas = AlertasManager.obtenerAlertasDisponibles(context);
        for (Alerta a : alertas) {
            if (a.getId() == id) return a;
        }
        return null;
    }
}