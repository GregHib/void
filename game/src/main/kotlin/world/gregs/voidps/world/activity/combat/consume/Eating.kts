package world.gregs.voidps.world.activity.combat.consume

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.hasOrStart
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption
import world.gregs.voidps.world.interact.entity.sound.playSound

on<ContainerOption>({ (item.def.has("heals") || item.def.has("excess")) && (option == "Eat" || option == "Drink" || option == "Heal") }) { player: Player ->
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
    if (player.hasOrStart(delay, ticks, persist = false)) {
        return@on
    }
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
        player.inventory.remove(slot, item.id)
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