package com.example.util

import java.text.Normalizer
import java.util.Locale

/**
 * Gera o "PIX copia e cola" (BR Code estático, padrão EMV do Banco Central)
 * a partir da chave, do nome e da cidade de quem recebe, com valor opcional.
 * Não depende de biblioteca nem de internet.
 */
object PixPayload {

    /** Tipos de chave PIX. */
    enum class KeyType(val label: String) {
        CPF("CPF"), CNPJ("CNPJ"), PHONE("Celular"), EMAIL("E-mail"), RANDOM("Chave aleatória"), OTHER("Chave Pix")
    }

    private val UUID_RE = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    /**
     * Descobre o tipo da chave pelo formato. 11 dígitos podem ser CPF ou celular:
     * vale CPF quando os dígitos verificadores batem; celular quando o número tem
     * cara de celular (DDD + 9) e não é um CPF válido, ou foi digitado como telefone.
     */
    fun detect(raw: String): KeyType? {
        val k = raw.trim()
        if (k.isEmpty()) return null
        if ("@" in k) return KeyType.EMAIL
        if (UUID_RE.matches(k)) return KeyType.RANDOM
        if (k.startsWith("+")) return KeyType.PHONE
        if (!k.all { it.isDigit() || it in ".-/() " }) return KeyType.OTHER
        val digits = k.filter { it.isDigit() }
        return when (digits.length) {
            14 -> KeyType.CNPJ
            11 -> when {
                looksLikePhone(k) -> KeyType.PHONE
                isValidCpf(digits) -> KeyType.CPF
                digits[2] == '9' -> KeyType.PHONE
                else -> KeyType.OTHER
            }
            10 -> KeyType.PHONE
            12, 13 -> if (digits.startsWith("55")) KeyType.PHONE else KeyType.OTHER
            else -> KeyType.OTHER
        }
    }

    /** Chave pronta para a pessoa copiar e colar no banco (sem pontos; celular com DDD). */
    fun displayKey(raw: String): String {
        val k = raw.trim()
        val digits = k.filter { it.isDigit() }
        return when (detect(k)) {
            KeyType.CPF, KeyType.CNPJ -> digits
            KeyType.PHONE -> if ((digits.length == 12 || digits.length == 13) && digits.startsWith("55")) digits.drop(2) else digits
            KeyType.EMAIL, KeyType.RANDOM -> k.lowercase(Locale.ROOT)
            else -> k
        }
    }

    /**
     * Retorna o código copia e cola, ou null se faltar chave ou nome. A cidade é
     * obrigatória no padrão, mas os bancos não a conferem: sem cidade, vai "BRASIL".
     */
    fun build(key: String, name: String, city: String = "", amount: Double? = null, txid: String = "***"): String? {
        val k = normalizeKey(key) ?: return null
        val n = sanitize(name, 25).ifBlank { return null }
        val c = sanitize(city, 15).ifBlank { "BRASIL" }

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
        val digits = k.filter { it.isDigit() }
        return when (detect(k)) {
            KeyType.EMAIL, KeyType.RANDOM -> k.lowercase(Locale.ROOT)
            KeyType.CPF, KeyType.CNPJ -> digits
            KeyType.PHONE -> if (k.startsWith("+")) "+$digits"
                else if ((digits.length == 12 || digits.length == 13) && digits.startsWith("55")) "+$digits"
                else "+55$digits"
            else -> k
        }
    }

    /** Digitado como telefone: "(11) 98765-4321" ou "11 98765-4321". */
    private fun looksLikePhone(original: String) = original.contains('(') || (original.contains(' ') && original.contains('-'))

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
