package rs.dusk.engine.client.ui

import rs.dusk.engine.client.ui.detail.InterfaceComponentDetail
import rs.dusk.engine.client.ui.detail.InterfaceDetail

interface InterfaceIO {
    fun sendOpen(inter: InterfaceDetail)
    fun sendClose(inter: InterfaceDetail)
    fun notifyClosed(inter: InterfaceDetail)
    fun notifyOpened(inter: InterfaceDetail)
    fun notifyRefreshed(inter: InterfaceDetail)
    fun sendPlayerHead(component: InterfaceComponentDetail)
    fun sendAnimation(component: InterfaceComponentDetail, animation: Int)
    fun sendNPCHead(component: InterfaceComponentDetail, npc: Int)
    fun sendText(component: InterfaceComponentDetail, text: String)
    fun sendVisibility(component: InterfaceComponentDetail, visible: Boolean)
    fun sendSprite(component: InterfaceComponentDetail, sprite: Int)
    fun sendItem(component: InterfaceComponentDetail, item: Int, amount: Int)
}