package com.example.myapplication.data.model

enum class ActivityType {
    HOUSEHOLD_CREATED,  // создано хозяйство

    USER_JOINED,        // участник присоединился
    USER_LEFT,          // участник вышел
    USER_REMOVED,       // участника удалили

    TASK_CREATED,       // задача создана
    TASK_ASSIGNED,      // задачу забронировали
    TASK_UNASSIGNED,    // задачу освободили
    TASK_COMPLETED,     // задачу выполнили

    PRIVILEGE_CREATED,  // привилегия создана
    PRIVILEGE_BOUGHT ;   // привилегию купили
    val title: String
        get() = when (this) {
            HOUSEHOLD_CREATED -> "Создание хозяйства"
            USER_JOINED -> "Присоединение"
            USER_LEFT -> "Выход"
            USER_REMOVED -> "Удаление"
            TASK_CREATED -> "Создание задачи"
            TASK_ASSIGNED -> "Назначение"
            TASK_UNASSIGNED -> "Освобождение"
            TASK_COMPLETED -> "Выполнение"
            PRIVILEGE_CREATED -> "Создание привилегии"
            PRIVILEGE_BOUGHT -> "Покупка"
        }
}

// ActivityActorScope.kt
enum class ActivityActorScope {
    ALL,  // вся активность хозяйства
    MY    // моя активность в хозяйстве
}
data class ActivityResponseDTO(
    val id: String,
    val householdId: String,
    val userId: String,
    val userName: String?,           // Имя пользователя (если есть)
    val activityType: ActivityType,
    val targetId: String?,           // ID связанной сущности (задачи/привилегии)
    val targetName: String?,         // Название связанной сущности
    val createdAt: String
) {
    val formattedDate: String
        get() = formatDate(createdAt)

    val title: String
        get() = when (activityType) {
            ActivityType.HOUSEHOLD_CREATED -> "Создание хозяйства"
            ActivityType.USER_JOINED -> "Присоединение участника"
            ActivityType.USER_LEFT -> "Выход участника"
            ActivityType.USER_REMOVED -> "Удаление участника"
            ActivityType.TASK_CREATED -> "Создание задачи"
            ActivityType.TASK_ASSIGNED -> "Назначение задачи"
            ActivityType.TASK_UNASSIGNED -> "Освобождение задачи"
            ActivityType.TASK_COMPLETED -> "Выполнение задачи"
            ActivityType.PRIVILEGE_CREATED -> "Создание привилегии"
            ActivityType.PRIVILEGE_BOUGHT -> "Покупка привилегии"
        }
    val description: String
        get() = when (activityType) {
            ActivityType.HOUSEHOLD_CREATED -> "Хозяйство было создано"
            ActivityType.USER_JOINED -> "${userName ?: "Пользователь"} присоединился к хозяйству"
            ActivityType.USER_LEFT -> "${userName ?: "Пользователь"} покинул хозяйство"
            ActivityType.USER_REMOVED -> "${userName ?: "Пользователь"} был удален из хозяйства"
            ActivityType.TASK_CREATED -> {
                val name = targetName?.takeIf { it.isNotBlank() } ?: "новая задача"
                "Создана задача: $name"
            }
            ActivityType.TASK_ASSIGNED -> {
                val name = targetName?.takeIf { it.isNotBlank() } ?: "задача"
                "Задача \"$name\" назначена"
            }
            ActivityType.TASK_UNASSIGNED -> {
                val name = targetName?.takeIf { it.isNotBlank() } ?: "задача"
                "Задача \"$name\" освобождена"
            }
            ActivityType.TASK_COMPLETED -> {
                val name = targetName?.takeIf { it.isNotBlank() } ?: "задача"
                "Задача \"$name\" выполнена"
            }
            ActivityType.PRIVILEGE_CREATED -> {
                val name = targetName?.takeIf { it.isNotBlank() } ?: "новая привилегия"
                "Создана привилегия: $name"
            }
            ActivityType.PRIVILEGE_BOUGHT -> {
                val name = targetName?.takeIf { it.isNotBlank() } ?: "привилегия"
                "Куплена привилегия: $name"
            }
        }
    val icon: String
        get() = when (activityType) {
            ActivityType.HOUSEHOLD_CREATED -> "🏠"
            ActivityType.USER_JOINED -> "👤"
            ActivityType.USER_LEFT -> "🚪"
            ActivityType.USER_REMOVED -> "❌"
            ActivityType.TASK_CREATED -> "📝"
            ActivityType.TASK_ASSIGNED -> "📌"
            ActivityType.TASK_UNASSIGNED -> "🔓"
            ActivityType.TASK_COMPLETED -> "✅"
            ActivityType.PRIVILEGE_CREATED -> "🎁"
            ActivityType.PRIVILEGE_BOUGHT -> "💰"
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