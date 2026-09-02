package content.area.misthalin.tutorial_island

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.message

class TutorialCommands : Script {

    init {
        adminCommand("tutorial", intArg("stage", optional = true), desc = "Jump to a Tutorial Island stage, or restart it") { args ->
            val stage = args.getOrNull(0)?.toIntOrNull() ?: 0
            if (stage !in 0 until TutorialIsland.stages) {
                message("Stage must be between 0 and ${TutorialIsland.stages - 1}.")
                return@adminCommand
            }
            set("tutorial_stage", stage)
            set("tutorial_designed", true)
            renderTutorial()
            message("Tutorial Island stage set to $stage.")
        }
    }
}
