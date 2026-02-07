package content.bot.fact

interface Matcher<P: Predicate<*>, O: Outcome> {
    /**
     * Would [outcome] satisfy [predicate]
     */
    fun matches(predicate: P, outcome: O): Boolean
}