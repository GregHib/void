package world.gregs.voidps.world.map.varrock.abyss

import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.activity.skill.mining.Pickaxe
import world.gregs.voidps.world.activity.skill.woodcutting.Hatchet
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic
import world.gregs.voidps.world.interact.entity.sound.playSound

// Object tile, opposite passage tile, teleport tile
// Target tiles should be slightly randomised
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

objectOperate("Mine", "abyss_rock") {
    player.message("You attempt to mine your way through...", ChatType.Filter)
    val pickaxe = Pickaxe.best(player)
    if (pickaxe == null) {
        player.message("You need a pickaxe for which you have the required Mining level to mine this rock.")
        return@objectOperate
    }
    player.setAnimation("${pickaxe.id}_swing_low")
    pause(7)
    val success = Level.success(player.levels.get(Skill.Mining), chance)
    if (!success) {
        player.message("...but fail to break-up the rock.", ChatType.Filter)
        player.clearAnimation()
        return@objectOperate
    }
    player.strongQueue("abyss_rock") {
        pause(1)
        val offset = target.tile.add(direction(target.tile))
        player.moveCamera(offset, 2500, 0, 0)
        player.turnCamera(target.tile, 0, 10, 10)
        pause(1)
        player["abyss_obstacles"] = 12
        pause(2)
        player["abyss_obstacles"] = 13
        val (walkTile, teleTile) = positions[target.tile]!!
        player.walkTo(walkTile)
        pause(2)
        player.message("...and manage to break through the rock.", ChatType.Filter)
        player.clearAnimation()
        player.tele(teleTile)
        pause(1)
        player["abyss_obstacles"] = 0
        player.turnCamera(player.tile.region.tile, 0, 0, 0)
        player.moveCamera(offset, 0, 0, 0)
        player.clearCamera()
        player.exp(Skill.Mining, 25.0)
    }
}

objectOperate("Chop", "abyss_tendrils") {
    player.message("You attempt to chop your way through...", ChatType.Filter)
    pause(4)
    val hatchet = Hatchet.best(player)
    if (hatchet == null) {
        player.message("You need a hatchet to chop through the tendrils.")
        player.message("You do not have a hatchet that you have the Woodcutting level to use.")
        return@objectOperate
    }
    player.setAnimation("${hatchet.id}_chop")
    pause(2)
    val success = Level.success(player.levels.get(Skill.Woodcutting), chance)
    if (!success) {
        player.message("...but fail to cut through the tendrils.", ChatType.Filter)
        return@objectOperate
    }
    player.strongQueue("abyss_tentacles") {
        val offset = target.tile.add(direction(target.tile))
        player.moveCamera(offset, 2500, 0, 0)
        player.turnCamera(target.tile, 0, 10, 10)
        pause(2)
        player["abyss_obstacles"] = 14
        player.setAnimation("${hatchet.id}_chop")
        pause(2)
        player["abyss_obstacles"] = 15
         val (walkTile, teleTile) = positions[target.tile]!!
        player.walkTo(walkTile)
        pause(2)
        player.message("...and manage to cut a way through the tendrils.", ChatType.Filter)
        pause(1)
        player.tele(teleTile)
        pause(1)
        player["abyss_obstacles"] = 0
        player.turnCamera(player.tile.region.tile, 0, 0, 0)
        player.moveCamera(offset, 0, 0, 0)
        player.clearCamera()
        player.exp(Skill.Woodcutting, 25.0)
    }
}

objectOperate("Burn-down", "abyss_boil") {
    player.message("You attempt to set the blockade on fire...", ChatType.Filter)
    pause(3)
    if (!player.inventory.contains("tinderbox")) {
        player.message("...but you don't have a tinderbox to burn it!")
        return@objectOperate
    }
    val success = Level.success(player.levels.get(Skill.Firemaking), chance)
    if (!success) {
        player.message("...but fail to burn it out of your way.", ChatType.Filter)
        return@objectOperate
    }
    player.strongQueue("abyss_boil") {
        player.setAnimation("light_fire")
        val offset = target.tile.add(direction(target.tile))
        player.moveCamera(offset, 2500, 0, 0)
        player.turnCamera(target.tile, 0, 10, 10)
        pause(6)
        player["abyss_obstacles"] = 16
        pause(6)
        player["abyss_obstacles"] = 17
         val (walkTile, teleTile) = positions[target.tile]!!
        player.walkTo(walkTile)
        areaGraphic("fire_wave_hit", target.tile, height = 128)
        player.playSound("boil_burst")
        pause(2)
        player.message("...and manage to burn it down and get past.", ChatType.Filter)
        pause(1)
        player.tele(teleTile)
        pause(1)
        player["abyss_obstacles"] = 0
        player.turnCamera(player.tile.region.tile, 0, 0, 0)
        player.moveCamera(offset, 0, 0, 0)
        player.clearCamera()
        player.exp(Skill.Firemaking, 25.0)
    }
}

objectOperate("Distract", "abyss_eyes") {
    player.message("You use your thieving skills to misdirect the eyes...", ChatType.Filter)
    pause(2)
    val success = Level.success(player.levels.get(Skill.Thieving), chance)
    if (!success) {
        player.message("...but fail to distract them enough to get past.", ChatType.Filter)
        return@objectOperate
    }
    player.strongQueue("abyss_eyes") {
        player.setAnimation(distractions.random())
        val offset = target.tile.add(direction(target.tile))
        player.moveCamera(offset, 2500, 0, 0)
        player.turnCamera(target.tile, 0, 10, 10)
        pause(4)
        player["abyss_obstacles"] = 18
        player.setAnimation(distractions.random())
        pause(2)
        player["abyss_obstacles"] = 19
         val (walkTile, teleTile) = positions[target.tile]!!
        player.walkTo(walkTile)
        pause(2)
        player.message("...and sneak past while they're not looking.", ChatType.Filter)
        pause(1)
        player.tele(teleTile)
        pause(1)
        player["abyss_obstacles"] = 0
        player.turnCamera(player.tile.region.tile, 0, 0, 0)
        player.moveCamera(offset, 0, 0, 0)
        player.clearCamera()
        player.exp(Skill.Thieving, 25.0)
    }
}

val distractions = arrayOf(
    "emote_clap",
    "stomp_eyes",
    "distract_eyes"
)

objectOperate("Squeeze-through", "abyss_gap") {
    player.message("You attempt to squeeze through the narrow gap...", ChatType.Filter)
    pause(4)
    player.setAnimation("abyss_kneel")
    pause(2)
    val success = Level.success(player.levels.get(Skill.Agility), chance)
    if (!success) {
        player.message("...but you are not agile enough to get through the gap.", ChatType.Filter)
        player.setAnimation("abyss_stand")
        return@objectOperate
    }
    player.strongQueue("abyss_gap", 2) {
        player.message("...and you manage to crawl through.", ChatType.Filter)
        player.setAnimation("crawling_cave")
        val (_, teleTile) = positions[target.tile]!!
        player.tele(teleTile)
        val offset = target.tile.add(direction(target.tile))
        player.moveCamera(offset, 2500, 0, 0)
        player.turnCamera(target.tile, 0, 10, 10)
        player.playSound("abyssal_squeezethrough", repeat = 4)
        pause(1)
        player["abyss_obstacles"] = 0
        player.turnCamera(player.tile.region.tile, 0, 0, 0)
        player.moveCamera(offset, 0, 0, 0)
        player.clearCamera()
        player.exp(Skill.Agility, 25.0)
    }
}

objectOperate("Go-through", "abyss_passage") {
    pause(2)
    val (_, teleTile) = positions[target.tile]!!
    player.tele(teleTile)
}

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