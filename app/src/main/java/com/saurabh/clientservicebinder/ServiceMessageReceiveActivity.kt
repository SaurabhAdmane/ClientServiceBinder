package com.saurabh.clientservicebinder

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import com.saurabh.clientservicebinder.databinding.ActivityServiceMessageReceiveBinding

class ServiceMessageReceiveActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityServiceMessageReceiveBinding
    lateinit var serviceIntent: Intent
    var randomNumberRequestMessenger: Messenger? = null
    var randomNumberReceiveMessenger: Messenger? = null
    private var isBound = false
    private var randomNumberValue = 0
    private var GET_RANDOM_NUMBER_FLAG = 0

    inner class ReceiveRandomNumberHandler : Handler() {
        override fun handleMessage(msg: Message) {
            randomNumberValue = 0
            when (msg.what) {
                GET_RANDOM_NUMBER_FLAG -> {
                    randomNumberValue = msg.arg1
                    binding.txtStatus.text = "Number receive from Service: $randomNumberValue"
                }
            }
            super.handleMessage(msg)
        }
    }

    var serviceConnection: ServiceConnection? = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            randomNumberRequestMessenger = Messenger(service)
            randomNumberReceiveMessenger = Messenger(ReceiveRandomNumberHandler())
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            randomNumberReceiveMessenger = null
            randomNumberRequestMessenger = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceMessageReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setClickListners()

        serviceIntent = Intent()
        serviceIntent.component = ComponentName(
            "com.saurabh.servicesdemo", "com.saurabh.servicesdemo.service.RemoteService"
        )
    }

    private fun setClickListners() {
        binding.btnBindService.setOnClickListener(this)
        binding.btnUnbindService.setOnClickListener(this)
        binding.btnGetRandomNumber.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_bind_service -> {
                bindToServiceApp()
            }

            R.id.btn_unbind_service -> {
                unbindFromServiceApp()
            }

            R.id.btn_get_random_number -> {
                readDataFromServiceApp()
            }
        }
    }

    private fun bindToServiceApp() {
        bindService(serviceIntent, serviceConnection!!, BIND_AUTO_CREATE)
        Toast.makeText(this, "Service Bound Successfully", LENGTH_SHORT).show()
        isBound=true
    }

    private fun unbindFromServiceApp() {
        if (isBound) {
            unbindService(serviceConnection!!)
            isBound = false
            Toast.makeText(this, "Service Unbound Successfully", LENGTH_SHORT).show()
        }
    }

    private fun readDataFromServiceApp() {
        if (isBound) {
            val requestMessage = Message.obtain(null, GET_RANDOM_NUMBER_FLAG)
            requestMessage.replyTo = randomNumberReceiveMessenger
            try {
                randomNumberRequestMessenger?.send(requestMessage)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "Service Unbound, can't get random number", LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceConnection = null
    }
}