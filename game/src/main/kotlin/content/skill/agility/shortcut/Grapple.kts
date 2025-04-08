package content.skill.agility.shortcut

import content.entity.gfx.areaGfx
import content.entity.player.dialogue.type.statement
import content.entity.sound.sound
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

val crossbows = setOf(
    "bronze_crossbow",
    "blurite_crossbow",
    "iron_crossbow",
    "steel_crossbow",
    "mithril_crossbow",
    "adamant_crossbow",
    "rune_crossbow",
)

val objects: GameObjects by inject()

objectApproach("Grapple", "lumbridge_broken_raft") {
    if (!hasRequirements(ranged = 37, agility = 8, strength = 17)) {
        return@objectApproach
    }
    player.steps.clear()
    val direction = if (player.tile.x < 3253) Direction.EAST else Direction.WEST
    // Skip first half if player is stuck on raft somehow
    if (player.tile.distanceTo(target.tile) > 2) {
        val start = if (direction == Direction.EAST) Tile(3246, 3179) else Tile(3259, 3180)
        if (player.tile.distanceTo(start) > 1) {
            player.message("I can't do that from here, get closer.")
            cancel()
            return@objectApproach
        }
        player.face(target)
        delay(2)
        player.anim("crossbow_accurate")
        player.sound("grapple_shoot")
        delay(3)
        player.message("You successfully grapple the raft and tie the rope to a tree.", ChatType.Filter)
        if (direction == Direction.EAST) {
            lumbridgeTree(grapple = false)
        } else {
            alKharidTree(grapple = false)
        }
        player.walkOverDelay(start.add(direction))
        player.anim("grapple_enter_water")
        areaGfx("big_splash", start.addX(direction.delta.x * 2), delay = 6)
        player.sound("grapple_splash", 3)
        player.exactMoveDelay(start.addX(direction.delta.x * 6), 120, direction)
    }
    if (direction == Direction.EAST) {
        player.walkToDelay(Tile(3252, 3180))
        player.walkToDelay(Tile(3253, 3180))
        player.face(Tile(3260, 3180))
    } else {
        player.walkToDelay(Tile(3252, 3180))
        player.face(Tile(3244, 3179))
    }
    delay(2)
    player.anim("crossbow_accurate")
    player.sound("grapple_shoot")
    delay(3)
    player.message("You successfully grapple the tree on the opposite bank.", ChatType.Filter)
    if (direction == Direction.EAST) {
        alKharidTree(grapple = true)
    } else {
        lumbridgeTree(grapple = true)
    }
    delay()
    player.anim("grapple_exit_water")
    areaGfx("big_splash", player.tile.add(direction), delay = 6)
    player.sound("grapple_splash", 3)
    val shore = if (direction == Direction.EAST) Tile(3258, 3180) else Tile(3248, 3179)
    player.exactMoveDelay(shore, 160, direction)
    val end = if (direction == Direction.EAST) Tile(3259, 3180) else Tile(3246, 3179)
    player.walkOverDelay(end)
}

fun Grapple.lumbridgeTree(grapple: Boolean) {
    val tree = objects[Tile(3244, 3179), "strong_yew"]
    tree?.replace("strong_yew_${if (grapple) "grapple" else "rope"}", ticks = 8)
    for (x in 3246..3251) {
        objects.add("grapple_rope", Tile(x, 3179), shape = ObjectShape.GROUND_DECOR, ticks = 8)
    }
}

fun Grapple.alKharidTree(grapple: Boolean) {
    val tree = objects[Tile(3260, 3179), "strong_tree"]
    tree?.replace("strong_tree_${if (grapple) "grapple" else "rope"}", ticks = 8)
    for (x in 3254..3259) {
        objects.add("grapple_rope", Tile(x, 3180), shape = ObjectShape.GROUND_DECOR, ticks = 8)
    }
}

objectOperate("Grapple", "falador_wall_north") {
    player.walkToDelay(Tile(3006, 3395))
    player.face(Direction.SOUTH)
    delay()
    if (!hasRequirements(ranged = 19, agility = 11, strength = 37)) {
        return@objectOperate
    }
    player.anim("grapple_wall_climb")
    player.gfx("grapple_wall_climb")
    player.sound("grapple_shoot", delay = 45)
    delay(11)
    player.clearGfx()
    player.clearAnim()
    player.tele(3006, 3394, 1)
}

objectOperate("Grapple", "falador_wall_south") {
    player.walkToDelay(Tile(3005, 3393))
    player.face(Direction.NORTH)
    delay()
    if (!hasRequirements(ranged = 19, agility = 11, strength = 37)) {
        return@objectOperate
    }
    player.anim("grapple_wall_climb")
    player.gfx("grapple_wall_climb")
    player.sound("grapple_shoot", delay = 45)
    delay(11)
    player.clearGfx()
    player.clearAnim()
    player.tele(3005, 3394, 1)
}

objectOperate("Jump", "falador_wall_jump_north") {
    player.walkToDelay(Tile(3006, 3394, 1))
    if (!player.has(Skill.Agility, 4)) {
        player.message("You need an agility level of at least 4 to climb down this wall.")
        return@objectOperate
    }
    player.anim("jump_down")
    delay(1)
    player.anim("jump_land")
    player.tele(3006, 3395, 0)
}

objectOperate("Jump", "falador_wall_jump_south") {
    player.walkToDelay(Tile(3005, 3394, 1))
    if (!player.has(Skill.Agility, 4)) {
        player.message("You need an agility level of at least 4 to climb down this wall.")
        return@objectOperate
    }
    player.anim("jump_down")
    delay(1)
    player.anim("jump_land")
    player.tele(3005, 3393, 0)
}

suspend fun ObjectOption<Player>.hasRequirements(ranged: Int, agility: Int, strength: Int): Boolean {
    if (!player.has(Skill.Ranged, ranged) || !player.has(Skill.Agility, agility) || !player.has(Skill.Strength, strength)) {
        statement("You need at least $ranged Ranged, $agility Agility and $strength Strength to do that.")
        return false
    }
    if (player.equipped(EquipSlot.Ammo).id != "mithril_grapple") {
        player.message("You need a mithril grapple tipped bolt with a rope to do that.")
        return false
    }
    if (!crossbows.contains(player.weapon.id)) {
        player.message("You need a crossbow equipped to do that.")
        return false
    }
    return true
}