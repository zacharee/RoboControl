package com.group6.robocontrol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v14.preference.PreferenceFragment
import android.support.v14.preference.SwitchPreference
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.preference.PreferenceFragmentCompat
import com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private val refresh by lazy { findViewById<SwipeRefreshLayout>(R.id.main) }

    private val fragment = Settings()
    private val receiver = NetworkReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        receiver.register()

        refresh.setOnRefreshListener(this)

        supportFragmentManager.beginTransaction()
                .replace(R.id.main, fragment)
                .commit()
    }

    override fun onRefresh() {
        refresh.isRefreshing = true

        runOnUiThread {
            fragment.setStates{
                refresh.isRefreshing = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        receiver.unregister()
    }

    class Settings : PreferenceFragmentCompat() {
        private val controller by lazy { Controller(activity!!) }
        val led by lazy { findPreference("led_state") as SwitchPreference }
        val sensor by lazy { findPreference("read_sensors") }
        val motor1 by lazy { findPreference("motor_1") as SeekBarPreferenceCompat }
        val motor2 by lazy { findPreference("motor_2") as SeekBarPreferenceCompat }
        val nascarDelay by lazy { findPreference("turn_delay") as SeekBarPreferenceCompat }
        val runNascar by lazy { findPreference("run_nascar") }
        val runChallenge by lazy { findPreference("run_challenge") }
        val apply by lazy { findPreference("apply") }
        val stop by lazy { findPreference("stop") }
        val connected by lazy { findPreference("connected") }

        override fun onCreatePreferences(p0: Bundle?, p1: String?) {
            addPreferencesFromResource(R.xml.home)

            (activity as MainActivity).onRefresh()

            led.setOnPreferenceChangeListener { _, any ->
                controller.toggleLed(if (any.toString().toBoolean()) 0 else 1)
                setLedIcon(any.toString().toBoolean())
                true
            }

            runNascar.setOnPreferenceClickListener {
                controller.runNascar(motor1.currentValue, motor2.currentValue, nascarDelay.currentValue)
                true
            }

            runChallenge.setOnPreferenceClickListener {
                controller.doChallenge(motor1.currentValue, motor2.currentValue, nascarDelay.currentValue)
                true
            }

            apply.setOnPreferenceClickListener {
                controller.setBothMotors(motor1.currentValue, motor2.currentValue)
                true
            }

            stop.setOnPreferenceClickListener {
                controller.setBothMotors(92, 92)
                true
            }

            sensor.setOnPreferenceClickListener {
                controller.getSensors { s -> setSensors(s) }
                true
            }
        }

        fun setStates(listener: () -> Unit) {
            controller.checkConnectionStatus { status ->
                if (status) {
                    controller.getLedState {
                        led.isChecked = it != 1
                        setLedIcon(led.isChecked)
                    }

                    controller.getMotorState(1) {
                        motor1.currentValue = it
                    }

                    controller.getMotorState(2) {
                        motor2.currentValue = it
                    }

                    controller.getSensors { setSensors(it) }

                    connected.summary = resources.getString(R.string.yes)
                } else {
                    connected.summary = resources.getString(R.string.no)
                }

                setConnectionIcon(status)

                listener.invoke()
            }
        }

        private fun setLedIcon(enabled: Boolean) {
//            led.icon = resources.getDrawable(
//                    if (enabled) R.drawable.led_on else R.drawable.led_off, null)
        }

        private fun setConnectionIcon(connected: Boolean) {
//            this.connected.icon = resources.getDrawable(
//                    if (connected) R.drawable.connected else R.drawable.disconnected, null)
        }

        private fun setSensors(sensors: Controller.Sensors) {
            sensor.summary = "Left: ${sensors.left}, Front: ${sensors.front}, Right: ${sensors.right}, Back: ${sensors.back}"
        }
    }

    inner class NetworkReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiManager.NETWORK_STATE_CHANGED_ACTION,
                    WifiManager.WIFI_STATE_CHANGED_ACTION -> onRefresh()
            }
        }

        fun register() {
            val filter = IntentFilter()
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)

            registerReceiver(this, filter)
        }

        fun unregister() {
            unregisterReceiver(this)
        }
    }
}
