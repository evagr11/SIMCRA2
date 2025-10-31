# SIMCRA2

🧠 1. Uso de herramientas de IA
Marca las que hayas utilizado durante el desarrollo del proyecto:

- [ ] ChatGPT

- [X] Copilot

- [ ] Gemini

- [ ] Otra: ________________________



Descripción breve

Explica qué parte del trabajo se realizó con ayuda de la IA (máx. 5 líneas).
Utilice la IA para la parte de Hardware (Bateria, Informacion y sensores) para saber como tenia que acceder a esta informacion, y me dio tambien los metodos `onPause()` y `onResume()`. Para los "Adapter", más para el de desaparecidos que para el de alertas. Y para la parte que mas abuse fue para todos los permisos y comprobaciones de versiones y creacion de las alertas/notificaciones



Ejemplo:  

> Se utilizó ChatGPT para generar la estructura base de la clase `UserAdapter`. Luego se modificaron los métodos `bind()` y `onCreateViewHolder()` para adaptarlos al proyecto.



---



🔍 2. Fragmentos generados o modificados con IA

Copia aquí **los fragmentos relevantes** que provinieron de la IA y explica qué cambios realizaste
## LandingActivity.java

```java
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                controller.verificarAlarmasExactas(this);
            } else {
                Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!controller.yaVerificado()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                controller.verificarAlarmasExactas(this);
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                controller.verificarAlarmasExactas(this);
            }
        }
    }
```

`onRequestPermissionsResult` : se ejecuta cuando un usuario responde a una solicitud de permisos. 
- `requestCode == 100` le indico el codigo de la solicitud que quiero q verifique
- `grantResults[0] == PackageManager.PERMISSION_GRANTED` comprueba si el usuario concedio el permiso. Dependiendo de la respuesta hace q la logica continue o q muestre un Toast

`onResume` : configura notificaciones programadas, dependiendo de si el permiso esta disponible o no 

## General.java

La IA me metio todo en onCrate, y yo lo saque en diferentes funciones a las q luego llamaba, tambien elimine comprobaciones que veia innecesarias (asumiendo q esta informacion la tendrian todos los dispositivos
```java
    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null && sensorListener != null) {
            sensorManager.unregisterListener(sensorListener);
        }
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && sensorListener != null) {
            Sensor sensorGiroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            Sensor sensorAcelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (sensorGiroscopio != null) {
                sensorManager.registerListener(sensorListener, sensorGiroscopio, SensorManager.SENSOR_DELAY_UI);
            }
            if (sensorAcelerometro != null) {
                sensorManager.registerListener(sensorListener, sensorAcelerometro, SensorManager.SENSOR_DELAY_UI);
            }
        }
        if (batteryReceiver != null) {
            registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
    }
```

`onPause` : "cuida" la bateria ya que pausa estos valores cuando el usuario cambia de pantalla o apaga la app
- `sensorManager.unregisterListener(sensorListener)` detiene la escucha de los sensores
- `unregisterReceiver(batteryReceiver)` hace lo mismo con los receptores de eventos de la bateria

`onResume` : vuelve a activar el codigo cuando vuelve a primer plano
- `registerListener(...)` vuelve a activar el sensorListener
- `registerReceiver(...)` igual para la bateria

## DesaparecidosAdapter.java

Mas que lógica me ayudo a crear la lista y como se iba a ir generando, no pongo el codigo aqui ya que le tengo entero comentado en el proyecto con lo que hace cada cosa. (View.DesaparecidoAdapter)

## BaseActivity.java

Aqui m ayudo con la estructura que tenia que tener en menu d navegacion para que funcionase y a la implementacion directamente del Boton del desarrollador sin xml (son campos de atributes de un xml pero le doy el valor aqui. Y en el menu de navegacion, lo mas "raro" que m dio fue `if (!(BaseActivity.this instanceof (pantalla a la q m refiera))`, q le pregunte y m dijo que era por si ya estaba en esa pantalla y le volvia a dar que no se "acumulase" la misma pantalla

## NotificacionSimcra.java

Esta clase es para cuando lanzo las alertas manualmente si el permiso no esta concedido sale del metodo sin hacer nada
```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
        return;
    }
}
```

Aquí se crea la "estetica" de la notificacion

```java
NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "canal_alertas")
        .setSmallIcon(R.mipmap.ic_alert)
        .setContentTitle(titulo)
        .setContentText(mensaje)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true);
```

Hago q el id d la notificacion se genere con `System.currentTimeMillis()` para que sea unico

```java
manager.notify((int) System.currentTimeMillis(), builder.build());
```

## AlertaReceiver.java

El codigo es similar a NotificacionSimcra, pero esta se activa automaticamente cuando recibe un Broadcast

AlertasManager.java
este fragmento lo que hace es obtener el indice manual guardado, q se una en modo desarrollador
```java
public static List<Alerta> obtenerAlertasDisponibles(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    int indiceManual = prefs.getInt(CLAVE_INDICE, 0);
}
```

Determina el numero d dias que han pasado desde la fecha d inicio y la fecha de una alerta
```java
//(en este caso lo q hare sera comentar el codigo bien)
    private static int calcularDiaDesdeFecha(String fechaStr, Context context) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date fechaAlerta = formatter.parse(fechaStr); //Convierte la cadena fechaStr en un objeto Date

            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String fechaInicioStr = prefs.getString("fecha_inicio_alertas", null); //Recupera la fecha de inicio guardada bajo la clave "fecha_inicio_alertas"
            if (fechaInicioStr == null) return 1; //Si no existe, devuelve 1 como valor por defecto

            SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date fechaInicio = dateOnly.parse(fechaInicioStr); //Convierte la cadena de fecha de inicio en un objeto Date.

            long diffMillis = fechaAlerta.getTime() - fechaInicio.getTime(); //Calcula la diferencia en milisegundos entre ambas fechas
            long diffDias = diffMillis / (1000 * 60 * 60 * 24); //Convierte esa diferencia a días completos

            return (int) diffDias + 1; //return (int) diffDias + 1;

        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }
```

Permite que la app programe una notificacion para que salte en una fecha y hora exactas, incluso si el dispositivo está en reposo
```java
//(en este caso lo q hare sera comentar el codigo bien)
    public static void programarNotificacion(Context context, String titulo, String mensaje, long tiempoEnMillis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) return;
        }

        //crea un Intent que sera recibido por AlertaReceiver
        Intent intent = new Intent(context, AlertaReceiver.class);
        //Añade los datos necesarios para construir la notificacion
        intent.putExtra("titulo", titulo);
        intent.putExtra("mensaje", mensaje);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) tiempoEnMillis, //tiempoEnMillis como identificador único para evitar colisiones
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE //Las flags aseguran que el intent sea inmutable y se actualice si ya exist
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoEnMillis, pendingIntent);
    }
```
- `PendingIntent` permite que el sistema ejecute el Intent en el futuro
- `RTC_WAKEUP` despierta el dispositivo si esta dormido
- `setExactAndAllowWhileIdle` garantiza que se ejecute en el momento exacto, incluso en modo ahorro de bateria

## LandingController.java

Esta clase la cree yo al final del proyecto ya q la IA lo habia puesto en LandingActivity, y lo moci para respetar el MVC.
- `obtenerIntentAcceso(Context context)` verifica si es la primera vez que el usuario abre la app, si es asi guarda false y dirige a Info1 y si no a General
- `crearCanalNotificacion(Context context)` crea el "canal_alertas" para mostrar notificaciones, esto es necesario para que las notificaciones funcionen
- `necesitaPermisoNotificaciones(Context context)` verifica si la version y si el permiso de notificaciones no ha sido concecido
- `solicitarPermisoNotificaciones(Activity activity)` solicita el permiso de notificacion al usuario, se utiliza 100 para identificar la respuesta en onRequestPermissionsResult
- `verificarAlarmasExactas(Context context)` verifica si el sistema permite alarmas exactas. Si no estan activas, muestra un mensaje y abre la configuracion, para que el usuario lo haga. Si todo esta correcto marta yaVerificado = true y llama a iniciarSimcra(context)
- `iniciarSimcra(Context context)` llama a los metodos AlertasManager para generar y programar todas las alertas disponibles
  

## COMPROBACIONES DE VERSION

### `(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)`

Verifica si el dispositivo tiene Android 8.0 Oreo (API 26) o superior.

Se usa cuando necesitas usar clases o métodos introducidos en Oreo, como NotificationChannel o LocalDateTime

### `(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)`

Verifica si el dispositivo tiene Android 13 (API 33) o superior.

Se usa para comprobar si necesitas pedir el permiso POST_NOTIFICATIONS, obligatorio desde esta versión para mostrar notificaciones.

### `(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)`

Verifica si el dispositivo tiene Android 12 (API 31) o superior.

Se usa para comprobar si puedes usar funciones como AlarmManager.canScheduleExactAlarms() o para solicitar permiso para alarmas exactas.

