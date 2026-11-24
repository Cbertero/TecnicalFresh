import java.io.FileInputStream
import java.util.Properties

// Datos de la firma del APK (keystore.properties en la raíz del proyecto)
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) load(FileInputStream(keystorePropsFile))
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "cl.duoc.tacticaldev"
    compileSdk = 35

    defaultConfig {
        applicationId = "cl.duoc.tacticaldev"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // Firma del APK release: solo se configura si existe keystore.properties
    signingConfigs {
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Activa la medición de cobertura de las pruebas unitarias (JaCoCo)
            enableUnitTestCoverage = true
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        // Las clases de Android (Log, Context, etc.) devuelven valores por defecto en vez de fallar
        unitTests.isReturnDefaultValues = true
        // Robolectric necesita los recursos (layouts, drawables, manifest) para las pruebas de UI
        unitTests.isIncludeAndroidResources = true
        // Kotest se ejecuta sobre JUnit Platform (JUnit 5)
        unitTests.all {
            it.useJUnitPlatform()
            // MockK carga un agente de Java para simular clases final/objects:
            // lo autorizamos explícitamente para que Java 21 no muestre el WARNING
            it.jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
            // No usamos extensiones @AutoScan de Kotest: evita el aviso y acelera el inicio
            it.systemProperty("kotest.framework.classpath.scanning.autoscan.disable", "true")
        }
    }
    testCoverage {
        jacocoVersion = "0.8.12"
    }
}

// Las pruebas unitarias se ejecutan solo en "debug": ahí está la actividad vacía
// (ui-test-manifest) que necesitan las pruebas de pantallas Compose.
androidComponents {
    beforeVariants(selector().withBuildType("release")) { variante ->
        variante.enableUnitTest = false
    }
}

dependencies {
    // Núcleo de Android y Jetpack Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.material.icons.extended)

    // Navegación y ViewModel en Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Persistencia local con Room (SQLite)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager: envía las visitas guardadas sin internet cuando vuelve la conexión
    implementation(libs.androidx.work.runtime.ktx)

    // DataStore Preferences: sesión del usuario (Guía 12)
    implementation(libs.androidx.datastore.preferences)

    // Coil: carga de la foto de evidencia en Compose
    implementation(libs.coil.compose)

    // Ubicación GPS (Fused Location Provider)
    implementation(libs.play.services.location)

    // Escáner de códigos QR (Google code scanner, no requiere permiso de cámara propio)
    implementation(libs.play.services.code.scanner)

    // Pruebas unitarias: Kotest (framework y aserciones) + MockK (mocks)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    // Pruebas con Android simulado en la JVM (Robolectric): Room, WorkManager, DataStore y pantallas Compose.
    // Se escriben con JUnit 4 y corren junto a Kotest gracias al motor "vintage" de JUnit 5.
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.test.ext.junit.ktx)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.androidx.work.testing)
    testRuntimeOnly(libs.junit.vintage.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    // Registra la actividad vacía que usa createComposeRule()
    debugImplementation(libs.androidx.ui.test.manifest)

    // Herramientas de vista previa
    debugImplementation(libs.androidx.ui.tooling)

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
}

// ---------------------------------------------------------------------------
// Reporte de cobertura: ./gradlew jacocoTestReport
// Resultado en app/build/reports/jacoco/jacocoTestReport/html/index.html
// ---------------------------------------------------------------------------
jacoco {
    toolVersion = "0.8.12"
}

// Solo se excluye código generado: las pantallas Compose, Room, WorkManager,
// DataStore, GPS y cámara se prueban en la JVM con Robolectric.
val exclusionesCobertura = listOf(
    "**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*",
    // Lambdas que genera el compilador de Compose (no son código escrito por nosotros)
    "**/ComposableSingletons*"
)

// Robolectric carga las clases con su propio ClassLoader: sin esto JaCoCo no las mide
tasks.withType<Test>().configureEach {
    // Robolectric carga un Android completo por prueba: con poca memoria la JVM se cae
    // (OutOfMemoryError) y las pruebas que faltaban no se miden. Se da más memoria y se
    // reinicia la JVM cada cierto número de clases de prueba para liberarla.
    maxHeapSize = "4g"
    forkEvery = 40
    // "Run with Coverage" de Android Studio usa el medidor de IntelliJ, que reescribe también
    // las clases de Android que carga Robolectric y la JVM las rechaza ("VerifyError: Bad
    // return type"). Sin la verificación de bytecode las pruebas corren y se miden igual.
    jvmArgs("-noverify")
    extensions.configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        // Solo el código de la app: si se instrumentan las clases de Android que carga
        // Robolectric, la JVM las rechaza con "VerifyError: Bad return type"
        includes = listOf("cl.duoc.tacticaldev.*")
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Ejecuta las pruebas unitarias y genera el reporte de cobertura."
    dependsOn("testDebugUnitTest")

    reports {
        html.required.set(true)
        xml.required.set(true)
    }

    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
            exclude(exclusionesCobertura)
        }
    )
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/*.exec",
                "jacoco/testDebugUnitTest.exec"
            )
        }
    )
}
