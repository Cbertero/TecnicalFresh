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

// Se excluye lo que NO se prueba con pruebas unitarias en la JVM:
// pantallas Compose (se prueban con pruebas instrumentadas), clases generadas
// y envoltorios del framework Android (GPS, cámara, DataStore, Room, Activity).
val exclusionesCobertura = listOf(
    "**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*",
    "**/ui/**",
    "**/MainActivity*",
    "**/ComposableSingletons*",
    "**/TacticalApp*",
    "**/di/**",
    "**/navigation/AppNavigation*",
    "**/navigation/ItemNavegacion*",
    "**/navigation/ItemsNavegacion*",
    "**/data/local/AppDatabase*",
    "**/data/local/dao/**",
    "**/data/location/**",
    "**/data/scanner/**",
    "**/data/preferences/**",
    "**/data/remote/ApiService*",
    "**/utils/FotoUtils*",
    "**/viewmodel/AppViewModelProvider*"
)

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
