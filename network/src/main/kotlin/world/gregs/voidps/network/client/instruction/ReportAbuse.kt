package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

/**
 * Client report about another player
 * @param name The display name of the accused player
 * @param type The type of offence supposedly committed
 * @param integer Unknown
 * @param string Unknown
 */
data class ReportAbuse(
    val name: String,
    val type: Int,
    val integer: Int,
    val string: String,
) : Instruction
