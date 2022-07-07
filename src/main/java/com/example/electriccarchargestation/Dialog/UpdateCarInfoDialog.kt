package com.example.electriccarchargestation.Dialog

import com.example.electriccarchargestation.DB.DBHelper
import com.example.electriccarchargestation.TBL_DATA_CLASS.CarInfo
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.electriccarchargestation.databinding.ActivityCarInfoBinding
import com.example.electriccarchargestation.databinding.DialogUpdateCarinfoBinding

class UpdateCarInfoDialog (val context: Context, val carInfobinding: ActivityCarInfoBinding) {
    private val items = arrayOf("DC차데모","AC완속","DC차데모+AC3상","DC콤보","DC차데모+DC콤보","DC차데모+AC3상+DC콤보","AC3상")
    private val myAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item,items)

    private val dbHelper = DBHelper(context)

    private val dialog= Dialog(context)

    fun showDialog(){

        val binding = DialogUpdateCarinfoBinding.inflate(LayoutInflater.from(context))

        dialog.setContentView(binding.root)

        dialog.show()

        binding.spinner2.adapter= myAdapter

        binding.spinner2.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                when(position) {
                    in 0..7   ->  {
                        binding.spinner2.getItemAtPosition(position)

                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        binding.btnCarInfo2Renew.setOnClickListener {

            carInfobinding.tvCarInfo2Name.text = "차종: " + binding.edtCarInfo2ReName.text

            carInfobinding.tvCarInfo2Type.text = "충전기 타입: " + binding.spinner2.selectedItem.toString()

            val selectedItem = binding.spinner2.selectedItem.toString()
            val carName = binding.edtCarInfo2ReName.text.toString()
            val chargeType = if(selectedItem == "DC차데모"){
                1
            }else if(selectedItem == "AC완속"){
                2
            }else if(selectedItem == "DC차데모+AC3상"){
                3
            }else if(selectedItem == "DC콤보"){
                4
            }else if(selectedItem == "DC차데모+DC콤보"){
                5
            }else if(selectedItem == "DC차데모+AC3상+DC콤보"){
                6
            }else {
                7
            }

            if(carName.length == 0){
                Toast.makeText(context, "모든정보 입력요망", Toast.LENGTH_SHORT).show()
            }else{
                val member = CarInfo(0,carName, chargeType)
                if(dbHelper.insertCarInfo(member)){
                    Toast.makeText(context, "수정성공", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "수정실패", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.dismiss()
        }
    }
}