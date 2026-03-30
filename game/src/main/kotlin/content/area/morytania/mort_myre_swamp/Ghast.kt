package content.area.morytania.mort_myre_swamp

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class Ghast : Script {
    init {
        npcCondition("has_food") {
            hasFood(it)
        }
        npcCondition("has_no_food") {
            !hasFood(it)
        }

        npcAttack("ghast", "miss") {
            mode = Retreat(this, it)
        }

        npcAttack("ghast", "energy") {
            mode = Retreat(this, it)
        }

        npcAttack("ghast", "rot") {
            rotFood(it)
            mode = Retreat(this, it)
        }
    }

    fun hasFood(character: Character): Boolean {
        if (character !is Player) {
            return false
        }
        for (item in character.inventory.items) {
            if (item.isEmpty()) {
                continue
            }
            if (item.def.options.contains("Eat")) {
                return true
            }
        }
        return false
    }

    fun rotFood(character: Character) {
        if (character !is Player) {
            return
        }
        for (item in character.inventory.items) {
            if (item.isEmpty()) {
                continue
            }
            if (item.def.options.contains("Eat")) {
                character.inventory.replace(item.id, "rotten_food")
                return
            }
        }
    }
}
