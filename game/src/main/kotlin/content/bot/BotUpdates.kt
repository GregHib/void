package content.bot

import world.gregs.voidps.engine.Script

class BotUpdates(val manager: BotManager) : Script {
    init {
        levelChanged { skill, _, _ ->
            if (isBot) {
                manager.update(bot, skill.name)
            }
        }

//        moved { from ->
//            if (isBot && tile != from) {
//                manager.update(bot, "tile")  // FIXME expensive
//            }
//        }

        variableSet { key, from, to ->
            if (isBot && from != to) {
                manager.update(bot, "var:$key")
            }
        }

        inventoryUpdated { inventory, _ ->
            if (isBot) {
                manager.update(bot, "inv:$inventory")
            }
        }

        entered("*") {
            if (isBot) {
                manager.update(bot, "enter:${it.name}")
            }
        }
    }
}