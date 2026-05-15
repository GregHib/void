package content.skill.summoning.pet

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

private const val FEED_HUNGER_REDUCTION = 15.0

class PetFeeding(private val definitions: PetDefinitions) : Script {

    init {
        val seenNpcs = mutableSetOf<String>()
        for (def in definitions.all) {
            registerFeed(def.babyNpc, seenNpcs)
            def.grownNpc?.let { registerFeed(it, seenNpcs) }
            def.overgrownNpc?.let { registerFeed(it, seenNpcs) }
        }
    }

    private fun registerFeed(npcId: String, seen: MutableSet<String>) {
        if (!seen.add(npcId)) return
        itemOnNPCOperate(npc = npcId) { interact ->
            val activeItem = get("pet_active_item", "")
            val def = definitions.forNpc(interact.target.id)
            if (def == null || activeItem.isBlank() || pet?.index != interact.target.index) {
                message("This isn't your pet.")
                return@itemOnNPCOperate
            }
            val food = interact.item.id
            if (food !in def.food) {
                message("Your pet doesn't seem interested in that.")
                return@itemOnNPCOperate
            }
            if (!inventory.remove(food)) return@itemOnNPCOperate
            anim("climb_down")
            updatePetStats(def.id) {
                hunger = (hunger - FEED_HUNGER_REDUCTION).coerceAtLeast(0.0)
                if (hunger < 75.0) {
                    warn = 0
                } else if (hunger < 90.0 && warn > 1) {
                    warn = 1
                }
            }
            sendPetDetailsStats()
            message("Your pet happily eats the ${food.replace('_', ' ')}.")
        }
    }
}
