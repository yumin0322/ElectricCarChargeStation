package com.example.electriccarchargestation.Dialog

import com.example.electriccarchargestation.databinding.DialogSplashProgressBinding
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle

/*******************************
 *
 *  2022/06/23, 28
 *  sha1028en 박동수
 *
 *****************************************************

 *  공공데이터를 받고 DB에 때려박는 시간이 예상을 초월하여
급조됀 다이얼로그.

 *  SplashActivity가 작업을 진행하는 동안, 다이얼로그가
화면을 붇들고 있는다( 40 ~ 50초? ).

 *  SplashActivity가 작업을 종료하면 해당 다이얼로그는
화면에서 사라진다...

 *  SplashActivity에서 비동기 작업 진행과 맟춰( 동기화)
pgSplashDialogCurrentProgress의 진행도를 갱신한다.

 *****************************************************/

class SplashProgressDialog(context: Context): Dialog( context) {
    private val bind by lazy { DialogSplashProgressBinding.inflate( this.layoutInflater) }

    override fun onCreate( savedInstanceState: Bundle?) {
        super.onCreate( savedInstanceState)
        this.setContentView( this.bind.root)
        this.setCanceledOnTouchOutside( false)
    }

    fun initProgress( progressFront: Int = 0 ,progressRear: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { this.bind.pgSplashDialogCurrentProgress.min = progressFront }
        this.bind.pgSplashDialogCurrentProgress.max = progressRear
    }

    fun setCurrentProgress( currentPos: Int) { this.bind.pgSplashDialogCurrentProgress.progress = currentPos }
}