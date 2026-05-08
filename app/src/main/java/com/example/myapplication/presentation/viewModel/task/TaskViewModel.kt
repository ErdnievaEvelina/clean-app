package com.example.myapplication.presentation.viewModel.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.task.TaskFilterType
import com.example.myapplication.data.model.task.TaskUiModel
import com.example.myapplication.domain.common.Result
import com.example.myapplication.domain.usecase.task.AssignTaskUseCase
import com.example.myapplication.domain.usecase.task.CompleteTaskUseCase
import com.example.myapplication.domain.usecase.task.CreateTaskUseCase
import com.example.myapplication.domain.usecase.task.DeleteTaskUseCase
import com.example.myapplication.domain.usecase.task.GetTasksUseCase
import com.example.myapplication.domain.usecase.task.UnassignTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<TaskUiModel> = emptyList(),
    val filter: TaskFilterType = TaskFilterType.ALL,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val isCreating: Boolean = false,
    val newTaskTitle: String = "",
    val newTaskDescription: String = "",
    val newTaskReward: String = ""
)
sealed class TasksAction {
    data object LoadTasks : TasksAction()
    data class FilterChanged(val filter: TaskFilterType) : TasksAction()
    data object ShowCreateDialog : TasksAction()
    data object DismissCreateDialog : TasksAction()
    data class CreateTask(
        val title: String,
        val description: String,
        val reward: Int
    ) : TasksAction()
    data class NewTitleChanged(val value: String) : TasksAction()
    data class NewDescriptionChanged(val value: String) : TasksAction()
    data class NewRewardChanged(val value: String) : TasksAction()
    data class AssignTask(val taskId: String) : TasksAction()
    data class UnassignTask(val taskId: String) : TasksAction()
    data class CompleteTask(val taskId: String) : TasksAction()
    data class DeleteTask(val taskId: String) : TasksAction()
    data object ErrorDismissed : TasksAction()
}
class TaskViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val assignTaskUseCase: AssignTaskUseCase,
    private val unassignTaskUseCase: UnassignTaskUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val householdId: String,
    private val currentUserId: String,
    private val initialFilter: TaskFilterType = TaskFilterType.ALL
): ViewModel() {
    private val _uiState = MutableStateFlow(TasksUiState(filter = initialFilter))
    val uiState = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun getCurrentUserId(): String = currentUserId
    fun handleAction(action: TasksAction) {
        when (action) {
            TasksAction.LoadTasks -> loadTasks()
            is TasksAction.FilterChanged -> {
                _uiState.update { it.copy(filter = action.filter) }
                loadTasks()  // Перезагружаем задачи с новым фильтром
            }

            TasksAction.ShowCreateDialog -> {
                _uiState.update {
                    it.copy(
                        showCreateDialog = true,
                        newTaskTitle = "",
                        newTaskDescription = "",
                        newTaskReward = ""
                    )
                }
            }

            TasksAction.DismissCreateDialog -> {
                _uiState.update { it.copy(showCreateDialog = false) }
            }

            is TasksAction.CreateTask -> createTask(action)
            is TasksAction.NewTitleChanged -> {
                _uiState.update { it.copy(newTaskTitle = action.value) }
            }

            is TasksAction.NewDescriptionChanged -> {
                _uiState.update { it.copy(newTaskDescription = action.value) }
            }

            is TasksAction.NewRewardChanged -> {
                _uiState.update { it.copy(newTaskReward = action.value) }
            }

            is TasksAction.AssignTask -> assignTask(action.taskId)
            is TasksAction.UnassignTask -> unassignTask(action.taskId)
            is TasksAction.CompleteTask -> completeTask(action.taskId)
            is TasksAction.DeleteTask -> deleteTask(action.taskId)
            TasksAction.ErrorDismissed -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }
    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when(val result = getTasksUseCase(householdId, uiState.value.filter)){
                is Result.Error -> {_uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message
                    )
                }}
                Result.Loading -> {}
                is Result.Success -> {_uiState.update {
                    it.copy(
                        isLoading = false,
                        tasks = result.data
                    )
                }}
            }

        }
    }
    private fun createTask(action: TasksAction.CreateTask){
        viewModelScope.launch {
            val reward = action.reward
            _uiState.update {
                it.copy(isCreating = true, error = null, showCreateDialog = false)
            }
            when(val result= createTaskUseCase(householdId, action.title, action.description.takeIf { it.isNotBlank() }, reward,)){
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            error = result.message
                        )
                    }
                }
                Result.Loading -> {}
                is Result.Success -> {_uiState.update { it.copy(isCreating = false) }
                    loadTasks()}
            }
        }
    }
    private fun assignTask(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when(val result = assignTaskUseCase(taskId)){
                is Result.Error -> {_uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message
                    )
                }}
                Result.Loading -> {}
                is Result.Success -> {_uiState.update { it.copy(isLoading = false) }
                    loadTasks()}
            }

        }
    }
    private fun unassignTask(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when(val result = unassignTaskUseCase(taskId)){
                is Result.Error -> {_uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message
                    )
                }}
                Result.Loading -> {}
                is Result.Success -> {_uiState.update { it.copy(isLoading = false) }
                    loadTasks()}
            }
        }
    }
    private fun completeTask(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when(val result = completeTaskUseCase(taskId)){
                is Result.Error -> {_uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message
                    )
                }}
                Result.Loading -> {}
                is Result.Success -> {_uiState.update { it.copy(isLoading = false) }
                    loadTasks()}
            }
        }
    }
    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when(val result= deleteTaskUseCase(taskId)){
                is Result.Error -> {_uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message
                    )
                }}
                Result.Loading -> {}
                is Result.Success -> {_uiState.update { it.copy(isLoading = false) }
                    loadTasks()}
            }

        }
    }
}