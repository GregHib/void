package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.follower
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class GeyserTitan : Script {
    init {
        // A bowl held up to the geyser fills with steaming water.
        itemOnNPCOperate("bowl", "geyser_titan_familiar") { (npc) ->
            if (npc != follower) {
                message("That's not your familiar.")
                return@itemOnNPCOperate
            }
            if (inventory.replace("bowl", "bowl_of_hot_water")) {
                message("You hold the bowl up to the geyser titan and it fills with boiling water.", ChatType.Filter)
            }
        }

        // The titan's waters recharge amulets of glory, as the Fountain of Heroes does.
        for (glory in listOf("amulet_of_glory", "amulet_of_glory_1", "amulet_of_glory_2", "amulet_of_glory_3")) {
            itemOnNPCOperate(glory, "geyser_titan_familiar") { (npc, item) ->
                if (npc != follower) {
                    message("That's not your familiar.")
                    return@itemOnNPCOperate
                }
                if (inventory.replace(item.id, "amulet_of_glory_4")) {
                    message("You feel a power emanating from the geyser titan as it recharges your amulet.", ChatType.Filter)
                }
            }
        }

        npcOperate("Interact", "geyser_titan_familiar") {
            npc<Neutral>("Did you know a snail can sleep up to three years?")
            player<Happy>("I wish I could do that. Ah...sleep.")
        }
    }
}
