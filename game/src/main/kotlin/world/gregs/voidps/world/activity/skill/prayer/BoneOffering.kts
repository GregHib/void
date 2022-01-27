package world.gregs.voidps.world.activity.skill.prayer

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObjectClick
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.hasOrStart
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.world.interact.dialogue.type.makeAmount
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic


on<InterfaceOnObjectClick>({ gameObject.id.startsWith("altar") }) { player: Player ->
    cancelled = player.hasEffect("skilling_delay")
}

on<InterfaceOnObject>({ container == "inventory" && item.def.has("prayer_xp") && obj.id.startsWith("altar") }) { player: Player ->
    val tile = Distance.getNearest(obj.tile, obj.size, player.tile)
    val count = player.inventory.getCount(item.id).toInt()
    if (count > 1) {
        player.dialogue {
            val (_, amount) = makeAmount(listOf(item.id), "", count)
            offer(player, item, itemSlot, amount, tile)
        }
    } else {
        offer(player, item, itemSlot, 1, tile)
    }
}

fun offer(player: Player, item: Item, index: Int, amount: Int, tile: Tile) {
    player.action(ActionType.Burying) {
        try {
            val xp = item.def["prayer_xp", 0.0]
            repeat(amount) {
                if (player.inventory.remove(item.id)) {
                    player.experience.add(Skill.Prayer, xp)
                    player.setAnimation("offer_bones")
                    areaGraphic("bone_offering", tile)
                    player.message("The gods ${
                        when {
                            xp <= 25 -> "accept"
                            xp <= 100 -> "are pleased with"
                            else -> "are very pleased with"
                        }
                    } your offering.", ChatType.Filter)
                    player.hasOrStart("skilling_delay", 2)
                    delay(2)
                } else {
                    cancel(ActionType.Burying)
                }
            }
        } finally {
            player.start("skilling_delay", 3)
        }
    }
}