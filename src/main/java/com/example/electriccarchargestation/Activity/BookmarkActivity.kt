package com.example.electriccarchargestation.Activity

import com.example.electriccarchargestation.Adapter.RecyclerViewAdapter
import com.example.electriccarchargestation.DB.DBHelper
import com.example.electriccarchargestation.TBL_DATA_CLASS.PublicData
import com.example.electriccarchargestation.databinding.ActivityBookmarkBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.electriccarchargestation.Decoration.RecyclerDecoration

class BookmarkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookmarkBinding
    private var itemViewDataList: MutableList<PublicData>? = null
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private val BOOKMARK_ACTIVITY = "bookmark"
    private val dbHelper = DBHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        itemViewDataList = mutableListOf<PublicData>()

        if (dbHelper.selectAllPublicData()?.size != 0) {
            //공공데이터가 있으면
            Log.d("sophia", "공공데이터 있음")
        } else {
            //공공데이터가 없으면
            for (i in 0 until 30) {

                //즐겨찾기 테스트를 위해 모든데이터 bookmark 표시를 1로 설정해놓는다.-> 0으로 변경
                itemViewDataList?.add(
                    PublicData(
                        0, null, "A+$i", i % 8, null, "상왕십리전기차주유소$i",
                        "행당${i}동", null, null, 0
                    )
                )
                if (dbHelper.insertPublicData(itemViewDataList!!.get(i))) {
                    Log.d("sophia", "DB입력성공")
                } else {
                    Log.d("sophia", "DB입력실패")
                }
            }
        }

        itemViewDataList = dbHelper.selectBookmarkPublicData()

        for (i in 0 until itemViewDataList!!.size) {
            Log.d("sophia", "bookmark: ${itemViewDataList!!.get(i).bookmark}")
        }

        recyclerViewAdapter = RecyclerViewAdapter(this, itemViewDataList, BOOKMARK_ACTIVITY)
        binding.recyclerView.adapter = recyclerViewAdapter

        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = linearLayoutManager

        //데코레이션을 리사이클러뷰에 적용하기
        binding.recyclerView.addItemDecoration(RecyclerDecoration(this))
    }
}
