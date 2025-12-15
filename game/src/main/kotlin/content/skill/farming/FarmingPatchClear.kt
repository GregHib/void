package content.skill.farming

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.weakQueue

class FarmingPatchClear : Script {
    init {
        objectOperate("Clear", "*_tree_farming_stump") { (target) ->
            message("You start digging up the tree stump.", type = ChatType.Filter)
            clear(this, target.id, stump = true)
        }

        itemOnObjectOperate("spade", "*_tree_farming_stump") { (target) ->
            message("You start digging up the tree stump.", type = ChatType.Filter)
            clear(this, target.id, stump = true)
        }

        objectOperate("Clear", "*_dead") { (target) ->
            message("You start digging the farming patch...", type = ChatType.Filter)
            clear(this, target.id)
        }

        itemOnObjectOperate("spade", "*_dead") { (target) ->
            message("You start digging the farming patch...", type = ChatType.Filter)
            clear(this, target.id)
        }
    }

    private fun clear(player: Player, variable: String, stump: Boolean = false) {
        if (!player.inventory.contains("spade")) {
            player.message("You need a spade to clear a farming patch.")
            return
        }
        player.anim("human_dig")
        player.sound("dig_spade")
        player.weakQueue("clear_patch", 2) {
            if (Level.success(player.levels.get(Skill.Farming), 60)) { // TODO proper chances
                if (stump) {
                    player.message("You dig up the tree stump.", type = ChatType.Filter)
                } else {
                    player.message("You have successfully cleared this patch for new crops.", type = ChatType.Filter)
                }
                player[variable] = "weeds_0"
            } else {
                clear(player, variable)
            }
        }
    }
}
