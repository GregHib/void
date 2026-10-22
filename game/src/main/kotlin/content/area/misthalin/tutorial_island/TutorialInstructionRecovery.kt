package content.area.misthalin.tutorial_island

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.hasOpen

/** Repairs client-side loss of the chat-box instructions without closing interfaces or actions. */
class TutorialInstructionRecovery : Script {
    init {
        playerSpawn {
            if (inTutorial) softTimers.start("tutorial_instruction_recovery")
        }

        // Also covers entering the tutorial through the development stage command after login.
        interfaceOpened("tutorial_text") {
            if (inTutorial) softTimers.startIfAbsent("tutorial_instruction_recovery")
        }

        timerStart("tutorial_instruction_recovery") { 5 }

        timerTick("tutorial_instruction_recovery") {
            if (!inTutorial || TutorialIsland.row(tutorialStage) == null) {
                -1 // Cancel once the character leaves the tutorial.
            } else {
                if (!hasOpen("character_creation")) {
                    // sendInstruction respects live dialogue and refreshes an already-open box.
                    // Refresh is necessary when the client closed it without notifying the server;
                    // reopening it through Player.open would clear delayed skill actions.
                    TutorialIsland.sendInstruction(this)
                }
                5
            }
        }
    }
}
