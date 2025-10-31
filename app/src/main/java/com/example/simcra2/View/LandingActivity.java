package com.example.simcra2.View;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.simcra2.Controller.AlertasManager;
import com.example.simcra2.R;

public class LandingActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private Button btnAcceder;
    private boolean yaVerificado = false;

    public static final String PREFS_APP = "PreferenciasApp";
    public static final String KEY_PRIMERA_VEZ = "esPrimeraVez";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        btnAcceder = findViewById(R.id.btnAccederInfo);
        progressBar = findViewById(R.id.progressBar);

        btnAcceder.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            btnAcceder.setVisibility(View.VISIBLE);
        }, 1000);

        btnAcceder.setOnClickListener(v -> {
            btnAcceder.setEnabled(false);
            btnAcceder.setText("Accediendo...");

            SharedPreferences prefs = getSharedPreferences(PREFS_APP, Context.MODE_PRIVATE);
            boolean esPrimeraVez = prefs.getBoolean(KEY_PRIMERA_VEZ, true);

            Intent intent;
            if (esPrimeraVez) {
                // Marca que ya no es primera vez
                prefs.edit().putBoolean(KEY_PRIMERA_VEZ, false).apply();
                intent = new Intent(LandingActivity.this, Info1.class);
            } else {
                intent = new Intent(LandingActivity.this, General.class);
            }

            startActivity(intent);
            finish();
        });


        // Crear canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    "canal_alertas",
                    "Alertas Simcra",
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription("Notificaciones importantes de alertas");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(canal);
        }

        // Solicitar permiso de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            } else {
                verificarAlarmasExactas();
            }
        } else {
            verificarAlarmasExactas();
        }
    }

    // Verifica si el sistema permite alarmas exactas
    private void verificarAlarmasExactas() {
        if (yaVerificado) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Activa las alarmas exactas para recibir alertas programadas", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return;
            }
        }

        yaVerificado = true;
        iniciarSimcra();
    }

    // Lanza la lógica de alertas reales
    private void iniciarSimcra() {
        AlertasManager.generarAlertas(this); // Si esto carga las alertas en memoria o base de datos
        AlertasManager.programarTodasLasAlertas(this); // Programa todas las alertas reales
    }

    // Manejo del resultado del permiso de notificaciones
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                verificarAlarmasExactas();
            } else {
                Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Detecta si el usuario regresa de ajustes y vuelve a verificar
    @Override
    protected void onResume() {
        super.onResume();

        if (!yaVerificado) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                verificarAlarmasExactas();
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                verificarAlarmasExactas();
            }
        }
    }
}