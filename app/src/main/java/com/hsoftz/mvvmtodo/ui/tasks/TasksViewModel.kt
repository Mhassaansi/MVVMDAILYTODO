package com.hsoftz.mvvmtodo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.hsoftz.mvvmtodo.data.PrefrencesManager
import com.hsoftz.mvvmtodo.data.SortOrder
import com.hsoftz.mvvmtodo.data.Task
import com.hsoftz.mvvmtodo.data.TaskDao
import com.hsoftz.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.hsoftz.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val prefrencesManager: PrefrencesManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")
    //MutableStateFlow("")

    val prefrencesFlow = prefrencesManager.prefrencesFlow

    private val taskEventChannel = Channel<TaskEvent>()
    val taskEvent = taskEventChannel.receiveAsFlow()
    private val tasksFlow = combine(
        searchQuery.asFlow(),
        prefrencesFlow
    ) { query, filterPrefrences ->
        Pair(query, filterPrefrences)
    }.flatMapLatest { (query, filterPrefrences) ->
        taskDao.getTasks(query, filterPrefrences.sortOrder, filterPrefrences.hideCompleted)
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        prefrencesManager.updatedSortOrder(sortOrder)
    }

    fun onHideCompeltedSelected(hidecompeleted: Boolean) = viewModelScope.launch {
        prefrencesManager.updatedHideCompeletd(hidecompeleted)
    }

    val tasks = tasksFlow.asLiveData()


    fun onTaskSelected(task: Task) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }

    fun onTaskSwipe(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskConfirmationMessage("Task Added")
            EDIT_TASK_RESULT_OK -> showTaskConfirmationMessage("Task Updated")
        }
    }

    private fun showTaskConfirmationMessage(text: String) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.ShowTaskConfirmationMessage(text))
    }

    fun onDeleteCompeletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToDeletedCompeletdScreen)
    }

    sealed class TaskEvent {
        object NavigateToAddTaskScreen : TaskEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TaskEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvent()
        data class ShowTaskConfirmationMessage(val msg: String) : TaskEvent()
        object NavigateToDeletedCompeletdScreen : TaskEvent()
    }

    fun onNewAddTaskClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAddTaskScreen)
    }
}

