package content.area.morytania.port_phasmatys

import content.entity.combat.hit.damage
import content.entity.player.dialogue.type.statement
import content.entity.player.effect.energy.runEnergy
import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.objectApproach
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

@Script
class WreckedGhostShip {

    val rocks = mapOf(
        Tile(3604, 3550) to Direction.WEST,
        Tile(3602, 3550) to Direction.EAST,
        Tile(3599, 3552) to Direction.WEST,
        Tile(3597, 3552) to Direction.EAST,
        Tile(3595, 3554) to Direction.NORTH,
        Tile(3595, 3556) to Direction.SOUTH,
        Tile(3597, 3559) to Direction.NORTH,
        Tile(3597, 3561) to Direction.SOUTH,
        Tile(3599, 3564) to Direction.EAST,
        Tile(3601, 3564) to Direction.WEST,
    )

    init {
        objectOperate("Cross", "wrecked_ghost_ship_gangplank") {
            player.walkOverDelay(Tile(3605, 3546, 1))
            player.tele(3605, 3548, 0)
            player.message("You cross the gangplank.", ChatType.Filter)
        }

        objectOperate("Cross", "wrecked_ghost_ship_gangplank_end") {
            player.walkOverDelay(Tile(3605, 3547))
            player.tele(3605, 3545, 1)
            player.message("You cross the gangplank.", ChatType.Filter)
        }

        objectOperate("Jump-to", "wrecked_ghost_ship_rock") {
            val direction = rocks[target.tile] ?: return@objectOperate
            jump(target.tile.add(direction).add(direction), direction)
        }

        objectApproach("Jump-to", "wrecked_ghost_ship_rock") {
            val direction = rocks[target.tile] ?: return@objectApproach
            val sameSide = when (direction) {
                Direction.NORTH -> player.tile.y <= target.tile.y
                Direction.EAST -> player.tile.x <= target.tile.x
                Direction.SOUTH -> player.tile.y >= target.tile.y
                Direction.WEST -> player.tile.x >= target.tile.x
                else -> false
            }
            if (sameSide) {
                jump(target.tile.add(direction).add(direction), direction)
            } else {
                jump(target.tile, direction.inverse())
            }
        }
    }

    suspend fun ObjectOption<Player>.jump(opposite: Tile, direction: Direction) {
        character.clear("face_entity")
        player.walkToDelay(target.tile)
        delay()
        if (!player.has(Skill.Agility, 25)) {
            player.message("You need level 25 agility to make that jump.")
            statement("You need level 25 agility to make that jump.")
            return
        }
        if (player.runEnergy < 500) {
            player.message("You don't have enough energy to make that jump")
            return
        }
        player.anim("rock_jump", delay = 26)
        player.sound("jump")
        player.exactMoveDelay(opposite, startDelay = 47, delay = 59, direction = direction)
        player.runEnergy -= 500
        if (Level.success(player.levels.get(Skill.Agility), 5..255)) { // Success rate is unknown
            player.exp(Skill.Agility, 10.0)
        } else {
            player.anim("fall_on_floor")
            player.sound("land_flatter")
            player.damage(10)
        }
    }
}
