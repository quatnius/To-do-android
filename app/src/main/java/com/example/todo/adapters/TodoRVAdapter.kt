package com.example.todo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.R
import com.example.todo.data.TodoEntity
import com.example.todo.databinding.SingleColumnBinding
import kotlinx.android.synthetic.main.single_column.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class TodoRVAdapter(val rvInterface: RVInterface): RecyclerView.Adapter<TodoRVAdapter.TodoViewHolder>(){

    private var DataList = emptyList<TodoEntity>()
    var _binding : SingleColumnBinding? = null
    val binding get() =  _binding!!
    private lateinit var context:Context
    private var lastPosition: Int = -1

    inner class TodoViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        init {
            itemView.checkbox_column.setOnClickListener {
                val position: Int = adapterPosition
                val data:TodoEntity = DataList[position]
                rvInterface.onCheckboxClick(data)
            }
            itemView.star_icon_column.setOnClickListener{
                val position: Int = adapterPosition
                val data: TodoEntity = DataList[position]
                rvInterface.onStarClick(data)
            }
            itemView.setOnClickListener{
                val position: Int = adapterPosition
                val data: TodoEntity = DataList[position]
                rvInterface.onViewClick(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        _binding = SingleColumnBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        context = parent.context
        return TodoViewHolder(binding.root)
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val currentItem = DataList[position]
        val star_icon: ImageView = binding.starIconColumn
        val due = currentItem.dueDate
        var formattedDate:String? = null
        var dateObject:Date? = null
        if (due!=null) {
            val format = SimpleDateFormat("dd/MM/yyyy")
            dateObject = format.parse(due)
            formattedDate = getFormattedDate(dateObject!!)
        }

        if (holder.adapterPosition > lastPosition) {
            val animation: Animation =
                AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fall_down)
            holder.itemView.startAnimation(animation)
        }

        binding.checkboxTextColumn.text = currentItem.title

        if (currentItem.completed) {
            binding.checkboxColumn.isChecked = true
            binding.checkboxTextColumn.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            binding.checkboxColumn.isChecked = false
        }

        if (currentItem.important == true) {
            binding.starIconColumn.isVisible = true
            binding.starIconColumn.imageTintList =
                (ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)))
        }

        if (currentItem.alarmTime!=null){
            val timeCombined = currentItem.alarmTime.toInt()
            val hrs:Int = timeCombined/100
            val min:Int = timeCombined%100
            if (due!=null){
                binding.checkboxAdditionalText.apply{
                    text = String.format("%02d:%02d \u2022 Due %s",hrs,min,formattedDate)
                    if (Date().after(dateObject)){
                        setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)))
                        //binding.alarmIconColumn.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red))
                    }
                }
            }else{
                binding.checkboxAdditionalText.
                    text = String.format("%02d:%02d",hrs,min)
            }
            binding.checkboxAdditionalText.visibility = View.VISIBLE
            binding.alarmIconColumn.visibility = View.VISIBLE
        }else{
            if (due!=null){
                binding.checkboxAdditionalText.apply {
                    text = String.format("Due %s",formattedDate)
                    if (Date().after(dateObject)){
                        setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)))
                        //binding.alarmIconColumn.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red))
                    }
                    visibility = View.VISIBLE
                }
                binding.alarmIconColumn.apply{
                    setImageResource(R.drawable.ic_calender_figma)
                    visibility = View.VISIBLE
                }
            }
        }

    }

    private fun getFormattedDate(d: Date): String {
        val today:Boolean = DateUtils.isToday(d.time)
        val tomorrow:Boolean = DateUtils.isToday(d.time - DateUtils.DAY_IN_MILLIS)
        val yesterday:Boolean = DateUtils.isToday(d.time + DateUtils.DAY_IN_MILLIS)

        return when {
            today -> {
                "Today"
            }
            tomorrow -> {
                "Tomorrow"
            }
            yesterday -> {
                "Yesterday"
            }
            else -> {
                val formatter = SimpleDateFormat("d MMM")
                formatter.format(d)
            }
        }
    }

    override fun getItemCount(): Int {
        return DataList.size
    }

    class TodoDiffCallback(var oldList:List<TodoEntity>,var newList: List<TodoEntity>):DiffUtil.Callback(){
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return (oldList.get(oldItemPosition).id == newList.get(newItemPosition).id)
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return (oldList.get(oldItemPosition).equals(newList.get(newItemPosition)))
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun NotifyChanges(DataList:List<TodoEntity>){
        val oldList = this.DataList
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            TodoDiffCallback(oldList,DataList)
        )
        this.DataList = DataList
        //notifyDataSetChanged()
        diffResult.dispatchUpdatesTo(this)
    }

    fun removeItemChange(position:Int){
        val oldList = this.DataList
        val newList = oldList.toMutableList()
        newList.removeAt(position)
        newList.toList()
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            TodoDiffCallback(oldList,newList)
        )
        this.DataList = newList
        //notifyDataSetChanged()
        diffResult.dispatchUpdatesTo(this)
    }

    fun addItemChange(entity: TodoEntity,position: Int){
        val oldList = this.DataList
        val newList = oldList.toMutableList()
        newList.add(position,entity)
        newList.toList()
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            TodoDiffCallback(oldList,newList)
        )
        this.DataList = newList
        //notifyDataSetChanged()
        diffResult.dispatchUpdatesTo(this)
    }

    interface RVInterface{
        fun onCheckboxClick(data:TodoEntity)
        fun onStarClick(data: TodoEntity)
        fun onViewClick(data: TodoEntity)
    }


}