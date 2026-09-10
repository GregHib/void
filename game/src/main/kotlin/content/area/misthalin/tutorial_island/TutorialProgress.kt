package content.area.misthalin.tutorial_island

import content.bot.isBot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.timer.Timer

/**
 * Restores the tutorial on login and chains character creation into the first stage.
 */
class TutorialProgress : Script {

    init {
        playerSpawn {
            if (!inTutorial) {
                return@playerSpawn
            }
            if (Settings["world.start.creation", true] && !isBot && !get("tutorial_designed", false)) {
                sendVariable("movement")
                this["delay"] = -1
                World.queue("tutorial_creation_$name", 1) {
                    open("character_creation")
                }
                return@playerSpawn
            }
            renderTutorial()
        }

        interfaceClosed("character_creation") {
            if (!inTutorial) {
                return@interfaceClosed
            }
            set("tutorial_designed", true)
            flagAppearance()
            stop("delay")
            renderTutorial()
        }

        // Dialogue shares the chat box slot with the instruction box. Wait a tick before
        // putting the instructions back so a multi-step conversation isn't interrupted
        // between two of its own boxes.
        interfaceClosed("dialogue_*") {
            if (!inTutorial) {
                return@interfaceClosed
            }
            softTimers.start("tutorial_instructions")
        }

        timerStart("tutorial_instructions") { 1 }

        timerTick("tutorial_instructions") {
            if (inTutorial) {
                renderTutorialText()
            }
            Timer.CANCEL
        }
    }
}
