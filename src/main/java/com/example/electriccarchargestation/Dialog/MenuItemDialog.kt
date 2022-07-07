package com.example.electriccarchargestation.Dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.electriccarchargestation.Activity.SearchActivity
import com.example.electriccarchargestation.Adapter.RecyclerViewAdapter
import com.example.electriccarchargestation.DB.DBHelper
import com.example.electriccarchargestation.databinding.DialogMenuItemBinding

class MenuItemDialog(val context: Context) {
    private val dialog = Dialog(context)
    private val items = arrayOf("전부보기","DC차데모","AC완속","DC차데모+AC3상","DC콤보","DC차데모+DC콤보","DC차데모+AC3상+DC콤보","AC3상")
    private val myAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, items)

    private val dbHelper = DBHelper(context)

    private var itemPosition: Int = 0

    private val SEARCH_ACTIVITY = "search"

    fun showDialog() {
        val binding = DialogMenuItemBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.show()

        binding.spinner.adapter = myAdapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                itemPosition = position
                binding.spinner.prompt = "타입 선택"
                when(position) {
                    in 0..7 -> {
                        (context as SearchActivity).selectChargeType = itemPosition
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.btnOk.setOnClickListener {
            val searchContext = context as SearchActivity
            val query = searchContext.searchString

            var carList = dbHelper.selectSearchPublicData(query, itemPosition)
            searchContext.binding.recyclerView.adapter = RecyclerViewAdapter(context, carList, SEARCH_ACTIVITY)

            dialog.dismiss()
        }
    }

}//end of class