package content.bot.fact

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.player.Player

data class Requirement<T>(val fact: Fact<T>, val predicate: Predicate<T>?) {

    fun check(player: Player): Boolean {
        return predicate?.test(fact.getValue(player)) ?: false
    }

    companion object {
        private val logger = InlineLogger()

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

//data class Parsed(
//    val id: String,
//    val template: String? = null,
//    val requires: MutableMap<String, Any>,
//    val resolves: MutableMap<String, Any>,
//) {
//    fun resolve(fields: Map<String, Map<String, Any>>) {
//        for (index in requires.indices) {
//            val map = requires[index]
//            val template = templates[index]
//            val fields = fields[index]
//            for ((key, value) in map) {
//                if (value !is String || !value.contains("$")) {
//                    continue
//                }
//                val ref = value.reference()
//                val name = ref.trim('$', '{', '}')
//                val replacement = fields[name] ?: error("No field found for type=${types[index]} key=$key ref=$ref")
//                map[key] = if (replacement is String) value.replace(ref, replacement) else replacement
//            }
//        }
//    }
//
//    private fun String.reference(): String {
//        if (startsWith('$')) {
//            return this
//        }
//        val index = indexOf($$"${")
//        if (index == -1) {
//            return "\$${substringAfter('$')}"
//        }
//        val end = indexOf('}', index) + 1
//        return substring(index, end)
//    }
//}

