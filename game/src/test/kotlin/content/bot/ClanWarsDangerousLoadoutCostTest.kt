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
        val gear = mutableMapOf<String, List<String>>()
        Config.fileReader(GEAR_PATH) {
            while (nextSection()) {
                val id = section()
                val items = mutableListOf<String>()
                while (nextPair()) {
                    if (key() == "items") {
                        while (nextElement()) {
                            items.add(string())
                        }
                    } else {
                        value()
                    }
                }
                gear[id] = items
            }
        }
        val loadoutItems = mutableMapOf<String, Set<String>>()
        Config.fileReader(LOADOUTS_PATH) {
            while (nextSection()) {
                val id = section()
                val items = mutableSetOf<String>()
                while (nextPair()) {
                    val key = key()
                    collectItems(key, value(), gear, items)
                }
                loadoutItems[id] = items
            }
        }
        val templateItems = mutableMapOf<String, Set<String>>()
        Config.fileReader(TEMPLATES_PATH) {
            while (nextSection()) {
                val id = section()
                val items = mutableSetOf<String>()
                while (nextPair()) {
                    when (key()) {
                        "setup" -> collectSetup(value(), loadoutItems, items)
                        "switch_loadouts" -> collectHybridLoadouts(value(), gear, items)
                        else -> value()
                    }
                }
                templateItems[id] = items
            }
        }

        val violations = mutableListOf<String>()
        var checked = 0
        Config.fileReader(BOTS_PATH) {
            while (nextSection()) {
                val activity = section()
                var template: String? = null
                while (nextPair()) {
                    when (key()) {
                        "template" -> template = string()
                        else -> value()
                    }
                }
                if (!activity.startsWith("clan_wars_ffa_dangerous_")) continue
                checked++
                val items = templateItems[template ?: error("No template for '$activity'")] ?: error("Missing template '$template' for '$activity'")
                check(items.isNotEmpty()) { "No items found for '$activity'" }
                for (pattern in items) {
                    for ((id, cost) in resolve(pattern)) {
                        if (cost > MAX_COST) {
                            violations += "activity=$activity item=$id cost=$cost (pattern=$pattern)"
                        }
                    }
                }
            }
        }

        assertTrue(checked > 0) { "No dangerous Clan Wars activities found" }
        assertTrue(violations.isEmpty()) {
            "Dangerous-arena bots must not carry items with def.cost > $MAX_COST. Violations:\n" +
                violations.joinToString("\n")
        }
    }

    /**
     * Items from one loadout key: an equipment slot (`id` or `gear`) or the `inventory` list
     */
    @Suppress("UNCHECKED_CAST")
    private fun collectItems(key: String, value: Any?, gear: Map<String, List<String>>, items: MutableSet<String>) {
        when (key) {
            "inventory" -> for (entry in value as? List<*> ?: return) {
                ((entry as? Map<*, *>)?.get("id") as? String)?.let(items::add)
            }
            "equipment" -> for ((slot, spec) in value as? Map<String, Any> ?: return) {
                collectItems(slot, spec, gear, items)
            }
            else -> {
                val spec = value as? Map<*, *> ?: return
                (spec["id"] as? String)?.let(items::add)
                (spec["gear"] as? String)?.let { items.addAll(gear[it] ?: error("Unknown gear '$it'")) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun collectSetup(setup: Any?, loadouts: Map<String, Set<String>>, items: MutableSet<String>) {
        for (entry in setup as? List<Map<String, Any>> ?: return) {
            val loadout = entry["loadout"] as? Map<String, Any> ?: continue
            val id = loadout["id"] as String
            items.addAll(loadouts[id] ?: error("Unknown loadout '$id'"))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun collectHybridLoadouts(loadouts: Any?, gear: Map<String, List<String>>, items: MutableSet<String>) {
        for ((_, raw) in loadouts as? Map<String, Any> ?: return) {
            for ((key, value) in raw as? Map<String, Any> ?: continue) {
                collectItems(key, value, gear, items)
            }
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
        private const val LOADOUTS_PATH = "../data/bot/pvp.loadouts.toml"
        private const val GEAR_PATH = "../data/bot/pvp.gear.toml"
        private const val BOTS_PATH = "../data/minigame/clan_wars/clan_wars.bots.toml"
    }
}
