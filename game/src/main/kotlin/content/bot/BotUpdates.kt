package content.bot

import com.github.michaelbull.logging.InlineLogger
import content.bot.behaviour.navigation.NavigationGraph
import content.bot.behaviour.setup.DynamicResolvers
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.InteractDialogue

/**
 * Listen for state changes which would change which activities are available to a bot
 */
class BotUpdates(
    val inventoryDefinitions: InventoryDefinitions,
    val graph: NavigationGraph,
) : Script {
    val logger = InlineLogger()

    init {
        /*
            Track state changes to re-evaluate available activities
         */
        levelChanged { skill, _, _ ->
            if (isBot) {
                bot.evaluate.add("skill:${skill.name.lowercase()}")
            }
        }

        inventoryUpdated { inventory, _ ->
            if (isBot) {
                bot.evaluate.add("inv:$inventory")
            }
        }

        entered("*") {
            if (isBot) {
                resetTimeout("area:${it.name}")
                bot.evaluate.add("area:${it.name}")
            }
        }

        variableSet { key, from, to ->
            if (isBot && from != to) {
                bot.evaluate.add("var:$key")
                resetTimeout("variable:$key")
            }
        }

        itemAdded(inventory = "inventory") {
            resetTimeout("item:${it.item.id}")
        }

        experience { skill, _, _ ->
            resetTimeout("skill:${skill.name.lowercase()}")
        }

        // Close level-up dialogues
        interfaceOpened("dialogue_level_up") {
            if (isBot) {
                instructions.trySend(InteractDialogue(interfaceId = 740, componentId = 3, option = -1))
            }
        }

        // Register shops
        worldSpawn {
            DynamicResolvers.shopItems.clear()
        }

        npcSpawn {
            if (def.contains("shop")) {
                val shop = def.get<String>("shop")
                val def = inventoryDefinitions.get(shop)
                registerShop(this, def, DynamicResolvers.shopItems)
                val sample = inventoryDefinitions.getOrNull("${shop}_sample") ?: return@npcSpawn
                registerShop(this, sample, DynamicResolvers.sampleItems)
            }
        }
    }

    /**
     * Reset timeout when produce has been produced
     */
    private fun Player.resetTimeout(key: String) {
        if (!isBot || bot.noTask()) {
            return
        }
        val frame = bot.frame()
        val produces = frame.behaviour.produces
        if (produces.contains(key)) {
            frame.timeout = 0
        }
    }

    private fun registerShop(npc: NPC, definition: InventoryDefinition, map: MutableMap<String, MutableList<Pair<String, String>>>) {
        val area: String = npc.def.getOrNull("area") ?: return
        val ids = definition.ids ?: return
        val amounts = definition.amounts ?: return
        for (index in ids.indices) {
            val id = ids[index]
            val amount = amounts[index]
            if (amount <= 0) {
                continue
            }
            val def = ItemDefinitions.getOrNull(id) ?: continue
            map.getOrPut(def.stringId) { mutableListOf() }.add(area to npc.id)
        }
    }
}
