package rs.dusk.engine.client.ui

import rs.dusk.engine.client.ui.detail.InterfaceDetail

interface InterfaceIO {
    fun sendOpen(inter: InterfaceDetail)
    fun sendClose(inter: InterfaceDetail)
    fun notifyClosed(inter: InterfaceDetail)
    fun notifyOpened(inter: InterfaceDetail)
    fun notifyRefreshed(inter: InterfaceDetail)
    fun sendPlayerHead(inter: InterfaceDetail, component: Int)
    fun sendAnimation(inter: InterfaceDetail, component: Int, animation: Int)
    fun sendNPCHead(inter: InterfaceDetail, component: Int, npc: Int)
    fun sendText(inter: InterfaceDetail, component: Int, text: String)
    fun sendVisibility(inter: InterfaceDetail, component: Int, visible: Boolean)
    fun sendSprite(inter: InterfaceDetail, component: Int, sprite: Int)
    fun sendItem(inter: InterfaceDetail, component: Int, item: Int, amount: Int)
    fun sendSettings(inter: InterfaceDetail, component: Int, from: Int, to: Int, setting: Int)
}