package org.redrune.network.codec.login.decode.message

import org.redrune.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class LobbyLoginMessage(
    val username: String,
    val password: String,
    val hd: Boolean,
    val resize: Boolean,
    val settings: String,
    val affiliate: Int,
    val isaacSeed: IntArray,
    val crcMap: MutableMap<Int, Pair<Int, Int>>
) : Message {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LobbyLoginMessage

        if (username != other.username) return false
        if (password != other.password) return false
        if (hd != other.hd) return false
        if (resize != other.resize) return false
        if (settings != other.settings) return false
        if (affiliate != other.affiliate) return false
        if (!isaacSeed.contentEquals(other.isaacSeed)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + hd.hashCode()
        result = 31 * result + resize.hashCode()
        result = 31 * result + settings.hashCode()
        result = 31 * result + affiliate
        result = 31 * result + isaacSeed.contentHashCode()
        return result
    }
}