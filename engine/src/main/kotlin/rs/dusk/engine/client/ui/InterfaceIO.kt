package rs.dusk.engine.client.ui

interface InterfaceIO {
    fun sendOpen(inter: Interface)
    fun sendClose(inter: Interface)
    fun notifyClosed(inter: Interface)
    fun notifyOpened(inter: Interface)
    fun notifyRefreshed(inter: Interface)
    fun sendPlayerHead(inter: Interface, component: Int)
    fun sendAnimation(inter: Interface, component: Int, animation: Int)
    fun sendNPCHead(inter: Interface, component: Int, npc: Int)
    fun sendText(inter: Interface, component: Int, text: String)
    fun sendVisibility(inter: Interface, component: Int, visible: Boolean)
    fun sendSprite(inter: Interface, component: Int, sprite: Int)
    fun sendItem(inter: Interface, component: Int, item: Int, amount: Int)
    fun sendSettings(inter: Interface, component: Int, from: Int, to: Int, setting: Int)
}