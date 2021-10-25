package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.event.Event

data class InterfaceSwitch(
    val id: String,
    val component: String,
    val fromItemId: Int,
    val fromSlot: Int,
    val toId: String,
    val toComponent: String,
    val toItemId: Int,
    val toSlot: Int
) : Event