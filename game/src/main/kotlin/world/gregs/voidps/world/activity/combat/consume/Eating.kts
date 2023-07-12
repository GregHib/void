package world.gregs.voidps.world.activity.combat.consume

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.contain.clear
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.remove
import world.gregs.voidps.engine.contain.replace
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption
import world.gregs.voidps.world.interact.entity.sound.playSound

on<InventoryOption>({ (item.def.has("heals") || item.def.has("excess")) && (option == "Eat" || option == "Drink" || option == "Heal") }) { player: Player ->
    val drink = option == "Drink"
    val combo = item.def.has("combo")
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
        return@on
    }
    player.start(delay, ticks)
    val consumable = Consumable(item)
    player.events.emit(consumable)
    if (consumable.cancelled) {
        return@on
    }
    val replacement = item.def["excess", ""]
    val message = item.def["eat_message", ""]
    if (replacement.isNotEmpty()) {
        player.inventory.replace(slot, item.id, replacement)
    } else {
        if (player.inventory.stackable(item.id)) {
            player.inventory.remove(item.id)
        } else {
            player.inventory.clear(slot)
        }
    }
    player.setAnimation("eat_drink")
    if (message.isNotEmpty()) {
        player.message(message, ChatType.Filter)
    } else {
        player.message("You ${if (drink) "drink" else "eat"} the ${item.def.name.lowercase()}.")
    }
    player.playSound(if (drink) "pour_tea" else "eat")
    player.events.emit(Consume(item, slot))
}

on<Consume>(priority = Priority.LOW) { player: Player ->
    val range: IntRange = item.def.getOrNull("heals") ?: return@on
    val amount = range.random()
    if (amount > 0) {
        player.levels.restore(Skill.Constitution, amount)
    }
}