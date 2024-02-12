package com.eduardo.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ListView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ingenieriajhr.blujhr.BluJhr
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var blue:BluJhr

    //create var for values Horas, Minutos, Segundos, Intervalo, Fotos
    var horas = 0
    var minutos = 0
    var segundos = 0
    var intervalo = 0
    var fotos = 0

    //list device arraylist
    var listDevice = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        blue = BluJhr(this)
        blue.onBluetooth()

        txtHoras.text = "Horas: "+horas.toString()
        txtMinutos.text = "Minutos: "+minutos.toString()
        txtSegundos.text = "Segundos: "+segundos.toString()
        txtIntervalo.text = "Intervalo: "+intervalo.toString()
        txtFotos.text = "Fotos: "+fotos.toString()



        btn_conectar.setOnClickListener {
            //create alerta para conectar a la lista
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("dispositivos bluetooth")
            val customLayout = layoutInflater.inflate(R.layout.list_view, null)
            dialog.setView(customLayout)
            //Crear alerta
            val alert = dialog.create()
            alert.show()
            //Elementos del layout
            val list = customLayout.findViewById<ListView>(R.id.listDeviceBluetooth)
            listDevice = blue.deviceBluetooth()
            //Si la lista esta vacia
            if (listDevice.isEmpty()){
                Toast.makeText(this, "No hay dispositivos", Toast.LENGTH_SHORT).show()
            }else{
                //crate arrayadapter simple
                val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, listDevice)
                list.adapter = adapter
            }

            list.setOnItemClickListener { parent, view, position, id ->
                blue.connect(listDevice[position])
                listenerFunctionRx(alert, listDevice[position])
            }

        }

        btn_desconectar.setOnClickListener {
            blue.closeConnection()
        }


        seekBarHoras.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                horas = progress
                txtHoras.text = "Horas: $progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        seekBarMinutos.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                minutos = progress
                txtMinutos.text = "Minutos: $progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        seekBarSegundos.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                segundos = progress
                txtSegundos.text = "Segundos: $progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        seekBarIntervalo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                intervalo = progress
                txtIntervalo.text = "Intervalo: $progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        seekBarFotos.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                fotos = progress
                txtFotos.text = "Fotos: $progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })


        btn_enviar.setOnClickListener {
            var status = 0
            var msg = ""
            if (switch_intervalometro.isChecked){
                status = 1
                msg = "1,$horas,$minutos,$segundos,$fotos,$intervalo\n"
            }else{
                msg = "1,$horas,$minutos,$segundos,0,0\n"
            }
            blue.bluTx(msg)
        }

    }

    private fun listenerFunctionRx(alert: AlertDialog, s: String) {

        blue.setDataLoadFinishedListener(object :BluJhr.ConnectedBluetooth{
            //create SweetAlertDialog loading


            override fun onConnectState(state: BluJhr.Connected) {
                when(state){
                    BluJhr.Connected.True -> {
                        Toast.makeText(this@MainActivity, "Conectado", Toast.LENGTH_SHORT).show()
                        txt_dispositivos.text = "Conectado a : ${s}"
                        alert.dismiss()
                    }
                    BluJhr.Connected.Disconnect -> {
                        Toast.makeText(this@MainActivity, "Desconectado", Toast.LENGTH_SHORT).show()
                        txt_dispositivos.text = "No conectado"
                    }
                    BluJhr.Connected.Pending -> {

                        Toast.makeText(this@MainActivity, "Pendiente", Toast.LENGTH_SHORT).show()
                    }
                    BluJhr.Connected.False->{
                        Toast.makeText(this@MainActivity, "No conectado", Toast.LENGTH_SHORT).show()
                        txt_dispositivos.text = "No conectado"
                    }
                }
            }
        })
    }


    /**
     * pedimos los permisos correspondientes, para android 12 hay que pedir los siguientes BLUETOOTH_SCAN y BLUETOOTH_CONNECT
     * en android 12 o superior se requieren permisos adicionales
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (blue.checkPermissions(requestCode,grantResults)){
            Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
            blue.initializeBluetooth()
        }else{
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                blue.initializeBluetooth()
            }else{
                Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!blue.stateBluetoooth() && requestCode == 100){
            blue.initializeBluetooth()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}