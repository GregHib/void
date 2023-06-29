package world.gregs.voidps.world.activity.skill.prayer

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.remove
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Distance.nearestTo
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.world.interact.dialogue.type.makeAmount
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic

on<ItemOnObject>({ operate && container == "inventory" && item.def.has("prayer_xp") && obj.id.startsWith("altar") }) { player: Player ->
    arriveDelay()
    val tile = obj.nearestTo(player.tile)
    val count = player.inventory.count(item.id)
    if (count > 1) {
        val (_, amount) = makeAmount(listOf(item.id), "", count)
        offer(amount, tile)
    } else {
        offer(1, tile)
    }
}

suspend fun ItemOnObject.offer(amount: Int, tile: Tile) {
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
            pause(2)
        } else {
            player.mode = EmptyMode
        }
    }
}