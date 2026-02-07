package content.bot.fact

import world.gregs.voidps.engine.entity.character.player.Player

data class Requirement<T>(val fact: Fact<T>, val predicate: Predicate<T>? = null) {

    fun check(player: Player): Boolean {
        return predicate?.test(player, fact.getValue(player)) ?: false
    }

    fun deficits(player: Player): List<Deficit> {
        return predicate?.evaluator?.evaluate(player, fact, predicate) ?: emptyList()
    }

    companion object {
        fun parse(list: List<Pair<String, List<Map<String, Any>>>>, name: String, requirePredicates: Boolean = true): List<Requirement<*>> {
            val requirements = mutableListOf<Requirement<*>>()
            for ((type, value) in list) {
                val parser = FactParser.parsers[type] ?: error("No fact parser for '$type' in ${name}.")
                for (map in value) {
                    val error = parser.check(map)
                    if (error != null) {
                        error("Fact '$type' $error in ${name}.")
                    }
                }
                val requirement = parser.requirement(value)
                if (requirePredicates && requirement.predicate == null) {
                    error("No predicates found for requirement $type map $value in ${name}.")
                }
                requirements.add(requirement)
            }
            return requirements
        }
    }
}
