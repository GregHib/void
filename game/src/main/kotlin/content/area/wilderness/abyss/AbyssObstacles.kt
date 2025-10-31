package content.area.wilderness.abyss

import content.entity.gfx.areaGfx
import content.entity.sound.sound
import content.skill.mining.Pickaxe
import content.skill.woodcutting.Hatchet
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class AbyssObstacles : Script {

    val positions = mapOf(
        Tile(3038, 4853) to (Tile(3039, 4855) to Tile(3039, 4844)),
        Tile(3049, 4849) to (Tile(3050, 4851) to Tile(3047, 4844)),
        Tile(3058, 4839) to (Tile(3060, 4840) to Tile(3051, 4838)),
        Tile(3060, 4830) to (Tile(3062, 4831) to Tile(3052, 4831)),
        Tile(3057, 4821) to (Tile(3059, 4822) to Tile(3050, 4826)),
        Tile(3049, 4813) to (Tile(3050, 4812) to Tile(3045, 4821)),
        Tile(3041, 4811) to (Tile(3042, 4810) to Tile(3047, 4820)),
        Tile(3026, 4813) to (Tile(3027, 4812) to Tile(3042, 4819)),
        Tile(3018, 4821) to (Tile(3017, 4822) to Tile(3031, 4820)),
        Tile(3018, 4833) to (Tile(3017, 4834) to Tile(3026, 4826)),
        Tile(3021, 4842) to (Tile(3020, 4843) to Tile(3027, 4840)),
        Tile(3028, 4849) to (Tile(3029, 4851) to Tile(3032, 4844)),
    )

    val chance = 2..100

    val distractions = arrayOf(
        "emote_clap",
        "stomp_eyes",
        "distract_eyes",
    )

    init {
        objectOperate("Mine", "abyss_rock") { (target) ->
            message("You attempt to mine your way through...", ChatType.Filter)
            val pickaxe = Pickaxe.best(this)
            if (pickaxe == null) {
                message("You need a pickaxe for which you have the required Mining level to mine this rock.")
                return@objectOperate
            }
            anim("${pickaxe.id}_swing_low")
            delay(7)
            val success = Level.success(levels.get(Skill.Mining), chance)
            if (!success) {
                message("...but fail to break-up the rock.", ChatType.Filter)
                clearAnim()
                return@objectOperate
            }
            delay(1)
            val offset = target.tile.add(direction(target.tile))
            moveCamera(offset, 2500, 0, 0)
            turnCamera(target.tile, 0, 10, 10)
            delay(1)
            set("abyss_obstacles", 12)
            delay(2)
            set("abyss_obstacles", 13)
            val (walkTile, teleTile) = positions[target.tile]!!
            walkToDelay(walkTile)
            message("...and manage to break through the rock.", ChatType.Filter)
            clearAnim()
            tele(teleTile)
            delay(1)
            set("abyss_obstacles", 0)
            turnCamera(tile.region.tile, 0, 0, 0)
            moveCamera(offset, 0, 0, 0)
            clearCamera()
            exp(Skill.Mining, 25.0)
        }

        objectOperate("Chop", "abyss_tendrils") { (target) ->
            message("You attempt to chop your way through...", ChatType.Filter)
            delay(4)
            val hatchet = Hatchet.best(this)
            if (hatchet == null) {
                message("You need a hatchet to chop through the tendrils.")
                message("You do not have a hatchet that you have the Woodcutting level to use.")
                return@objectOperate
            }
            anim("${hatchet.id}_chop")
            delay(2)
            val success = Level.success(levels.get(Skill.Woodcutting), chance)
            if (!success) {
                message("...but fail to cut through the tendrils.", ChatType.Filter)
                return@objectOperate
            }
            val offset = target.tile.add(direction(target.tile))
            moveCamera(offset, 2500, 0, 0)
            turnCamera(target.tile, 0, 10, 10)
            delay(2)
            set("abyss_obstacles", 14)
            anim("${hatchet.id}_chop")
            delay(2)
            set("abyss_obstacles", 15)
            val (walkTile, teleTile) = positions[target.tile]!!
            walkToDelay(walkTile)
            message("...and manage to cut a way through the tendrils.", ChatType.Filter)
            delay(1)
            tele(teleTile)
            delay(1)
            set("abyss_obstacles", 0)
            turnCamera(tile.region.tile, 0, 0, 0)
            moveCamera(offset, 0, 0, 0)
            clearCamera()
            exp(Skill.Woodcutting, 25.0)
        }

        objectOperate("Burn-down", "abyss_boil") { (target) ->
            message("You attempt to set the blockade on fire...", ChatType.Filter)
            delay(3)
            if (!inventory.contains("tinderbox")) {
                message("...but you don't have a tinderbox to burn it!")
                return@objectOperate
            }
            val success = Level.success(levels.get(Skill.Firemaking), chance)
            if (!success) {
                message("...but fail to burn it out of your way.", ChatType.Filter)
                return@objectOperate
            }
            anim("light_fire")
            val offset = target.tile.add(direction(target.tile))
            moveCamera(offset, 2500, 0, 0)
            turnCamera(target.tile, 0, 10, 10)
            delay(6)
            set("abyss_obstacles", 16)
            delay(6)
            set("abyss_obstacles", 17)
            val (walkTile, teleTile) = positions[target.tile]!!
            areaGfx("fire_wave_impact", target.tile, height = 128)
            sound("boil_burst")
            walkToDelay(walkTile)
            message("...and manage to burn it down and get past.", ChatType.Filter)
            delay()
            tele(teleTile)
            delay()
            set("abyss_obstacles", 0)
            turnCamera(tile.region.tile, 0, 0, 0)
            moveCamera(offset, 0, 0, 0)
            clearCamera()
            exp(Skill.Firemaking, 25.0)
        }

        objectOperate("Distract", "abyss_eyes") { (target) ->
            message("You use your thieving skills to misdirect the eyes...", ChatType.Filter)
            delay(2)
            val success = Level.success(levels.get(Skill.Thieving), chance)
            if (!success) {
                message("...but fail to distract them enough to get past.", ChatType.Filter)
                return@objectOperate
            }
            anim(distractions.random())
            val offset = target.tile.add(direction(target.tile))
            moveCamera(offset, 2500, 0, 0)
            turnCamera(target.tile, 0, 10, 10)
            delay(4)
            set("abyss_obstacles", 18)
            anim(distractions.random())
            delay(2)
            set("abyss_obstacles", 19)
            val (walkTile, teleTile) = positions[target.tile]!!
            walkToDelay(walkTile)
            message("...and sneak past while they're not looking.", ChatType.Filter)
            delay(1)
            tele(teleTile)
            delay(1)
            set("abyss_obstacles", 0)
            turnCamera(tile.region.tile, 0, 0, 0)
            moveCamera(offset, 0, 0, 0)
            clearCamera()
            exp(Skill.Thieving, 25.0)
        }

        objectOperate("Squeeze-through", "abyss_gap") { (target) ->
            message("You attempt to squeeze through the narrow gap...", ChatType.Filter)
            delay(4)
            anim("abyss_kneel")
            delay(2)
            val success = Level.success(levels.get(Skill.Agility), chance)
            if (!success) {
                message("...but you are not agile enough to get through the gap.", ChatType.Filter)
                anim("stand")
                return@objectOperate
            }
            delay(2)
            message("...and you manage to crawl through.", ChatType.Filter)
            anim("crawling_cave")
            val (_, teleTile) = positions[target.tile]!!
            tele(teleTile)
            val offset = target.tile.add(direction(target.tile))
            moveCamera(offset, 2500, 0, 0)
            turnCamera(target.tile, 0, 10, 10)
            sound("abyssal_squeezethrough", repeat = 4)
            delay(1)
            set("abyss_obstacles", 0)
            turnCamera(tile.region.tile, 0, 0, 0)
            moveCamera(offset, 0, 0, 0)
            clearCamera()
            exp(Skill.Agility, 25.0)
        }

        objectOperate("Go-through", "abyss_passage") { (target) ->
            delay(2)
            val (_, teleTile) = positions[target.tile]!!
            tele(teleTile)
        }
    }

    // Object tile, opposite passage tile, teleport tile
    // Target tiles should be slightly randomised

    fun direction(tile: Tile): Direction {
        val delta = tile.minus(tile.region.tile)
        return when {
            delta.x in 16..32 -> Direction.WEST
            delta.x in 32..48 -> Direction.EAST
            delta.y in 16..32 -> Direction.SOUTH
            delta.y in 32..48 -> Direction.NORTH
            else -> Direction.NONE
        }
    }
}
