import java.util.Properties
import javax.inject.Inject
import org.gradle.process.ExecOperations

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
}

// Pasta com as chaves de assinatura (fora do projeto). Vem de keystore.properties
// (não vai para o Git): pasta=C:\\caminho\\da\\pasta
val chavePasta: File? = rootProject.file("keystore.properties").takeIf { it.exists() }?.let { f ->
  val p = Properties().apply { f.inputStream().use { load(it) } }
  p.getProperty("pasta")?.let { File(it) }?.takeIf { it.isDirectory }
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.dividicard.xfkql"
    minSdk = 24
    targetSdk = 36
    versionCode = 21
    versionName = "1.14"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    // Chave antiga (a de debug que assinou da 1.4 à 1.13). O Gradle assina com ela e,
    // se a pasta da chave estiver configurada, a tarefa assinarComRotacao assina de novo
    // com a antiga + a nova (rotação de chave). Prefere a cópia guardada na pasta da chave.
    create("release") {
      val guardada = chavePasta?.resolve("debug.keystore")
      storeFile = if (guardada != null && guardada.exists()) guardada else file("${System.getProperty("user.home")}/.android/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      // R8 + recursos enxutos: código otimizado e menor. Junto com o build não depurável,
      // é o que deixa o Compose fluido (os baseline profiles das bibliotecas só valem aqui).
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug { }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  // O lint da release baixa artefatos que não estão no cache offline; os testes cobrem o app.
  lint { checkReleaseBuilds = false }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Room grava o esquema de cada versão do banco em app/schemas (versionar no Git):
// serve de referência para escrever e testar as próximas migrations.
ksp { arg("room.schemaLocation", "$projectDir/schemas") }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  // implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  // implementation(libs.logging.interceptor)
  // implementation(libs.moshi.kotlin)
  // implementation(libs.play.services.location)
  // implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  // "ksp"(libs.moshi.kotlin.codegen)
}

/*
 * Rotação de chave (APK Signature Scheme v3): o APK sai assinado com a chave antiga e com
 * a nova, ligadas pelo arquivo de linhagem. Quem tem a 1.13 instalada atualiza por cima
 * normalmente; do Android 9 em diante o app passa a ser reconhecido pela chave nova.
 * Android 7 e 8 não conhecem rotação e continuam conferindo a chave antiga, então as duas
 * precisam ficar guardadas para sempre.
 */
abstract class AssinarComRotacao : DefaultTask() {
  @get:Inject abstract val execOps: ExecOperations
  @get:Internal abstract val apk: RegularFileProperty
  @get:Internal abstract val apksignerJar: RegularFileProperty
  @get:Internal abstract val pasta: DirectoryProperty

  @TaskAction
  fun assinar() {
    val apkFile = apk.get().asFile
    val dir = pasta.get().asFile
    val java = File(System.getProperty("java.home"), "bin/java").path
    val jar = apksignerJar.get().asFile.path
    val tmp = File(apkFile.parentFile, apkFile.nameWithoutExtension + "-rotacao.apk")
    fun f(nome: String) = File(dir, nome).also { require(it.exists()) { "Falta ${it.path}" } }.path
    execOps.exec {
      commandLine(
        java, "-jar", jar, "sign",
        "--ks", f("debug.keystore"), "--ks-key-alias", "androiddebugkey", "--ks-pass", "pass:android", "--key-pass", "pass:android",
        "--next-signer",
        "--ks", f("acerto-release.jks"), "--ks-key-alias", "acerto", "--ks-pass", "file:" + f("senha.txt"),
        "--lineage", f("acerto-lineage.bin"),
        "--rotation-min-sdk-version", "28",
        "--v4-signing-enabled", "false",
        "--out", tmp.path, apkFile.path
      )
    }
    execOps.exec { commandLine(java, "-jar", jar, "verify", "--min-sdk-version", "24", tmp.path) }
    apkFile.delete()
    check(tmp.renameTo(apkFile)) { "Não consegui substituir ${apkFile.path}" }
    logger.lifecycle("APK assinado com a chave antiga + a nova (rotação): ${apkFile.path}")
  }
}

if (chavePasta != null) {
  val assinar = tasks.register<AssinarComRotacao>("assinarComRotacao") {
    group = "build"
    description = "Assina a release com a chave antiga e a nova (rotação de chave)."
    apk.set(layout.buildDirectory.file("outputs/apk/release/app-release.apk"))
    val buildTools = android.buildToolsVersion
    apksignerJar.set(androidComponents.sdkComponents.sdkDirectory.map { it.file("build-tools/$buildTools/lib/apksigner.jar") })
    pasta.set(chavePasta)
    outputs.upToDateWhen { false }
  }
  tasks.configureEach { if (name == "assembleRelease") finalizedBy(assinar) }
} else {
  logger.warn("keystore.properties não encontrado: a release sai só com a chave antiga (sem rotação).")
}
