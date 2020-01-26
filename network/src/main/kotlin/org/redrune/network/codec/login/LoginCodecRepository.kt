package org.redrune.network.codec.login

import org.redrune.network.codec.CodecRepository

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 25, 2020 3:21 p.m.
 */
object LoginCodecRepository : CodecRepository() {
    override fun initialize() {
//        bindDecoder(LobbyMessageDecoder())
    }
}