package com.example.electriccarchargestation.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.electriccarchargestation.Activity.MainActivity
import com.example.electriccarchargestation.DB.DBHelper
import com.example.electriccarchargestation.R
import com.example.electriccarchargestation.TBL_DATA_CLASS.PublicData
import com.example.electriccarchargestation.databinding.ItemViewBinding

class RecyclerViewAdapter(val context: Context, var publicDatalist: MutableList<PublicData>?,val selectActivity: String): RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {

    private val dbHelper = DBHelper(context)
    private val SEARCH_ACTIVITY = "search"
    private val BOOKMARK_ACTIVITY = "bookmark"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val binding = ItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return RecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val binding = holder.binding
        var selectedPublicData = publicDatalist?.get(position)

        binding.tvItemName.text = selectedPublicData?.stationName
        binding.tvItemAddress.text = selectedPublicData?.address
        if(selectedPublicData?.bookmark == 1) {
            binding.ivEmptyStar.setImageResource(R.drawable.fullstar_24)
        } else {
            binding.ivEmptyStar.setImageResource(R.drawable.emptystar_24)
        }

        binding.root.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("selectedPublicData", selectedPublicData)
            context.startActivity(intent)
        }

        binding.ivEmptyStar.setOnClickListener {
            if(selectedPublicData?.bookmark == 1) {
                selectedPublicData.bookmark = 0
                dbHelper.updateBookmark(selectedPublicData)
                if(selectActivity == SEARCH_ACTIVITY){
                    notifyItemChanged(position)
                    Toast.makeText(context, "즐겨찾기 취소", Toast.LENGTH_SHORT).show()
                }else if(selectActivity == BOOKMARK_ACTIVITY){
                    publicDatalist?.clear()
                    Toast.makeText(context, "즐겨찾기 취소", Toast.LENGTH_SHORT).show()
                    publicDatalist = dbHelper.selectBookmarkPublicData()
                    notifyDataSetChanged()
                }
            } else {
                selectedPublicData?.bookmark = 1
                if(dbHelper.updateBookmark(selectedPublicData!!)) {
                    Toast.makeText(context, "즐겨찾기 성공", Toast.LENGTH_SHORT).show()
                }
                notifyItemChanged(position)
            }

        }
    }//end of onBindView

    override fun getItemCount(): Int = publicDatalist?.size ?: 0

    class RecyclerViewHolder(val binding: ItemViewBinding): RecyclerView.ViewHolder(binding.root)
}