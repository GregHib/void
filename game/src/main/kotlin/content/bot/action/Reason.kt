package content.bot.action

import content.bot.fact.Fact

interface Reason {
    object Cancelled : HardReason
    object NoRoute : HardReason
    object NoTarget : SoftReason
    data class Requirement(val fact: Fact) : HardReason
}
interface SoftReason : Reason
interface HardReason : Reason

