package com.example.electriccarchargestation.TBL_DATA_CLASS

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

/*******************************************************

 *  2022/06/22 ~ 23
 *  sha1028en 박동수

 *  속도가 중요하지 않은 상황이거나( 처리 갯수가 얼마 안돼는)
 *  다른 파일간 데이터를 주고 받아야 할 경우( 액티비티간 mutableList 전달)

 *  이 객체는 처리 속도가 매우 절망적으로 느리다... 명심하자

__테스트 코드 사용 예시__

 * val items    = PublicDataItemParcel.getExampleItems()
 * val item     = items[ 5],
 * val statId   = items[ i].stationId
 * this.binding.tvMainAddress.text = items[ position].address
 * items[ position].chargeType = "어쩌구 저쩌구..." <- 안됌. 읽는건 O / 수정은 X

 *  val buf: ArrayList< Parcelable> = this.items as ArrayList<Parcelable>
val intent  = Intent( this, MainActivity::class.java)
intent.putExtra("items", buf)
. . .
val items = intent.getParcelableArrayListExtra< PublicDataItemParcel>("items") as MutableList< PublicDataItemParcel>



 * 사용전에 1번줄. 패키지명 변경하고 쓰시오...
 ********************************************************/


@Parcelize
data class PublicDataParcel(
    val     stationId:      String,     // 충전소 ID
    val     chargerAmount:  String,     // 충전량 ( 7, 200, 공백...) 단위는 kw/h
    val     chargeType:     Int,        // 충전 타입( 0: 전부 보기)
    val     parkingFree:    String,     // 주차요금 무료 유무( T / F )
    val     stationName:    String,     // 충전소 이름
    val     address:        String,     // 주소
    val     latitude:       Float,      // 위도
    val     longtitude:     Float,      // 경도
    val     bookmark:       Int         // 북마크 ( 1: 북마크 됌, 이외의 값: 북마크 아님)

): Parcelable {
    companion object: Parceler<PublicDataParcel> {

        // 기능 테스트용 코드 입니다! 특히 리사이클러뷰 담당 하시는 분들을 위한!
        // 리사이클러 뷰에다 실제로 값을 넣으시고 싶으신 분들은 사용해 뵤시겠어요?
        fun getExampleMutableList(): MutableList< PublicDataParcel>  {
            val items = mutableListOf< PublicDataParcel>()

            for ( i in 0..10) {
                items.add( PublicDataParcel(
                    stationId       = "EC00003$i",
                    chargerAmount   = "200",
                    chargeType      = ( i % 8 ),
                    parkingFree     = if( i % 2 == 0 ) "T" else "F",
                    stationName     = "행복한 윤수님$i,",
                    address         = "서울시 관악구 남부순환로 2$i 길-20",
                    latitude        = 200.400f,
                    longtitude      = 523.799f,
                    bookmark        = 523
                ))
            }

            return items
        }



        override fun create(parcel: Parcel): PublicDataParcel = PublicDataParcel( parcel)

        override fun PublicDataParcel.write(parcel: Parcel, flags: Int) {
            parcel.writeString( this.stationId)
            parcel.writeString( this.chargerAmount)
            parcel.writeInt( this.chargeType)
            parcel.writeString( this.parkingFree)
            parcel.writeString( this.stationName)
            parcel.writeString( this.address)
            parcel.writeFloat( this.latitude)
            parcel.writeFloat( this.longtitude)
            parcel.writeInt( this.bookmark)
        }
    }

    constructor( parcel: Parcel): this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt())
}