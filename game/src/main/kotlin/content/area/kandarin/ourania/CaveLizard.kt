package content.area.kandarin.ourania

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

class CaveLizard : Script {

    init {
        npcSpawn("cave_lizard") {
            softTimers.start("aggressive_hunt_mode_switch")
        }

        npcTimerStart("aggressive_hunt_mode_switch") { random.nextInt(6, 12) }

        npcTimerTick("aggressive_hunt_mode_switch") {
            huntMode = if (random.nextBoolean()) "aggressive" else "aggressive_npcs"
            Timer.CONTINUE
        }

        huntNPC("aggressive_npcs") { target ->
            if (id == "cave_lizard" && target.id.startsWith("zamorak_")) {
                interactNpc(target, "Attack")
            }
        }

        huntPlayer("cave_lizard", "aggressive") { target ->
            interactPlayer(target, "Attack")
        }
    }
}
