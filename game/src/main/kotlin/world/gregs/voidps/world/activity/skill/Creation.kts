import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnInterface
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.inventoryFull
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.definition.data.Making
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.dialogue.type.makeAmount
import world.gregs.voidps.world.interact.entity.sound.playSound

val definitions: ItemDefinitions by inject()

val Item.creates: List<String>
    get() = def["creates", emptyList()]

on<InterfaceOnInterface>({ fromItem.def.has("creates") && toItem.def.has("creates") }) { player: Player ->
    if (player.hasEffect("skilling_delay")) {
        return@on
    }
    val overlaps = fromItem.creates.filter { toItem.creates.contains(it) }
    if (overlaps.isEmpty()) {
        return@on
    }
    player.action(ActionType.Cooking) {
        player.dialogue {
            val m: List<Making> = definitions.get(overlaps.first())["make"]
            val type = when (m.first().skill) {
                Skill.Cooking -> "cook"
                else -> "make"
            }
            val (overlap, amount) = makeAmount(
                overlaps,
                type = type,
                maximum = player.inventory.getCount(fromItem.id).toInt(),// TODO
                text = "How many would you like to $type?"
            )

            val definition = definitions.get(overlap)
            val list: List<Making> = definition["make"]
            if (amount <= 0) {
                return@dialogue
            }
            val making = list.firstOrNull { m -> m.remove.any { it.id == fromItem.id } && m.remove.any { it.id == toItem.id } } ?: return@dialogue
            val skill = making.skill ?: return@dialogue
            player.action(ActionType.Cooking) {
                try {
                    var count = 0
                    loop@ while (isActive && count < amount) {
                        if (!player.has(skill, making.level, true)) {
                            break
                        }

                        if (player.inventory.spaces - making.remove.size + making.add.size < 0) {
                            player.inventoryFull()
                            break
                        }
                        for (item in making.requires) {
                            if (!player.inventory.contains(item.id, item.amount)) {
                                player.message("You need a ${item.def.name} to $type this.")// TODO
                                break@loop
                            }
                        }
                        for (item in making.remove) {
                            if (!player.inventory.contains(item.id, item.amount)) {
                                player.message("You don't have enough ${item.def.name} to $type this.")// TODO
                                break@loop
                            }
                        }
                        if (making.animation.isNotEmpty()) {
                            player.setAnimation(making.animation)
                        }
                        if (making.graphic.isNotEmpty()) {
                            player.setGraphic(making.graphic)
                        }
                        if (making.sound.isNotEmpty()) {
                            player.playSound(making.sound)
                        }
                        if (making.ticks > 0) {
                            if (count == 0) {
                                player.start("skilling_delay", making.ticks)
                            }
                            delay(making.ticks)
                        }
                        count++
                        for (item in making.remove) {
                            player.inventory.remove(item.id, item.amount)
                        }
                        for (item in making.add) {
                            player.inventory.add(item.id, item.amount)
                        }
                        if (making.message.isNotEmpty()) {
                            player.message(making.message)
                        }
                    }
                    player.awaitDialogues()
                } finally {
                    player.clearAnimation()
                }
            }
        }
    }
}