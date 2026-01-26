package content.bot.action

interface Reason {
    object Cancelled : HardReason
    object Requirements : HardReason
}
interface SoftReason : Reason
interface HardReason : Reason

