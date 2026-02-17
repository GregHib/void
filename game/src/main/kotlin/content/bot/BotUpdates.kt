package content.bot

import com.github.michaelbull.logging.InlineLogger
import content.bot.behaviour.setup.DynamicResolvers
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Listen for state changes which would change which activities are available to a bot
 */
class BotUpdates : Script {
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

        interfaceOpened("*") {
            if (isBot) {
                bot.evaluate.add("iface:$it")
            }
        }

        interfaceClosed("*") {
            if (isBot) {
                bot.evaluate.add("iface:$it")
            }
        }

        entered("*") {
            if (isBot) {
                bot.evaluate.add("area:${it.name}")
                resetTimeout("area:${it.name}")
            }
        }

        exited("*") {
            if (isBot) {
                bot.evaluate.add("area:${it.name}")
            }
        }

        // Reset timeout when something is produced

        variableSet { key, from, to ->
            if (isBot && from != to) {
                bot.evaluate.add("var:$key")
                resetTimeout("variable:$key")
            }
        }

        itemAdded(inventory = "inventory") {
            resetTimeout("item:${it.item.id}")
        }

        itemRemoved(inventory = "inventory") {
            resetTimeout("item:empty")
        }

        experience { skill, _, _ ->
            resetTimeout("skill:${skill.name.lowercase()}")
        }

        // Close level-up dialogues
        interfaceOpened("dialogue_level_up") {
            if (isBot) {
                close("dialogue_level_up")
            }
        }

        // Register shops
        worldSpawn {
            DynamicResolvers.shopItems.clear()
            DynamicResolvers.sampleItems.clear()
        }

        npcSpawn {
            if (!def.contains("shop")) {
                return@npcSpawn
            }
            val shop = def.get<String>("shop")
            val def = InventoryDefinitions.get(shop)
            registerShop(this, def, DynamicResolvers.shopItems)
            val sample = InventoryDefinitions.getOrNull("${shop}_sample") ?: return@npcSpawn
            registerShop(this, sample, DynamicResolvers.sampleItems)
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
