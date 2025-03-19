package com.example.mohassu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.ArrayAdapter
import com.example.mohassu.databinding.DialogPromiseMenuBinding

class MenuWithIconAdapter(
    context: Context,
    private val items: Array<String>,
    private val icons: IntArray
) : ArrayAdapter<String>(context, 0, items) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: DialogPromiseMenuBinding

        val view = if (convertView == null) {
            binding = DialogPromiseMenuBinding.inflate(inflater, parent, false)
            binding.root
        } else {
            binding = DialogPromiseMenuBinding.bind(convertView)
            convertView
        }

        binding.itemIcon.setImageResource(icons[position])
        binding.itemText.text = items[position]

        return view
    }
}
