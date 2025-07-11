package com.example.mybatagor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mybatagor.databinding.ViewtasksBinding
import com.example.mybatagor.model.Task

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit = {},
    private val onTaskChecked: (Task, Boolean) -> Unit = { _, _ -> },
    private val onTaskOptionsClick: (Task) -> Unit = {}
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DIFF_CALLBACK) {

    inner class TaskViewHolder(val binding: ViewtasksBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskDescription.text = task.description
            binding.tvDueDate.text = task.dueDate
            binding.chipPriority.text = task.priority


            // Hapus listener lama dulu biar gak dobel triggering
            binding.cbTaskComplete.setOnCheckedChangeListener(null)
            binding.cbTaskComplete.isChecked = task.isCompleted

            // Pasang listener baru
            binding.cbTaskComplete.setOnCheckedChangeListener { _, isChecked ->
                onTaskChecked(task, isChecked)
            }

            // Klik pada item
            binding.root.setOnClickListener {
                onTaskClick(task)
            }

            // Klik opsi (3 titik)
            binding.btnTaskOptions.setOnClickListener {
                onTaskOptionsClick(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ViewtasksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem
            }
        }
    }
}
