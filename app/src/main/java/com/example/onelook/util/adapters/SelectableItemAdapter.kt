package com.example.onelook.util.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.onelook.R
import com.example.onelook.databinding.ItemSelectableNumberBinding
import com.example.onelook.databinding.ItemSelectableOvalBinding
import com.example.onelook.databinding.ItemSelectableRectBinding
import com.example.onelook.util.capital

class SelectableItemAdapter(
    private val context: Context,
    private val items: List<SelectableItem>,
    private val onClickListener: (Int) -> Unit = { }
) :
    Adapter<ViewHolder>() {

    private var selectedItemPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            SELECTABLE_RECT_WITH_TEXT -> {
                SelectableRectWithTextVH(
                    ItemSelectableRectBinding.inflate(inflater, parent, false)
                )
            }

            SELECTABLE_OVAL_NUMBER -> {
                SelectableOvalNumberVH(
                    ItemSelectableNumberBinding.inflate(inflater, parent, false)
                )
            }

            SELECTABLE_OVAL_WITH_TEXT -> {
                SelectableOvalWithTextVH(
                    ItemSelectableOvalBinding.inflate(inflater, parent, false)
                )
            }

            else -> throw IllegalArgumentException("View type of $viewType is unknown")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return when (holder) {
            is SelectableRectWithTextVH -> {
                holder.bind(items[position] as SelectableRectWithText)
            }

            is SelectableOvalNumberVH -> {
                holder.bind(items[position] as SelectableOvalNumber)
            }

            is SelectableOvalWithTextVH -> {
                holder.bind(items[position] as SelectableOvalWithText)
            }

            else -> throw IllegalArgumentException("${items[position]::class.simpleName} in data set is unknown")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SelectableRectWithText -> SELECTABLE_RECT_WITH_TEXT
            is SelectableOvalWithText -> SELECTABLE_OVAL_WITH_TEXT
            is SelectableOvalNumber -> SELECTABLE_OVAL_NUMBER
            else -> throw IllegalArgumentException("${items[position]::class.simpleName} in data set is unknown")
        }
    }

    companion object {
        const val SELECTABLE_RECT_WITH_TEXT = 0
        const val SELECTABLE_OVAL_NUMBER = 1
        const val SELECTABLE_OVAL_WITH_TEXT = 2
    }

    fun updateSelectedItem(newPosition: Int) {
        notifyItemChanged(newPosition)
        notifyItemChanged(selectedItemPosition)
        selectedItemPosition = newPosition
    }

    inner class SelectableRectWithTextVH(val binding: ItemSelectableRectBinding) :
        ViewHolder(binding.root) {

        init {
            binding.imageButtonIcon.setOnClickListener {
                onClickListener(adapterPosition)
            }
        }

        fun bind(item: SelectableRectWithText) {
            binding.apply {

                imageButtonIcon.apply {
                    setImageResource(item.iconSelected)
                    setBackgroundResource(
                        if (adapterPosition == selectedItemPosition) R.drawable.bg_ic_big_radius_selected
                        else R.drawable.bg_ic_big_radius
                    )
                    val color = ContextCompat.getColor(
                        context,
                        if (adapterPosition == selectedItemPosition) R.color.black
                        else R.color.dark_grey
                    )
                    DrawableCompat.setTint(this.drawable, color)
                }

                val typedArray =
                    context.obtainStyledAttributes(null, intArrayOf(R.attr.colorText))
                val textColor =
                    if (adapterPosition == selectedItemPosition) typedArray.getColor(0, 0)
                    else ContextCompat.getColor(context, R.color.dark_grey)
                typedArray.recycle()

                textViewName.apply {
                    text = item.text.capital
                    setTextColor(textColor)
                }
            }
        }
    }//SelectableRectWithTextVH

    inner class SelectableOvalNumberVH(val binding: ItemSelectableNumberBinding) :
        ViewHolder(binding.root) {

        init {
            binding.textViewNumber.setOnClickListener {
                onClickListener(adapterPosition)
            }
        }

        fun bind(item: SelectableOvalNumber) {
            binding.apply {

                val textColor = ContextCompat.getColor(
                    context,
                    if (adapterPosition == selectedItemPosition) R.color.black
                    else R.color.dark_grey
                )

                textViewNumber.apply {
                    text = item.number
                    setTextColor(textColor)
                    setBackgroundResource(
                        if (adapterPosition == selectedItemPosition) R.drawable.bg_ic_oval_selected
                        else R.drawable.bg_ic_oval
                    )
                }
            }
        }
    }//SelectableOvalNumberVH

    inner class SelectableOvalWithTextVH(val binding: ItemSelectableOvalBinding) :
        ViewHolder(binding.root) {

        init {
            binding.imageButtonIcon.setOnClickListener {
                onClickListener(adapterPosition)
            }
        }

        fun bind(item: SelectableOvalWithText) {
            binding.apply {

                imageButtonIcon.apply {
                    setImageResource(item.iconSelected)
                    setBackgroundResource(
                        if (adapterPosition == selectedItemPosition) R.drawable.bg_ic_oval_selected
                        else R.drawable.bg_ic_oval
                    )
                    val color = ContextCompat.getColor(
                        context,
                        if (adapterPosition == selectedItemPosition) R.color.black
                        else R.color.dark_grey
                    )
                    DrawableCompat.setTint(this.drawable, color)
                }

                val typedArray =
                    context.obtainStyledAttributes(null, intArrayOf(R.attr.colorText))
                val textColor =
                    if (adapterPosition == selectedItemPosition) typedArray.getColor(0, 0)
                    else ContextCompat.getColor(context, R.color.dark_grey)
                typedArray.recycle()


                textViewName.apply {
                    text = item.text.capital
                    setTextColor(textColor)
                }
            }
        }
    }//SelectableRectWithTextVH
}