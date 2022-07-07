package com.example.electriccarchargestation.Activity

import com.example.electriccarchargestation.BitmapData.BitmapData
import com.example.electriccarchargestation.DB.DBHelper
import com.example.electriccarchargestation.R
import com.example.electriccarchargestation.TBL_DATA_CLASS.CarInfo
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.electriccarchargestation.databinding.ActivityInsertCarInfoBinding

class InsertCarInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInsertCarInfoBinding
    private val REQUEST_CODE = 1
    private var imageView: ImageView? = null
    private var uri: Uri? = null
    private val dbHelper = DBHelper(this)
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityInsertCarInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = arrayOf("DC차데모","AC완속","DC차데모+AC3상","DC콤보","DC차데모+DC콤보","DC차데모+AC3상+DC콤보","AC3상")
        val myAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,items)

        binding.spinner.adapter= myAdapter

        binding.spinner.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                when(position) {
                    in 0..6   ->  {
                        binding.spinner.getItemAtPosition(position)
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }
    val requestGalleryLauncher =registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        try{

            val calculateRadio = calculateInSampleSize(it.data!!.data!!,
                resources.getDimensionPixelSize(com.example.electriccarchargestation.R.dimen.imageSize2),resources.getDimensionPixelSize(com.example.electriccarchargestation.R.dimen.imageSize2))

            val option = BitmapFactory.Options()

            option.inSampleSize= calculateRadio

            var inputStream = contentResolver.openInputStream(it.data!!.data!!)
            bitmap = BitmapFactory.decodeStream(inputStream,null,option)

            BitmapData.bitmap = bitmap
            BitmapData.flag = true

            inputStream!!.close()
            inputStream = null

            if(bitmap!=null){
                binding.ivCarInfoPhoto.setImageBitmap(bitmap)
            }else{
                Log.d("kin", "비트맵 오류")
            }

        }catch (e:Exception){
            e.printStackTrace()
        }


    }

    fun onClickView(view: View){

        when(view?.id){

            R.id.btnCarInfoSkip ->{
                val intent= Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.btnCarInfoInput ->{
                val selectedItem = binding.spinner.selectedItem.toString()
                val carName = binding.edtCarInfo1Name.text.toString()
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
                    Toast.makeText(this, "모든정보 입력요망", Toast.LENGTH_SHORT).show()
                }else{
                    val member = CarInfo(0,carName, chargeType)
                    if(dbHelper.insertCarInfo(member)){
                        Toast.makeText(this, "삽입성공", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "삽입실패", Toast.LENGTH_SHORT).show()
                    }
                }

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.btnInsertPhoto ->{
                var intent=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type="image/*"
                requestGalleryLauncher.launch(intent)
            }
        }
    }

    private fun calculateInSampleSize(uri: Uri, requestWidth:Int, requestHeight:Int):Int{
        val options =BitmapFactory.Options()
        //외부에 잇는 이미지를 비트맵으로 가져오기 전에 정보만 파악
        options.inJustDecodeBounds=true
        try{
            var inputStream=contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream!!.close()
            inputStream=null
        }catch (e: Exception){
            e.printStackTrace()
        }

        val height=options.outHeight
        val width=options.outWidth
        var inSampleSize = 1

        if(height > requestHeight || width > requestWidth){
            val halfHeight = height / 2
            val halfWidth = width / 2

            while(halfHeight/inSampleSize>=requestHeight && halfWidth/inSampleSize >= requestWidth){
                inSampleSize *=2
            }
        }

        return inSampleSize
    }
}