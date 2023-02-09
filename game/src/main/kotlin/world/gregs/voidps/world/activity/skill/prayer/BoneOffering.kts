package world.gregs.voidps.world.activity.skill.prayer

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObjectClick
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.remove
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.hasOrStart
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.world.interact.dialogue.type.makeAmount
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic

on<InterfaceOnObjectClick>({ gameObject.id.startsWith("altar") }) { player: Player ->
    cancelled = player.hasEffect("skilling_delay")
}

on<InterfaceOnObject>({ container == "inventory" && item.def.has("prayer_xp") && obj.id.startsWith("altar") }) { player: Player ->
    val tile = Distance.getNearest(obj.tile, obj.size, player.tile)
    val count = player.inventory.count(item.id)
    if (count > 1) {
        val (_, amount) = makeAmount(listOf(item.id), "", count)
        offer(amount, tile)
    } else {
        offer(1, tile)
    }
}

suspend fun InterfaceOnObject.offer(amount: Int, tile: Tile) {
    val xp = item.def["prayer_xp", 0.0]
    try {
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
                pause(2)
            } else {
                player.mode = EmptyMode
            }
        }
    } finally {
        player.start("skilling_delay", 3)
    }
}