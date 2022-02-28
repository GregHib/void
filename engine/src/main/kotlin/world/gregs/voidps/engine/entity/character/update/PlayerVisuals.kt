package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.BodyParts
import world.gregs.voidps.engine.entity.character.update.visual.*
import world.gregs.voidps.engine.entity.character.update.visual.player.Appearance
import world.gregs.voidps.engine.entity.character.update.visual.player.Face
import world.gregs.voidps.engine.entity.character.update.visual.player.MovementType
import world.gregs.voidps.engine.entity.character.update.visual.player.TemporaryMoveType

class PlayerVisuals(
    var flag: Int = 0,
    override var aspects: MutableMap<Int, Visual> = mutableMapOf(),
    body: BodyParts
) : Visuals {

    val watch = Watch()
    val timeBar = TimeBar()
    val forceChat = ForceChat()
    val hits = Hits()
    val face = Face()
    val forceMovement = ForceMovement()
    val secondaryGraphic = Graphic()
    val colourOverlay = ColourOverlay()
    val temporaryMoveType = TemporaryMoveType()
    val primaryGraphic = Graphic()
    val animation = Animation()
    val appearance = Appearance(body = body)
    val movementType = MovementType()

    override fun reset(character: Character) {
        flag = 0
        watch.resetWhenNeeded(character)
        timeBar.resetWhenNeeded(character)
        forceChat.resetWhenNeeded(character)
        hits.resetWhenNeeded(character)
        face.resetWhenNeeded(character)
        forceMovement.resetWhenNeeded(character)
        secondaryGraphic.resetWhenNeeded(character)
        colourOverlay.resetWhenNeeded(character)
        temporaryMoveType.resetWhenNeeded(character)
        primaryGraphic.resetWhenNeeded(character)
        animation.resetWhenNeeded(character)
        appearance.resetWhenNeeded(character)
        movementType.resetWhenNeeded(character)
    }

    override fun <T : Visual> getOrPut(mask: Int, put: () -> T): T {
        return aspects.getOrPut(mask, put) as T
    }

    override fun flag(mask: Int) {
        flag = flag or mask
    }

    override fun flagged(mask: Int): Boolean {
        return flag and mask != 0
    }
}