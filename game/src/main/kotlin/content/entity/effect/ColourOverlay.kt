package content.entity.effect

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.*

@Script
class ColourOverlay : Api {

    @Timer("colour_overlay")
    override fun start(character: Character, timer: String, restart: Boolean): Int {
        val overlay = character.visuals.colourOverlay
        return (overlay.delay + overlay.duration) / 30
    }

    @Timer("colour_overlay")
    override fun tick(character: Character, timer: String) = Timer.CANCEL

    @Timer("colour_overlay")
    override fun stop(character: Character, timer: String, logout: Boolean) {
        character.visuals.colourOverlay.reset()
    }
}
