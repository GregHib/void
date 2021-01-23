package world.gregs.voidps.ai

/**
 * The [option] and [target] chosen by the [DecisionMaker]
 */
data class Decision(
    val target: Any,
    val option: Option<*, *>,
    val score: Double
)