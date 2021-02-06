package world.gregs.voidps.ai

/**
 * The [option] and [target] chosen by the [DecisionMaker]
 */
data class Decision<C : Context, T : Any>(
    val context: C,
    val target: T,
    val option: Option<C, T>,
    val score: Double
) {
    fun invoke() {
        option.action?.invoke(context, target)
    }
}