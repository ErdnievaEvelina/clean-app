package com.example.myapplication.data.model.transaction
enum class TransactionType {
    TASK_COMPLETION,
    PRIVILEGE_BOUGHT,
    BALANCE_RESET
}
data class TransactionResponseDTO(
    val id: String,
    val householdId: String,
    val userId: String,
    val amount: Int,
    val type: TransactionType,
    val createdAt: String,
    val taskId: String?,
    val privilegeId: String?
){
    val formattedAmount: String
        get() = if (amount > 0) "+$amount" else amount.toString()

    val formattedDate: String
        get() = formatDate(createdAt)

    val title: String
        get() = when (type) {
            TransactionType.TASK_COMPLETION -> "Выполнение задачи"
            TransactionType.PRIVILEGE_BOUGHT -> "Покупка привилегии"
            TransactionType.BALANCE_RESET -> "Сброс баланса"
        }

    val description: String
        get() = when (type) {
            TransactionType.TASK_COMPLETION -> "Начисление за выполнение задачи"
            TransactionType.PRIVILEGE_BOUGHT -> "Списание за покупку привилегии"
            TransactionType.BALANCE_RESET -> "Баланс обнулен"
        }

    private fun formatDate(dateString: String): String {
        return try {
            val dateTime = dateString.split("T")
            val date = dateTime[0].split("-")
            val time = dateTime[1].substring(0, 5)
            "${date[2]}.${date[1]}.${date[0]} $time"
        } catch (e: Exception) {
            dateString
        }
    }
}