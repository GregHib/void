package world.gregs.voidps.world.activity.skill.woodcutting

import world.gregs.voidps.ai.scale
import world.gregs.voidps.ai.toDouble
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.has
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Boosted
import world.gregs.voidps.engine.entity.character.player.skill.Leveled
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.world.interact.entity.bot.*

val inventoryHatchets: BotContext.() -> List<Pair<Hatchet, IndexedValue<Item>>> = {
    bot.inventory.getItems().withIndex().mapNotNull {
        val hatchet = Hatchet.get(it.value.def.name)
        if (hatchet == null) null else Pair(hatchet, it)
    }
}

val betterThanEquippedHatchet: BotContext.(Pair<Hatchet, IndexedValue<Item>>) -> Double = { (hatchet) ->
    val currentWeapon = bot.equipment.getItemId(EquipSlot.Weapon.index)
    if (currentWeapon.isBlank()) {
        1.0
    } else {
        val current = Hatchet.get(currentWeapon)
        (current != null && current.ordinal < hatchet.ordinal).toDouble()
    }
}

val equipHatchet = SimpleBotOption(
    name = "equip better hatchet or if no weapon",
    targets = inventoryHatchets,
    considerations = listOf(betterThanEquippedHatchet),
    action = { (_, it) ->
        bot.instructions.tryEmit(InteractInterface(149, 0, it.value.id, it.index, 1))
    }
)

on<Registered>({ it.isBot }) { bot: Player ->
    bot.botOptions.add(equipHatchet)
    updateHatchetDesire(bot)
}

on<Boosted>({ it.isBot }) { bot: Player ->
    updateHatchetDesire(bot)
}

on<Leveled>({ it.isBot }) { bot: Player ->
    updateHatchetDesire(bot)
}

on<ItemChanged>({ it.isBot && Hatchet.isHatchet(item.name) }) { bot: Player ->
    updateHatchetDesire(bot)
}

fun updateHatchetDesire(bot: Player) {
    val best = (Hatchet.highest(bot)?.index ?: -1) + 1.0
    val woodcuttingDesire = bot.woodcuttingDesire
    var hasHatchet = false
    // For all hatchets best - worst
    Hatchet.regular.reversed().forEach { hatchet ->
        if (Hatchet.hasRequirements(bot, hatchet, false)) {
            if (bot.has(hatchet.id)) {
                if (hasHatchet) {
                    // Worse hatchets are undesirable
                    val uselessness = (hatchet.index + 1.0).scale(0.0, best)
                    bot.setUndesired(hatchet.id, uselessness)
                } else {
                    // No desire for owned hatchets
                    hasHatchet = true
                    bot.desiredItems.remove(hatchet.id)
                }
            } else if (!hasHatchet) {
                val desire = (hatchet.index + 1.0).scale(0.0, best) * woodcuttingDesire
                bot.setDesire(hatchet.id, desire)
            } else {
                // No desire to get rid of items not owned
                bot.undesiredItems.remove(hatchet.id)
            }
        } else {
            bot.desiredItems.remove(hatchet.id)
            bot.undesiredItems.remove(hatchet.id)
        }
    }
    println("Desires ${bot.desiredItems} ${bot.undesiredItems}")
}