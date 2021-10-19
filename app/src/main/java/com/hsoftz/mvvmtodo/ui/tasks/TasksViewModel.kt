package com.hsoftz.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.hsoftz.mvvmtodo.data.PrefrencesManager
import com.hsoftz.mvvmtodo.data.SortOrder
import com.hsoftz.mvvmtodo.data.Task
import com.hsoftz.mvvmtodo.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val prefrencesManager:PrefrencesManager
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

   val prefrencesFlow=prefrencesManager.prefrencesFlow

   private val taskEventChannel = Channel<TaskEvent>()
   val taskEvent=taskEventChannel.receiveAsFlow()
    private val tasksFlow = combine(
        searchQuery,
        prefrencesFlow
    ) { query,filterPrefrences->
        Pair(query,filterPrefrences)
    }.flatMapLatest { (query,filterPrefrences) ->
        taskDao.getTasks(query, filterPrefrences.sortOrder,filterPrefrences.hideCompleted)
    }

    fun onSortOrderSelected(sortOrder: SortOrder)=viewModelScope.launch {
        prefrencesManager.updatedSortOrder(sortOrder)
    }
    fun onHideCompeltedSelected(hidecompeleted:Boolean)=viewModelScope.launch {
        prefrencesManager.updatedHideCompeletd(hidecompeleted)
    }
    val tasks = tasksFlow.asLiveData()


   fun onTaskSelected(task: Task){

   }

    fun onTaskCheckChanged(task:Task,isChecked:Boolean)=viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }
    fun onTaskSwipe(task: Task)=viewModelScope.launch{
      taskDao.delete(task)
        taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task)=viewModelScope.launch {
        taskDao.insert(task)
    }
    sealed class TaskEvent{
        data class ShowUndoDeleteTaskMessage(val task:Task):TaskEvent(){
        }
    }
}

