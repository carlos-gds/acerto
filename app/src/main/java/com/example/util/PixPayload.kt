package com.example.util

import java.text.Normalizer
import java.util.Locale

/**
 * Gera o "PIX copia e cola" (BR Code estático, padrão EMV do Banco Central)
 * a partir da chave, do nome e da cidade de quem recebe, com valor opcional.
 * Não depende de biblioteca nem de internet.
 */
object PixPayload {

    /** Retorna o código copia e cola, ou null se faltar chave, nome ou cidade. */
    fun build(key: String, name: String, city: String, amount: Double? = null, txid: String = "***"): String? {
        val k = normalizeKey(key) ?: return null
        val n = sanitize(name, 25).ifBlank { return null }
        val c = sanitize(city, 15).ifBlank { return null }

        val account = field("00", "br.gov.bcb.pix") + field("01", k)
        val sb = StringBuilder()
            .append(field("00", "01"))
            .append(field("26", account))
            .append(field("52", "0000"))
            .append(field("53", "986"))
        if (amount != null && amount > 0.0) {
            sb.append(field("54", String.format(Locale.US, "%.2f", amount)))
        }
        sb.append(field("58", "BR"))
            .append(field("59", n))
            .append(field("60", c))
            .append(field("62", field("05", txid.take(25).ifBlank { "***" })))
            .append("6304")
        return sb.append(crc16(sb.toString())).toString()
    }

    /**
     * Deixa a chave no formato que o PIX espera: e-mail e chave aleatória em
     * minúsculas, CPF/CNPJ só com dígitos, celular com +55.
     */
    fun normalizeKey(raw: String): String? {
        val k = raw.trim()
        if (k.isEmpty()) return null
        if ("@" in k) return k.lowercase(Locale.ROOT)
        if (Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$").matches(k)) {
            return k.lowercase(Locale.ROOT)
        }
        val digits = k.filter { it.isDigit() }
        if (k.startsWith("+")) return "+$digits"
        return when (digits.length) {
            14 -> digits // CNPJ
            11 -> if (isValidCpf(digits) && !looksLikeMobile(k)) digits else "+55$digits"
            10 -> "+55$digits" // fixo com DDD
            12, 13 -> if (digits.startsWith("55")) "+$digits" else "+55$digits"
            else -> k
        }
    }

    private fun looksLikeMobile(original: String) = original.contains('(') || original.contains(' ') && original.contains('-')

    fun isValidCpf(cpf: String): Boolean {
        if (cpf.length != 11 || cpf.all { it == cpf[0] }) return false
        val d = cpf.map { it - '0' }
        val v1 = (0 until 9).sumOf { d[it] * (10 - it) }.let { (it * 10 % 11) % 10 }
        val v2 = (0 until 10).sumOf { d[it] * (11 - it) }.let { (it * 10 % 11) % 10 }
        return v1 == d[9] && v2 == d[10]
    }

    /** Sem acento, em maiúsculas, só letras, números e espaço, no tamanho máximo do campo. */
    fun sanitize(text: String, max: Int): String {
        val noAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
        return noAccents.uppercase(Locale.ROOT)
            .replace(Regex("[^A-Z0-9 ]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(max)
            .trim()
    }

    private fun field(id: String, value: String): String = id + value.length.toString().padStart(2, '0') + value

    /** CRC16-CCITT (polinômio 0x1021, valor inicial 0xFFFF), em hexadecimal maiúsculo. */
    fun crc16(payload: String): String {
        var crc = 0xFFFF
        for (b in payload.toByteArray(Charsets.UTF_8)) {
            crc = crc xor ((b.toInt() and 0xFF) shl 8)
            repeat(8) {
                crc = if (crc and 0x8000 != 0) (crc shl 1) xor 0x1021 else crc shl 1
                crc = crc and 0xFFFF
            }
        }
        return crc.toString(16).uppercase(Locale.ROOT).padStart(4, '0')
    }
}
