import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.StartEffect
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.sendRunEnergy
import world.gregs.voidps.utility.Math
import world.gregs.voidps.world.interact.entity.player.energy.MAX_ENERGY
import world.gregs.voidps.world.interact.entity.player.energy.energyPercent

on<StartEffect>({ effect == "energy" }) { player: Player ->
    delay(player, 1, loop = true){
        val energy = player["energy", MAX_ENERGY]
        val movement = player.getVar("movement", "walk")
        val change = when {
            player.movement.moving && movement == "run" -> getDrainAmount(player)
            energy < MAX_ENERGY -> getRestoreAmount(player)
            else -> 0
        }
        if (change != 0) {
            val updated = (energy + change).coerceIn(0, MAX_ENERGY)
            setEnergy(player, updated)
            walkWhenOutOfEnergy(player, updated)
        }
    }
}

fun getDrainAmount(player: Player): Int {
    val weight = player["weight", 0].coerceIn(-64, 64)
    val decrement = (67 * weight) / 64
    return -decrement
}

fun getRestoreAmount(player: Player): Int {
    val agility = player.levels.get(Skill.Agility)
    // Approximations based on wiki
    return when (player.getVar("movement", "walk")) {
        "rest" -> Math.interpolate(agility, 168, 310, 1, 99)
        "music" -> Math.interpolate(agility, 240, 400, 1, 99)
        else -> Math.interpolate(agility, 27, 157, 1, 99)
    }
}

fun setEnergy(player: Player, energy: Int) {
    player["energy", true] = energy
    player.sendRunEnergy(player.energyPercent())
}

fun walkWhenOutOfEnergy(player: Player, energy: Int) {
    if (energy == 0) {
        player.setVar("movement", "walk")
        player.running = false
    }
}