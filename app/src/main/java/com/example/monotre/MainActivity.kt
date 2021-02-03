package com.example.monotre

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.RemoteException
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item.*
import org.altbeacon.beacon.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), BeaconConsumer, SingleDialogFragment.NoticeDialogListener {

    companion object{
        const val PERMISSIONS_REQUEST_CODE = 1000
        const val BLUETOOTH_REQUEST_CODE = 10
    }
    private val bluetoothAdapter: BluetoothAdapter by lazy{
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if(adapter == null){
            showErrorToast(R.string.bluetooth_is_not_supported)
            finish()
        }
        adapter
    }

    private fun requestBluetoothFeature(){
        if(bluetoothAdapter.isEnabled)
            return

        val enableBluetoothIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBluetoothIntent, BLUETOOTH_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            BLUETOOTH_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    showErrorToast(R.string.bluetooth_is_not_working)
                    finish()
                    return
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showErrorToast(messageId: Int){
        Toast.makeText(this, messageId, Toast.LENGTH_LONG)
             .show()
    }

    private lateinit var beaconManager: BeaconManager
    private var isScanning = false
    
    private var inputItemName : String = ""
    private var inputUUID     : String = ""
    private var inputMajor    : String = ""
    private var inputMinor    : String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppCompat_DayNight)
        setContentView(R.layout.activity_main)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            floatingActionButtonOnToast("登録したいデバイスを選択してください")
        }

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconUtil.IBEACON_FORMAT))

        checkPermission()
    }

        /**
         * ここから
         */
    
    private fun floatingActionButtonOnToast(str: String) {
        toastDisplay(str)
        SingleDialogFragment().show(supportFragmentManager, "missiles")
    }
    
        override fun toastDisplay(str: String) {
            Toast.makeText(applicationContext, str, Toast.LENGTH_LONG).show()
        }
    
        override fun onCreateOptionsMenu(menu: Menu): Boolean {
            // Inflate the menu; this adds items to the action bar if it is present.
            menuInflater.inflate(R.menu.menu_main, menu)
            return true
        }
    
        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            return when (item.itemId) {
                R.id.action_settings -> true
                else -> super.onOptionsItemSelected(item)
            }
        
        }
    
        override fun onDialogPositiveClick(dialog: DialogFragment) {
            Log.d("mainActivity", "onDialogPositiveClick")
            val dialogView: Dialog = dialog.dialog ?: run {
                Log.d("err", "dialogView is error")
                return
            }
        
            val itemNameEt = dialogView.findViewById(R.id.itemName) as EditText? ?: run {
                Log.d("err", "itemNameDialogView is error")
                return
            }
        
            val uuidEt = dialogView.findViewById(R.id.UUID) as EditText? ?: run {
                Log.d("err", "itemNameDialogView is error")
                return
            }
            Log.d("uuidEt", "UUID : ${uuidEt.text}")
        
            val majorEt = dialogView.findViewById(R.id.major) as EditText? ?: run {
                Log.d("err", "itemNameDialogView is error")
                return
            }
        
            val minorEt = dialogView.findViewById(R.id.minor) as EditText? ?: run {
                Log.d("err", "itemNameDialogView is error")
                return
            }
        
            inputItemName = itemNameEt.text.toString()
            inputUUID     = uuidEt.text.toString()
            inputMajor    = majorEt.text.toString()
            inputMinor    = minorEt.text.toString()
        
            Log.d("normally", " itemName: $inputItemName UUID: $inputUUID " +
                    "major: $inputMajor minor: $inputMinor")
            
//            nzn氏にpass
            beaconManager.bind(this)
        }
    
        override fun onDialogNegativeClick(dialog: DialogFragment) {
            Log.d("mainActivity", "onDialogNegativeClick")
        }
    
    private fun checkPermission(){
        if((ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)   != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE)
        }
    }


    fun externalStoragePath(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }


    // Start Service.
    override fun onResume(){
        super.onResume()
        requestBluetoothFeature()
        isScanning = true
    }
    
    // Service termination.
    override fun onPause(){
        super.onPause()
        beaconManager.unbind(this)
    }
    
    override fun onBeaconServiceConnect(){

        beaconManager.addMonitorNotifier(object : MonitorNotifier {
    
            override fun didEnterRegion(region: Region) {
                Log.d("iBeacon", "Enter Region")
                beaconManager.startRangingBeaconsInRegion(region)
            }
    
            override fun didExitRegion(region: Region) {
                beaconManager.stopRangingBeaconsInRegion(region)
            }
    
            override fun didDetermineStateForRegion(i: Int, region: Region) {
                Log.d("MainActivity", "Determine State $i")
            }
        })

        // 表示用データクラス
        data class BeaconDetail(val id1: Identifier, val id2: Identifier, val id3: Identifier, val distance: Double){
        }

        val beaconDetailsMap = mutableMapOf<String, String>()
        var distanceFirst = ""
        beaconManager.addRangeNotifier { beacons, region ->
            if(beacons.count() > 0){
                beacons
                        .map{
                            val beaconDetail = BeaconDetail(it.id1, it.id2, it.id3, it.distance).toString().replace(it.id1.toString(), "")
                            // ↑ .replaceでUUIDを削除
                            distanceFirst = it.distance.toString()
                            beaconDetailsMap += it.id1.toString() to beaconDetail
                            it
                        }
                        .map { "UUID:" + it.id1 + " major:" + it.id2 + " minor:" + it.id3 + " RSSI:" + it.rssi + " Distance:" + it.distance + " txPower" + it.txPower }
                        .forEach { Log.d("iBeacon", it)}
                Log.d("iBeacon", "beacon available")
    
                /**
                 * ここから
                 */
//                val listView: ListView = findViewById(R.id.listView1)
                val listItems = mutableListOf<ListItem>()
                
                val bmp: Bitmap? = BitmapFactory
                        .decodeResource(resources, R.mipmap.key)
                Log.d("distanceFirst", distanceFirst)


                val item = ListItem.builder(resources)
                        .build(bmp, inputItemName, distanceFirst)
                //test
                listItems.add(item)
                
                val adapter = ListAdapter(this, R.layout.list_item, listItems)
                listView1.adapter = adapter
    
                Log.d("normally", "listView")
                
            }else{
                Log.d("iBeacon", "No beacon available")
            }
        }
    
        Log.d("inputId : ", "inputItemName : $inputItemName, inputUUID : $inputUUID,  inputMajor : $inputMajor, inputMinor : $inputMinor")
        val idUUID:Identifier  = Identifier.parse(inputUUID)
        val idMajor:Identifier = Identifier.parse(inputMajor)
        val idMinor:Identifier = Identifier.parse(inputMinor)
        val mRegion: Region    = Region(inputItemName, idUUID, idMajor, idMinor)
        
        try {
            Log.d("Debug", "Start Monitoring.")
            beaconManager.startMonitoringBeaconsInRegion(mRegion)

        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }
    
}

