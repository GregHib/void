package content.area.wilderness.abyss

import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.canFit
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class AbyssalDemon(private val stepValidator: StepValidator) : Script {

    init {
        npcCombatDamage("abyssal_demon") { (source) ->
            if (random.nextInt(6) == 0) {
                val tile = teleportTile(source.tile) ?: return@npcCombatDamage
                tele(tile, clearMode = false)
                anim("abyssal_demon_teleport")
                gfx("abyssal_demon_teleport")
                sound("abyssal_demon_teleport")
            } else if (random.nextInt(3) == 0) {
                val tile = source.teleportTile(tile) ?: return@npcCombatDamage
                source.tele(tile)
                source.gfx("abyssal_demon_teleport")
            }
        }
    }

    /**
     * Random tile next to [anchor] this character can stand on, which is under a roof only if [anchor] is.
     * Stops teleports through the outer walls of buildings onto the empty tiles outside (e.g. Slayer Tower top floor)
     */
    private fun Character.teleportTile(anchor: Tile): Tile? {
        val roofed = Collisions.check(anchor, CollisionFlag.ROOF)
        val tiles = anchor.toCuboid(1).filter { it != anchor && Collisions.check(it, CollisionFlag.ROOF) == roofed && stepValidator.canFit(it, collision, size, blockMove) }
        return tiles.randomOrNull(random)
    }
}
