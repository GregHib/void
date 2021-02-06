package world.gregs.voidps.ai

/**
 * A [utility system](https://en.wikipedia.org/wiki/Utility_system)
 * Selects the best [Option] based on multiple [Option.considerations] represented as weighted utilities
 */
class DecisionMaker {

    fun <C : Context> decide(context: C, options: Set<Option<C, *>>): Decision<*, *>? {
        val decision = select(context, options) ?: return null
        context.last = decision
        return decision
    }

    private fun <C : Context> select(context: C, options: Set<Option<C, *>>): Decision<*, *>? {
        return options.fold(null as Decision<*, *>?) { highest, option ->
            option.getHighestTarget(context, highest?.score ?: 0.0) ?: highest
        }
    }

}