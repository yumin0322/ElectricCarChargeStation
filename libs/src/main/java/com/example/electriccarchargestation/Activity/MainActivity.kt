package com.example.electriccarchargestation.Activity

import com.example.electriccarchargestation.R
import com.example.electriccarchargestation.databinding.ActivityMainBinding

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.electriccarchargestation.DB.DBHelper
import com.example.electriccarchargestation.TBL_DATA_CLASS.PublicData
import com.example.electriccarchargestation.TBL_DATA_CLASS.PublicDataParcel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

/*1. GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback 작성 */
class MainActivity : AppCompatActivity(),GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback {
    private lateinit var binding: ActivityMainBinding
    //위치 정보를 획득 객체참조 변수
    private lateinit var providerClient: FusedLocationProviderClient
    //위치정보를 획득하기 위한 접속
    private lateinit var apiClient: GoogleApiClient
    //지도 객체정보
    private var googleMap: GoogleMap? = null

    private var FABMainStatus = false

    private var locationPermissionGranted = false

    private var lastKnownLocation: Location? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient

    private var getPublicData: PublicData? = null

    private var publicDataLlist: MutableList<PublicData>? = null

    private val dbHelper = DBHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        getPublicData = intent.getSerializableExtra("selectedPublicData") as? PublicData
        Log.d("shin", "${getPublicData}")

        publicDataLlist = dbHelper.selectAllPublicData()

        Places.initialize(applicationContext, "AIzaSyCYiqKCy_1DooOnB-i0cDhNk5dUjQOyOeE")
        placesClient = Places.createClient(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        binding.btnSearch.setOnClickListener {
            val intent = Intent(this@MainActivity, SearchActivity::class.java)
            startActivity(intent)
        }

        binding.FABCarInfo.setOnClickListener{
            val intent = Intent(this@MainActivity, CarInfoActivity::class.java)
            startActivity(intent)
        }

        binding.FABBookmark.setOnClickListener {
            val intent = Intent(this@MainActivity, BookmarkActivity::class.java)
            startActivity(intent)
        }
        binding.FABMain.setOnClickListener {
            toggle()
        }


        //2. 퍼미션 요청 정보를 콜백함수
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
            //요청했던 퍼미션이 모두 true가 되었는지 안되었는지 점검
            if(it.all { permission -> permission.value == true }){
                //위치정보를 얻기위한 접속 요청
                apiClient.connect()
            }else{
                Toast.makeText(this,"권한 거부로 앱 사용불가합니다.", Toast.LENGTH_SHORT).show()
            }
        }//퍼미션 요청

        //3. 화면에서 fragment view 찾아서 거기에 지도를 맵핑하도록 동기화처리
        (supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment).getMapAsync(this)

        //4. 위치정보를 획득
        providerClient = LocationServices.getFusedLocationProviderClient(this)

        //5. 위치정보를 획득하기 위한 접속
        apiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API) // API LocationServices.API
            .addConnectionCallbacks(this) // ConnectionCallbacks 현재 클래스있음.
            .addOnConnectionFailedListener(this) // OnConnectionFailedListener 현재 클래스있음.
            .build()

        //6. 퍼미션 요청 정보
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) !== PackageManager.PERMISSION_GRANTED){

            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE))
        }else{
            //위치정보를 얻기위한 접속 요청
            apiClient.connect()
            locationPermissionGranted = true
        }

    }

    //플로팅액션버튼 애니메이션
    private fun toggle() {
        if(FABMainStatus) {
            // 플로팅 액션 버튼 닫기
            // 애니메이션 추가
            val FABBadGoAnimation = ObjectAnimator.ofFloat(binding.FABBookmark, "translationY", 0f)
            FABBadGoAnimation.start()
            val FABGoodAnimation = ObjectAnimator.ofFloat(binding.FABCarInfo, "translationY", 0f)
            FABGoodAnimation.start()
            // 메인 플로팅 이미지 변경
            binding.FABMain.setImageResource(R.drawable.next_24)
        }else {
            // 플로팅 액션 버튼 열기
            val FABBadGoAnimation = ObjectAnimator.ofFloat(binding.FABBookmark, "translationY", -200f)
            FABBadGoAnimation.start()
            val FABGoodAnimation = ObjectAnimator.ofFloat(binding.FABCarInfo, "translationY", -350f)
            FABGoodAnimation.start()
            // 메인 플로팅 이미지 변경
            binding.FABMain.setImageResource(R.drawable.less_24)
        }
        // 플로팅 버튼 상태 변경
        FABMainStatus = !FABMainStatus
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

/*
           googleMap?.setOnMapLongClickListener(object : GoogleMap.OnMapLongClickListener {
                override fun onMapLongClick(latLng: LatLng?) {
                    val markerOptions = MarkerOptions()
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                    markerOptions.position(latLng!!)

                    googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                    googleMap?.addMarker(markerOptions)

                }
            })
            googleMap?.setOnMarkerClickListener(object :GoogleMap.OnMarkerClickListener{
                override fun onMarkerClick(marker: Marker?): Boolean {
                    marker?.remove()
                    return true
                }
            })
*/

        updateLocationUI()

        Log.d("shin","${getPublicData}")
        if(getPublicData != null){
            val latLng = LatLng(getPublicData!!.latitude!!.toDouble(), getPublicData!!.longtitude!!.toDouble())
            moveMap(latLng)
            getPublicData = null
            Log.d("shin","${getPublicData}")
        }else{
            getDeviceLocation()
        }
    }

    //Location Provider: 위치 제공자
    //Location Provider 명령을 내렸을때 준비가 가능한 상태가 되면 불러지는 콜백함수(위치정보를 얻을수있음)
    override fun onConnected(p0: Bundle?) {
        /*//퍼미션을 물어보야됨 (개인정보 요청)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            providerClient.lastLocation.addOnSuccessListener(this@MainActivity, object:
                OnSuccessListener<Location> {
                override fun onSuccess(location: Location?) {
                    location?.let {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        //지도맵에 (위도, 경도)
                        moveMap(LatLng(latitude,longitude))
                    }
                }
            })
        }// end of if


        apiClient.disconnect()*/

        var latLng: LatLng

        publicDataLlist.let {
            for(publicData in publicDataLlist!!){
//                val publicData = publicDataLlist?.get(i)

                val name = publicData?.stationName
                val address = publicData?.address
                latLng = LatLng(publicData?.latitude!!.toDouble(), publicData?.longtitude!!.toDouble())
                marker(latLng ,name, address)
            }
        }

    }

    //요청 지연
    override fun onConnectionSuspended(p0: Int) {
        Log.d("shin", "ConnectionSuspended = ${p0}")
    }
    //요청 실패
    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d("shin", "ConnectionFailed = ${p0}")
    }

    //지도맵 객체를 매개변수로 전달하는 콜백함수
//    override fun onMapReady(googleMap: GoogleMap?) {
//        this.googleMap = googleMap
//       getDeviceLocation()
//    }

    private fun moveMap(latLng: LatLng){
        /*//위도와 경도를 함께하는 객체 생성
        val latLng = LatLng(latitude, longitude)*/

        //카메라 위치
        var position: CameraPosition
        position = CameraPosition.builder()
            .target(latLng)
            .zoom(15f)
            .build()

        //지도 에다가 카메라를 배치함.
        googleMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(position))
    }

    //
    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (googleMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                googleMap?.isMyLocationEnabled = true
                googleMap?.uiSettings?.isMyLocationButtonEnabled = true

            } else {
                googleMap?.isMyLocationEnabled = false
                googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    //현재 위치 함수
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device
                        val currentLat = task.result.latitude
                        val currentLng = task.result.longitude
                        val newLocation = LatLng(currentLat, currentLng)
                        val position = CameraPosition.builder()
                            .target(newLocation)
                            .zoom(15f)
                            .build()
                        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.d("jung", "${e.printStackTrace()}")
        }
    }
    fun marker(latLng: LatLng, stationName: String? = null, address: String? = null){
        //우리팀에 가지고 있는 마크업으로 표시하고자 할때
        val bitmapDrawable: BitmapDrawable
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            bitmapDrawable = getDrawable(R.drawable.location_marker) as BitmapDrawable
        }else{
            bitmapDrawable = resources.getDrawable(R.drawable.marker) as BitmapDrawable
        }
        val scaleBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 100, 100, false)
        val descriptor = BitmapDescriptorFactory.fromBitmap(scaleBitmap)
        //마크업 표시
        val markerOptions = MarkerOptions()

//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        markerOptions.icon(descriptor)
        markerOptions.position(latLng)

        //마크업 풍선도움말
        markerOptions.title(stationName)
        markerOptions.snippet(address)

        googleMap?.addMarker(markerOptions)
    }

}