package content.area.misthalin.tutorial_island

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.Timer

/** Repairs client-side loss of instructions without closing interfaces or cancelling actions. */
class TutorialInstructionRecovery : Script {
    init {
        playerSpawn { startRecovery() }

        interfaceOpened("tutorial_text") { startRecovery() }

        timerStart("tutorial_instruction_recovery") { 5 }

        timerTick("tutorial_instruction_recovery") { recoverInstruction() }
    }

    private fun Player.startRecovery() {
        if (inTutorial) {
            softTimers.startIfAbsent("tutorial_instruction_recovery")
        }
    }

    private fun Player.recoverInstruction(): Int {
        if (!inTutorial || TutorialIsland.row(tutorialStage) == null) {
            return Timer.CANCEL
        }
        if (!hasOpen("character_creation")) {
            // Refresh avoids Player.open clearing delayed skill actions or replacing dialogue.
            TutorialIsland.sendInstruction(this)
        }
        return 5
    }
}
