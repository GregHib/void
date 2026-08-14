package content.area.morytania.port_phasmatys

import content.entity.combat.hit.damage
import content.entity.player.dialogue.type.statement
import content.entity.player.effect.energy.runEnergy
import content.quest.member.ghosts_ahoy.GhostsAhoy
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import world.gregs.voidps.type.random
import kotlin.random.nextInt

class WreckedGhostShip : Script {

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
            walkOverDelay(Tile(3605, 3546, 1))
            tele(3605, 3548, 0)
            message("You cross the gangplank.", ChatType.Filter)
        }

        objectOperate("Cross", "wrecked_ghost_ship_gangplank_end") {
            walkOverDelay(Tile(3605, 3547))
            tele(3605, 3545, 1)
            message("You cross the gangplank.", ChatType.Filter)
        }

        objectOperate("Jump-to", "wrecked_ghost_ship_rock") { (target) ->
            val direction = rocks[target.tile] ?: return@objectOperate
            jump(target, target.tile.add(direction).add(direction), direction)
        }

        objectApproach("Jump-to", "wrecked_ghost_ship_rock") { (target) ->
            val direction = rocks[target.tile] ?: return@objectApproach
            val sameSide = when (direction) {
                Direction.NORTH -> tile.y <= target.tile.y
                Direction.EAST -> tile.x <= target.tile.x
                Direction.SOUTH -> tile.y >= target.tile.y
                Direction.WEST -> tile.x >= target.tile.x
                else -> false
            }
            if (sameSide) {
                jump(target, target.tile.add(direction).add(direction), direction)
            } else {
                jump(target, target.tile, direction.inverse())
            }
        }

        objectApproach("Search", "ahoy_ship_mast") {
            windSpeed()
        }

        objectOperate("Search", "ahoy_ship_mast") {
            windSpeed()
        }

        objTeleportTakeOff("Climb-down", "wrecked_ghost_ship_ladder_down") { obj, _ ->
            if (obj.tile.equals(3615, 3545, 2)) {
                message("That ladder doesn't go anywhere very safe.")
                Teleport.CANCEL
            } else {
                Teleport.CONTINUE
            }
        }

        entered("ahoy_shipwreck_top") {
            open("ahoy_windspeed")
            set("ahoy_windspeed", false)
            timers.start("windspeed")
        }

        exited("ahoy_shipwreck_top") {
            close("ahoy_windspeed")
            timers.stop("windspeed")
        }

        timerStart("windspeed") {
            random.nextInt(0..16) + 2
        }

        timerTick("windspeed") {
            val lowWind = get("ahoy_windspeed", false)
            set("ahoy_windspeed", !lowWind)
            interfaces.sendText("ahoy_windspeed", "content", if (lowWind) "High" else "Low")
            random.nextInt(0..16) + 2
        }
    }

    private suspend fun Player.windSpeed() {
        if (!get("ahoy_windspeed", false)) {
            statement("You can see a tattered flag blowing in the wind.<br>The wind is blowing too hard to make out any details.")
            return
        }
        when (random.nextInt(3)) {
            0 -> statement("You can see a tattered flag blowing in the wind.<br>The top half of the flag is coloured ${GhostsAhoy.flagColor(get("ahoy_mast_top", 0))}.")
            1 -> statement("You can see a tattered flag blowing in the wind.<br>The bottom half of the flag is coloured ${GhostsAhoy.flagColor(get("ahoy_mast_bottom", 0))}.")
            else -> statement("You can see a tattered flag blowing in the wind.<br>The skull emblem is coloured ${GhostsAhoy.flagColor(get("ahoy_mast_skull", 0))}.")
        }
    }

    suspend fun Player.jump(target: GameObject, opposite: Tile, direction: Direction) {
        clear("face_entity")
        walkToDelay(target.tile)
        delay()
        if (!has(Skill.Agility, 25)) {
            message("You need level 25 agility to make that jump.")
            statement("You need level 25 agility to make that jump.")
            return
        }
        if (runEnergy < 500) {
            message("You don't have enough energy to make that jump")
            return
        }
        anim("rock_jump", delay = 26)
        sound("jump")
        exactMoveDelay(opposite, startDelay = 47, delay = 59, direction = direction)
        runEnergy -= 500
        if (Level.success(levels.get(Skill.Agility), 5..255)) { // Success rate is unknown
            exp(Skill.Agility, 10.0)
        } else {
            anim("fall_on_floor")
            sound("land_flatter")
            damage(10)
        }
    }
}
