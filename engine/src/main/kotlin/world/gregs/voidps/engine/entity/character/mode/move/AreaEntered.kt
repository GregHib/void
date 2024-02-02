package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.type.Area

data class AreaEntered(
    override val character: Character,
    val name: String,
    val tags: Set<String>,
    val area: Area
) : SuspendableEvent, CharacterContext {
    override var onCancel: (() -> Unit)? = null
}

fun enter(area: String = "*", tag: String = "*", block: suspend AreaEntered.() -> Unit) {
    on<AreaEntered>({ wildcardEquals(area, name) && (tag == "*" || tags.any { wildcardEquals(tag, it) }) }) { _: Player ->
        block.invoke(this)
    }
}