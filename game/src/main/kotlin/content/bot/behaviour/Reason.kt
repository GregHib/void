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
    data class Requirement(val condition: Condition) : HardReason {
        override fun toString() = "Requirement($condition)"
    }
}
interface SoftReason : Reason
interface HardReason : Reason
