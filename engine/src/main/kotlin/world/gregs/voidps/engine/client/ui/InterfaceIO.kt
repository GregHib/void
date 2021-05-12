package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.client.ui.detail.InterfaceComponentDetail

interface InterfaceIO {
    fun sendPlayerHead(component: InterfaceComponentDetail)
    fun sendAnimation(component: InterfaceComponentDetail, animation: Int)
    fun sendNPCHead(component: InterfaceComponentDetail, npc: Int)
    fun sendText(component: InterfaceComponentDetail, text: String)
    fun sendVisibility(component: InterfaceComponentDetail, visible: Boolean)
    fun sendSprite(component: InterfaceComponentDetail, sprite: Int)
    fun sendItem(component: InterfaceComponentDetail, item: Int, amount: Int)
}