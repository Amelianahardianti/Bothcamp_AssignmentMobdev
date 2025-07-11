package com.example.mybatagor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.mybatagor.TaskRepository
import com.example.mybatagor.model.Task
import com.google.firebase.firestore.FirebaseFirestore

class TaskViewModel(
    private val repository: TaskRepository = TaskRepository(FirebaseFirestore.getInstance())
) : ViewModel() {

    val taskList: LiveData<List<Task>> = repository.getTasks()

    fun addTask(task: Task) = repository.addTask(task)

    fun updateTask(task: Task) = repository.updateTask(task)

    fun deleteTask(taskId: String) = repository.deleteTask(taskId)

    fun startListeningForTasks() = repository.startListeningForTasks()

}

