package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

/**
 * Client report about another player
 * @param name The display name of the accused player
 * @param type The type of offence supposedly committed
 * @param mute Whether the reporter requested the accused be muted (moderators only)
 * @param suggestion Additional text submitted with the report
 */
data class ReportAbuse(
    val name: String,
    val type: Int,
    val mute: Int,
    val suggestion: String,
) : Instruction
