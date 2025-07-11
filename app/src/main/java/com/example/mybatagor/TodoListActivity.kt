package com.example.mybatagor

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mybatagor.adapter.TaskAdapter
import com.example.mybatagor.databinding.TodolistBinding
import com.example.mybatagor.model.Task
import com.example.mybatagor.viewmodel.TaskViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth

class TodoListActivity : AppCompatActivity() {

    private lateinit var binding: TodolistBinding
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TodolistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
        taskViewModel.startListeningForTasks()
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }



    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { Task ->
                // Handle task click if needed
            },
            onTaskChecked = { task, isChecked ->
                updateTaskCompletion(task, isChecked)
            },
            onTaskOptionsClick = { task ->
                showTaskOptionsDialog(task)
            }
        )

        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(this@TodoListActivity)
            adapter = taskAdapter
        }
    }

    private fun observeViewModel() {
        taskViewModel.taskList.observe(this, Observer { tasks ->
            taskAdapter.submitList(tasks)
            updateStats(tasks)
        })
    }

    private fun setupClickListeners() {
        // Tombol FAB untuk tambah task
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }


    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.show()

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etDueDate = dialogView.findViewById<EditText>(R.id.etDueDate)
        val spinnerPriority = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerPriority)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)

        // Isi dropdown prioritas
        val priorities = arrayOf("Very Important", "Important", "Not really important")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, priorities)
        spinnerPriority.setAdapter(adapter)

        // Tampilkan calendar saat klik kolom tanggal
        etDueDate.setOnClickListener {
            showDatePicker { selectedDate ->
                etDueDate.setText(selectedDate)
            }
        }

        // Tombol Batal
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Tombol Simpan
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDescription.text.toString().trim()
            val date = etDueDate.text.toString().trim()
            val priorityValue = spinnerPriority.text.toString().trim()

            if (title.isNotBlank()) {
                val task = Task(
                    title = title,
                    description = desc,
                    dueDate = date,
                    priority = if (priorityValue.isNotEmpty()) priorityValue else "Not really important",
                    isCompleted = false
                )

                taskViewModel.addTask(task)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formatted = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(formatted)
            },
            year, month, day
        )

        datePickerDialog.show()
    }


    private fun updateTaskCompletion(task: Task, isCompleted: Boolean) {
        val updatedTask = task.copy(isCompleted = isCompleted)

        // Panggil ViewModel untuk menangani update, sama seperti fungsi edit dan hapus
        taskViewModel.updateTask(updatedTask)
    }


    private fun showTaskOptionsDialog(task: Task) {
        val options = arrayOf("Edit", "Delete")

        AlertDialog.Builder(this)
            .setTitle("Opsi Task")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditTaskDialog(task)
                    1 -> showDeleteConfirmationDialog(task)
                }
            }
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etDueDate = dialogView.findViewById<EditText>(R.id.etDueDate)
        val spinnerPriority = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerPriority)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        // Pre-fill data
        etTitle.setText(task.title)
        etDescription.setText(task.description)
        etDueDate.setText(task.dueDate)

        val priorities = arrayOf("Very Important", "Important", "Not really important")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, priorities)
        spinnerPriority.setAdapter(adapter)
        spinnerPriority.setText(task.priority, false)

        // Date picker
        etDueDate.setOnClickListener {
            showDatePicker { selectedDate ->
                etDueDate.setText(selectedDate)
            }
        }

        // Buat dialog custom
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Tombol SIMPAN dari XML
        btnSave.setOnClickListener {
            val updatedTask = task.copy(
                title = etTitle.text.toString().trim(),
                description = etDescription.text.toString().trim(),
                dueDate = etDueDate.text.toString().trim(),
                priority = spinnerPriority.text.toString().trim()
            )
            taskViewModel.updateTask(updatedTask)
            dialog.dismiss()
        }

        // Tombol BATAL dari XML
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun updateTaskInFirestore(task: Task) {
        if (task.id.isNotEmpty()) {
            firestore.collection("tasks")
                .document(task.id)
                .set(task)
                .addOnSuccessListener {
                    Toast.makeText(this, "Task berhasil diupdate", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal update task: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.w("TodoListActivity", "Error updating task", e)
                }
        }
    }

    private fun showDeleteConfirmationDialog(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Task")
            .setMessage("Apakah Anda yakin ingin menghapus task '${task.title}'?")
            .setPositiveButton("Hapus") { _, _ ->
                taskViewModel.deleteTask(task.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }


    private fun updateStats(tasks: List<Task>) {
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.isCompleted }

        binding.tvTotalTasks.text = totalTasks.toString()
        binding.tvCompletedTasks.text = completedTasks.toString()
    }


}