package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

interface SourceContext<C : Character> : Event {
    val character: C
}

val SourceContext<Player>.player: Player
    get() = character

val SourceContext<Player>.npc: Player
    get() = character