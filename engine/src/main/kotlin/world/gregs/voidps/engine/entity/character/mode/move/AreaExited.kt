package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.type.Area

data class AreaExited(
    override val character: Character,
    val name: String,
    val tags: Set<String>,
    val area: Area
) : SuspendableEvent, CharacterContext {
    override var onCancel: (() -> Unit)? = null
}

fun exitArea(area: String = "*", tag: String = "*", block: suspend AreaExited.() -> Unit) {
    on<AreaExited>({ wildcardEquals(area, name) && (tag == "*" || tags.any { wildcardEquals(tag, it) }) }) {
        block.invoke(this)
    }
}