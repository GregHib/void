package content.entity.player.modal.tab

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class OpenQuestJournal(
    override val character: Player,
    val quest: String,
) : Interaction<Player>() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "journal_open"
        1 -> quest
        else -> null
    }
}

fun questJournalOpen(quest: String, handler: suspend OpenQuestJournal.() -> Unit) {
    Events.handle<OpenQuestJournal>("journal_open", quest) {
        handler.invoke(this)
    }
}
