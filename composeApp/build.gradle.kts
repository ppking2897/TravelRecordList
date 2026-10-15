import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

buildkonfig {
    packageName = "io.github.ppking2897.travelrecord.config"

    defaultConfigs {
        buildConfigField(
            STRING,
            "GOOGLE_PLACES_API_KEY",
            localProperties.getProperty("GOOGLE_PLACES_API_KEY", "")
        )
    }
}

kotlin {
    androidLibrary {
        namespace = "io.github.ppking2897.travelrecord.composeapp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources.enable = true

        // AGP 9 的 KMP library 預設不建 Android 端測試，要明確開啟才有 androidHostTest 與 JVM 測試任務
        withHostTest {
            isReturnDefaultValues = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation("androidx.datastore:datastore-preferences:1.1.1")
            implementation(libs.ktor.client.okhttp)
            // Android Studio @Preview 渲染需要的 runtime（ComposeViewAdapter 等）
            implementation("org.jetbrains.compose.ui:ui-tooling:1.10.3")
        }
        commonMain.dependencies {
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.10.3")
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.peekaboo.ui)
            implementation(libs.peekaboo.image.picker)
            implementation(libs.coil.compose)
            implementation(libs.zoomimage.compose.coil3)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotest.property)
        }
        getByName("androidHostTest").dependencies {
            // Kotest 在 JVM 上靠 JUnit Platform 執行，只有 Android 端需要
            implementation(libs.kotest.runner.junit5)
        }
    }
}

// peekaboo-ui 0.5.2（已無新版）帶進 CameraX 1.3.2，其原生程式庫未對齊 16 KB 分頁；在此統一拉高 CameraX 版本
dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    constraints {
        listOf("camera-core", "camera-camera2", "camera-lifecycle", "camera-video", "camera-view").forEach { artifact ->
            add("androidMainImplementation", "androidx.camera:$artifact:${libs.versions.camerax.get()}")
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

// JVM 測試用的 SQLite 原生程式庫：Android 版只附手機用的 .so，host test 改載入 sqlite-bundled-jvm 內對應作業系統的檔案
val sqliteHostNative by configurations.creating { isTransitive = false }
dependencies { sqliteHostNative("androidx.sqlite:sqlite-bundled-jvm:${libs.versions.sqlite.get()}") }
val sqliteNativeDir = layout.buildDirectory.dir("sqlite-host-native")
val sqliteNative: Pair<String, String> = run {
    val os = System.getProperty("os.name").lowercase()
    val arm = System.getProperty("os.arch").lowercase().let { it.contains("aarch64") || it.contains("arm") }
    when {
        os.contains("win") -> "natives/windows_x64/sqliteJni.dll" to "sqliteJni.dll"
        os.contains("mac") -> "natives/osx_${if (arm) "arm64" else "x64"}/libsqliteJni.dylib" to "libsqliteJni.dylib"
        else -> "natives/linux_${if (arm) "arm64" else "x64"}/libsqliteJni.so" to "libsqliteJni.so"
    }
}
val extractSqliteHostNative by tasks.registering(Copy::class) {
    from({ zipTree(sqliteHostNative.singleFile) }) {
        include(sqliteNative.first)
        eachFile { path = name }
        includeEmptyDirs = false
    }
    into(sqliteNativeDir)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    dependsOn(extractSqliteHostNative)
    systemProperty("androidx.sqlite.driver.bundled.path", sqliteNativeDir.get().asFile.absolutePath)
    systemProperty("androidx.sqlite.driver.bundled.name", sqliteNative.second)
}
