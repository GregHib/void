package content.quest.free.vampire_slayer

import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class GarlicDrawers : Script {
    init {
        objectOperate("Open", "morgan_garlic_drawers") { search() }
    }

    private suspend fun Player.search() {
        if (!inventory.add("garlic")) {
            message("You don't have enough inventory space to take the garlic.")
            return
        }
        statement("You search the drawers and find a clove of garlic.")
    }
}
