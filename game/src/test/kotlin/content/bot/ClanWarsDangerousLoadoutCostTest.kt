package content.bot

import WorldTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.config.Config
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.event.wildcardEquals

class ClanWarsDangerousLoadoutCostTest : WorldTest() {

    @Test
    fun `Dangerous Clan Wars FFA bots carry no item with cache cost above 1M`() {
        val templateItems = mutableMapOf<String, Set<String>>()
        Config.fileReader(TEMPLATES_PATH) {
            while (nextSection()) {
                val id = section()
                val items = mutableSetOf<String>()
                while (nextPair()) {
                    when (key()) {
                        "setup" -> collectItems(value(), items)
                        "loadouts" -> collectLoadoutItems(value(), items)
                        else -> value()
                    }
                }
                templateItems[id] = items
            }
        }

        val violations = mutableListOf<String>()
        Config.fileReader(BOTS_PATH) {
            while (nextSection()) {
                val tierId = section()
                val isDangerous = tierId.startsWith("clan_wars_ffa_dangerous_")
                val tierItems = mutableSetOf<String>()
                var template: String? = null
                while (nextPair()) {
                    when (key()) {
                        "template" -> template = string()
                        "setup" -> if (isDangerous) collectItems(value(), tierItems) else value()
                        else -> value()
                    }
                }
                if (!isDangerous) continue
                val items = tierItems.toMutableSet()
                if (template != null) {
                    items += templateItems[template] ?: error("Missing template '$template' for tier '$tierId'")
                }
                for (pattern in items) {
                    for ((id, cost) in resolve(pattern)) {
                        if (cost > MAX_COST) {
                            violations += "tier=$tierId item=$id cost=$cost (pattern=$pattern)"
                        }
                    }
                }
            }
        }

        assertTrue(violations.isEmpty()) {
            "Dangerous-arena bots must not carry items with def.cost > $MAX_COST. Violations:\n" +
                violations.joinToString("\n")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun collectItems(setup: Any?, items: MutableSet<String>) {
        val list = setup as? List<Map<String, Any>> ?: return
        for (entry in list) {
            (entry["equipment"] as? Map<String, Any>)?.let { equipment ->
                for ((_, slot) in equipment) {
                    ((slot as? Map<*, *>)?.get("id") as? String)?.let(items::add)
                }
            }
            (entry["inventory"] as? List<*>)?.let { inv ->
                for (slot in inv) {
                    ((slot as? Map<*, *>)?.get("id") as? String)?.let(items::add)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun collectLoadoutItems(loadouts: Any?, items: MutableSet<String>) {
        val map = loadouts as? Map<String, Any> ?: return
        for ((_, raw) in map) {
            val entry = raw as? Map<String, Any> ?: continue
            collectItems(listOf(entry), items)
        }
    }

    private fun resolve(pattern: String): List<Pair<String, Int>> {
        val results = mutableListOf<Pair<String, Int>>()
        for (token in pattern.split(',')) {
            if (token.contains('*') || token.contains('#')) {
                var matched = false
                for (stringId in ItemDefinitions.ids.keys) {
                    if (wildcardEquals(token, stringId)) {
                        matched = true
                        results += stringId to ItemDefinitions.get(stringId).cost
                    }
                }
                check(matched) { "Wildcard '$token' matched no items" }
            } else {
                val def = ItemDefinitions.get(token)
                check(def.id != -1) { "Unknown item id '$token'" }
                results += token to def.cost
            }
        }
        return results
    }

    companion object {
        private const val MAX_COST = 1_000_000
        private const val TEMPLATES_PATH = "../data/bot/minigame_combat.templates.toml"
        private const val BOTS_PATH = "../data/minigame/clan_wars/clan_wars.bots.toml"
    }
}
