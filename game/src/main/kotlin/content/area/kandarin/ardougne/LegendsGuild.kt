package content.area.kandarin.ardougne

import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class LegendsGuild : Script {

    init {
        objectOperate("Look", "legends_guild_totem_pole") {
            if (inventory.contains("combat_bracelet") && inventory.replace("combat_bracelet", "combat_bracelet_4")) {
                combatBracelet()
            } else if (inventory.contains("skills_necklace") && inventory.replace("skills_necklace", "skills_necklace_4")) {
                skillsNecklace()
            } else {
                statement("This totem pole is truly awe inspiring. It depicts powerful Karamjan animals. It is very well carved and brings a sense of power and spiritual fulfilment to anyone who looks at it.")
                message("You don't have any jewellery that the totem can recharge.")
            }
        }

        itemOnObjectOperate("combat_bracelet", "legends_guild_totem_pole") {
            if (player.inventory.replace(itemSlot, item.id, "combat_bracelet_4")) {
                player.combatBracelet()
            }
        }

        itemOnObjectOperate("skills_necklace", "legends_guild_totem_pole") {
            if (player.inventory.replace(itemSlot, item.id, "skills_necklace_4")) {
                player.skillsNecklace()
            }
        }
    }

    suspend fun Player.combatBracelet() {
        message("You touch the jewellery against the totem pole...")
        anim("human_pickupfloor")
        item("combat_bracelet", 300, "You feel a power emanating from the totem pole as it recharges your bracelet. You can now rub the bracelet to teleport and wear it to get information while on a Slayer assignment.")
    }

    suspend fun Player.skillsNecklace() {
        message("You touch the jewellery against the totem pole...")
        anim("human_pickupfloor")
        item("skills_necklace", 200, "You feel a power emanating from the totem pole as it recharges your necklace. You can now rub the necklace to teleport and wear it to get more caskets while big net Fishing.")
    }
}
