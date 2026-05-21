package com.example.smsgateway

import android.content.Context
import android.telephony.SmsManager
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject

class SmsServer(private val context: Context) :
    NanoHTTPD(8080) {

    override fun serve(session: IHTTPSession): Response {

        if (
            session.uri == "/sendSms" &&
            session.method == Method.POST
        ) {

            return try {

                val files = HashMap<String, String>()

                session.parseBody(files)

                val body = files["postData"] ?: ""

                val json = JSONObject(body)

                val phone = json.getString("phone")
                val message = json.getString("message")

                sendSms(phone, message)

                newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    """{"status":"success"}"""
                )

            } catch (e: Exception) {

                newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR,
                    "application/json",
                    """{"error":"${e.message}"}"""
                )
            }
        }

        return newFixedLengthResponse("Server Running")
    }

    private fun sendSms(phone: String, message: String) {

        val smsManager = SmsManager.getDefault()

        smsManager.sendTextMessage(
            phone,
            null,
            message,
            null,
            null
        )
    }
}