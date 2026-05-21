package content.skill.summoning.pet

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

private const val FEED_HUNGER_REDUCTION = 1500
private const val WARN_HUNGRY = 7500
private const val WARN_STARVING = 9000

class PetFeeding : Script {

    init {
        val npcIds = allPetRows().flatMap {
            listOfNotNull(it.npcOrNull("baby_npc"), it.npcOrNull("grown_npc"), it.npcOrNull("overgrown_npc"))
        }.toSet().joinToString(",")

        itemOnNPCOperate(npc = npcIds) { interact ->
            val activeItem = get("pet_active_item", "")
            val row = petRowForNpc(interact.target.id)
            if (row == null || activeItem.isBlank() || pet?.index != interact.target.index) {
                message("This isn't your pet.")
                return@itemOnNPCOperate
            }
            val food = interact.item.id
            if (food !in row.itemList("food")) {
                message("Your pet doesn't seem interested in that.")
                return@itemOnNPCOperate
            }
            if (!inventory.remove(food)) return@itemOnNPCOperate
            anim("climb_down")
            val newHunger = dec("pet_${row.rowId}_hunger", FEED_HUNGER_REDUCTION)
            if (newHunger < WARN_HUNGRY) {
                set("pet_${row.rowId}_warn", 0)
            } else if (newHunger < WARN_STARVING && getPetWarn(row.rowId) > 1) {
                set("pet_${row.rowId}_warn", 1)
            }
            sendPetDetailsStats()
            message("Your pet happily eats the ${food.replace('_', ' ')}.")
        }
    }
}
