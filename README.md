

```
 █████╗ ██████╗  ██████╗ ██╗  ██╗ ██████╗ ███████╗████████╗
██╔══██╗██╔══██╗██╔════╝ ██║  ██║██╔═══██╗██╔════╝╚══██╔══╝
███████║██║  ██║██║  ███╗███████║██║   ██║███████╗   ██║   
██╔══██║██║  ██║██║   ██║██╔══██║██║   ██║╚════██║   ██║   
██║  ██║██████╔╝╚██████╔╝██║  ██║╚██████╔╝███████║   ██║   
╚═╝  ╚═╝╚═════╝  ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚══════╝   ╚═╝   
```

**Motor nativo de bloqueo de anuncios para Android — impulsado por Rust.**


![Platform](https://img.shields.io/badge/platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Engine](https://img.shields.io/badge/Engine-Rust-CE422B?style=for-the-badge&logo=rust&logoColor=white)
![Build](https://img.shields.io/badge/Build-Gradle%20KTS-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![License](https://img.shields.io/badge/license-Open%20Source-blue?style=for-the-badge)



---

## ¿Qué es AdGhost?

**AdGhost** es una aplicación Android que bloquea anuncios directamente en el dispositivo usando un motor de filtrado escrito en **Rust**, sin depender de servicios externos ni extensiones del navegador. Integra ese motor nativo con la capa Android vía **JNI/NDK** y presenta el contenido filtrado a través de un **WebView** embebido.

> Sin servidores intermediarios. Sin telemetría. El filtrado ocurre 100% en tu dispositivo.

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| App Android / UI | Kotlin + AndroidX |
| Motor de bloqueo | Rust (`adblock-rust`) |
| Integración nativa | JNI / Android NDK (`.so`) |
| Visualización web | WebView embebido |
| Build system | Gradle Kotlin DSL (`.kts`) |

---

## Arquitectura del proyecto

```
AdGhost/
│
├── app/                          # Módulo Android (APK)
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/adghost/app/
│       │   ├── AdGhostApp.kt     ← Application subclass
│       │   ├── MainActivity.kt   ← Entrada principal de la UI
│       │   ├── webview/          ← Lógica de filtrado en WebView
│       │   └── ui/
│       │       ├── components/   ← Componentes reutilizables
│       │       ├── screens/      ← Pantallas de la app
│       │       └── theme/        ← Estilos y tema visual
│       └── jniLibs/              ← Librerías .so empaquetadas por ABI
│
├── adblock-native/               ← Bindings JNI (Rust ↔ Android)
├── adblock-rust-master/          ← Motor de bloqueo en Rust
├── logo/                         ← Assets gráficos
│
├── build.gradle.kts              # Build raíz
├── settings.gradle.kts           # Configuración multi-proyecto
├── gradle.properties
└── gradlew / gradlew.bat
```

---

## Cómo funciona

```
  ┌─────────────────────────────────┐
  │       App Android (Kotlin)      │
  │  MainActivity · AdGhostApp.kt   │
  └────────────┬────────────────────┘
               │  llama vía JNI
               ▼
  ┌─────────────────────────────────┐
  │     Motor Nativo (Rust)         │
  │  adblock-rust · adblock-native  │
  │  → evalúa reglas de filtrado    │
  │  → bloquea / permite petición   │
  └────────────┬────────────────────┘
               │  respuesta filtrada
               ▼
  ┌─────────────────────────────────┐
  │        WebView embebido         │
  │  renderiza contenido sin ads    │
  └─────────────────────────────────┘
```

La app actúa como capa de presentación. Cada petición de red generada por el WebView pasa por el motor Rust antes de resolverse, aplicando las listas de filtros localmente.

---

## Requisitos previos

Antes de compilar, asegúrate de tener instalado:

- **Android SDK** + Platform Tools
- **Android NDK** (para compilar/enlazar las librerías nativas)
- **JDK** compatible con la versión de Gradle del proyecto
- **Rust toolchain** (`cargo`) con soporte para targets Android:

```bash
rustup target add aarch64-linux-android
rustup target add armv7-linux-androideabi
rustup target add x86_64-linux-android
```

- **cargo-ndk** (recomendado para compilación cruzada):

```bash
cargo install cargo-ndk
```

Variables de entorno necesarias:

```bash
export ANDROID_HOME=/path/to/android-sdk
export ANDROID_NDK_HOME=/path/to/ndk
export JAVA_HOME=/path/to/jdk
```

---

## Compilación e instalación

### Opción A — Las `.so` ya están en `jniLibs/` (ruta rápida)

```bash
# Clonar el repositorio
git clone https://github.com/tu-usuario/AdGhost.git
cd AdGhost

# Compilar el APK de debug
./gradlew assembleDebug

# Instalar en dispositivo/emulador conectado
./gradlew installDebug
```

### Opción B — Compilar el motor Rust desde cero

```bash
# 1. Compilar la librería nativa para cada ABI
cd adblock-rust-master
cargo ndk -t aarch64-linux-android -t armv7-linux-androideabi -t x86_64-linux-android \
      -o ../app/src/main/jniLibs build --release

# 2. Volver a la raíz y ensamblar el APK
cd ..
./gradlew assembleDebug

# 3. Instalar
./gradlew installDebug
```

### Ver logs en tiempo real

```bash
adb logcat -s AdGhost
```

---

## Funcionalidades

- **Bloqueo de anuncios en WebView** — filtra peticiones de red antes de que carguen
- **Motor Rust de alto rendimiento** — evaluación de reglas eficiente con bajo consumo de CPU/RAM
- **Listas de filtros locales** — sin dependencia de servicios externos en runtime
- **Interfaz organizada por pantallas** — activar/desactivar bloqueo, ajustes y temas
- **Sin telemetría** (auditable: el motor está en código abierto)
- **Distribución en APK único** — no requiere extensiones ni VPN del sistema

---

## El mismo motor que usa Brave

AdGhost utiliza **`adblock-rust`** — la misma librería open source que impulsa el bloqueo de anuncios del navegador **Brave**.

> Brave es uno de los navegadores con mayor enfoque en privacidad del mundo, con más de 70 millones de usuarios activos. Su motor de filtrado está escrito en Rust y es compatible con las listas de filtros de **Adblock Plus** y **uBlock Origin**.

Esto significa que AdGhost hereda de forma directa:

- ✅ Compatibilidad con las listas de filtros más usadas del ecosistema (EasyList, EasyPrivacy, etc.)
- ✅ Un motor battle-tested y auditado por la comunidad de Brave
- ✅ Actualizaciones y mejoras que llegan desde un proyecto con respaldo real

🔗 Repositorio oficial: [brave/adblock-rust](https://github.com/brave/adblock-rust)

---

## Por qué Rust para el motor

| Factor | Rust vs. Java/Kotlin puro |
|---|---|
| Rendimiento | Más rápido en evaluación de regex y listas grandes |
| Memoria | Sin GC; menor footprint en runtime |
| Seguridad | Garantías de memoria en tiempo de compilación |
| Portabilidad | Compila para múltiples ABIs Android con cargo-ndk |

---

## Consideraciones importantes

> **Legal / distribución** — Las apps de bloqueo de anuncios pueden entrar en conflicto con las políticas de Google Play. Verifica los TOS antes de publicar.

> **Mantenimiento** — La mezcla Kotlin + JNI + Rust aumenta la superficie de bugs entre versiones de NDK. Mantén las versiones del NDK y de la Rust toolchain sincronizadas.

> **Auditoría** — Aunque el bloqueo es local, revisa siempre si el código incluye telemetría u otras conexiones de red no documentadas.

---

## Dónde empezar a leer el código

| Archivo / Carpeta | Por qué es importante |
|---|---|
| `app/.../MainActivity.kt` | Punto de entrada de la UI y gestión del ciclo de vida |
| `app/.../webview/` | Interceptación de peticiones y aplicación de filtros |
| `adblock-rust-master/` | Motor de bloqueo: listas, reglas y lógica de evaluación |
| `adblock-native/` | Bindings JNI que conectan Rust con Kotlin |
| `app/src/main/jniLibs/` | `.so` compiladas por ABI listas para empaquetar |

---

## Contribuir

```bash
# Fork → clone → rama descriptiva
git checkout -b feat/nombre-de-la-feature

# Cambios + commit semántico
git commit -m "feat: descripción breve del cambio"

# Push y Pull Request
git push origin feat/nombre-de-la-feature
```

Por favor abre un issue antes de enviar PRs grandes para alinear el enfoque.

---



Hecho con Rust, Kotlin y cero tolerancia a los anuncios.


