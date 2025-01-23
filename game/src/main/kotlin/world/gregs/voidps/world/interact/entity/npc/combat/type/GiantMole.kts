package world.gregs.voidps.world.interact.entity.npc.combat.type

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.facing
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.itemChange
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.interact.entity.combat.attackers
import world.gregs.voidps.world.interact.entity.combat.fightStyle
import world.gregs.voidps.world.interact.entity.combat.hit.npcCombatHit
import world.gregs.voidps.world.interact.entity.gfx.areaGraphic
import world.gregs.voidps.world.interact.entity.player.equip.inventoryItem
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

inventoryItem("Dig", "spade") {
    val playerTile: Tile = player.tile
    player.anim("dig_with_spade")
    if (!acceptedTiles.contains(playerTile)) {
        return@inventoryItem
    }
    player.open("warning_dark")
}

objectOperate("Climb", "giant_mole_lair_escape_rope") {
    player.anim("climb_up")
    player.tele(acceptedTiles.random())
}

interfaceOption(component = "proceed", id = "warning_dark") {
    player.tele(initialCaveTile, clearInterfaces = true)
}

interfaceOption(component = "stayout", id = "warning_dark") {
    player.closeInterfaces()
}

npcCombatHit("giant_mole") {
    val currentHealth = it.levels.get(Skill.Constitution)
    var shouldBurrow = false
    if (it.fightStyle == "magic" && damage != 0) {
        shouldBurrow = shouldBurrowAway(currentHealth)
    } else if (it.fightStyle != "magic") {
        shouldBurrow = shouldBurrowAway(currentHealth)
    }
    if (shouldBurrow && !it.hasClock("awaiting_mole_burrow_complete")) {
        it.start("awaiting_mole_burrow_complete", 4)
        giantMoleBurrow(it)
    }
}

fun giantMoleBurrow(mole: NPC) {
    for (attacker in mole.attackers) {
        attacker.mode = EmptyMode
    }
    mole.attackers.clear()
    var tileToDust = mole.tile.add(getRandomFacing(mole.facing).delta)
    mole.queue("await_mole_to_face", 1) {
        if (tileToDust == Tile.EMPTY) {
            logger.warn { "failed to get facing tile for Giant Mole, using default tile." }
            tileToDust = initialCaveTile
        }
        mole.face(tileToDust)
        pause(1)
        if (shouldThrowDirt()) {
            handleDirtOnScreen(mole.tile)
        }
        mole.anim("giant_mole_burrow")
        areaSound("giant_mole_burrow_down", mole.tile)
        areaGraphic("burrow_dust", tileToDust)
        pause(1)
        val newLocation = gianMoleSpawns.random(mole)
        mole.tele(newLocation!!)
        mole.anim("mole_burrow_up")
    }
}

fun getRandomFacing(currentlyFacing: Direction): Direction {
    var randomDirection: Direction
    do {
        randomDirection = Direction.entries.filter { it != Direction.NONE }.random()
    } while (randomDirection == currentlyFacing)
    return randomDirection
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
        player.inventory.transaction {
            for (index in inventory.indices) {
                val item = inventory[index]
                if (item.id.endsWith("candle_lit")) {
                    replace(item.id, item.id.removeSuffix("_lit"))
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
    if (!hasLightSource(player)) {
        player.open("level_three_darkness")
    }
}

exitArea("giant_mole_lair") {
    if (player.interfaces.contains("level_three_darkness")) {
        player.close("level_three_darkness")
    }
}

playerSpawn { player ->
    if (giantMoleLair.contains(player.tile)) {
        if (!hasLightSource(player)) {
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
    val playerEquipment = player.equipment.items

    for (item in playerItems) {
        if (item.id.endsWith("lantern_lit") || item.id.endsWith("candle_lit") || item.id == "firemaking_cape_t" || item.id == "firemaking_cape") {
            return true
        }
    }

    for (equipmentItem in playerEquipment) {
        if (equipmentItem.id == "firemaking_cape" || equipmentItem.id == "firemaking_cape_t") {
            return true
        }
    }

    return false
}
