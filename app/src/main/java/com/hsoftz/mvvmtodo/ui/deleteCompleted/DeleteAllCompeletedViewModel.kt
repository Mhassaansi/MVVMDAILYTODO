package com.hsoftz.mvvmtodo.ui.deleteCompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.hsoftz.mvvmtodo.data.TaskDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DeleteAllCompeletedViewModel @ViewModelInject constructor(
    private val taskDao:TaskDao,
    @ApplicationContext private val applicationScope: CoroutineScope
) : ViewModel(){


    fun onConfirmClick()= applicationScope.launch {
        taskDao.deleteCompeletedTask()
    }
}