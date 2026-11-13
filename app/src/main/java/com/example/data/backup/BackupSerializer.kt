package com.example.data.backup

import org.json.JSONArray
import org.json.JSONObject

/**
 * Serializes and deserializes [BackupPayload] to and from structured JSON.
 * Operates without external dependencies using Android's native JSON library.
 */
object BackupSerializer {

    fun serialize(payload: BackupPayload): String {
        val root = JSONObject()
        root.put("backupVersion", payload.backupVersion)
        root.put("appName", payload.appName)
        root.put("createdAt", payload.createdAt)

        val peopleArray = JSONArray()
        for (person in payload.people) {
            val pObj = JSONObject().apply {
                put("id", person.id)
                put("name", person.name)
                put("phone", person.phone)
                put("colorHex", person.colorHex)
                put("createdAt", person.createdAt)
                put("avatar", person.avatar)
            }
            peopleArray.put(pObj)
        }
        root.put("people", peopleArray)

        val expensesArray = JSONArray()
        for (expense in payload.expenses) {
            val eObj = JSONObject().apply {
                put("id", expense.id)
                put("title", expense.title)
                put("totalAmount", expense.totalAmount)
                put("type", expense.type)
                put("totalInstallments", expense.totalInstallments)
                put("isRecurring", expense.isRecurring)
                put("startYear", expense.startYear)
                put("startMonth", expense.startMonth)
                put("personId", expense.personId)
                put("myShareAmount", expense.myShareAmount)
                put("otherShareAmount", expense.otherShareAmount)
                put("notes", expense.notes)
                put("createdAt", expense.createdAt)
            }
            expensesArray.put(eObj)
        }
        root.put("expenses", expensesArray)

        val installmentsArray = JSONArray()
        for (inst in payload.installments) {
            val iObj = JSONObject().apply {
                put("id", inst.id)
                put("expenseId", inst.expenseId)
                put("personId", inst.personId)
                put("personName", inst.personName)
                put("expenseTitle", inst.expenseTitle)
                put("expenseType", inst.expenseType)
                put("installmentNumber", inst.installmentNumber)
                put("totalInstallments", inst.totalInstallments)
                put("year", inst.year)
                put("month", inst.month)
                put("amount", inst.amount)
                put("isPaid", inst.isPaid)
                if (inst.paidAt != null) {
                    put("paidAt", inst.paidAt)
                } else {
                    put("paidAt", JSONObject.NULL)
                }
                put("isMyShare", inst.isMyShare)
            }
            installmentsArray.put(iObj)
        }
        root.put("installments", installmentsArray)

        return root.toString(2)
    }

    fun deserialize(jsonString: String): BackupPayload {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) {
            throw IllegalArgumentException("O arquivo de backup está vazio.")
        }

        val root = try {
            JSONObject(trimmed)
        } catch (e: Exception) {
            throw IllegalArgumentException("Formato JSON inválido no arquivo de backup: ${e.message}")
        }

        if (!root.has("backupVersion")) {
            throw IllegalArgumentException("Campo obrigatório 'backupVersion' ausente no arquivo.")
        }

        val version = root.getInt("backupVersion")
        val appName = root.optString("appName", "Acerto")
        val createdAt = root.optLong("createdAt", System.currentTimeMillis())

        val people = mutableListOf<PersonBackupDto>()
        val peopleArray = root.optJSONArray("people") ?: JSONArray()
        for (i in 0 until peopleArray.length()) {
            val pObj = peopleArray.getJSONObject(i)
            people.add(
                PersonBackupDto(
                    id = pObj.getLong("id"),
                    name = pObj.getString("name"),
                    phone = pObj.optString("phone", ""),
                    colorHex = pObj.optString("colorHex", "#10B981"),
                    createdAt = pObj.optLong("createdAt", System.currentTimeMillis()),
                    // Backups antigos (sem avatar) ou valores desconhecidos -> NEUTRAL
                    avatar = com.example.data.model.PersonAvatar.from(pObj.optString("avatar", "")).name
                )
            )
        }

        val expenses = mutableListOf<ExpenseBackupDto>()
        val expensesArray = root.optJSONArray("expenses") ?: JSONArray()
        for (i in 0 until expensesArray.length()) {
            val eObj = expensesArray.getJSONObject(i)
            expenses.add(
                ExpenseBackupDto(
                    id = eObj.getLong("id"),
                    title = eObj.getString("title"),
                    totalAmount = eObj.getDouble("totalAmount"),
                    type = eObj.getString("type"),
                    totalInstallments = eObj.optInt("totalInstallments", 1),
                    isRecurring = eObj.optBoolean("isRecurring", false),
                    startYear = eObj.getInt("startYear"),
                    startMonth = eObj.getInt("startMonth"),
                    personId = eObj.getLong("personId"),
                    myShareAmount = eObj.optDouble("myShareAmount", 0.0),
                    otherShareAmount = eObj.optDouble("otherShareAmount", 0.0),
                    notes = eObj.optString("notes", ""),
                    createdAt = eObj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        val installments = mutableListOf<InstallmentBackupDto>()
        val installmentsArray = root.optJSONArray("installments") ?: JSONArray()
        for (i in 0 until installmentsArray.length()) {
            val iObj = installmentsArray.getJSONObject(i)
            val paidAt = if (iObj.isNull("paidAt")) null else iObj.optLong("paidAt")
            installments.add(
                InstallmentBackupDto(
                    id = iObj.getLong("id"),
                    expenseId = iObj.getLong("expenseId"),
                    personId = iObj.getLong("personId"),
                    personName = iObj.getString("personName"),
                    expenseTitle = iObj.getString("expenseTitle"),
                    expenseType = iObj.getString("expenseType"),
                    installmentNumber = iObj.getInt("installmentNumber"),
                    totalInstallments = iObj.getInt("totalInstallments"),
                    year = iObj.getInt("year"),
                    month = iObj.getInt("month"),
                    amount = iObj.getDouble("amount"),
                    isPaid = iObj.getBoolean("isPaid"),
                    paidAt = paidAt,
                    isMyShare = iObj.optBoolean("isMyShare", false)
                )
            )
        }

        return BackupPayload(
            backupVersion = version,
            appName = appName,
            createdAt = createdAt,
            people = people,
            expenses = expenses,
            installments = installments
        )
    }
}
