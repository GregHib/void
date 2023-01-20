package world.gregs.voidps.engine.client.ui.dialogue

import world.gregs.voidps.engine.event.Event

data class ContinueDialogue(
    val id: String,
    val component: String,
    val option: Int
) : Event