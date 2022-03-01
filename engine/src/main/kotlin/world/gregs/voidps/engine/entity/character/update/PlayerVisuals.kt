package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.player.BodyParts
import world.gregs.voidps.engine.entity.character.update.visual.player.*
import world.gregs.voidps.engine.entity.item.BodyPart

class PlayerVisuals(
    equipment: Container
) : Visuals() {

    val face = Face()
    val temporaryMoveType = TemporaryMoveType()
    val appearance = Appearance(body = BodyParts(equipment).apply {
        BodyPart.all.forEach {
            this.updateConnected(it)
        }
    })
    val movementType = MovementType()

    override fun reset(character: Character) {
        super.reset(character)
        watch.clear()
        timeBar.clear()
        forceChat.clear()
        hits.clear()
        face.clear()
        forceMovement.clear()
        secondaryGraphic.clear()
        colourOverlay.clear()
        if (temporaryMoveType.needsReset()) {
            flag(TEMPORARY_MOVE_TYPE_MASK)
            temporaryMoveType.reset()
        }
        primaryGraphic.clear()
        animation.clear()
        appearance.clear()
        if (movementType.needsReset()) {
            flag(MOVEMENT_TYPE_MASK)
            movementType.reset()
        }
    }
}