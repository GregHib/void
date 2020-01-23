package org.redrune.tools.constants

import com.google.common.collect.ImmutableList
import org.redrune.tools.YAMLParser
import java.math.BigInteger

/**
 * This class contains constant values for networking
 *
 * @author Tyluur <itstyluur></itstyluur>@gmail.com>
 * @since 8/30/2017
 */
class NetworkConstants {

    companion object {
        /**
         * The runescape protocol number of the server
         */
        val CLIENT_MAJOR_BUILD = YAMLParser.getInt("clientBuild")

        /**
         * The port the server listens on
         */
        const val PORT_ID = 43594

        /**
         * The localhost ip address
         */
        const val LOCALHOST = "127.0.0.1"

        /**
         * The keys sent during grab server decoding
         */
        val GRAB_SERVER_KEYS = intArrayOf(
            1362,
            77448,
            44880,
            39771,
            24563,
            363672,
            44375,
            0,
            1614,
            0,
            5340,
            142976,
            741080,
            188204,
            358294,
            416732,
            828327,
            19517,
            22963,
            16769,
            1244,
            11976,
            10,
            15,
            119,
            817677,
            1624243
        )

        /**
         * The modulus rsa key for login.
         */
        val LOGIN_RSA_MODULUS = BigInteger(YAMLParser.getString("lsRsaModulus"), 16)

        /**
         * The private rsa key for login
         */
        val LOGIN_RSA_PRIVATE = BigInteger(YAMLParser.getString("lsRsaPrivate"), 16)

        /**
         * The modulus rsa key for file transmission
         */
        val FILE_SERVER_RSA_MODULUS = BigInteger(YAMLParser.getString("fsRsaModulus"), 16)

        /**
         * The private rsa key for file transmission
         */
        val FILE_SERVER_RSA_PRIVATE = BigInteger(YAMLParser.getString("fsRsaPrivate"), 16)

        /**
         * The list of exceptions that are ignored
         */
        val IGNORED_EXCEPTIONS: ImmutableList<String> = ImmutableList.of(
            "An existing connection was forcibly closed by the remote host",
            "An established connection was aborted by the software in your host machine"
        )

        /**
         * The map sizes
         */
        val MAP_SIZES = intArrayOf(104, 120, 136, 168)

        /**
         * The length of a timeout
         */
        const val TIMEOUT_RATE = 30000 // 1minute;
    }
}