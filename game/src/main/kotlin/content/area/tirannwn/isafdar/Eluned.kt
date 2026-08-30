package content.area.tirannwn.isafdar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Eluned : Script {

    init {
        npcOperate("Talk-to", "eluned,eluned_2,eluned_3,eluned_lletya") {
            npc<Happy>("Hello there, it's a lovely day for a walk in the woods. So what can I help you with?")
            if (!inventory.contains("crystal_teleport_seed_uncharged")) {
                player<Neutral>("I'm just looking around.")
                return@npcOperate
            }
            player<Neutral>("I am looking to recharge teleportation crystals.")
            recharge()
        }

        itemOnNPCOperate("crystal_teleport_seed_uncharged", "eluned,eluned_2,eluned_3,eluned_lletya") {
            recharge()
        }
    }

    suspend fun Player.recharge() {
        val price = rechargePrice()
        npc<Happy>("Very well. I'll recharge your teleportation crystal for $price gold. What do you say?")
        choice {
            option<Neutral>("Recharge a crystal.") {
                if (!inventory.contains("coins", price)) {
                    player<Sad>("Actually, I don't have enough coins.")
                    return@option
                }
                if (inventory.remove("coins", price) && inventory.remove("crystal_teleport_seed_uncharged")) {
                    inventory.add("crystal_teleport_seed_4")
                    inc("teleport_crystal_recharges")
                    statement("Eluned recharges your elven teleportation crystal for $price gold.")
                }
            }
            option<Neutral>("Nevermind, I really must be going.")
        }
    }

    fun Player.rechargePrice(): Int = (750 - 150 * get("teleport_crystal_recharges", 0)).coerceAtLeast(150)
}
