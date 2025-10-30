package content.area.troll_country.trollheim

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals

class Trollheim : Script {

    init {
        objectOperate("Climb", "trollheim_rocks") { (target) ->
            face(target)
            if (target.tile.equals(2901, 3680)) {
                anim("human_climbing_down")
                val target = Tile(2903, 3680)
                exactMoveDelay(target, 120, direction = Direction.WEST)
                exp(Skill.Agility, 1.0)
            } else if (target.tile.equals(2902, 3680)) {
                val target = Tile(2900, 3680)
                renderEmote("climbing")
                walkOverDelay(target)
                clearRenderEmote()
                exp(Skill.Agility, 8.0)
            }
        }

        objectOperate("Climb", "trollheim_rocks_4") { (target) ->
            face(target)
            if (target.tile.equals(2885, 3684)) {
                if (tile.x >= target.tile.x) {
                    anim("rocks_climb_down")
                    exactMoveDelay(target.tile.copy(x = 2884), 50, direction = Direction.EAST)
                    exp(Skill.Agility, 1.0)
                    clearAnim()
                } else {
                    renderEmote("climbing")
                    walkOverDelay(target.tile.copy(x = 2886))
                    exp(Skill.Agility, 8.0)
                    clearRenderEmote()
                }
            } else if (target.tile.equals(2887, 3661)) {
                if (tile.y >= target.tile.y) {
                    anim("rocks_climb_down", delay = 15)
                    exactMoveDelay(target.tile.addY(-1), 50, direction = Direction.NORTH)
                    exp(Skill.Agility, 1.0)
                    clearAnim()
                } else {
                    renderEmote("climbing")
                    walkOverDelay(target.tile.addY(1))
                    exp(Skill.Agility, 1.0)
                    clearRenderEmote()
                }
            }
        }

        objectOperate("Climb", "trollheim_rocks_5") { (target) ->
            face(target)
            if (target.tile.equals(2885, 3683)) {
                if (tile.x >= target.tile.x) {
                    anim("rocks_climb_down")
                    exactMoveDelay(target.tile.addX(-1), 50, direction = Direction.EAST)
                    exp(Skill.Agility, 1.0)
                    clearAnim()
                } else {
                    renderEmote("climbing")
                    walkOverDelay(target.tile.addX(1))
                    exp(Skill.Agility, 8.0)
                    clearRenderEmote()
                }
            } else if (target.tile.equals(2888, 3661)) {
                if (tile.y >= target.tile.y) {
                    anim("rocks_climb_down", delay = 15)
                    exactMoveDelay(target.tile.addY(-1), 50, direction = Direction.NORTH)
                    exp(Skill.Agility, 1.0)
                    clearAnim()
                } else {
                    renderEmote("climbing")
                    walkOverDelay(target.tile.addY(1))
                    exp(Skill.Agility, 1.0)
                    clearRenderEmote()
                }
            }
        }

        objectOperate("Climb", "trollheim_rocks_hard") { (target) ->
            face(target)
            if (target.tile.equals(2908, 3682)) {
                anim("human_climbing_down", delay = 10)
                exactMoveDelay(target.tile, 40, direction = Direction.WEST)
                anim("human_climbing_down", delay = 10)
                exactMoveDelay(Tile(2909, 3684), 120, direction = Direction.SOUTH)
                exp(Skill.Agility, 8.0)
                face(Direction.SOUTH)
                clearAnim()
            } else if (target.tile.equals(2909, 3683)) {
                renderEmote("climbing")
                walkOverDelay(Tile(2909, 3682))
                clearRenderEmote()
                delay()
                renderEmote("climbing")
                walkOverDelay(Tile(2907, 3682))
                exp(Skill.Agility, 8.0)
                clearRenderEmote()
            }
        }

        objectOperate("Climb", "trollheim_rocks_advanced") { (target) ->
            if (!has(Skill.Agility, 44)) {
                return@objectOperate
            }
            face(target)
            message("You climb onto the rock...")
            anim("rocks_pile_climb", delay = 30)
            val direction = if (tile.x >= target.tile.x) Direction.WEST else Direction.EAST
            exactMoveDelay(target.tile.add(direction), startDelay = 30, delay = 94, direction = direction)
            message("...and step down the other side.")
            clearAnim()
        }

        objectOperate("Climb", "trollheim_rocks_medium") { (target) ->
            if (!has(Skill.Agility, 43)) {
                return@objectOperate
            }
            // TODO 1-4 damage on failure
            face(Direction.NORTH)
            if (tile.y >= target.tile.y) {
                anim("human_climbing_down", delay = 10)
                exactMoveDelay(target.tile.addY(-2), 120, direction = Direction.NORTH)
                clearAnim()
            } else {
                renderEmote("climbing")
                walkOverDelay(target.tile.addY(2))
                clearRenderEmote()
            }
            exp(Skill.Agility, 8.0)
        }

        objectOperate("Climb", "trollheim_rocks_easy") { (target) ->
            if (!has(Skill.Agility, 41)) {
                return@objectOperate
            }
            // TODO 1-4 damage on failure
            face(Direction.NORTH)
            if (tile.x >= target.tile.x) {
                anim("human_climbing_down", delay = 10)
                exactMoveDelay(target.tile.addX(-2), 120, direction = Direction.EAST)
                clearAnim()
            } else {
                renderEmote("climbing")
                walkOverDelay(target.tile.addX(2))
                clearRenderEmote()
            }
            exp(Skill.Agility, 8.0)
        }
    }

    // TODO levels and failing
}
