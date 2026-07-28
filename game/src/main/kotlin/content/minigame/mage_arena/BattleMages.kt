package content.minigame.mage_arena

import content.skill.melee.weapon.fightStyle
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class BattleMages : Script {

    init {
        huntPlayer(mode = "battle_mage") { target ->
            if (target.equipped(EquipSlot.Cape).id == "${def["god", ""]}_cape") {
                return@huntPlayer
            }
            if (target.contains("kolodion_index")) {
                return@huntPlayer
            }
            interactPlayer(target, "Attack")
        }

        canAttack("battle_mage_*") { _ ->
            if (fightStyle != "magic") {
                message("You can only use magic in the arena!")
                false
            } else {
                true
            }
        }
    }
}
