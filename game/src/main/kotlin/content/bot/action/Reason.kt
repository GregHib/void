package content.bot.action

interface Reason {
    data class Invalid(val message: String) : HardReason
    object Cancelled : HardReason
    object NoRoute : HardReason
    object Timeout : HardReason
    object Stuck : SoftReason
    object NoTarget : SoftReason
    data class Requirement(val fact: content.bot.fact.Requirement<*>) : HardReason
}
interface SoftReason : Reason
interface HardReason : Reason

