package com.example.myapplication.domain.usecase.activity
import com.example.myapplication.data.model.ActivityActorScope
import com.example.myapplication.data.model.ActivityResponseDTO
import com.example.myapplication.data.model.ActivityType
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.repository.ActivityRepository

class GetHouseholdActivityUseCase(
    private val repository: ActivityRepository
) {
    suspend operator fun invoke(
        householdId: String,
        activityType: ActivityType? = null,
        actorScope: ActivityActorScope = ActivityActorScope.ALL
    ): Result<List<ActivityResponseDTO>> {
        return repository.getHouseholdActivity(householdId, activityType, actorScope)
    }
}