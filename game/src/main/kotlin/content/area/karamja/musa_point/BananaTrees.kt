package content.area.karamja.musa_point

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class BananaTrees : Script {

    init {
        objectOperate("Pick", "karamja_banana_tree_1,karamja_banana_tree_2,karamja_banana_tree_3,karamja_banana_tree_4,karamja_banana_tree_5") { (target) ->
            if (!inventory.add("banana")) {
                inventoryFull()
                return@objectOperate
            }
            sound("pick")
            anim("take")
            message("You pick a banana.")

            val remaining = target.id.removePrefix("karamja_banana_tree_").toInt() - 1
            // Replaced permanently with a single re-armed timer; removing the replacement reverts
            // the tree all the way back to the original full one rather than only one stage.
            GameObjects.timers.cancel(target)
            val replacement = target.replace("karamja_banana_tree_$remaining")
            GameObjects.timers.add(replacement, Settings["world.objs.bananaTree.regrowTicks", 300]) {
                GameObjects.remove(replacement, collision = true)
            }

            if (!get("five_a_day_task", false) && inc("five_a_day_progress") >= 5) {
                set("five_a_day_task", true)
            }
        }

        objectOperate("Search", "karamja_banana_tree_0") {
            message("There are no bananas left on the tree.")
        }
    }
}
