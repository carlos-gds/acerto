package com.example.data.repository

import com.example.data.db.ExpenseDao
import com.example.data.db.InstallmentDao
import com.example.data.db.PersonDao
import com.example.data.model.BillParticipantShare
import com.example.data.model.Expense
import com.example.data.model.ExpenseType
import com.example.data.model.InstallmentItem
import com.example.data.model.MY_PERSON_ID
import com.example.data.model.Person
import com.example.util.Money
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FinanceRepository(
    private val personDao: PersonDao,
    private val expenseDao: ExpenseDao,
    private val installmentDao: InstallmentDao,
    /** Banco usado para agrupar gravações em transação; null (testes) executa direto. */
    private val database: RoomDatabase? = null
) {
    /**
     * Executa [block] numa transação do Room: operações com vários passos
     * (excluir despesa e parcelas, editar compra, excluir pessoa...) gravam tudo
     * ou nada, sem deixar dados órfãos se o app fechar no meio.
     */
    private suspend fun <R> tx(block: suspend () -> R): R =
        if (database != null) database.withTransaction(block) else block()

    val allPeople: Flow<List<Person>> = personDao.getAllPeople()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getInstallmentsForMonth(year: Int, month: Int): Flow<List<InstallmentItem>> {
        return installmentDao.getInstallmentsForMonth(year, month)
    }

    fun getInstallmentsForPerson(personId: Long): Flow<List<InstallmentItem>> {
        return installmentDao.getInstallmentsForPerson(personId)
    }

    fun getAllInstallments(): Flow<List<InstallmentItem>> {
        return installmentDao.getAllInstallments()
    }

    suspend fun insertPerson(
        name: String,
        phone: String = "",
        colorHex: String = "#10B981",
        avatar: String = com.example.data.model.PersonAvatar.NEUTRAL.name
    ): Long {
        val person = Person(name = name.trim(), phone = phone.trim(), colorHex = colorHex, avatar = avatar)
        return personDao.insertPerson(person)
    }

    suspend fun getPersonByName(name: String): Person? {
        return personDao.getPersonByName(name.trim())
    }

    suspend fun findOrCreatePerson(name: String, defaultColorHex: String = "#8B5CF6"): Person {
        val cleanName = name.trim().ifEmpty { "Outros" }
        val existing = personDao.getPersonByName(cleanName)
        if (existing != null) {
            return existing
        }
        val colors = listOf("#3B82F6", "#EC4899", "#8B5CF6", "#10B981", "#F59E0B", "#6366F1", "#14B8A6")
        val color = colors[Math.abs(cleanName.hashCode()) % colors.size]
        val newPerson = Person(name = cleanName, phone = "", colorHex = color)
        val id = personDao.insertPerson(newPerson)
        return newPerson.copy(id = id)
    }

    suspend fun updatePerson(person: Person): Unit = tx {
        personDao.updatePerson(person)
        installmentDao.updatePersonName(person.id, person.name)
    }

    suspend fun deletePerson(person: Person): Unit = tx {
        installmentDao.deleteByPersonId(person.id)
        personDao.deletePerson(person)
    }

    suspend fun addCardPurchase(
        title: String,
        totalAmount: Double,
        person: Person,
        totalInstallments: Int,
        startYear: Int,
        startMonth: Int,
        notes: String = "",
        markPastInstallmentsAsPaid: Boolean = false,
        referenceYear: Int = Calendar.getInstance().get(Calendar.YEAR),
        referenceMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    ): Long = tx {
        val expense = Expense(
            title = title.trim(),
            totalAmount = Money.fromReal(totalAmount),
            type = ExpenseType.CARD_PURCHASE,
            totalInstallments = totalInstallments,
            isRecurring = false,
            startYear = startYear,
            startMonth = startMonth,
            personId = person.id,
            myShareAmount = Money.fromReal(0.0),
            otherShareAmount = Money.fromReal(totalAmount),
            notes = notes.trim()
        )
        val expenseId = expenseDao.insertExpense(expense)

        // Calculate installment amounts
        val baseInstallment = ((totalAmount / totalInstallments) * 100).toLong() / 100.0
        val remainder = Math.round((totalAmount - (baseInstallment * totalInstallments)) * 100) / 100.0

        val items = mutableListOf<InstallmentItem>()
        for (i in 1..totalInstallments) {
            val totalMonths = (startYear * 12 + (startMonth - 1)) + (i - 1)
            val instYear = totalMonths / 12
            val instMonth = (totalMonths % 12) + 1
            val installmentAmount = if (i == 1) baseInstallment + remainder else baseInstallment

            val isPast = (instYear < referenceYear) || (instYear == referenceYear && instMonth < referenceMonth)
            val isPaid = markPastInstallmentsAsPaid && isPast
            val paidAt = if (isPaid) System.currentTimeMillis() else null

            items.add(
                InstallmentItem(
                    expenseId = expenseId,
                    personId = person.id,
                    personName = person.name,
                    expenseTitle = title.trim(),
                    expenseType = ExpenseType.CARD_PURCHASE,
                    installmentNumber = i,
                    totalInstallments = totalInstallments,
                    year = instYear,
                    month = instMonth,
                    amount = Money.fromReal(installmentAmount),
                    isPaid = isPaid,
                    paidAt = paidAt,
                    isMyShare = false
                )
            )
        }
        installmentDao.insertInstallments(items)
        return@tx expenseId
    }

    suspend fun addSharedBill(
        title: String,
        totalAmount: Double,
        person: Person,
        myShareAmount: Double,
        otherShareAmount: Double,
        startYear: Int,
        startMonth: Int,
        isRecurring: Boolean,
        recurrenceMonthsCount: Int = 12,
        notes: String = ""
    ): Long = tx {
        val participants = mutableListOf<BillParticipantShare>()
        if (myShareAmount > 0.0) {
            participants.add(
                BillParticipantShare(
                    personId = MY_PERSON_ID,
                    personName = "Eu",
                    amount = myShareAmount,
                    isMyShare = true
                )
            )
        }
        if (otherShareAmount > 0.0) {
            participants.add(
                BillParticipantShare(
                    personId = person.id,
                    personName = person.name,
                    amount = otherShareAmount,
                    isMyShare = false
                )
            )
        }
        return@tx addSharedBillWithParticipants(
            title = title,
            totalAmount = totalAmount,
            participants = participants,
            startYear = startYear,
            startMonth = startMonth,
            isRecurring = isRecurring,
            recurrenceMonthsCount = recurrenceMonthsCount,
            notes = notes
        )
    }

    suspend fun addSharedBillWithParticipants(
        title: String,
        totalAmount: Double,
        participants: List<BillParticipantShare>,
        startYear: Int,
        startMonth: Int,
        isRecurring: Boolean,
        recurrenceMonthsCount: Int = 12,
        notes: String = ""
    ): Long = tx {
        val totalMonths = if (isRecurring) recurrenceMonthsCount else 1
        val firstOtherPersonId = participants.firstOrNull { !it.isMyShare }?.personId ?: MY_PERSON_ID
        val myShareAmount = participants.firstOrNull { it.isMyShare }?.amount ?: 0.0
        val otherShareAmount = participants.filter { !it.isMyShare }.sumOf { it.amount }

        val expense = Expense(
            title = title.trim(),
            totalAmount = Money.fromReal(totalAmount),
            type = ExpenseType.SHARED_BILL,
            totalInstallments = totalMonths,
            isRecurring = isRecurring,
            startYear = startYear,
            startMonth = startMonth,
            personId = firstOtherPersonId,
            myShareAmount = Money.fromReal(myShareAmount),
            otherShareAmount = Money.fromReal(otherShareAmount),
            notes = notes.trim()
        )
        val expenseId = expenseDao.insertExpense(expense)

        val items = mutableListOf<InstallmentItem>()
        for (i in 1..totalMonths) {
            val currentTotalMonths = (startYear * 12 + (startMonth - 1)) + (i - 1)
            val instYear = currentTotalMonths / 12
            val instMonth = (currentTotalMonths % 12) + 1

            for (participant in participants) {
                if (participant.amount > 0.0) {
                    items.add(
                        InstallmentItem(
                            expenseId = expenseId,
                            personId = participant.personId,
                            personName = participant.personName,
                            expenseTitle = title.trim(),
                            expenseType = ExpenseType.SHARED_BILL,
                            installmentNumber = i,
                            totalInstallments = totalMonths,
                            year = instYear,
                            month = instMonth,
                            amount = Money.fromReal(participant.amount),
                            isPaid = participant.isMyShare, // User's own share is marked as their responsibility
                            isMyShare = participant.isMyShare
                        )
                    )
                }
            }
        }
        installmentDao.insertInstallments(items)
        return@tx expenseId
    }

    suspend fun setInstallmentPaid(id: Long, isPaid: Boolean) {
        val current = installmentDao.getInstallmentById(id) ?: return
        val paidAt = if (isPaid) System.currentTimeMillis() else null
        val updated = current.copy(isPaid = isPaid, paidAt = paidAt)
        installmentDao.updateInstallment(updated)
    }

    /**
     * Pagamento parcial: a parcela é dividida em duas linhas com o mesmo número —
     * a parte paga (marcada como paga) e o restante (pendente). Totais, cobrança e
     * backup continuam certos sem mudar o banco. Retorna false se o valor não couber.
     */
    suspend fun registerPartialPayment(installmentId: Long, paidCents: Long): Boolean = tx {
        val item = installmentDao.getInstallmentById(installmentId) ?: return@tx false
        if (item.isPaid || paidCents <= 0 || paidCents >= item.amount) return@tx false
        installmentDao.updateInstallment(item.copy(amount = item.amount - paidCents))
        installmentDao.insertInstallments(
            listOf(item.copy(id = 0, amount = paidCents, isPaid = true, paidAt = System.currentTimeMillis()))
        )
        true
    }

    suspend fun getInstallmentExpenseId(installmentId: Long): Long? =
        installmentDao.getInstallmentById(installmentId)?.expenseId

    /** Foto da compra/conta e de todas as parcelas, para poder desfazer uma exclusão. */
    data class ExpenseSnapshot(val expense: Expense, val installments: List<InstallmentItem>)

    suspend fun snapshotExpense(expenseId: Long): ExpenseSnapshot? = tx {
        val expense = expenseDao.getExpenseById(expenseId) ?: return@tx null
        ExpenseSnapshot(expense, installmentDao.getInstallmentListByExpenseId(expenseId))
    }

    /** Desfaz a exclusão: volta a compra e as parcelas exatamente como estavam (mesmos ids). */
    suspend fun restoreExpense(snapshot: ExpenseSnapshot): Unit = tx {
        installmentDao.deleteByExpenseId(snapshot.expense.id)
        expenseDao.insertExpense(snapshot.expense)
        installmentDao.insertInstallments(snapshot.installments)
    }

    suspend fun deleteExpense(expenseId: Long): Unit = tx {
        installmentDao.deleteByExpenseId(expenseId)
        expenseDao.deleteExpenseById(expenseId)
    }

    suspend fun deleteSingleInstallment(installmentId: Long): Unit = tx {
        val item = installmentDao.getInstallmentById(installmentId) ?: return@tx
        val expenseId = item.expenseId
        installmentDao.deleteById(installmentId)
        val remainingCount = installmentDao.getCountByExpenseId(expenseId)
        if (remainingCount == 0) {
            expenseDao.deleteExpenseById(expenseId)
        }
    }

    suspend fun getExpenseById(expenseId: Long): Expense? {
        return expenseDao.getExpenseById(expenseId)
    }

    suspend fun updateExpenseDetails(
        expenseId: Long,
        newTitle: String,
        newTotalAmount: Double,
        newPerson: Person,
        newTotalInstallments: Int,
        startYear: Int,
        startMonth: Int,
        markPastInstallmentsAsPaid: Boolean = false
    ): Unit = tx {
        val existing = expenseDao.getExpenseById(expenseId) ?: return@tx

        if (existing.type == ExpenseType.SHARED_BILL) {
            val myShare = if (existing.myShareAmount > 0) Money.toReal(existing.myShareAmount) else (newTotalAmount / 2.0)
            val otherShare = if (newTotalAmount >= myShare) newTotalAmount - myShare else (newTotalAmount / 2.0)
            updateSharedBill(
                expenseId = expenseId,
                newTitle = newTitle,
                newTotalAmount = newTotalAmount,
                newPerson = newPerson,
                newMyShareAmount = myShare,
                newOtherShareAmount = otherShare,
                updateAllMonths = true,
                specificYear = startYear,
                specificMonth = startMonth
            )
            return@tx
        }

        val updated = existing.copy(
            title = newTitle.trim(),
            totalAmount = Money.fromReal(newTotalAmount),
            totalInstallments = newTotalInstallments,
            personId = newPerson.id,
            startYear = startYear,
            startMonth = startMonth,
            otherShareAmount = Money.fromReal(newTotalAmount)
        )
        expenseDao.updateExpense(updated)

        // Capture previous payment states if possible
        val existingItems = installmentDao.getInstallmentListByExpenseId(expenseId)
        val paidMap = existingItems.associate { it.installmentNumber to (it.isPaid to it.paidAt) }
        installmentDao.deleteByExpenseId(expenseId)

        val baseInstallment = ((newTotalAmount / newTotalInstallments) * 100).toLong() / 100.0
        val remainder = Math.round((newTotalAmount - (baseInstallment * newTotalInstallments)) * 100) / 100.0

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

        val newItems = mutableListOf<InstallmentItem>()
        for (i in 1..newTotalInstallments) {
            val totalMonths = (startYear * 12 + (startMonth - 1)) + (i - 1)
            val instYear = totalMonths / 12
            val instMonth = (totalMonths % 12) + 1
            val installmentAmount = if (i == 1) baseInstallment + remainder else baseInstallment

            val wasPaidPair = paidMap[i]
            val isPast = (instYear < currentYear) || (instYear == currentYear && instMonth < currentMonth)
            val isPaid = wasPaidPair?.first ?: (markPastInstallmentsAsPaid && isPast)
            val paidAt = wasPaidPair?.second ?: if (isPaid) System.currentTimeMillis() else null

            newItems.add(
                InstallmentItem(
                    expenseId = expenseId,
                    personId = newPerson.id,
                    personName = newPerson.name,
                    expenseTitle = newTitle.trim(),
                    expenseType = existing.type,
                    installmentNumber = i,
                    totalInstallments = newTotalInstallments,
                    year = instYear,
                    month = instMonth,
                    amount = Money.fromReal(installmentAmount),
                    isPaid = isPaid,
                    paidAt = paidAt,
                    isMyShare = false
                )
            )
        }
        installmentDao.insertInstallments(newItems)
    }

    suspend fun updateSharedBill(
        expenseId: Long,
        newTitle: String,
        newTotalAmount: Double,
        newPerson: Person,
        newMyShareAmount: Double,
        newOtherShareAmount: Double,
        updateAllMonths: Boolean,
        specificYear: Int,
        specificMonth: Int
    ): Unit = tx {
        val existing = expenseDao.getExpenseById(expenseId) ?: return@tx
        val allInstallments = installmentDao.getInstallmentListByExpenseId(expenseId)

        if (updateAllMonths) {
            val updated = existing.copy(
                title = newTitle.trim(),
                totalAmount = Money.fromReal(newTotalAmount),
                personId = newPerson.id,
                myShareAmount = Money.fromReal(newMyShareAmount),
                otherShareAmount = Money.fromReal(newOtherShareAmount)
            )
            expenseDao.updateExpense(updated)

            val toUpdate = mutableListOf<InstallmentItem>()
            val toInsert = mutableListOf<InstallmentItem>()
            val toDelete = mutableListOf<Long>()

            val byMonth = allInstallments.groupBy { "${it.year}_${it.month}" }

            for ((_, monthItems) in byMonth) {
                val myItem = monthItems.find { it.isMyShare }
                val otherItem = monthItems.find { !it.isMyShare }
                val sampleItem = monthItems.first()

                if (newMyShareAmount > 0.0) {
                    if (myItem != null) {
                        toUpdate.add(myItem.copy(expenseTitle = newTitle.trim(), amount = Money.fromReal(newMyShareAmount)))
                    } else {
                        toInsert.add(
                            InstallmentItem(
                                expenseId = expenseId,
                                personId = MY_PERSON_ID,
                                personName = "Eu",
                                expenseTitle = newTitle.trim(),
                                expenseType = ExpenseType.SHARED_BILL,
                                installmentNumber = sampleItem.installmentNumber,
                                totalInstallments = sampleItem.totalInstallments,
                                year = sampleItem.year,
                                month = sampleItem.month,
                                amount = Money.fromReal(newMyShareAmount),
                                isPaid = true,
                                isMyShare = true
                            )
                        )
                    }
                } else if (myItem != null) {
                    toDelete.add(myItem.id)
                }

                if (newOtherShareAmount > 0.0) {
                    if (otherItem != null) {
                        toUpdate.add(
                            otherItem.copy(
                                expenseTitle = newTitle.trim(),
                                personId = newPerson.id,
                                personName = newPerson.name,
                                amount = Money.fromReal(newOtherShareAmount)
                            )
                        )
                    } else {
                        toInsert.add(
                            InstallmentItem(
                                expenseId = expenseId,
                                personId = newPerson.id,
                                personName = newPerson.name,
                                expenseTitle = newTitle.trim(),
                                expenseType = ExpenseType.SHARED_BILL,
                                installmentNumber = sampleItem.installmentNumber,
                                totalInstallments = sampleItem.totalInstallments,
                                year = sampleItem.year,
                                month = sampleItem.month,
                                amount = Money.fromReal(newOtherShareAmount),
                                isPaid = false,
                                isMyShare = false
                            )
                        )
                    }
                } else if (otherItem != null) {
                    toDelete.add(otherItem.id)
                }
            }

            if (toUpdate.isNotEmpty()) {
                installmentDao.updateInstallments(toUpdate)
            }
            if (toInsert.isNotEmpty()) {
                installmentDao.insertInstallments(toInsert)
            }
            for (id in toDelete) {
                installmentDao.deleteById(id)
            }
        } else {
            val monthItems = allInstallments.filter { it.year == specificYear && it.month == specificMonth }
            val myItem = monthItems.find { it.isMyShare }
            val otherItem = monthItems.find { !it.isMyShare }
            val sample = monthItems.firstOrNull() ?: allInstallments.firstOrNull()

            val toUpdate = mutableListOf<InstallmentItem>()
            val toInsert = mutableListOf<InstallmentItem>()

            if (sample != null) {
                if (newMyShareAmount > 0.0) {
                    if (myItem != null) {
                        toUpdate.add(myItem.copy(expenseTitle = newTitle.trim(), amount = Money.fromReal(newMyShareAmount)))
                    } else {
                        toInsert.add(
                            InstallmentItem(
                                expenseId = expenseId,
                                personId = MY_PERSON_ID,
                                personName = "Eu",
                                expenseTitle = newTitle.trim(),
                                expenseType = ExpenseType.SHARED_BILL,
                                installmentNumber = sample.installmentNumber,
                                totalInstallments = sample.totalInstallments,
                                year = specificYear,
                                month = specificMonth,
                                amount = Money.fromReal(newMyShareAmount),
                                isPaid = true,
                                isMyShare = true
                            )
                        )
                    }
                } else if (myItem != null) {
                    installmentDao.deleteById(myItem.id)
                }

                if (newOtherShareAmount > 0.0) {
                    if (otherItem != null) {
                        toUpdate.add(
                            otherItem.copy(
                                expenseTitle = newTitle.trim(),
                                personId = newPerson.id,
                                personName = newPerson.name,
                                amount = Money.fromReal(newOtherShareAmount)
                            )
                        )
                    } else {
                        toInsert.add(
                            InstallmentItem(
                                expenseId = expenseId,
                                personId = newPerson.id,
                                personName = newPerson.name,
                                expenseTitle = newTitle.trim(),
                                expenseType = ExpenseType.SHARED_BILL,
                                installmentNumber = sample.installmentNumber,
                                totalInstallments = sample.totalInstallments,
                                year = specificYear,
                                month = specificMonth,
                                amount = Money.fromReal(newOtherShareAmount),
                                isPaid = false,
                                isMyShare = false
                            )
                        )
                    }
                } else if (otherItem != null) {
                    installmentDao.deleteById(otherItem.id)
                }

                if (toUpdate.isNotEmpty()) {
                    installmentDao.updateInstallments(toUpdate)
                }
                if (toInsert.isNotEmpty()) {
                    installmentDao.insertInstallments(toInsert)
                }
            }
        }
    }

    suspend fun populateInitialDataIfEmpty() {
        if (personDao.getCount() > 0) return

        val voId = insertPerson("Vó Maria", "", "#8B5CF6")
        val carlosId = insertPerson("Carlos", "", "#3B82F6")
        val anaId = insertPerson("Ana", "", "#EC4899")

        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH) + 1 // 1-12

        val voPerson = Person(id = voId, name = "Vó Maria", colorHex = "#8B5CF6")
        val carlosPerson = Person(id = carlosId, name = "Carlos", colorHex = "#3B82F6")
        val anaPerson = Person(id = anaId, name = "Ana", colorHex = "#EC4899")

        // 1. Shared Internet bill (R$ 100 split 50/50 between Eu and Vó Maria for 12 months)
        addSharedBill(
            title = "Internet Fibra",
            totalAmount = 100.0,
            person = voPerson,
            myShareAmount = 50.0,
            otherShareAmount = 50.0,
            startYear = currentYear,
            startMonth = currentMonth,
            isRecurring = true,
            recurrenceMonthsCount = 12,
            notes = "Dividido 50% para mim e 50% para minha vó"
        )

        // 2. Card purchase: Carlos bought Nike sneakers for R$ 360 in 6 installments
        addCardPurchase(
            title = "Tênis Nike",
            totalAmount = 360.0,
            person = carlosPerson,
            totalInstallments = 6,
            startYear = currentYear,
            startMonth = currentMonth,
            notes = "Comprado no cartão de crédito"
        )

        // 3. Card purchase: Ana bought Airfryer for R$ 300 in 3 installments
        addCardPurchase(
            title = "Airfryer Mondial",
            totalAmount = 300.0,
            person = anaPerson,
            totalInstallments = 3,
            startYear = currentYear,
            startMonth = currentMonth,
            notes = "Parcelado no cartão"
        )
    }
}


