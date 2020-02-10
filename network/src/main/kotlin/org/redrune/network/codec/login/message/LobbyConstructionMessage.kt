package org.redrune.network.codec.login.message

import org.redrune.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 10, 2020
 */
data class LobbyConstructionMessage(val username: String, val clientRight: Int, val lastIPAddress: String) : Message {

}