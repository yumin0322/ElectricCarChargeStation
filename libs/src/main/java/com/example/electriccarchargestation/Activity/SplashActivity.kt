package com.example.electriccarchargestation.Activity

import com.example.electriccarchargestation.DB.DBHelper
import com.example.electriccarchargestation.databinding.ActivitySplashBinding
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.electriccarchargestation.BuildConfig
import com.example.electriccarchargestation.Dialog.SplashProgressDialog
import com.example.electriccarchargestation.TBL_DATA_CLASS.PublicData
import kotlinx.coroutines.*
import org.json.JSONObject
import org.json.XML
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/******************************************************************
 *
 *  2022/06/21 ~ 28
 *  sha1028en 박동수
 *
 * SplashActivity
 *
 ********************************************************************

 *  첫 실행시 보여지는 액티비티로, DB 초기화와 다음 액티비티 흐름을 정의한다.

 *  처음 실행시 DB에 아무것도 없다면... initPublicData를 실행하고
 *  그 다음에 DB에 받아온 값들을 INSERT한다.( 서울시 내 전기차 정보 9999개 )
 *  그리고 CarInfoActivity로 간다.

 *  DB에 뭐가 있다면... 2초뒤에 MainActivity로 간다.

 *******************************************************************

 *  2022/06/28

 *  실제 진행을 표시하는 프로그레스 바 추가...
비동기 작업과 동기화돼어 작업이 진행됄수로
프로그레스 바도 차오른다.

 *  Lottie 외부 의존성을 추가.
SplashActivity에 애니메아션 아이콘 추가함.

 **********************************************************************

 * 요구 사항

 *  local.properties에 URL, TOKEN 기재.
 *  모듈단위 build.gradle 최상단에 프로퍼티스 초기화
 *  모듈단위 build.gradle defaultConfig 에 buildConfigField 추가( URL, TOKEN)
( 여기까지 모르겠으면 각각 파일 찾아가서 내용 복붙하셔요잉? )

 * 모듈단위 build.gradle 플러그인에       id 'kotlin-parcelize'추가.
 * 모듈단위 build.gradle 외부 종속성에    implementation "com.airbnb.android:lottie:3.6.1"
 * 모듈단위 build.gradle 외부 종속성에    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2'
 * ( HttpUrlConnection은 비동기 통신으로 무조건 백그라운드에서 작업 해야만 한다. )

 * manifest.xml에                               <uses-permission android:name="android.permission.INTERNET"/>
 * manifest.xml의 application 에                 android:usesCleartextTraffic="true"
 * 외부 라이브러리에( 프로젝트 -> app -> libs)      java-json.jar( xmlToJson)

 * 사용전에 1번줄. 패키지명 변경하고 쓰시오...
 ********************************************************************/


class SplashActivity :
    AppCompatActivity() {
    private val bind by lazy { ActivitySplashBinding.inflate( this.layoutInflater) }
    private val progressDialog by lazy { SplashProgressDialog( this) }
    private val dbHelper = DBHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate( savedInstanceState)
        setContentView( this.bind.root)

        this.initLottieAnime()

        //when DB.publicdataTBL is EMPTY? insert values into DB.publicdataTBL. than goto CarInfoActivity
        if( this.dbHelper.selectAllPublicData()?.size ?: 0 > 0) { this.pauseAfterGotoMainActivity( 2000) }

        //when DB.publicdataTBL isnt EMPTY?( ALREADY HAS ) pause and then goto MainActivity
        else {
            this.initPublicData( 1000)
            this.progressDialog.show()
        }
    }

    // wait and go MainActivity
    private fun pauseAfterGotoMainActivity(milliSec: Int) {
        val handler = Handler()
        handler.postDelayed({ runOnUiThread{ this.gotoMainActivity() } }, milliSec.toLong())
    }

    // GOTO MainActivity
    private fun gotoMainActivity() {
        val intent = Intent( this, MainActivity::class.java)
        this.startActivity( intent)
        this.finish()
    }

    // GOTO CarInfoActivity
    private fun gotoCarInfoActivity() {
        val intent  = Intent( this, InsertCarInfoActivity::class.java)
        this.startActivity( intent)
        this.finish()
    }


    // init lottieSplashAnime
    private fun initLottieAnime() { this.bind.lottieSplashAnime.playAnimation() }


    // insert Public Data into DB and goto MainActivity
    private fun initPublicData( amount: Int): MutableList<PublicData> {
        var items = mutableListOf<PublicData>()

        // for Exception on Coroutine ASYNC Task.
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            this@SplashActivity.progressDialog.dismiss()
            this@SplashActivity.dbHelper.close()
            Log.e("SHA1028EN", "ASYNC TASK EXCEPTION $throwable")
            runOnUiThread{ Toast.makeText( this@SplashActivity, "CAN NOT REQUEST DATA...", Toast.LENGTH_SHORT).show() }
        }

        // ASYNC TASK
        val coroutineScope = CoroutineScope( Dispatchers.IO)
        coroutineScope.launch ( exceptionHandler)  {

            // Request Public data...
            val task = async  ( exceptionHandler)  { this@SplashActivity.setPublicData( amount) }

            // wait for ASYNC TASK ENDING...( Receive from Public Data)
            task.await()
            items = task.getCompleted()

            //TODO Receive Public Data
            if( items.size > 0) {

                // init Progress Bar
                val progressRear = items.size
                runOnUiThread { this@SplashActivity.progressDialog.initProgress( 0, progressRear) }

                //insert value into DB.publicdataTLB
                for( i in 0 until items.size) {
                    this@SplashActivity.dbHelper.insertPublicData( items[ i])
                    this@SplashActivity.progressDialog.setCurrentProgress( i)
                }
            } else { runOnUiThread{ Toast.makeText(this@SplashActivity,"Cant read Data...", Toast.LENGTH_SHORT).show() } }

            // release Resources
            task.cancel()
            exceptionHandler.cancel()
            coroutineScope.cancel()

            // TASK Finish? GOTO CarInfoActivity
            runOnUiThread{
                this@SplashActivity.progressDialog.dismiss()
                this@SplashActivity.gotoCarInfoActivity()
            }
        } // endl ASYNC TASK

        return items
    }

    // connect HTTP : GET
    private suspend fun setPublicData(amount: Int) = withContext( Dispatchers.IO) {
        val items = mutableListOf< PublicData>()

        // Set URL
        val urlBuilder = StringBuilder( BuildConfig.PUBLICDATA_URL)

        // for Svc Key
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + BuildConfig.PUBLICDATA_KEY)

        // Page
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"))

        // Page Per Amount( 10 ~ 9999)
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("$amount", "UTF-8"))

        // relate Per Min( 1 ~ 10)
        urlBuilder.append("&" + URLEncoder.encode("period", "UTF-8") + "=" + URLEncoder.encode("5", "UTF-8"))

        // region code( nn)
        urlBuilder.append("&" + URLEncoder.encode("zcode", "UTF-8") + "=" + URLEncoder.encode("11", "UTF-8"))

        // Charger Name
        // urlBuilder.append("&" + URLEncoder.encode("statNm", "UTF-8") + "=" + URLEncoder.encode("*", "UTF-8"))

        val url = URL( urlBuilder.toString())
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod  = "GET"
        conn.setRequestProperty("Content-type", "application/json")


        val rd: BufferedReader = if ( conn.responseCode in 200..300)    { BufferedReader(InputStreamReader( conn.inputStream))}
        else                                                            { BufferedReader(InputStreamReader( conn.errorStream)) }

        var line: String?
        while ( rd.readLine().also { line = it } != null) {

            var jsonObject = XML.toJSONObject( line.toString())
            jsonObject = jsonObject.getJSONObject("response")
            jsonObject = jsonObject.getJSONObject("body")
            jsonObject = jsonObject.getJSONObject("items")
            val buf = jsonObject.getJSONArray("item")

            for( i in 0 until buf.length())   {

                var tmp = buf[i] as JSONObject
                items.add( PublicData(
                    id              = i,
                    stationId       = tmp.getString("statId"),
                    chargeAmount   = tmp.getString("output"),
                    chargeType      = tmp.getInt("chgerType"),
                    parkingFree     = tmp.getString("parkingFree"),
                    stationName     = tmp.getString("statNm"),
                    address         = tmp.getString( "addr"),
                    latitude        = tmp.getDouble("lat").toFloat(),
                    longtitude      = tmp.getDouble("lng").toFloat(),
                    bookmark        = 523
                ))
            }
        }
        rd.close()
        conn.disconnect()
        items
    }
}