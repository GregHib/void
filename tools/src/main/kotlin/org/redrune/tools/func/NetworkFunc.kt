package org.redrune.tools.func

import java.util.*


/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 10, 2020
 */
class NetworkFunc {

    companion object {
        /**
         * Converts an IP-Address as string to Integer.
         *
         * @return The Integer.
         */
        fun IPAddressToNumber(ipAddress: String?): Int {
            val st = StringTokenizer(ipAddress, ".")
            val ip = IntArray(4)
            var i = 0
            while (st.hasMoreTokens()) {
                ip[i++] = st.nextToken().toInt()
            }
            return ip[0] shl 24 or (ip[1] shl 16) or (ip[2] shl 8) or ip[3]
        }
    }

}