package content.bot.action

import content.bot.fact.Condition

interface Reason {
    object Cancelled : HardReason
    object NoRoute : HardReason
    object NoTarget : SoftReason
    data class Requirement(val fact: Condition) : HardReason
}
interface SoftReason : Reason
interface HardReason : Reason

