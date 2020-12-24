package rs.dusk.ai

/**
 * A [utility system](https://en.wikipedia.org/wiki/Utility_system)
 * Selects the best [Option] based on multiple [Option.considerations] represented as weighted utilities
 */
class DecisionMaker {

    fun decide(context: Context): Decision? {
        context.last = select(context) ?: return null
        return context.last
    }

    private fun select(context: Context): Decision? {
        return context.options.fold(null as Decision?) { highest, option -> option.getHighestTarget(context, highest?.score ?: 0.0) ?: highest }
    }

}