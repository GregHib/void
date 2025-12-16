package content.skill.farming

import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.*

class FarmingPatchCure : Script {

    init {
        itemOnObjectOperate("plant_cure", "*", handler = ::plantCure)
        itemOnObjectOperate("secateurs,magic_secateurs", "*_tree_diseased*", handler = ::prune)
    }

    private suspend fun prune(player: Player, interact: ItemOnObjectInteract) {
        val target = interact.target
        if (!target.id.startsWith("farming_tree_patch")) {
            return
        }
        player.message("You start pruning the tree...", type = ChatType.Filter)
        player.anim("pruning_all_fairy")
        player.sound("farming_prune", repeat = 2)
        player.delay(4)
        val type = target.def(player).stringId.substringBefore("_")
        player.message("You have successfully removed all the diseased leaves.", type = ChatType.Filter)
        player.addOrDrop("leaves_$type")
        player[target.id] = player[target.id, "weeds_3"].replace("_diseased", "")
    }

    private suspend fun plantCure(player: Player, interact: ItemOnObjectInteract) {
        val target = interact.target
        if (!target.id.startsWith("farming_")) {
            return
        }
        if (target.id.startsWith("farming_tree_patch")) {
            player.message("To cure trees you need to prune the diseased leaves away with a pair of secateurs.")
            return
        }
        if (!player.inventory.contains("plant_cure")) {
            player.message("You need a plant cure to cure the disease on this patch.")
            return
        }
        if (!target.def(player).stringId.contains("_diseased")) {
            player.message("This patch doesn't need curing.")
            return
        }
        player.message("You treat the ${target.patchName()} with the plant cure.", type = ChatType.Filter)
        player.sound("farming_plant_cure")
        player.anim("farming_plant_cure")
        player.delay(2)
        if (player.inventory.replace("plant_cure", "vial")) {
            player.message("It is restored to health.", type = ChatType.Filter)
            player[target.id] = player[target.id, "weeds_3"].replace("_diseased", "")
        }
    }
}
