package content.entity.effect

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.timer.*

class ColourOverlay : Script {

    init {
        timerStart("colour_overlay", ::start)
        npcTimerStart("colour_overlay", ::start)
        timerTick("colour_overlay", ::tick)
        npcTimerTick("colour_overlay", ::tick)
        timerStop("colour_overlay", ::stop)
        npcTimerStop("colour_overlay", ::stop)
    }

    fun start(character: Character, restart: Boolean): Int {
        val overlay = character.visuals.colourOverlay
        return (overlay.delay + overlay.duration) / 30
    }

    fun tick(character: Character): Int = Timer.CANCEL

    fun stop(character: Character, logout: Boolean) {
        character.visuals.colourOverlay.reset()
    }
}
