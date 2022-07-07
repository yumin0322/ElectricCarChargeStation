package com.example.electriccarchargestation.Activity

import com.example.electriccarchargestation.BitmapData.BitmapData
import com.example.electriccarchargestation.DB.DBHelper
import com.example.electriccarchargestation.TBL_DATA_CLASS.CarInfo
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.example.electriccarchargestation.Dialog.UpdateCarInfoDialog
import com.example.electriccarchargestation.databinding.ActivityCarInfoBinding

class CarInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarInfoBinding
    private lateinit var updateCarInfoDialog: UpdateCarInfoDialog

    private var carInfo: CarInfo? = null
    private var bitmap: Bitmap? = null
    private val dbHelper = DBHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCarInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carInfo = dbHelper.selectCarInfo()

        bitmap = BitmapData.bitmap

        if(BitmapData.flag){
            binding.ivCarInfo2Photo.setImageBitmap(bitmap)
        }else{
            binding.ivCarInfo2Photo.setImageResource(com.example.electriccarchargestation.R.drawable.car_photo)
        }

        val myChargeType = carInfo?.chargeType

        binding.tvCarInfo2Name.text= "차종: " + carInfo?.carName

        binding.tvCarInfo2Type.text = "충전기 타입: " + if(myChargeType == 1){
            "DC차데모"
        }else if(myChargeType == 2){
            "AC완속"
        }else if(myChargeType == 3){
            "DC차데모+AC3상"
        }else if(myChargeType == 4){
            "DC콤보"
        }else if(myChargeType == 5){
            "DC차데모+DC콤보"
        }else if(myChargeType == 6){
            "DC차데모+AC3상+DC콤보"
        }else if(myChargeType == 7){
            "AC3상"
        }else{
            ""
        }

        updateCarInfoDialog= UpdateCarInfoDialog(this, binding)

        binding.btnCarInfo2Rewrite.setOnClickListener {
            updateCarInfoDialog.showDialog()
        }

        val requestGalleryLauncher =registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
            try{
                val calculateRadio = calculateInSampleSize(it.data!!.data!!,
                    resources.getDimensionPixelSize(com.example.electriccarchargestation.R.dimen.imageSize),resources.getDimensionPixelSize(com.example.electriccarchargestation.R.dimen.imageSize))
                val option = BitmapFactory.Options()

                option.inSampleSize= calculateRadio

                var inputStream = contentResolver.openInputStream(it.data!!.data!!)
                val bitmap = BitmapFactory.decodeStream(inputStream,null,option)

                BitmapData.bitmap = bitmap
                BitmapData.flag = true

                inputStream!!.close()
                inputStream = null

                if(bitmap!=null){
                    binding.ivCarInfo2Photo.setImageBitmap(bitmap)
                }else{
                    Log.d("kin", "비트맵 오류")
                }

            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        binding.ivCarInfo2Photo.setOnClickListener {
            var intent=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type="image/*"
            requestGalleryLauncher.launch(intent)
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