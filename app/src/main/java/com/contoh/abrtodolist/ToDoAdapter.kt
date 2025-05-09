package com.contoh.abrtodolist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ToDoAdapter(

    private val toDoItems: List<ToDoItem>,
    private val onItemCheckedChange: (ToDoItem, Boolean) -> Unit
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    inner class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTodoText: TextView = itemView.findViewById(R.id.textview_todo_text)
        val checkBoxTodoItem: CheckBox = itemView.findViewById(R.id.checkbox_todo_item)
        fun bind(item: ToDoItem) {
            textViewTodoText.text = item.text
            checkBoxTodoItem.setOnCheckedChangeListener(null)
            checkBoxTodoItem.isChecked = item.isCompleted
            updateTextStrikeThrough(textViewTodoText, item.isCompleted)

            checkBoxTodoItem.setOnCheckedChangeListener { _, isChecked ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val currentItem = toDoItems[adapterPosition]
                    currentItem.isCompleted = isChecked
                    updateTextStrikeThrough(textViewTodoText, isChecked)
                    onItemCheckedChange(currentItem, isChecked)
                }
            }
        }

        private fun updateTextStrikeThrough(textView: TextView, isCompleted: Boolean) {
            if (isCompleted) {
                textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemtodopage, parent, false)
        return ToDoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(toDoItems[position])
    }

    override fun getItemCount(): Int {
        return toDoItems.size
    }

    fun getItems(): List<ToDoItem> {
        return toDoItems
    }
}