package com.example.smsgateway

import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkUtils {

    fun getLocalIpAddress(): String {

        try {

            val interfaces = NetworkInterface.getNetworkInterfaces()

            for (networkInterface in interfaces) {

                val addresses = networkInterface.inetAddresses

                for (address in addresses) {

                    if (
                        !address.isLoopbackAddress &&
                        address is Inet4Address
                    ) {
                        return address.hostAddress ?: "Unknown"
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return "Not Connected"
    }
}