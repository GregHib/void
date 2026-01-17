package content.area.wilderness

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.move
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.type.random

class ChaosElemental : Script {

    init {
        npcCondition("free_inventory_spaces") { target -> target is Player && !target.inventory.isFull() }

        npcCondition("free_inventory_spaces") { target -> target is Player && !target.hasClock("confusion_cooldown") }

        npcImpact("chaos_elemental", "confusion") { target ->
            val tile = tile.toCuboid(10).random(target) ?: return@npcImpact false
            target.tele(tile)
            target.start("confusion_cooldown", 10)
            true
        }

        npcImpact("chaos_elemental", "madness") { target ->
            if (target is Player) {
                for (i in 0 until random.nextInt(1, 4)) {
                    if (target.inventory.isFull()) {
                        break
                    }
                    val (slot, item) = target.equipment.items
                        .withIndex()
                        .filter { it.value.isNotEmpty() }
                        .randomOrNull(random) ?: break
                    target.equipment.move(slot, target.inventory)
                }
            }
            true
        }
    }
}
