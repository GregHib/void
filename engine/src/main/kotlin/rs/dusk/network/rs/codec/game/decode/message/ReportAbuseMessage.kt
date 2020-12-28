package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * Client report about another player
 * @param name The display name of the accused player
 * @param type The type of offence supposedly committed
 * @param integer Unknown
 * @param string Unknown
 */
data class ReportAbuseMessage(val name: String, val type: Int, val integer: Int, val string: String) : Message