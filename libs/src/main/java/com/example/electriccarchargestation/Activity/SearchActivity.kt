package com.example.electriccarchargestation.Activity

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.electriccarchargestation.Adapter.RecyclerViewAdapter
import com.example.electriccarchargestation.DB.DBHelper
import com.example.electriccarchargestation.Decoration.RecyclerDecoration
import com.example.electriccarchargestation.Dialog.MenuItemDialog
import com.example.electriccarchargestation.R
import com.example.electriccarchargestation.TBL_DATA_CLASS.PublicData
import com.example.electriccarchargestation.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    //MenuItemDialog에서 사용 : public
    lateinit var binding: ActivitySearchBinding
    var searchString: String? = null
    var selectChargeType: Int = 0

    private lateinit var menuItemDialog: MenuItemDialog
    private val SEARCH_ACTIVITY = "search"

    private var carList: MutableList<PublicData>? = null
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter

    private val dbHelper = DBHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carList = mutableListOf<PublicData>()

        if(dbHelper.selectAllPublicData()?.size != 0){
            //공공데이터가 있으면
            Log.d("shin", "공공데이터 있음")
        }else{
            //공공데이터가 없으면
            for(i in 0 until 10){
                carList?.add(PublicData(0, null, null, 0 + i, null, "상왕십리전기충전소$i", "행당1동 왕십리광장로 $i", null, null, 0))
                if(dbHelper.insertPublicData(carList!!.get(i))){
                    Log.d("shin", "DB입력성공")
                }else{
                    Log.d("shin", "DB입력실패")
                }
            }
        }

        carList = dbHelper.selectAllPublicData()

        recyclerViewAdapter = RecyclerViewAdapter(this, carList!!, SEARCH_ACTIVITY)
        binding.recyclerView.adapter = recyclerViewAdapter

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.addItemDecoration(RecyclerDecoration(this))

    }//end of onCreate

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchMenu = menu?.findItem(R.id.search_Item)
        val searchView = searchMenu?.actionView as androidx.appcompat.widget.SearchView

        searchView.setOnQueryTextListener(object:androidx.appcompat.widget.SearchView.OnQueryTextListener{
            //검색 완료
            override fun onQueryTextSubmit(query: String?): Boolean {
                carList?.clear()
                carList = dbHelper.selectSearchPublicData(query, selectChargeType)
                for(i in 0 until carList!!.size) {
                    Log.d("shin", "stationName: ${carList!!.get(i).stationName}, address: ${carList!!.get(i).address}")
                }
                binding.recyclerView.adapter = RecyclerViewAdapter(this@SearchActivity, carList!!, SEARCH_ACTIVITY)
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if(!query.isNullOrBlank()){
                    searchString = query
                    carList?.clear()
                    carList = dbHelper.selectSearchPublicData(query, selectChargeType)
                    binding.recyclerView.adapter = RecyclerViewAdapter(this@SearchActivity, carList!!, SEARCH_ACTIVITY)
                }else{
                    carList?.clear()
                    carList = dbHelper.selectAllPublicData()
                    binding.recyclerView.adapter = RecyclerViewAdapter(this@SearchActivity, carList!!, SEARCH_ACTIVITY)
                }

                return true
            }
        })

        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item?.itemId) {
            R.id.option_Item-> {
                menuItemDialog = MenuItemDialog(this)
                menuItemDialog.showDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}//end of class