package world.gregs.voidps.engine.client.ui.dialogue

import world.gregs.voidps.engine.event.Event

data class ContinueDialogue(
    val id: Int,
    val name: String,
    val componentId: Int,
    val component: String,
    val type: String,
    val option: Int
) : Event