import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnInterface
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.definition.ItemOnItemDefinitions
import world.gregs.voidps.engine.entity.definition.config.ItemOnItemDefinition
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.capitalise
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.dialogue.type.makeAmount
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.math.max

val itemOnItem: ItemOnItemDefinitions by inject()

on<InterfaceOnInterface>({ itemOnItem.contains(fromItem, toItem) }) { player: Player ->
    if (player.hasEffect("skilling_delay")) {
        return@on
    }
    val overlaps = itemOnItem.get(fromItem, toItem)
    if (overlaps.isEmpty()) {
        return@on
    }
    player.action(ActionType.Making) {
        val maximum = getMaximum(overlaps, player)
        val (def, amount) = if (makeImmediately(player, overlaps, maximum)) {
            player.closeDialogue()
            overlaps.first() to 1
        } else {
            val type = overlaps.first().type
            val (selection, amount) = player.makeAmount(
                overlaps.map { it.add.first().id }.distinct().toList(),
                type = type.capitalise(),
                maximum = maximum,
                text = "How many would you like to $type?"
            )
            overlaps.first { it.add.first().id == selection } to amount
        }
        val skill = def.skill
        try {
            var count = 0
            loop@ while (isActive && count < amount && player.awaitDialogues()) {
                if (skill != null && !player.has(skill, def.level, true)) {
                    break
                }

                if (player.inventory.spaces - def.remove.size + def.add.size < 0) {
                    player.inventoryFull()
                    break
                }

                for (item in def.requires) {
                    if (!player.inventory.contains(item.id, item.amount)) {
                        player.message("You need a ${item.def.name.lowercase()} to $type this.")
                        break@loop
                    }
                }
                for (item in def.remove) {
                    if (!player.inventory.contains(item.id, item.amount)) {
                        player.message("You don't have enough ${item.def.name.lowercase()} to $type this.")
                        break@loop
                    }
                }
                if (count == 0) {
                    player.start("skilling_delay", def.delay)
                    delay(def.delay)
                } else {
                    delay(def.ticks)
                }
                def.animation.let { player.setAnimation(it) }
                def.graphic.let { player.setGraphic(it) }
                def.sound.let { player.playSound(it) }
                def.message.let { player.message(it, ChatType.Filter) }
                var used = false
                for (i in 0 until max(def.remove.size, def.add.size)) {
                    val remove = def.remove.getOrNull(i)
                    val add = def.add.getOrNull(i)
                    val index = when {
                        count > 0 -> -1
                        !used && toItem == remove -> {
                            used = true
                            toSlot
                        }
                        fromItem == remove -> fromSlot
                        else -> -1
                    }
                    if (remove != null && add != null) {
                        if (index == -1) {
                            player.inventory.replace(remove.id, add.id)
                        } else {
                            player.inventory.replace(index, remove.id, add.id)
                        }
                    } else if (remove != null) {
                        if (index == -1) {
                            player.inventory.remove(remove.id, remove.amount)
                        } else {
                            player.inventory.remove(index, remove.id, remove.amount)
                        }
                    } else if (add != null) {
                        player.inventory.add(add.id, add.amount)
                    }
                }
                count++
            }
        } finally {
            player.clearAnimation()
        }
    }
}

on<InterfaceClosed>({ id == "dialogue_skill_creation" }) { player: Player ->
    player.clear("selecting_amount")
}

on<InterfaceOpened>({ id == "dialogue_skill_creation" }) { player: Player ->
    player["selecting_amount"] = true
}

fun makeImmediately(player: Player, overlaps: List<ItemOnItemDefinition>, maximum: Int): Boolean {
    return (overlaps.size == 1 && maximum == 1) || player["selecting_amount", false] || player.hasEffect("in_combat")
}

fun getMaximum(overlaps: List<ItemOnItemDefinition>, player: Player): Int {
    var max = 0
    for (overlap in overlaps) {
        val min = overlap.remove.distinct().minOf { item ->
            val count = player.inventory.getCount(item).toInt()
            val required = overlap.remove.filter { it.id == item.id }.sumOf { it.amount }
            count / required
        }
        if (min > max) {
            max = min
        }
    }
    return max
}