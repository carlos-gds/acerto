package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.example.data.model.InstallmentItem
import java.io.File
import java.io.FileOutputStream

/**
 * Cobrança como imagem: um card com as parcelas do mês, o total pendente e a
 * chave PIX, no visual do Acerto. Vai junto com o texto (que traz a chave para copiar). Desenhado com o Canvas do Android, sem biblioteca.
 */
object ChargeImage {

    private const val W = 1080
    private const val PAD = 72f

    private val BG = 0xFF0C0D10.toInt()
    private val CARD = 0xFF15161B.toInt()
    private val LINE = 0x22EEF1F4
    private val INK = 0xFFEEF1F4.toInt()
    private val INK2 = 0xFF9BA1AB.toInt()
    private val RED = 0xFFF0354B.toInt()
    private val AMBER = 0xFFF2B13C.toInt()
    private val GREEN = 0xFF4CC985.toInt()

    private fun paint(size: Float, color: Int, bold: Boolean = false) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun fit(text: String, p: TextPaint, width: Float): String =
        TextUtils.ellipsize(text, p, width, TextUtils.TruncateAt.END).toString()

    /** Desenha o card. [items] são as parcelas da pessoa no mês. */
    fun render(
        personName: String, month: Int, year: Int, items: List<InstallmentItem>, pixKey: String?,
        pixName: String? = null, pixBank: String? = null
    ): Bitmap {
        val rowH = 128f
        val hasPix = !pixKey.isNullOrBlank()
        val pending = Money.toReal(items.filter { !it.isPaid }.sumOf { it.amount })
        val height = (PAD + 70 + 56 + 90 + 40 + items.size * rowH + 60 + 150 + (if (hasPix) 230 else 0) + 90 + PAD).toInt()

        val bmp = Bitmap.createBitmap(W, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(BG)
        var y = PAD + 56

        // Marca
        val brand = paint(64f, INK, bold = true)
        c.drawText("Acerto", PAD, y, brand)
        c.drawText(".", PAD + brand.measureText("Acerto"), y, paint(64f, RED, bold = true))
        y += 64
        c.drawText("Cobrança de ${CurrencyFormatter.getMonthName(month).lowercase()} de $year", PAD, y, paint(36f, INK2))
        y += 90
        c.drawText(fit("Para $personName", paint(50f, INK, true), W - 2 * PAD), PAD, y, paint(50f, INK, true))
        y += 40

        // Parcelas
        val cardTop = y
        val cardBottom = y + items.size * rowH + 24
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CARD }
        c.drawRoundRect(RectF(PAD - 24, cardTop, W - PAD + 24, cardBottom), 36f, 36f, fill)
        val divider = Paint().apply { color = LINE; strokeWidth = 2f }
        y += 12
        items.forEachIndexed { i, it ->
            if (i > 0) c.drawLine(PAD, y, W - PAD + 24, y, divider)
            val value = CurrencyFormatter.format(Money.toReal(it.amount))
            val vPaint = paint(40f, INK, true)
            val vW = vPaint.measureText(value)
            c.drawText(fit(it.expenseTitle, paint(40f, INK, true), W - 2 * PAD - vW - 40), PAD, y + 54, paint(40f, INK, true))
            c.drawText("Parcela ${it.installmentNumber} de ${it.totalInstallments}", PAD, y + 98, paint(32f, INK2))
            c.drawText(value, W - PAD - vW, y + 54, vPaint)
            val status = if (it.isPaid) "Pago" else "Pendente"
            val sPaint = paint(30f, if (it.isPaid) GREEN else AMBER, true)
            c.drawText(status, W - PAD - sPaint.measureText(status), y + 98, sPaint)
            y += rowH
        }
        y = cardBottom + 90

        // Total
        c.drawText(if (pending > 0) "Total pendente" else "Tudo pago neste mês", PAD, y, paint(36f, INK2))
        y += 96
        c.drawText(CurrencyFormatter.format(pending), PAD, y, paint(88f, INK, true))
        y += 40

        // PIX
        if (hasPix) {
            y += 30
            val box = RectF(PAD - 24, y, W - PAD + 24, y + 190)
            c.drawRoundRect(box, 32f, 32f, fill)
            val type = PixPayload.detect(pixKey!!)
            val label = if (type == null || type == PixPayload.KeyType.OTHER) "Chave PIX" else "PIX · ${type.label}"
            c.drawText(label, PAD, y + 56, paint(32f, INK2))
            c.drawText(fit(PixPayload.displayKey(pixKey), paint(42f, INK, true), W - 2 * PAD), PAD, y + 112, paint(42f, INK, true))
            val who = listOfNotNull(pixName?.trim()?.takeIf { it.isNotEmpty() }, pixBank?.trim()?.takeIf { it.isNotEmpty() })
            if (who.isNotEmpty()) c.drawText(fit(who.joinToString(" · "), paint(32f, INK2), W - 2 * PAD), PAD, y + 160, paint(32f, INK2))
            y += 190
        }

        // Rodapé
        y += 70
        c.drawText("Feito com o app Acerto.", PAD, y, paint(28f, INK2))
        return bmp
    }

    /** Salva a imagem no cache e abre o compartilhar com imagem + texto. */
    fun share(context: Context, bitmap: Bitmap, text: String, chooserTitle: String): Boolean {
        return try {
            val dir = File(context.cacheDir, "cobrancas").apply { mkdirs() }
            val file = File(dir, "cobranca.png")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(send, chooserTitle)
            if (context !is android.app.Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            // Sem imagem (ex.: falta de espaço): manda só o texto, como antes
            ShareIntentHelper.sharePlainText(context, text, chooserTitle)
        }
    }
}
