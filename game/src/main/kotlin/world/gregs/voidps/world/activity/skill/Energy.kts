package world.gregs.voidps.world.activity.skill

import kotlinx.coroutines.Job
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEffect
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.encode.sendRunEnergy
import world.gregs.voidps.utility.Math.interpolate

StringMapVariable(173, Variable.Type.VARP, true, mapOf(
    "walk" to 0,
    "run" to 1,
    "rest" to 3,
    "music" to 4
)).register("movement")

val maxEnergy = 10000

class Energy : PlayerEffect("restore") {
    lateinit var task: Job

    override fun onStart(player: Player) {
        super.onStart(player)
        task = delay(1, loop = true) {
            val energy = player["energy", maxEnergy]
            val movement = player.getVar("movement", "walk")
            var change = 0
            if (player.movement.steps.isNotEmpty() && movement == "run") {
                val weight = player["weight", 0].coerceIn(-64, 64)
                val decrement = (67 * weight) / 64
                change = decrement * -10
            } else if (energy < maxEnergy) {
                val agility = player.levels.get(Skill.Agility)
                // Approximations based on wiki
                change = when (player.getVar("movement", "walk")) {
                    "rest" -> interpolate(agility, 168, 310, 1, 99)
                    "music" -> interpolate(agility, 240, 400, 1, 99)
                    else -> interpolate(agility, 27, 157, 1, 99)
                }
            }
            if(change != 0) {
                val updated = (energy + change).coerceIn(0, maxEnergy)
                player["energy", true] = updated
                val percentage = (updated / maxEnergy.toDouble() * 100).toInt()
                player.sendRunEnergy(percentage)
            }
        }
    }

    override fun onFinish(player: Player) {
        super.onFinish(player)
        task.cancel()
    }
}


on<InterfaceOpened>({ name == "energy_orb" }) { player: Player ->
    player.sendRunEnergy(player["energy", maxEnergy] / maxEnergy * 100)
}

on<Registered> { player: Player ->
    player.sendVar("movement")
    player.running = player.getVar("movement", "walk") == "run"
    player.effects.add(Energy())
}

on<InterfaceOption>({ name == "energy_orb" && option == "Turn Run mode on" }) { player: Player ->
    val walk = player.getVar("movement", "walk") == "walk"
    player.setVar("movement", if (walk) "run" else "walk")
    player.running = walk
}

val animations = setOf(
    "rest_arms_back",
    "rest_arms_crossed",
    "rest_legs_out"
)

on<InterfaceOption>({ name == "energy_orb" && option == "Rest" }) { player: Player ->
    player.action(ActionType.Resting) {
        val type: String = player.getVar("movement", "walk")
        val anim = animations.random()
        try {
            player.setVar("movement", "rest")
            player.setAnimation(anim)
            player.message("You begin resting..", ChatType.GameFilter)
            await(Suspension.Infinite)
        } finally {
            player.setAnimation(anim.replace("rest", "stand"))
            player.setVar("movement", type)
            player.movement.frozen = true
            delay(player, if (type == "run") 2 else 3) {
                player.clearAnimation()
                player.movement.frozen = false
            }
        }
    }
}