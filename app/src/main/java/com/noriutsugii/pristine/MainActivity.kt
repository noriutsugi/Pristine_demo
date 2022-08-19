package com.noriutsugii.pristine

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.time.Duration
import java.util.*


class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT:Int = 1
    val PRICE_PER_HOUR:Float = 100F

    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var context: Context
    lateinit var textConnectionStatus: TextView
    lateinit var textStatus: TextView
    lateinit var textAmtTot: TextView
    lateinit var textDuration: TextView

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_main)
        context = this

        textAmtTot = findViewById(R.id.textAmtTot)
        textDuration = findViewById(R.id.textDuration)
        textConnectionStatus = findViewById(R.id.textConnectionStatus)
        textStatus = findViewById(R.id.textStatus)
        val btnRecommended = findViewById<Button>(R.id.btnRecommended)
        val btnCustom = findViewById<Button>(R.id.btnCustom)
        val btnTurnOn = findViewById<Button>(R.id.btnTurnOn)
        val btnDisconnect = findViewById<Button>(R.id.btnDisconnect)
        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val btn15 = findViewById<Button>(R.id.btn15)
        val btn30 = findViewById<Button>(R.id.btn30)
        val btn60 = findViewById<Button>(R.id.btn60)
        val btnPay = findViewById<Button>(R.id.btnPay)
        val btnDebug = findViewById<Button>(R.id.btnDebug)
        val btnAppProc = findViewById<Button>(R.id.btnAppProc)
        val btnUPIID = findViewById<Button>(R.id.btnUPIID)
        val customDur = findViewById<SeekBar>(R.id.customDur)
        val layButton = findViewById<ConstraintLayout>(R.id.layButtonTime)
        val laySeek = findViewById<ConstraintLayout>(R.id.laySeekTime)
        val layAppProc = findViewById<ConstraintLayout>(R.id.layAppProc)
        val layUPIID = findViewById<ConstraintLayout>(R.id.layUPIID)
        val layDebug = findViewById<ConstraintLayout>(R.id.layDebug)

        fun viewTurnOn(bool: Boolean){
            if (bool == true) {
                btnTurnOn.isClickable=true
                btnTurnOn.visibility= View.VISIBLE
                btnConnect.visibility= View.INVISIBLE
                btnDisconnect.visibility= View.INVISIBLE
            } else if (bool == false) {
                btnTurnOn.isClickable=false
                btnTurnOn.visibility= View.INVISIBLE
                btnConnect.visibility= View.INVISIBLE
                btnDisconnect.visibility= View.INVISIBLE
            }
        }

        fun viewDisconnect(bool: Boolean){
            if (bool == true) {
                btnDisconnect.isClickable=true
                btnDisconnect.visibility= View.VISIBLE
                btnConnect.visibility= View.INVISIBLE
                btnTurnOn.visibility= View.INVISIBLE

            } else if (bool == false) {
                btnTurnOn.visibility= View.INVISIBLE
                btnConnect.visibility= View.INVISIBLE
                btnDisconnect.visibility= View.INVISIBLE
            }
        }

        fun viewConnect(bool: Boolean){
            if (bool == true) {
                btnConnect.isClickable=true
                btnConnect.visibility= View.VISIBLE
                btnTurnOn.visibility= View.INVISIBLE
                btnDisconnect.visibility= View.INVISIBLE
            } else if (bool == false) {
                btnConnect.isClickable=false
                btnTurnOn.visibility= View.INVISIBLE
                btnConnect.visibility= View.INVISIBLE
                btnDisconnect.visibility= View.INVISIBLE
            }
        }

        viewConnect(false)
        viewDisconnect(false)
        viewTurnOn(false)

        layButton.visibility = View.VISIBLE
        laySeek.visibility = View.INVISIBLE
        layAppProc.visibility = View.VISIBLE
        layUPIID.visibility = View.INVISIBLE
        layDebug.visibility = View.INVISIBLE

        btnTurnOn?.setOnClickListener(){
            if (!bluetoothAdapter.isEnabled){
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                viewConnect(true)
            }
        }

        btnConnect?.setOnClickListener(){
            if (!bluetoothAdapter.isEnabled){
                viewTurnOn(true)
            } else {
                viewConnect(false)
                btDiscover(btPair())
            }
        }

        btnPay?.setOnClickListener(){
            val amtString: String = textAmtTot.text as String
            if (amtString.toFloat() > 0){
                var UPI = getUPIString("8176906776@okbizaxis", "Noriutsugiii", "7531", "", "1234", "pls", amtString, "INR", "")
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(UPI)
                val chooser = Intent.createChooser(intent, "Pay with...")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    startActivityForResult(chooser, 1, null)
                }
            }
        }

        btn15?.setOnClickListener(){
            var amtTot:Float = PRICE_PER_HOUR * (15F/60F)
            textAmtTot.text = String.format("%.2f", amtTot)
            textDuration.text = "15 minutes"
            customDur.setProgress(5)

        }

        btn30?.setOnClickListener(){
            var amtTot:Float = PRICE_PER_HOUR * (30F/60F)
            textAmtTot.text = String.format("%.2f", amtTot)
            textDuration.text = "30 minutes"
            customDur.setProgress(20)
        }

        btn60?.setOnClickListener(){
            var amtTot:Float = PRICE_PER_HOUR * (60F/60F)
            textAmtTot.text = String.format("%.2f", amtTot)
            textDuration.text = "60 minutes"
            customDur.setProgress(50)
        }

        btnRecommended?.setOnClickListener(){
            layButton.visibility = View.VISIBLE
            laySeek.visibility = View.INVISIBLE
        }
        btnCustom?.setOnClickListener(){
            layButton.visibility = View.INVISIBLE
            laySeek.visibility = View.VISIBLE
        }

        btnAppProc?.setOnClickListener(){
            layAppProc.visibility = View.VISIBLE
            layUPIID.visibility = View.INVISIBLE
            layDebug.visibility = View.INVISIBLE
        }
        btnUPIID?.setOnClickListener(){
            layAppProc.visibility = View.INVISIBLE
            layUPIID.visibility = View.VISIBLE
            layDebug.visibility = View.INVISIBLE
        }
        btnDebug?.setOnClickListener(){
            layAppProc.visibility = View.INVISIBLE
            layUPIID.visibility = View.INVISIBLE
            layDebug.visibility = View.VISIBLE
        }

        //seek?.setOnSeekBarChangeListener
        customDur.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Write code to perform some action when progress is changed.
                var dur:Int = customDur.progress + 10
                var amtTot:Float = PRICE_PER_HOUR * (dur.toFloat()/60F)
                textAmtTot.text = String.format("%.2f", amtTot)
                textDuration.text = dur.toString() + " minutes"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is started.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is stopped.

            }
        })

        if(btCheck()) {
            if (!bluetoothAdapter.isEnabled) {
                viewTurnOn(true)
            } else {
                viewConnect(true)
            }
        }
    }

    fun btCheck(): Boolean {
        val bluetoothManager = this.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothManager == null) {
            textConnectionStatus.text = "Device does not support bluetooth"
            return false
        } else {
            textConnectionStatus.text = "bluetooth is available"
            return true
        }
    }

    @SuppressLint("MissingPermission")
    fun btPair(): BluetoothDevice {
        textConnectionStatus.text = "Pairing device with washing machine"
        val devices = bluetoothAdapter.bondedDevices
        for (device in devices){
            if (device.address.lowercase() == "b8:27:eb:a5:c1:87"){
                textStatus.text = "Device already paired with washing machine"
                return device
            }
        }
        return bluetoothAdapter.getRemoteDevice("b8:27:eb:a5:c1:87".uppercase())
    }

    fun btDiscover(device: BluetoothDevice?) {
        textStatus.append("btDiscover")
        val requestCode = 1;
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        startActivityForResult(discoverableIntent, requestCode)
        val initConnection = ConnectThread(btPair())
        initConnection.start()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val btnTurnOn = findViewById<Button>(R.id.btnTurnOn)
        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val btnDisconnect = findViewById<Button>(R.id.btnDisconnect)
        val bluetoothManager = this.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        when(requestCode) {
            REQUEST_ENABLE_BT ->
                if (resultCode == Activity.RESULT_OK) {
                    btnConnect.isClickable=true
                    btnConnect.visibility= View.VISIBLE
                    btnTurnOn.visibility= View.INVISIBLE
                    btnDisconnect.visibility= View.INVISIBLE

                    textConnectionStatus.text = "bluetooth has been turned on"
                } else {
                    if (!bluetoothAdapter.isEnabled){
                        textConnectionStatus.text = "Unable to turn on bluetooth"
                        btnTurnOn.isClickable=true
                        btnTurnOn.visibility= View.VISIBLE
                        btnConnect.visibility= View.INVISIBLE
                        btnDisconnect.visibility= View.INVISIBLE
                    }
                    else {
                        textConnectionStatus.text = "bluetooth is ready to connect"
                        btnConnect.visibility= View.VISIBLE
                        btnTurnOn.visibility= View.INVISIBLE
                        btnDisconnect.visibility= View.INVISIBLE
                    }
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        //private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        //private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            // var numBytes: Int // bytes returned from read()
            textConnectionStatus.text = "Sucessfully connected to washing machine"
        }
        fun write(msg: String) {
            val data: ByteArray = (msg + "\n").toByteArray()
            mmOutStream.write(data)
        }
    }

    private inner class myConnectedSocket(private val mmSocket: BluetoothSocket) : Thread() {
        private val btnDisconnect = findViewById<Button>(R.id.btnDisconnect)
        override fun run() {
            textStatus.text = "Configure washing machine preferences"
            val btService = ConnectedThread(mmSocket!!)
            btService.start()
            val btnON = findViewById<Button>(R.id.btnON)
            val btnOFF = findViewById<Button>(R.id.btnOFF)
            btnON?.setOnClickListener(){
                btService.write("power on")
            }
            btnOFF?.setOnClickListener(){
                btService.write("power off")
            }
        }
        fun cancel() {
            try {
                mmSocket?.close()
                btnDisconnect.visibility= View.INVISIBLE
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val btnDisconnect = findViewById<Button>(R.id.btnDisconnect)
        private val btnConnect = findViewById<Button>(R.id.btnConnect)

        private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(MY_UUID)
        }
        @SuppressLint("MissingPermission")
        public override fun run() {
            bluetoothAdapter?.cancelDiscovery()
            mmSocket?.let { socket ->
                socket.connect()
                val btService = myConnectedSocket(mmSocket!!)
                btService.start()
                runOnUiThread(Runnable {
                    btnDisconnect?.setOnClickListener(){
                        btService.cancel()
                        btnConnect.visibility= View.VISIBLE
                    }
                    btnConnect.visibility= View.INVISIBLE
                    btnDisconnect.visibility= View.VISIBLE
                })
            }
        }
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }
}

private fun getUPIString(
    payeeAddress: String, payeeName: String, payeeMCC: String, trxnID: String, trxnRefId: String,
    trxnNote: String, payeeAmount: String, currencyCode: String, refUrl: String
): String? {
    val UPI = ("upi://pay?pa=" + payeeAddress + "&pn=" + payeeName
            + "&mc=" + payeeMCC + "&tid=" + trxnID + "&tr=" + trxnRefId
            + "&tn=" + trxnNote + "&am=" + payeeAmount + "&cu=" + currencyCode
            + "&refUrl=" + refUrl)
    return UPI.replace(" ", "+")
}
