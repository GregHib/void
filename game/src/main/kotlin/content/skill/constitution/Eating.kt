package content.skill.constitution

import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Publishers
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.type.Script
import world.gregs.voidps.type.sub.Inventory

@Script
class Eating {

    val publishers: Publishers by inject()

    @world.gregs.voidps.type.sub.Consume
    fun consume(player: Player, item: Item): Boolean {
        val range: IntRange = item.def.getOrNull("heals") ?: return false
        val amount = range.random()
        if (amount > 0) {
            if (player.levels.restore(Skill.Constitution, amount) > 0) {
                player["om_nom_nom_nom_task"] = true
            }
        }
        return false
    }

    @Inventory("Heal")
    @Inventory("Drink")
    @Inventory("Eat")
    fun eat(player: Player, option: String, item: Item, slot: Int) {
        if (!item.def.contains("heals") && !item.def.contains("excess")) {
            return
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
            return
        }
        player.start(delay, ticks)
        if (!Publishers.all.consume(player, item, slot)) {
            return
        }
        val consumable = Consumable(item)
        player.emit(consumable)
        if (consumable.cancelled) {
            return
        }
        val replacement = item.def["excess", ""]
        val message = item.def["eat_message", ""]
        val smash = player["vial_smashing", false] && replacement == "vial"
        if (replacement.isEmpty() || smash) {
            player.inventory.remove(slot, item.id)
        } else {
            player.inventory.replace(slot, item.id, replacement)
        }
        player.anim("eat_drink")
        if (message.isNotEmpty()) {
            player.message(message, ChatType.Filter)
        } else {
            player.message("You ${if (drink) "drink" else "eat"} the ${item.def.name.lowercase()}.", ChatType.Filter)
        }
        player.sound(if (drink) "drink" else "eat")
        player.emit(Consume(item, slot))
        if (smash) {
            player.message("You quickly smash the empty vial using the trick a Barbarian taught you.", ChatType.Filter)
        }
    }
}
