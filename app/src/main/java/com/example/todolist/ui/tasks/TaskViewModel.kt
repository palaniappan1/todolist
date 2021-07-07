package com.example.todolist.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.todolist.data.PreferencesManager
import com.example.todolist.data.SortOrder
import com.example.todolist.data.Task
import com.example.todolist.data.TaskDao
import com.example.todolist.ui.ADD_TASK_RESULT_OK
import com.example.todolist.ui.EDIT_TASK_RESULT_OK
import com.example.todolist.util.exhaustive
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state : SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery","")

    val preferencesFlow = preferencesManager.preferencesAsFlow

    private val taskEventChannel = Channel<TasksEvent>()

    val taskEvent = taskEventChannel.receiveAsFlow()

    private val taskFlow = combine(searchQuery.asFlow(), preferencesFlow) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreference) ->
        taskDao.getTasks(query, filterPreference.sortOrder, filterPreference.hideCompleted)
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedChange(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskCheckedChanged(task: Task, checked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = checked))
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch{
        taskEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }

    fun onAddNewTaskClicked() = viewModelScope.launch{
        taskEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        when(result){
            ADD_TASK_RESULT_OK -> {
                showTaskSavedConfirmationMessage("Task Added")
            }
            EDIT_TASK_RESULT_OK -> {
                showTaskSavedConfirmationMessage("Task Edited")
            }
        }
    }

    private fun showTaskSavedConfirmationMessage(message: String) = viewModelScope.launch{
        taskEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(message))
    }

    fun onDeleteAllCompletedTasks() = viewModelScope.launch{
        taskEventChannel.send(TasksEvent.NavigateToDeleteAllCompletedScreen)
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        taskEventChannel.send(TasksEvent.ShowUnndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch{
        taskDao.insert(task)
    }

    val tasks = taskFlow.asLiveData()

    sealed class TasksEvent{
        object NavigateToDeleteAllCompletedScreen : TasksEvent()
        object NavigateToAddTaskScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task : Task) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg : String) : TasksEvent()
        data class ShowUnndoDeleteTaskMessage(val task : Task) : TasksEvent()
    }
}

