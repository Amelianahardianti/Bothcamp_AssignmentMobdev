package com.example.mybatagor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mybatagor.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TaskRepository(private val db: FirebaseFirestore) {

    private val tasksLiveData = MutableLiveData<List<Task>>()

    fun startListeningForTasks() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(uid).collection("tasks")
            .addSnapshotListener { snapshot, _ ->
                val taskList = snapshot?.documents?.mapNotNull { doc ->
                    val task = doc.toObject(Task::class.java)
                    task?.apply { id = doc.id }
                } ?: emptyList()

                tasksLiveData.value = taskList
            }
    }

    fun getTasks(): LiveData<List<Task>> = tasksLiveData

    fun addTask(task: Task) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(uid).collection("tasks").add(task)
    }

    fun updateTask(task: Task) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(uid).collection("tasks").document(task.id).set(task)
    }

    fun deleteTask(taskId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(uid).collection("tasks").document(taskId).delete()
    }

}
