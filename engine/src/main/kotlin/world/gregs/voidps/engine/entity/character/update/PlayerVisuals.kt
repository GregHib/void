package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character

/*
class PlayerVisuals(
    body: BodyParts
) : Visuals() {

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
    val appearance = Appearance(body = body)
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
        appearance.resetWhenNeeded(character)
        movementType.resetWhenNeeded(character)
    }
}*/
class PlayerVisuals(
    var flag: Int = 0,
    override var aspects: MutableMap<Int, Visual> = mutableMapOf()
) : Visuals {

    override fun <T : Visual> getOrPut(mask: Int, put: () -> T): T {
        return aspects.getOrPut(mask, put) as T
    }

    override fun flag(mask: Int) {
        flag = flag or mask
    }

    override fun flagged(mask: Int): Boolean {
        return flag and mask != 0
    }

    override fun reset(character: Character) {
        flag = 0
        aspects.forEach { (_, visual) ->
            visual.resetWhenNeeded(character)
        }
    }
}