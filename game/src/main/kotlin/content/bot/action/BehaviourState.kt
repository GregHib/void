package content.bot.action

sealed interface BehaviourState  {
    object Pending : BehaviourState
    object Running : BehaviourState
    object Success : BehaviourState
    data class Failed(val reason: Reason) : BehaviourState
    data class Wait(var ticks: Int) : BehaviourState
}