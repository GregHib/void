package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class Command(
    override val character: Character,
    val prefix: String,
    val content: String
) : Interaction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}

fun command(command: String, block: suspend Command.() -> Unit) {
    on<Command>({ wildcardEquals(command, prefix) }) { player: Player ->
        block.invoke(this)
    }
}