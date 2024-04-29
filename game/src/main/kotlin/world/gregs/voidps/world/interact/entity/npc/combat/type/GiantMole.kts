package world.gregs.voidps.world.interact.entity.npc.combat.type

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.facing
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.itemChange
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic
import world.gregs.voidps.world.interact.entity.player.equip.inventoryOption
import world.gregs.voidps.world.interact.entity.sound.areaSound
import kotlin.random.Random

val logger = InlineLogger()
val areas: AreaDefinitions by inject()
val players: Players by inject()

val acceptedTiles = listOf(
    Tile(3005, 3376, 0),
    Tile(2999, 3375, 0),
    Tile(2996, 3377, 0),
    Tile(2989, 3378, 0)
)

val giantMoleLair = areas["giant_mole_lair"]
val gianMoleSpawns = areas["giant_mole_spawn_area"]
val initialCaveTile: Tile = Tile(1752, 5237, 0)

inventoryOption("Dig") {
    val playerTile: Tile = player.tile
    if(!acceptedTiles.contains(playerTile)) {
        return@inventoryOption
    }
    player.setAnimation("dig_with_spade")
    player.open("warning_dark")
}

objectOperate("Climb", "giant_mole_lair_escape_rope") {
    player.setAnimation("climb_up")
    player.tele(acceptedTiles.random())
}

interfaceOption(component = "proceed", id = "warning_dark") {
    player.tele(initialCaveTile, clearInterfaces = true)
}

interfaceOption(component = "stayout", id = "warning_dark") {
    player.closeInterfaces()
}

combatAttack {
    val npc = target as NPC
    if(npc.id == "giant_mole") {
        val currentHealth = npc.levels.get(Skill.Constitution)
        var shouldBurrow = false
        if (it.fightStyle == "magic" && damage != 0) {
            shouldBurrow = shouldBurrowAway(currentHealth)
        } else if (it.fightStyle != "magic") {
            shouldBurrow = shouldBurrowAway(currentHealth)
        }
        if(shouldBurrow && !World.timers.contains("await_mole_burrowing")) {
            giantMoleBurrow(npc)
        }
    }
}

fun giantMoleBurrow(mole: NPC) {
    mole.attackers.clear()
    var tileToDust = getTotalDirection(mole.facing, mole.tile)
    World.queue("await_mole_to_face", 1) {
        if (tileToDust == Tile.EMPTY) {
            logger.info { "failed to get facing tile for Giant Mole, using default tile." }
            tileToDust = initialCaveTile
        }
        mole.face(tileToDust)
        World.queue("display_burrow_dust", 1) {
            if (shouldThrowDirt()) {
                handleDirtOnScreen(mole.tile)
            }
            mole.setAnimation("giant_mole_burrow")
            areaSound("giant_mole_burrow_down", mole.tile)
            areaGraphic("burrow_dust", tileToDust)
            World.queue("await_mole_burrowing", 1) {
                val newLocation = gianMoleSpawns.random(mole)
                mole.tele(newLocation!!)
                mole.setAnimation("mole_burrow_up")
            }
        }
    }
}

fun getTotalDirection(facing: Direction, moleTile: Tile): Tile {
    return when (facing) {
        Direction.NORTH -> Tile(moleTile.x, moleTile.y + 1)
        Direction.SOUTH -> Tile(moleTile.x, moleTile.y - 1)
        Direction.EAST -> Tile(moleTile.x + 1, moleTile.y)
        Direction.WEST -> Tile(moleTile.x - 1, moleTile.y)
        Direction.NORTH_EAST, Direction.SOUTH_EAST -> Tile(moleTile.x + 1, moleTile.y)
        Direction.NORTH_WEST, Direction.SOUTH_WEST -> Tile(moleTile.x - 1, moleTile.y)
        Direction.NONE -> Tile.EMPTY
    }
}

// 13% chance to throw dirt on players screen
fun shouldThrowDirt(): Boolean {
    val dirtChance = Random.nextInt(0, 100)
    return dirtChance <= 13
}

fun handleDirtOnScreen(moleTile: Tile) {
    val nearMole = mutableListOf<Player>()
    for (tile in moleTile.toCuboid(5)) {
        for (player in players[tile]) {
            nearMole.add(player)
        }
    }
    for (player in nearMole) {
        player.open("dirt_on_screen")
        val playerInventory = player.inventory.items
        for (item in playerInventory) {
            if (item.id.contains("candle_lit")) {
                val newItem = item.id.replace("_lit", "")
                player.inventory.transaction {
                    replace(item.id, newItem)
                }
            }
        }
    }
    World.queue("dirt_on_screen_timer_player", 3) {
        for (player in nearMole) {
            player.close("dirt_on_screen")
        }
    }
}

fun shouldBurrowAway(health: Int): Boolean {
    val maxHealth = 2000
    val minThreshold = maxHealth * 0.05
    val maxThreshold = maxHealth * 0.50
    if (health in minThreshold.toInt()..maxThreshold.toInt()) {
        val shouldBurrow = Random.nextInt(0, 100)
        return shouldBurrow <= 25
    }
    return false
}

enterArea("giant_mole_lair") {
    if(!hasLightSource(player)) {
        player.open("level_three_darkness")
    }
}

exitArea("giant_mole_lair") {
    if(player.interfaces.contains("level_three_darkness")) {
        player.close("level_three_darkness")
    }
}

playerSpawn { player ->
    if(giantMoleLair.contains(player.tile)) {
        if(!hasLightSource(player)) {
            player.open("level_three_darkness")
        }
    }
}

itemChange("inventory") { player: Player ->
    if (giantMoleLair.contains(player.tile)) {
        val hasLightSource = hasLightSource(player)
        if (!hasLightSource && !player.interfaces.contains("level_three_darkness")) {
            player.open("level_three_darkness")
        } else if (hasLightSource && player.interfaces.contains("level_three_darkness")) {
            player.close("level_three_darkness")
        }
    }
}

fun hasLightSource(player: Player): Boolean {
    val playerItems = player.inventory.items

    for (item in playerItems) {
        if (item.id.contains("lantern_lit") || item.id.contains("candle_lit")) {
            return true
        }
    }
    return false
}