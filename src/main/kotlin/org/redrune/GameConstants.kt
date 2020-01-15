package org.redrune

import org.redrune.util.YAMLParser

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class GameConstants {

    companion object {

        val CACHE_DIRECTORY = YAMLParser.getString("cachePath")

        /**
         * The rsa modulus
         */
        val RSA_MODULUS = YAMLParser.getString("rsaModulus")

        /**
         * The private rsa key
         */
        val RSA_PRIVATE = YAMLParser.getString("rsaPrivate")
    }
}