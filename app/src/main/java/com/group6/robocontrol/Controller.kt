package com.group6.robocontrol

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.Buffer

class Controller(private val context: Context) {
    companion object {
        const val IP = "http://192.168.4.1/"
    }

    val ledThread = HandlerThread("LED").apply {
        start()
    }
    val motorThread = HandlerThread("MOTOR").apply {
        start()
    }
    val connectionThread = HandlerThread("CONNECTION").apply {
        start()
    }

    val ledHandler = Handler(ledThread.looper)
    val motorHandler = Handler(motorThread.looper)
    val connectionHandler = Handler(connectionThread.looper)
    val mainHandler = Handler(Looper.getMainLooper())

    fun checkConnectionStatus(listener: (connected: Boolean) -> Unit) {
        connectionHandler.post {
            var ret = true
            val url = URL(IP)

            val value = try {
                val connection = url.openConnection() as HttpURLConnection
                try {
                    readStream(connection.inputStream)
                } catch (e: Exception) {
                    ret = false
                } finally {
                    connection.disconnect()
                }
                ret
            } catch (e: Exception) {
                false
            }

            mainHandler.post { listener.invoke(value) }
        }
    }

    fun toggleLed(state: Int) {
        ledHandler.post {
            val url = URL(IP + "toggleLed(($state))")
            val connection = url.openConnection() as HttpURLConnection

            try {
                readStream(connection.inputStream)
            } catch (e: Exception) {} finally {
                connection.disconnect()
            }
        }
    }

    fun setMotor(which: Int, speed: Int) {
        motorHandler.post {
            val url = URL(IP + "setMotor(($which,$speed))")
            val connection = url.openConnection() as HttpURLConnection

            try {
                readStream(connection.inputStream)
            } catch (e: Exception) {} finally {
                connection.disconnect()
            }
        }
    }

    fun setBothMotors(one: Int, two: Int) {
        motorHandler.post {
            val url = URL(IP + "setBothMotors(($one,$two))")
            val connection = url.openConnection() as HttpURLConnection

            try {
                readStream(connection.inputStream)
            } catch (e: Exception) {} finally {
                connection.disconnect()
            }
        }
    }

    fun runNascar(one: Int, two: Int, delay: Int) {
        motorHandler.post {
            val url = URL(IP + "runNascar(($one,$two:$delay")
            val connection = url.openConnection() as HttpURLConnection

            try {
                readStream(connection.inputStream)
            } catch (e: Exception) {} finally {
                connection.disconnect()
            }
        }
    }

    fun doChallenge(one: Int, two: Int, delay: Int) {
        motorHandler.post {
            val url = URL(IP + "doChallenge(($one,$two:$delay")
            val connection = url.openConnection() as HttpURLConnection

            try {
                readStream(connection.inputStream)
            } catch (e: Exception) {} finally {
                connection.disconnect()
            }
        }
    }

    fun getLedState(listener: (state: Int) -> Unit) {
        ledHandler.post {
            val url = URL(IP + "getLedState((")
            val connection = url.openConnection() as HttpURLConnection

            try {
                val reader = connection.inputStream.bufferedReader()
                val value = reader.readLine()
                mainHandler.post { listener.invoke(value.toInt()) }
            } catch (e: Exception) {} finally {
                connection.disconnect()
            }
        }
    }

    fun getMotorState(which: Int, listener: (state: Int) -> Unit) {
        motorHandler.post {
            val url = URL(IP + "getMotorState(($which))")
            val connection = url.openConnection() as HttpURLConnection

            try {
                val reader = connection.inputStream.bufferedReader()
                val value = reader.readLine()
                mainHandler.post { listener.invoke(value.toInt()) }
            } catch (e: Exception) {} finally {
                connection.disconnect()
            }
        }
    }

    fun getSensors(callback: (Sensors) -> Unit) {
        ledHandler.post {
            val url = URL(IP + "getSensors(())")
            val connection = url.openConnection() as HttpURLConnection

            try {
                val reader = connection.inputStream.bufferedReader()
                val value = reader.readLine()
                val split = value.split(",")
                
                mainHandler.post { callback.invoke(Sensors(split[0], split[1], split[2], split[3])) }
            } catch (e: Exception) {} finally {
                connection.disconnect()
            }
        }
    }

    private fun readStream(`is`: InputStream): String {
        return try {
            val bo = ByteArrayOutputStream()
            var i = `is`.read()
            while (i != -1) {
                bo.write(i)
                i = `is`.read()
            }
            bo.toString()
        } catch (e: IOException) {
            ""
        }

    }

    data class Sensors(
            var left: String,
            var front: String,
            var right: String,
            var back: String
    )
}