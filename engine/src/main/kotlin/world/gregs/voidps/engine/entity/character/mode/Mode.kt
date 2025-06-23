package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.Character

/**
 * Mode represents a finite state machine for the main thing each [Character] is doing.
 */
interface Mode {
    fun start() {}
    fun tick()
    fun stop(replacement: Mode) {}
}
