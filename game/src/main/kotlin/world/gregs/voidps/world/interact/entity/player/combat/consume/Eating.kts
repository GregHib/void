package world.gregs.voidps.world.interact.entity.player.combat.consume

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.world.interact.entity.player.equip.inventoryOptions
import world.gregs.voidps.world.interact.entity.sound.playSound

inventoryOptions("Eat", "Drink", "Heal") {
    if (!item.def.contains("heals") && !item.def.contains("excess")) {
        return@inventoryOptions
    }
    val drink = option == "Drink"
    val combo = item.def.contains("combo")
    val delay = when {
        combo -> "combo_delay"
        drink -> "drink_delay"
        else -> "food_delay"
    }
    val ticks = when {
        combo -> 1
        drink -> 2
        else -> 3
    }
    if (player.hasClock(delay)) {
        return@inventoryOptions
    }
    player.start(delay, ticks)
    val consumable = Consumable(item)
    player.emit(consumable)
    if (consumable.cancelled) {
        return@inventoryOptions
    }
    val replacement = item.def["excess", ""]
    val message = item.def["eat_message", ""]
    if (replacement.isNotEmpty()) {
        player.inventory.replace(slot, item.id, replacement)
    } else {
        player.inventory.remove(slot, item.id)
    }
    player.setAnimation("eat_drink")
    if (message.isNotEmpty()) {
        player.message(message, ChatType.Filter)
    } else {
        player.message("You ${if (drink) "drink" else "eat"} the ${item.def.name.lowercase()}.")
    }
    player.playSound(if (drink) "pour_tea" else "eat")
    player.emit(Consume(item, slot))
}

consume { player ->
    val range: IntRange = item.def.getOrNull("heals") ?: return@consume
    val amount = range.random()
    if (amount > 0) {
        if (player.levels.restore(Skill.Constitution, amount) > 0) {
            player["om_nom_nom_nom_task"] = true
        }
    }
}