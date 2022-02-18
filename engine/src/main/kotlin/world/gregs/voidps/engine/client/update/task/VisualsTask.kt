package world.gregs.voidps.engine.client.update.task

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.Visuals

abstract class VisualsTask<C : Character>(
    iterator: TaskIterator<C>,
    override val characters: CharacterList<C>,
    internal val encoders: Array<VisualEncoder<Visual>>,
    addMasks: IntArray // Order of these is important
) : CharacterTask<C>(iterator) {

    internal val addEncoders = addMasks.map { mask -> encoders.first { it.mask == mask } }

    /**
     * Encodes [Visual] changes into an insertion and delta update
     */
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun run(character: C) {
        val visuals = character.visuals
        if (visuals.flag == 0) {
            visuals.update = null
            return
        }
        encodeUpdate(visuals)
        if (addEncoders.any { encoder -> visuals.flagged(encoder.mask) }) {
            encodeAddition(visuals)
        }
        visuals.flag = 0
    }

    /**
     * Encodes all flagged visuals into one reusable [Visuals.update]
     */
    abstract fun encodeUpdate(visuals: Visuals)

    /**
     * Encodes [addEncoders] visuals into one reusable [Visuals.addition]
     */
    abstract fun encodeAddition(visuals: Visuals)

}