package content.skill.melee.weapon.special

import content.entity.combat.hit.hit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.update.view.Viewport.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inject

class DragonHalberd : Script {

    val players: Players by inject()
    val npcs: NPCs by inject()

    init {
        specialAttack("sweep") { target, id ->
            anim("${id}_special")
            gfx("${id}_special")
            sound("${id}_special")
            val dir = target.tile.delta(tile).toDirection()
            val firstTile = target.tile.add(if (dir.isDiagonal()) dir.horizontal() else dir.rotate(2))
            val secondTile = target.tile.add(if (dir.isDiagonal()) dir.vertical() else dir.rotate(-2))
            val list = mutableListOf<Character>()
            list.add(target)
            val set = if (target is Player) players else npcs
            val groups = set.filter { it != target && it.tile.within(tile, VIEW_RADIUS) }.groupBy { it.tile }
            list.addAll(groups.getOrDefault(target.tile, emptyList()))
            list.addAll(groups.getOrDefault(firstTile, emptyList()))
            list.addAll(groups.getOrDefault(secondTile, emptyList()))
            list.take(if (target is Player) 3 else 10).onEach {
                hit(it)
            }
            if (target.size > 1) {
                set("second_hit", true)
                hit(target)
                clear("second_hit")
            }
        }
    }
}
