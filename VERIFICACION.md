# Verificación de la entrega

Fecha: 9 de septiembre de 2026.

## Ejecutado correctamente

- Generación del Gradle Wrapper 8.13 mediante Gradle oficial: `BUILD SUCCESSFUL`.
- Resolución y configuración de los plugins del proyecto Android con JDK 17.
- **6 pruebas JUnit de `TaskValidationTest`: 6 aprobadas, 0 fallos, 0 errores.** Se compilaron y ejecutaron los archivos originales `TaskValidation.kt` y `TaskValidationTest.kt` en un proyecto Kotlin/JVM auxiliar, sin modificarlos y sin utilizar el SDK Android.
- Lectura y análisis de los cuatro archivos XML de recursos y manifiesto.
- Revisión de la estructura, dependencias y operaciones CRUD del código fuente.

## Pendiente en Android Studio

Se intentó ejecutar `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` y `:app:assembleDebugAndroidTest`. El proceso se detuvo antes de compilar el código Android porque el entorno no pudo leer el SDK instalado: acceso denegado a `platforms/android-35/package.xml` y otros archivos. Gradle tampoco pudo comprobar las licencias de los componentes instalados.

Por ello, **la compilación completa, el APK, Android Lint, las pruebas del ViewModel y las pruebas instrumentadas de Room no están verificados**. No se ejecutó la interfaz en un dispositivo o emulador. Las pruebas del ViewModel y del DAO se entregan como código para ejecutarlas con el SDK accesible.

Abre el proyecto con Android Studio, verifica Platform 35 y Build-Tools 35.0.0 en SDK Manager y ejecuta los comandos del README. Si Android Studio solicita aceptar licencias, revísalas en SDK Manager. El proyecto no contiene rutas locales de este equipo.
