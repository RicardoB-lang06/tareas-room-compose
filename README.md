# Mis tareas · Room + Jetpack Compose

Aplicación Android en Kotlin con persistencia local, CRUD completo y una interfaz en español. Los datos se guardan en `tasks.db` y se conservan al cerrar la aplicación. No requiere cuenta ni conexión a Internet para funcionar.

## Abrir y ejecutar

1. En Android Studio selecciona **Open** y abre la carpeta `TareasRoom`, que contiene `settings.gradle.kts`.
2. Utiliza Android Studio Meerkat Feature Drop o posterior, con **Gradle JDK 17 o 21**. La configuración está en Settings → Build, Execution, Deployment → Build Tools → Gradle. No uses JDK 25 con Gradle 8.13.
3. En SDK Manager instala **Android SDK Platform 35** y **Build-Tools 35.0.0** si aún no están disponibles.
4. Sincroniza Gradle. La primera sincronización necesita Internet para descargar las dependencias.
5. Selecciona el módulo **app** y pulsa **Run** en un dispositivo o emulador con **Android 8.0 / API 26 o superior**.

El proyecto incluye Gradle Wrapper 8.13. Android Studio puede crear `local.properties` con la ruta del SDK de tu equipo; ese archivo no se incluye en la entrega.

## Funciones

- **Crear:** pulsa el botón **+**, completa el formulario y selecciona Guardar.
- **Listar:** las tareas aparecen en una `LazyColumn`, primero las pendientes y luego las completadas; cada grupo se ordena por fecha límite.
- **Editar:** pulsa Editar para modificar título, descripción o fecha.
- **Completar:** utiliza el `Switch` de la tarjeta. El progreso y los filtros se actualizan automáticamente.
- **Eliminar:** pulsa Eliminar y confirma en el diálogo.
- **Filtrar:** Todas, Pendientes y Hechas.
- Estado vacío, indicador de carga, recuperación tras errores, modo oscuro y etiquetas de accesibilidad.

## Validaciones y fechas

- Título obligatorio, de 1 a 80 caracteres después de quitar espacios en los extremos.
- Descripción opcional, máximo 500 caracteres después de quitar espacios en los extremos.
- Fecha obligatoria: hoy o posterior al crear o cambiar una fecha; rango del calendario 1900–2100.
- Se permite editar una tarea vencida conservando su fecha original.
- Los errores aparecen junto al campo correspondiente. Un fallo de guardado conserva el formulario para reintentar.
- Se bloquean los controles mientras se guarda para evitar operaciones duplicadas.
- `dueDate` es un `Long` en milisegundos desde Epoch: representa una **fecha de calendario a medianoche UTC**, sin hora ni recordatorio. El calendario y la presentación usan la misma convención para evitar que la fecha cambie al convertirla a otra zona horaria. “Hoy” se calcula según el calendario local del dispositivo.

## Organización

```text
app/src/main/java/com/example/tareasroom/
├── MainActivity.kt               Entrada de Compose y creación del ViewModel
├── TaskApplication.kt            Contenedor de dependencias
├── data/
│   ├── Task.kt                   Entidad con id, title, description, isCompleted, dueDate
│   ├── TaskDao.kt                Insertar, actualizar, eliminar y observar con Flow
│   ├── AppDatabase.kt            RoomDatabase Singleton, versión 1
│   └── TaskRepository.kt         Contrato y repositorio que utiliza el DAO
└── ui/
    ├── TaskViewModel.kt          StateFlow, eventos y viewModelScope
    ├── TaskValidation.kt         Reglas de entrada y conversión de fechas
    ├── TaskListScreen.kt         Lista, filtros, progreso, FAB y Switch
    ├── TaskEditorDialog.kt       Formulario de creación/edición y DatePicker
    └── theme/Theme.kt            Colores claro/oscuro
```

**Flujo de datos:** Compose envía eventos al ViewModel; el ViewModel ejecuta operaciones en `viewModelScope`; el repositorio llama al DAO. Room emite la lista mediante `Flow`, el ViewModel la expone como `StateFlow` y Compose la recoge con `collectAsStateWithLifecycle()`.

El Singleton utiliza `@Volatile`, `synchronized` y `applicationContext`. Las escrituras del DAO son `suspend`; no se permite acceso a la base de datos en el hilo principal. El cambio de estado actualiza únicamente `isCompleted`, evitando sobrescribir los otros campos con una copia antigua. El borrador se mantiene durante cambios de configuración gracias al ViewModel; no se persiste un borrador sin guardar tras la terminación del proceso.

## Versiones

| Componente | Versión |
|---|---|
| Room (runtime, ktx, compiler) | **2.7.1** |
| Kotlin / Compose Compiler | 2.1.20 |
| KSP | 2.1.20-1.0.32 |
| Compose BOM | 2025.04.01 |
| Android Gradle Plugin | 8.11.1 |
| Gradle Wrapper | 8.13 |
| Lifecycle | 2.9.0 |
| SDK mínimo / compilación / destino | 26 / 35 / 35 |

Room se configura con KSP en `app/build.gradle.kts`; el esquema se exporta a `app/schemas` al compilar. Cuando se modifique la entidad, hay que incrementar la versión de la base de datos y añadir una migración. No se usa migración destructiva.

Referencias oficiales: [Room y sus dependencias](https://developer.android.com/jetpack/androidx/releases/room), [compatibilidad de AGP 8.11](https://developer.android.com/build/releases/agp-8-11-0-release-notes) y [Compose BOM de abril de 2025](https://android-developers.googleblog.com/2025/04/whats-new-in-jetpack-compose-april-25.html).

## Comprobaciones

Desde una terminal en la carpeta del proyecto, con JDK 17 o 21:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
# Con un dispositivo o emulador conectado:
.\gradlew.bat :app:connectedDebugAndroidTest
```

En macOS/Linux utiliza `chmod +x gradlew` una vez y sustituye `.\gradlew.bat` por `./gradlew`.

- `TaskValidationTest`: límites, espacios, fecha vacía, fecha pasada, edición de vencidas y conversión de fechas.
- `TaskViewModelTest`: validación antes de guardar, CRUD reactivo, prevención de doble guardado y recuperación de errores.
- `TaskDaoTest` (instrumentada): identificadores autogenerados, orden, CRUD y persistencia al cerrar y reabrir una base de datos real de prueba.

Para comprobar la persistencia manualmente, crea una tarea, cierra por completo la aplicación y vuelve a abrirla. La tarea debe permanecer. Desinstalar la aplicación o borrar sus datos elimina la base local.

El estado de la verificación realizada durante la entrega se registra en `VERIFICACION.md`.
