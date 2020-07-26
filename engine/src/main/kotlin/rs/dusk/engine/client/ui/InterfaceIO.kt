package rs.dusk.engine.client.ui

interface InterfaceIO {
    fun sendOpen(inter: Interface)
    fun sendClose(inter: Interface)
    fun notifyClosed(inter: Interface)
    fun notifyOpened(inter: Interface)
    fun notifyRefreshed(inter: Interface)
}