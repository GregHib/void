package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.player.BodyParts
import world.gregs.voidps.engine.entity.character.update.visual.player.Appearance
import world.gregs.voidps.engine.entity.character.update.visual.player.Face
import world.gregs.voidps.engine.entity.character.update.visual.player.MovementType
import world.gregs.voidps.engine.entity.character.update.visual.player.TemporaryMoveType
import world.gregs.voidps.engine.entity.item.BodyPart

class PlayerVisuals(
    equipment: Container
) : Visuals() {

    val face = Face()
    val temporaryMoveType = TemporaryMoveType()
    val appearance = Appearance(body = BodyParts(equipment, intArrayOf(3, 14, 18, 26, 34, 38, 42)).apply {
        BodyPart.all.forEach {
            this.updateConnected(it)
        }
    })
    val movementType = MovementType()

    override fun reset(character: Character) {
        super.reset(character)
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
}