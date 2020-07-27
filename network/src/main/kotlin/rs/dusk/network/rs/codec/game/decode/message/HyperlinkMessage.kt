package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Request to open a hyperlink
 * @param name Readable name
 * @param script Windows script name
 * @param third Unknown value
 */
data class HyperlinkMessage(val name: String, val script: String, val third: Int) : Message