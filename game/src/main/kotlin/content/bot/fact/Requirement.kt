package content.bot.fact

import world.gregs.voidps.engine.entity.character.player.Player

data class Requirement<T>(val fact: Fact<T>, val predicate: Predicate<T>? = null) {

    fun check(player: Player): Boolean {
        return predicate?.test(fact.getValue(player)) ?: false
    }

    companion object {
        fun parse(list: List<Pair<String, Map<String, Any>>>, name: String, requirePredicates: Boolean = true): List<Requirement<*>> {
            val requirements = mutableListOf<Requirement<*>>()

            for ((type, map) in list) {
                val parser = FactParser.parsers[type] ?: error("No fact parser for '$type' in ${name}.")
                val error = parser.check(map)
                if (error != null) {
                    error("Fact '$type' $error in ${name}.")
                }
                val requirement = parser.requirement(map)
                if (requirePredicates && requirement.predicate == null) {
                    error("No predicates found for requirement $type map $map in ${name}.")
                }
                requirements.add(requirement)
            }
            return requirements
        }
    }
}
