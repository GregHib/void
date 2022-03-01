package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.update.visual.npc.Transformation
import world.gregs.voidps.engine.entity.character.update.visual.npc.Turn

class NPCVisuals : Visuals() {

    val transform = Transformation()
    val turn = Turn()

    override fun reset(character: Character) {
        super.reset(character)
        transform.resetWhenNeeded(character)
        animation.resetWhenNeeded(character)
        primaryGraphic.resetWhenNeeded(character)
        turn.resetWhenNeeded(character)
        forceMovement.resetWhenNeeded(character)
        colourOverlay.resetWhenNeeded(character)
        hits.resetWhenNeeded(character)
        watch.resetWhenNeeded(character)
        forceChat.resetWhenNeeded(character)
        timeBar.resetWhenNeeded(character)
        secondaryGraphic.resetWhenNeeded(character)
    }
}