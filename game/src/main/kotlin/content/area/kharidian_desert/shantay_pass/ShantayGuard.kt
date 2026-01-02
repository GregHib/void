package content.area.kharidian_desert.shantay_pass

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class ShantayGuard(val objects: GameObjects) : Script {
    init {
        npcOperate("Talk-to", "shantay_guard") {
            npc<Talk>("Go talk to Shantay. I'm on duty and I don't have time to talk to the likes of you!")
            message("The guard seems quite bad tempered, probably from having to wear heavy armour in this intense heat.")
        }

        npcOperate("Talk-to", "shantay_guard_still") {
            npc<Happy>("Hello there! What can I do for you?")
            choice {
                option<Talk>("I'd like to go into the desert please.") {
                    if (!inventory.contains("shantay_pass")) {
                        npc<Talk>("You need a Shantay pass to get through this gate. See Shantay, he will sell you one for a very reasonable price.")
                        return@option
                    }
                    interactObject(objects[Tile(3303, 3116), "shantay_pass"]!!, "Go-through")
                }
                option<Talk>("Nothing thanks.") {
                    npc<Talk>("Okay then, have a nice day.")
                }
            }
        }
    }
}
