package com.example.electriccarchargestation.DB

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.electriccarchargestation.TBL_DATA_CLASS.CarInfo
import com.example.electriccarchargestation.TBL_DATA_CLASS.PublicData
import java.io.IOException

class DBHelper(context: Context):
    SQLiteOpenHelper(context, DB_NAME, null, DB_VER){

    private companion object{
        const val DB_VER  = 1
        const val DB_NAME = "charge_stationDB"
        const val PUBLIC_DATA_TABLE_NAME = "publicdataTBL"
        const val CARINFO_TABLE_NAME = "carinfoTBL"
    }

    //테이블 생성
    override fun onCreate(db: SQLiteDatabase?) {
        val createDataQuery = """
            create table $PUBLIC_DATA_TABLE_NAME(
            id integer primary key autoincrement,
            stationId TEXT,
            chargeAmount TEXT,
            chargeType integer,
            parkingFree TEXT,
            stationName TEXT,
            address TEXT,
            latitude REAL,
            longtitude REAL,
            bookmark integer)
        """.trimIndent()

        val createCarInfoQuery = """
            create table $CARINFO_TABLE_NAME(
            id integer primary key autoincrement,
            carName TEXT,
            chargeType integer)
        """.trimIndent()

        db?.execSQL(createDataQuery)
        db?.execSQL(createCarInfoQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        val dropDataQuery = "drop table if exists $PUBLIC_DATA_TABLE_NAME"
        val dropCarInfoQuery = "drop table if exists $CARINFO_TABLE_NAME"
        db?.execSQL(dropDataQuery)
        db?.execSQL(dropCarInfoQuery)
    }

    //공공데이터(충전소 정보) 입력
    fun insertPublicData(publicData: PublicData): Boolean {
        var insertFlag = false
        val db = this.writableDatabase
        val insertQuery = """
            insert into $PUBLIC_DATA_TABLE_NAME(stationId, chargeAmount, chargeType, parkingFree, stationName, address, latitude, longtitude, bookmark)
            VALUES(
            '${publicData.stationId}',
            '${publicData.chargeAmount}',
            ${publicData.chargeType},
            '${publicData.parkingFree}',
            '${publicData.stationName}',
            '${publicData.address}',
            ${publicData.latitude},
            ${publicData.longtitude},
            ${publicData.bookmark})
        """.trimIndent()

        try {
            db.execSQL(insertQuery)
            insertFlag = true
        }catch (e: SQLiteException){
            Log.d("shin", "insertPublicData: ${e.printStackTrace()}")
        }finally {
            try {
                db.close()
            }catch (e: IOException){
                Log.d("shin", "insertPublicData: ${e.printStackTrace()}")
            }
        }

        return insertFlag
    }

    //차정보 입력
    fun insertCarInfo(carInfo: CarInfo): Boolean{
        var insertFlag = false
        val db = this.writableDatabase
        val insertQuery = """
            insert into $CARINFO_TABLE_NAME(
            carName,
            chargeType)
            VALUES(
            '${carInfo.carName}',
            ${carInfo.chargeType})
        """.trimIndent()

        try {
            db.execSQL(insertQuery)
            insertFlag = true
        }catch (e: SQLiteException){
            Log.d("jung", "insertCarInfo: ${e.printStackTrace()}")
        }finally {
            try {
                db.close()
            }catch (e: IOException){
                Log.d("jung", "insertCarInfo: ${e.printStackTrace()}")
            }
        }

        return insertFlag
    }

    //충전소 정보 전부 가져오기
    fun selectAllPublicData(): MutableList<PublicData>?{
        var publicDataList: MutableList<PublicData>? = mutableListOf<PublicData>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        val selectQuery = """
            select * from $PUBLIC_DATA_TABLE_NAME
        """.trimIndent()

        try {
            cursor = db.rawQuery(selectQuery, null)

            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getInt(0)
                    val stationId = cursor.getString(1)
                    val chargeAmount = cursor.getString(2)
                    val chargeType = cursor.getInt(3)
                    val parkingFree = cursor.getString(4)
                    val stationName = cursor.getString(5)
                    val address = cursor.getString(6)
                    val latitude = cursor.getFloat(7)
                    val longtitude = cursor.getFloat(8)
                    val bookmark = cursor.getInt(9)

                    val publicData = PublicData(
                        id,
                        stationId,
                        chargeAmount,
                        chargeType,
                        parkingFree,
                        stationName,
                        address,
                        latitude,
                        longtitude,
                        bookmark)

                    publicDataList?.add(publicData)
                }
            }
        }catch (e: Exception){
            Log.d("jung", "selectAllPublicData: ${e.printStackTrace()}")
        }finally {
            try {
                db.close()
            }catch (e: IOException){
                Log.d("jung", "selectAllPublicData: ${e.printStackTrace()}")
            }
        }

        return publicDataList
    }

    //차정보 가져오기
    fun selectCarInfo(): CarInfo?{
        var carInfo: CarInfo? = null
        val db = this.readableDatabase
        var cursor: Cursor? = null
        val selectQuery = """
            select * from $CARINFO_TABLE_NAME
        """.trimIndent()

        try {
            cursor = db.rawQuery(selectQuery, null)
            if (cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getInt(0)
                    val carName = cursor.getString(1)
                    val chargeType = cursor.getInt(2)

                    carInfo = CarInfo(id, carName, chargeType)
                }
            }
        }catch (e: Exception){
            Log.d("jung", "selectCarInfo: ${e.printStackTrace()}")
        }finally {
            try {
                db.close()
            }catch (e: IOException){
                Log.d("jung", "selectCarInfo: ${e.printStackTrace()}")
            }
        }

        return carInfo
    }

    //검색한 충전소 가져오기
    fun selectSearchPublicData( searchString: String?, selectChargeType: Int?): MutableList<PublicData>?{
        var publicDataList: MutableList<PublicData>? = mutableListOf<PublicData>()
        val db = this.readableDatabase

        var cursor: Cursor? = null

        val selectQuery = if(!searchString.isNullOrBlank() && selectChargeType == 0){
            """
            select * from $PUBLIC_DATA_TABLE_NAME 
            where stationName like '%$searchString%' or address like '%$searchString%'
            """.trimIndent()
        }else if(!searchString.isNullOrBlank() && selectChargeType != 0){
            """
            select * from $PUBLIC_DATA_TABLE_NAME 
            where chargeType = '$selectChargeType' and (stationName like '%$searchString%' or address like '%$searchString%')
            """.trimIndent()
        }else if(searchString.isNullOrBlank() && selectChargeType == 0){
            """
            select * from $PUBLIC_DATA_TABLE_NAME 
            """.trimIndent()
        }else{
            """
            select * from $PUBLIC_DATA_TABLE_NAME
            where chargeType = '$selectChargeType'
            """.trimIndent()
        }

        try {
            cursor = db.rawQuery(selectQuery, null)

            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getInt(0)
                    val stationId = cursor.getString(1)
                    val chargeAmount = cursor.getString(2)
                    val chargeType = cursor.getInt(3)
                    val parkingFree = cursor.getString(4)
                    val stationName = cursor.getString(5)
                    val address = cursor.getString(6)
                    val latitude = cursor.getFloat(7)
                    val longtitude = cursor.getFloat(8)
                    val bookmark = cursor.getInt(9)

                    val publicData = PublicData(
                        id,
                        stationId,
                        chargeAmount,
                        chargeType,
                        parkingFree,
                        stationName,
                        address,
                        latitude,
                        longtitude,
                        bookmark)

                    publicDataList?.add(publicData)
                }
            }
        }catch (e: Exception){
            Log.d("jung", "selectSearchPublicData: ${e.printStackTrace()}")
        }finally {
            try {
                db.close()
            }catch (e: IOException){
                Log.d("jung", "selectSearchPublicData: ${e.printStackTrace()}")
            }
        }

        return publicDataList
    }

    //즐겨찾기 수정
    fun updateBookmark(publicData: PublicData): Boolean{
        var updateFlag = false
        val db = this.writableDatabase
        val updateQuery = """
            update $PUBLIC_DATA_TABLE_NAME set bookmark = ${publicData.bookmark}
            where id = ${publicData.id}
        """.trimIndent()

        try {
            db.execSQL(updateQuery)
            updateFlag = true
        }catch (e: SQLiteException){
            Log.d("jung", "updateBookmark: ${e.printStackTrace()}")
        }finally {
            try {
                db.close()
            }catch (e: IOException){
                Log.d("jung", "updateBookmark: ${e.printStackTrace()}")
            }
        }

        return updateFlag
    }

    //즐겨찾기 가져오기
    fun selectBookmarkPublicData(): MutableList<PublicData>?{
        var publicDataList: MutableList<PublicData>? = mutableListOf<PublicData>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        val selectQuery = """
            select * from $PUBLIC_DATA_TABLE_NAME where bookmark = 1
        """.trimIndent()

        try {
            cursor = db.rawQuery(selectQuery, null)

            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getInt(0)
                    val stationId = cursor.getString(1)
                    val chargeAmount = cursor.getString(2)
                    val chargeType = cursor.getInt(3)
                    val parkingFree = cursor.getString(4)
                    val stationName = cursor.getString(5)
                    val address = cursor.getString(6)
                    val latitude = cursor.getFloat(7)
                    val longtitude = cursor.getFloat(8)
                    val bookmark = cursor.getInt(9)

                    val publicData = PublicData(
                        id,
                        stationId,
                        chargeAmount,
                        chargeType,
                        parkingFree,
                        stationName,
                        address,
                        latitude,
                        longtitude,
                        bookmark)

                    publicDataList?.add(publicData)
                }
            }
        }catch (e: Exception){
            Log.d("jung", "selectBookmarkPublicData: ${e.printStackTrace()}")
        }finally {
            try {
                db.close()
            }catch (e: IOException){
                Log.d("jung", "selectBookmarkPublicData: ${e.printStackTrace()}")
            }
        }

        return publicDataList
    }

    //차정보 수정
    fun updateCarInfo(carInfo: CarInfo): Boolean{
        var updateFlag = false
        val db = this.writableDatabase
        val updateQuery = """
            update $CARINFO_TABLE_NAME set carName = ${carInfo.carName}, chargeType = ${carInfo.chargeType}
            where id = ${carInfo.id}
        """.trimIndent()

        try {
            db.execSQL(updateQuery)
            updateFlag = true
        }catch (e: SQLiteException){
            Log.d("jung", "updateCarInfo: ${e.printStackTrace()}")
        }finally {
            try {
                db.close()
            }catch (e: IOException){
                Log.d("jung", "updateCarInfo: ${e.printStackTrace()}")
            }
        }

        return updateFlag
    }

    //클릭한 마커의 위치로 검색된 충전소 정보 가져오기
    fun selectLocationPublicData(latitude: Float, longtitude: Float): PublicData?{
        var publicData: PublicData? = null
        val db = this.readableDatabase
        var cursor: Cursor? = null

        val selectQuery = """
            select * from $PUBLIC_DATA_TABLE_NAME
            where latitude = '$latitude' and longtitude = '$longtitude'
        """.trimIndent()

        try {
            cursor = db.rawQuery(selectQuery, null)

            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getInt(0)
                    val stationId = cursor.getString(1)
                    val chargeAmount = cursor.getString(2)
                    val chargeType = cursor.getInt(3)
                    val parkingFree = cursor.getString(4)
                    val stationName = cursor.getString(5)
                    val address = cursor.getString(6)
                    val latitude = cursor.getFloat(7)
                    val longtitude = cursor.getFloat(8)
                    val bookmark = cursor.getInt(9)

                    publicData = PublicData(
                        id,
                        stationId,
                        chargeAmount,
                        chargeType,
                        parkingFree,
                        stationName,
                        address,
                        latitude,
                        longtitude,
                        bookmark)
                }
            }
        }catch (e: SQLiteException){
            Log.d("jung", "selectLocationPublicData: ${e.printStackTrace()}")
        }finally {
            try {
                db.close()
            }catch (e: IOException){
                Log.d("jung", "selectLocationPublicData: ${e.printStackTrace()}")
            }
        }

        return publicData
    }
}