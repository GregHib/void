package content.area.misthalin.zanaris

import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class Zanaris : Script {
    init {
        objectOperate("Use", "fairy_ring_zanaris") { (target) ->
            walkOverDelay(target.tile)
            Teleport.teleport(this, Tile(3201, 3169), "fairy")
        }

        objectOperate("Enter", "zanaris_crop_circle") {
            walkToDelay(Tile(2427, 4446))
            Teleport.teleport(this, Tile(2591, 4319), "puro_puro")
        }

        objectOperate("Exit", "puro_puro_exit") {
            walkToDelay(Tile(2592, 4320))
            Teleport.teleport(this, Tile(2591, 4319), "puro_puro")
        }

        objectOperate("Quick-leave", "puro_puro_exit") {
            tele(2426, 4445) // TODO check how this works
        }

        objectOperate("Squeeze-past", "zanaris_jutting_wall") { (target) ->
            val level = if (target.tile.x == 2400) 46 else 66
            if (!has(Skill.Agility, level, message = true)) {
                return@objectOperate
            }
            message("You try to squeeze past.", type = ChatType.Filter)
            if (Level.success(levels.get(Skill.Agility), 50..254)) {
                val direction = if (tile.y < target.tile.y) {
                    Direction.NORTH
                } else {
                    Direction.SOUTH
                }
                walkToDelay(target.tile.add(direction.inverse()))
                face(direction)
                anim("spear_trap_walk_${if (direction == Direction.SOUTH) "right" else "left"}")
                exactMoveDelay(target.tile.add(direction), startDelay = 28, delay = 124, direction = direction)
                sound("squeeze_out")
                exp(Skill.Agility, 10.0)
            } else {
                val direction = if (tile.y < target.tile.y) {
                    Direction.NORTH
                } else {
                    Direction.SOUTH
                }
                walkToDelay(target.tile.add(direction.inverse()))
                anim("spear_trap_caught_${if (direction == Direction.SOUTH) "right" else "left"}")
                face(direction)
                exactMoveDelay(target.tile, startDelay = 28, delay = 48, direction = direction)
                val trap = GameObjects.find(target.tile.addX(-1), "zanaris_damaged_wall")
                // FIXME: Directional anim broken on the right side
                for (ouch in listOf("Ahhh...", "Owww...", "Arrgghhhh!")) {
                    anim("side_hurt_${if (direction == Direction.SOUTH) "left" else "left"}")
                    trap.anim("spear_trap_release")
                    say(ouch)
                    delay(1)
                }
                anim("dive_player")
                sound("human_hit")
                sound("spear_trap_jump")
                sound("male_defend_1")
                sound("male_defend_2")
                directHit(20)
                directHit(20)
                exp(Skill.Agility, 6.0)
                exactMoveDelay(target.tile.add(direction), startDelay = 10, delay = 20, direction = direction)
            }
        }
    }
}
