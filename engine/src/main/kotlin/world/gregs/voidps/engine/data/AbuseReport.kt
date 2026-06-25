package world.gregs.voidps.engine.data

/**
 * A player submitted report about another player breaking a rule
 * @param reporter Account name of the player filing the report
 * @param reported Display name of the accused player as submitted
 * @param rule Identifier of the rule broken
 * @param ruleName Readable name of the rule broken
 * @param mute Whether a moderator requested the accused be muted
 * @param suggestion Additional text submitted with the report
 * @param time Epoch millisecond timestamp the report was received
 * @param evidence Recent chat messages sent by the accused player
 */
data class AbuseReport(
    val reporter: String,
    val reported: String,
    val rule: Int,
    val ruleName: String,
    val mute: Boolean,
    val suggestion: String,
    val time: Long,
    val evidence: List<String>,
)
