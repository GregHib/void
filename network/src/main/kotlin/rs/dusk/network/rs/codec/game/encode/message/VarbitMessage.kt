package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * A variable bit of a [VarpMessage]; also known as "ConfigFile", known in the client as "clientvarpbit
 * @param id The file id
 * @param value The value to pass to the config file
 * @param large Whether to encode value with integer rather than short (optional - calculated automatically)
 */
data class VarbitMessage(val id: Int, val value: Int, val large: Boolean = value !in Byte.MIN_VALUE..Byte.MAX_VALUE) : Message