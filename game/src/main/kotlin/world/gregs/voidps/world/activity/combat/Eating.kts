import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.hasOrStart
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.skill.cooking.Consume
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption
import world.gregs.voidps.world.interact.entity.sound.playSound

on<ContainerOption>({ (item.def.has("heals") || item.def.has("eaten")) && (option == "Eat" || option == "Drink" || option == "Heal") }) { player: Player ->
    val drink = option == "Drink"
    val combo = item.def.has("combo")
    val delay = when {
        combo -> "combo_eating_delay"
        drink -> "drinking_delay"
        else -> "eating_delay"
    }
    if (player.hasOrStart(delay, if (combo) 1 else 3, persist = false)) {
        return@on
    }
    val replacement = item.def["eaten", ""]
    val message = item.def["eat_message", ""]
    if (replacement.isNotEmpty()) {
        player.inventory.replace(slot, item.id, replacement)
    } else {
        player.inventory.remove(slot, item.id)
    }
    player.setAnimation("eat_drink")
    if (message.isNotEmpty()) {
        player.message(message, ChatType.GameFilter)
    } else {
        player.message("You ${if (drink) "drink" else "eat"} the ${item.def.name.lowercase()}.")
    }
    player.playSound(if (drink) "pour_tea" else "eat")
    player.events.emit(Consume(item, slot))

}

on<Consume>({ !cancel }, Priority.LOW) { player: Player ->
    val range = item.def.getOrNull("heals") as? IntRange ?: return@on
    val amount = range.random()
    if (amount > 0) {
        player.levels.restore(Skill.Constitution, amount)
    }
}