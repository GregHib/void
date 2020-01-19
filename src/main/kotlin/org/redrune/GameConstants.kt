package org.redrune

import org.redrune.util.YAMLParser

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class GameConstants {

    companion object {

        /**
         * The name of the server
         */
        val SERVER_NAME = YAMLParser.getString("name")

        /**
         * The major build version
         */
        val BUILD_MAJOR = YAMLParser.getInt("buildMajor")

        /**
         * The minor build version
         */
        val BUILD_MINOR = YAMLParser.getDouble("buildMinor")

        /**
         * The location of the cache
         */
        val CACHE_DIRECTORY = YAMLParser.getString("cachePath")

    }
}