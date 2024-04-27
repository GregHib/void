package world.gregs.voidps.world.interact.entity.npc.combat.type

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.player.equip.inventoryOption
import kotlin.random.Random

val logger = InlineLogger()
val areas: AreaDefinitions by inject()

//TODO: Add drop table
//TODO: inventory update: if the players light source get's turned off need to reapply darnkess overlay
//TODO: find correct giant mole NPC ID
//TODO: make giantMoleLair cover the whole area

// list of tiles where the mole hills are located.
val acceptedTiles = listOf(
    Tile(3005, 3376, 0),
    Tile(2999, 3375, 0),
    Tile(2996, 3377, 0),
    Tile(2989, 3378, 0)
)

val giantMoleLair = areas["giant_mole_lair"]
val initialCaveTile: Tile = Tile(1752, 5237, 0)

//temp
var commandLocation: Tile = Tile.EMPTY

adminCommand("tomole") {
    player.tele(commandLocation)
}
//temp

inventoryOption("Dig") {
    val playerTile: Tile = player.tile
    if(!acceptedTiles.contains(playerTile)) {
        return@inventoryOption
    }
    player.setAnimation("dig_with_spade")
    player.open("warning_dark")
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
        if(shouldBurrowAway(currentHealth) && !World.timers.contains("await_mole_burrowing")) {
            if (it.fightStyle == "magic" && damage != 0) {
                giantMoleBurrow(npc)
            } else if (it.fightStyle != "magic") {
                giantMoleBurrow(npc)
            }
        }
    }
}

//TODO: theres a small chance that when the mole burrows to throws dirt on the clients screen extinguishing some light sources
fun giantMoleBurrow(mole: NPC) {
    mole.attackers.clear() //stop players attacking while mole is burrowing away. N: maybe a for loop getting all players attacking and setting their target to null is better?
    mole.setAnimation("giant_mole_burrow")
    World.queue("await_mole_burrowing", 3) {
        val newLocation = giantMoleLair.random(mole)
        commandLocation = newLocation!!
        mole.tele(newLocation)
    }
}

// 15% maybe too high
fun shouldThrowDirt(): Boolean {
    val dirtChance = Random.nextInt(0, 100)
    return dirtChance <= 15
}

//if the moles health is between 50% and 5% percent give a 25% to burrow away
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

// check player inventory to see if they have a lit light source.
fun hasLightSource(player: Player): Boolean {
    val playerItems = player.inventory.items

    for (item in playerItems) {
        if (item.id.contains("lantern_lit") || item.id.contains("candle_lit")) {
            return true
        }
    }
    return false
}