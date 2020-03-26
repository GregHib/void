package org.redrune.network.rs.codec.login.encode.message

import org.redrune.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class LobbyConfigurationMessage(val username: String, val lastIpAddress: String, val lastLogin: Long) : Message