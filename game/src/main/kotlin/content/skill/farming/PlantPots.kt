package content.skill.farming

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.weakQueue

class PlantPots : Script {
    init {
        itemOnObjectOperate("plant_pot_empty", "*_patch_weeds_*") {
            message("This patch needs weeding first.")
        }
        itemOnObjectOperate("plant_pot_empty", "*_patch_weeded", handler = ::plantPot)
        itemOnItem("watering_can_0", "*_seedling") { _, _ ->
            message("You need to fill the watering can first.")
        }
    }

    private fun plantPot(player: Player, interact: ItemOnObjectInteract) {
        if (!player.inventory.contains("plant_pot_empty")) {
            return
        }
        if (!player.inventory.contains("gardening_trowel")) {
            player.message("You need a gardening trowel to do that.")
            return
        }
        val value = player[interact.target.id, "weeds_life3"]
        if (value != "weeds_0" && value.startsWith("weeds_3")) {
            player.message("This patch needs weeding first.")
            return
        } else if (value != "weeds_0") {
            player.noInterest()
            return
        }
        player.anim("farming_trowel_digging")
        player.weakQueue("fill_plant_pot", 2) {
            if (!player.inventory.replace("plant_pot_empty", "plant_pot")) {
                return@weakQueue
            }
            plantPot(player, interact)
        }
    }
}
