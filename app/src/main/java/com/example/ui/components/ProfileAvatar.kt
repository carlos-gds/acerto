package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.components.glass.pressScale
import com.example.ui.theme.AcertoPalette
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Foto de perfil guardada só no aparelho (pasta interna do app, fora do backup JSON). */
object ProfilePhotoStore {
    private const val MAX_SIDE = 512

    private fun file(context: Context) = File(context.filesDir, "profile_photo.jpg")

    fun load(context: Context): ImageBitmap? {
        val f = file(context)
        if (!f.exists()) return null
        return runCatching { BitmapFactory.decodeFile(f.absolutePath)?.asImageBitmap() }.getOrNull()
    }

    /** Lê a imagem escolhida, corrige a rotação, recorta em quadrado e reduz para 512 px. */
    fun save(context: Context, uri: Uri): Boolean = runCatching {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (minOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return false
        val rotation = resolver.openInputStream(uri)?.use {
            when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        val side = minOf(decoded.width, decoded.height)
        val scale = minOf(1f, MAX_SIDE.toFloat() / side)
        val matrix = Matrix().apply { postRotate(rotation); postScale(scale, scale) }
        val square = Bitmap.createBitmap(
            decoded, (decoded.width - side) / 2, (decoded.height - side) / 2, side, side, matrix, true
        )
        file(context).outputStream().use { square.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        true
    }.getOrDefault(false)

    fun delete(context: Context) {
        file(context).delete()
    }
}

/**
 * Círculo de perfil do cabeçalho: mostra a foto da pessoa ou um ícone.
 * Sem foto, tocar abre a galeria; com foto, abre o menu Trocar / Remover.
 */
@Composable
fun ProfileAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    interactive: Boolean = true,
    /** Se informado, o toque faz isto (ex.: abrir os Ajustes) em vez de abrir a galeria. */
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var photo by remember { mutableStateOf<ImageBitmap?>(null) }
    var version by remember { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }
    LaunchedEffect(version) {
        photo = withContext(Dispatchers.IO) { ProfilePhotoStore.load(context) }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            withContext(Dispatchers.IO) { ProfilePhotoStore.save(context, uri) }
            version++
        }
    }
    val pick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    val interaction = remember { MutableInteractionSource() }

    Box(modifier) {
        Box(
            Modifier
                .size(size)
                .pressScale(interaction, pressedScale = 0.92f)
                .clip(CircleShape)
                .background(AcertoPalette.PillDark)
                .border(1.5.dp, AcertoPalette.TextOnHeader.copy(alpha = 0.18f), CircleShape)
                .then(
                    if (!interactive) Modifier
                    else Modifier
                        .clickable(interactionSource = interaction, indication = null, role = Role.Button) {
                            when {
                                onClick != null -> onClick()
                                photo == null -> pick()
                                else -> menuOpen = true
                            }
                        }
                        .semantics { contentDescription = if (onClick != null) "Perfil e ajustes" else if (photo == null) "Adicionar foto de perfil" else "Foto de perfil" }
                        .testTag("profile_avatar")
                ),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = photo, label = "profile_photo") { img ->
                if (img != null) {
                    Image(img, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = AcertoPalette.TextOnHeader,
                            modifier = Modifier.size(size * 0.55f)
                        )
                    }
                }
            }
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text("Trocar foto") },
                leadingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                onClick = { menuOpen = false; pick() }
            )
            DropdownMenuItem(
                text = { Text("Remover foto", color = Color(0xFFDC2626)) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626)) },
                onClick = {
                    menuOpen = false
                    scope.launch {
                        withContext(Dispatchers.IO) { ProfilePhotoStore.delete(context) }
                        version++
                    }
                }
            )
        }
    }
}
