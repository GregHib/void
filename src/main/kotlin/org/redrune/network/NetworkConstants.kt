package org.redrune.network

import com.google.common.collect.ImmutableList
import io.netty.util.AttributeKey
import java.math.BigInteger

/**
 * This class contains constant values for networking
 *
 * @author Tyluur <itstyluur></itstyluur>@gmail.com>
 * @since 8/30/2017
 */
interface NetworkConstants {

    companion object {
        /**
         * The runescape protocol number of the server
         */
        const val PROTOCOL_NUMBER = 667

        /**
         * The port the server listens on
         */
        const val PORT_ID = 43594

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
         * The game server RSA key exponent.
         */
        val LOGIN_EXPONENT =
            BigInteger("118762062543447234074727456808991118872170985751713606465722882159677729022791781634631275342059815254405716406345046978427092008247432011727406028949468214427650611830755367261560706476584680737149281348803965978585646403259797833935425150657845103665015467365527987767725318315713043512372527291907415762273")
        /**
         * The game server RSA key modulus.
         */
        val LOGIN_MODULUS =
            BigInteger("123733137684565391382986985515878973634831964473007354491671126289247096002904505166425503816809330286277302494636833012609314653193945563916110405049937997195310625096132297106334199144922705176219016362504626538048084031862798816953255666088269887586583538984815994152019844370040268440057091407812614894353")
        /**
         * The attribute that contains the key for a session.
         */
        val SESSION_KEY: AttributeKey<NetworkSession> = AttributeKey.valueOf<NetworkSession>("session.key")
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