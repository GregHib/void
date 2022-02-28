package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.update.visual.*
import world.gregs.voidps.engine.entity.character.update.visual.npc.Transformation
import world.gregs.voidps.engine.entity.character.update.visual.npc.Turn

class NPCVisuals(
    var flag: Int = 0,
) : Visuals {

    val transform = Transformation()
    override val animation = Animation()
    override val primaryGraphic = Graphic()
    val turn = Turn()
    override val forceMovement = ForceMovement()
    override val colourOverlay = ColourOverlay()
    override val hits = Hits()
    override val watch = Watch()
    override val forceChat = ForceChat()
    override val timeBar = TimeBar()
    override val secondaryGraphic = Graphic()

    override fun flag(mask: Int) {
        flag = flag or mask
    }

    override fun flagged(mask: Int): Boolean {
        return flag and mask != 0
    }

    override fun reset(character: Character) {
        flag = 0
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