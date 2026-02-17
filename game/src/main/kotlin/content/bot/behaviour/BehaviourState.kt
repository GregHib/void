package content.bot.behaviour

sealed interface BehaviourState {
    object Pending : BehaviourState {
        override fun toString() = "Pending"
    }
    object Running : BehaviourState {
        override fun toString() = "Running"
    }
    object Success : BehaviourState {
        override fun toString() = "Success"
    }
    data class Failed(val reason: Reason) : BehaviourState {
        override fun toString() = "Failed($reason)"
    }
    data class Wait(var ticks: Int, val next: BehaviourState) : BehaviourState {
        override fun toString() = "Wait($ticks, $next)"
    }
}
