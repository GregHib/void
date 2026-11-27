package content.bot.behaviour

import content.bot.behaviour.condition.Condition

interface Reason {
    data class Invalid(val message: String) : HardReason {
        override fun toString() = "Invalid(\"$message\")"
    }
    object Cancelled : HardReason {
        override fun toString() = "Cancelled"
    }
    object NoRoute : HardReason {
        override fun toString() = "NoRoute"
    }
    object Timeout : HardReason {
        override fun toString() = "Timeout"
    }
    object Stuck : SoftReason {
        override fun toString() = "Stuck"
    }
    object NoTarget : SoftReason {
        override fun toString() = "NoTarget"
    }

    /** A player-owned bot escaped a fight it was losing; the owner decides what it does next. */
    object Escaped : HardReason {
        override fun toString() = "Escaped"
    }

    data class Requirement(val condition: Condition) : HardReason {
        override fun toString() = "Requirement($condition)"
    }
}
interface SoftReason : Reason
interface HardReason : Reason
