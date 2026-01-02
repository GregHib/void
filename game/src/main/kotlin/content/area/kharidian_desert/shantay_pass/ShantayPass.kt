package content.area.kharidian_desert.shantay_pass

import content.entity.player.bank.bank
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.entity.player.dialogue.type.warning
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class ShantayPass : Script {

    init {
        objectOperate("Go-through", "shantay_pass") {
            val exit = tile.y <= 3116
            if (!exit) {
                if (!inventory.remove("shantay_pass")) {
                    npc<Talk>("shantay_guard_still", "You need a Shantay pass to get through this gate. See Shantay, he will sell you one for a very reasonable price.")
                    return@objectOperate
                }
                if (!warning("shantay_pass")) {
                    return@objectOperate
                }
                message("The guard takes your Shantay Pass as you go through the gate.")
            }
            pass(exit)
        }

        objectOperate("Look-at", "shantay_pass") {
            statement("You look at the huge stone gate.<br>Near the gate is a large billboard poster, it reads:")
            statement("<red>The Desert is a VERY Dangerous place. Do not enter if you are <red>afraid of dying. Beware of high temperatures, sand storms, robbers, <red>and slavers. No responsibility is take by Stantay if anything bad <red>should happen to you in any circumstances whatsoever.")
            statement("Despite this warning lots of people seem to pass through the gate.")
        }

        objectOperate("Open", "shantay_chest") {
            open("bank")
        }

        npcOperate("Bribe", "shantay_guard_still") {
            //youtu.be/qGX2YLs1Pb0?t=2457
            if (!inventory.contains("coins", 200)) {
                choice("The guard won't let you through without a pass.") {
                    option("Offer him 200 coins from your bank.") {
                        if (bank.remove("coins", 200)) {
                            pass(false)
                        } else {
                            // TODO
                        }
                    }
                    option("Stop")
                }
                return@npcOperate
            }
            // TODO
        }
    }

    private suspend fun Player.pass(exit: Boolean) {
        val west = tile.x < 3304
        walkToDelay(Tile(if (west) 3303 else 3305, if (exit) 3116 else 3117))
        walkOverDelay(Tile(if (west) 3303 else 3305, if (exit) 3117 else 3116))
    }
}