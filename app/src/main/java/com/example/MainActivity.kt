package com.example

import android.graphics.Color as AndroidColor
import android.os.Build
import android.app.KeyguardManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import android.os.SystemClock
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.screens.LockScreen
import com.example.util.AppLock
import com.example.util.MonthlyReminder
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.glass.rememberReducedMotion
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.QuemMeDeveTheme
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  /** App bloqueado: mostra só a tela de desbloqueio, nunca o conteúdo. */
  private var locked by mutableStateOf(false)
  private var backgroundAt = 0L

  // Android 7–9: tela de senha do próprio sistema
  private val credentialLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == RESULT_OK) locked = false
    }

  private fun lockWanted() = AppLock.isEnabled(this) && AppLock.isAvailable(this)

  override fun onStop() {
    super.onStop()
    backgroundAt = SystemClock.elapsedRealtime()
  }

  override fun onStart() {
    super.onStart()
    if (backgroundAt > 0 && lockWanted() &&
      SystemClock.elapsedRealtime() - backgroundAt > AppLock.RELOCK_AFTER_MS
    ) {
      locked = true
    }
  }

  private fun unlock() {
    if (!lockWanted()) { locked = false; return }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val builder = BiometricPrompt.Builder(this)
        .setTitle("Desbloquear o Acerto")
        .setSubtitle("Use a digital, o rosto ou a senha do celular")
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        builder.setAllowedAuthenticators(
          BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
      } else {
        @Suppress("DEPRECATION")
        builder.setDeviceCredentialAllowed(true)
      }
      builder.build().authenticate(
        CancellationSignal(),
        mainExecutor,
        object : BiometricPrompt.AuthenticationCallback() {
          override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
            locked = false
          }
        }
      )
    } else {
      val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
      @Suppress("DEPRECATION")
      val intent = km.createConfirmDeviceCredentialIntent("Desbloquear o Acerto", "Use a senha do celular")
      if (intent != null) credentialLauncher.launch(intent) else locked = false
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    locked = lockWanted()
    // Lembrete mensal: garante o alarme agendado (ex.: depois de atualizar o app)
    if (MonthlyReminder.isEnabled(this)) MonthlyReminder.schedule(this)
    enableEdgeToEdge()

    // Android 12+: saída animada da tela de abertura do sistema
    // (o laço cresce e desvanece enquanto o fundo Obsidian some em fade).
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      splashScreen.setOnExitAnimationListener { splashView ->
        splashView.iconView?.animate()
          ?.scaleX(1.18f)?.scaleY(1.18f)?.alpha(0f)
          ?.setDuration(320L)
          ?.setInterpolator(DecelerateInterpolator())
          ?.start()
        splashView.animate()
          .alpha(0f)
          .setStartDelay(120L)
          .setDuration(300L)
          .setInterpolator(DecelerateInterpolator())
          .withEndAction { splashView.remove() }
          .start()
      }
    }

    setContent {
      val viewModel: FinanceViewModel = viewModel()
      val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

      // Barras de status e navegação acompanham o tema escolhido no app,
      // não o tema do sistema: ícones claros no modo escuro e escuros no claro.
      DisposableEffect(isDarkMode) {
        val barStyle = if (isDarkMode) {
          SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        } else {
          SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        onDispose {}
      }

      QuemMeDeveTheme(darkTheme = isDarkMode) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          if (locked) {
            LaunchedEffect(Unit) { unlock() }
            LockScreen(onUnlock = { unlock() })
          } else {
            HomeScreen(viewModel = viewModel)
          }
        }
      }

      // Android 7–11: continua a imagem de abertura da janela e faz a mesma saída animada.
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        LegacyLaunchIntro()
      }
    }
  }
}

/** Abertura para Android 7–11: mesmo fundo Obsidian e laço da janela, com saída suave. */
@Composable
private fun LegacyLaunchIntro() {
  var finished by rememberSaveable { mutableStateOf(false) }
  if (finished) return
  val reducedMotion = rememberReducedMotion()
  val progress = remember { Animatable(0f) }
  LaunchedEffect(Unit) {
    if (!reducedMotion) {
      launch { progress.animateTo(1f, tween(durationMillis = 420, delayMillis = 120, easing = FastOutSlowInEasing)) }
        .join()
    }
    finished = true
  }
  Box(
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer { alpha = 1f - progress.value }
      .background(
        Brush.verticalGradient(
          listOf(Color(0xFF1F2335), Color(0xFF090A0E), Color(0xFF0D0E17))
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(R.mipmap.ic_launcher_foreground),
      contentDescription = null,
      modifier = Modifier
        .size(208.dp)
        .graphicsLayer {
          val s = 1f + 0.18f * progress.value
          scaleX = s
          scaleY = s
        }
    )
  }
}
