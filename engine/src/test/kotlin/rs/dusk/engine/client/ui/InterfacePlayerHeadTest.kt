package rs.dusk.engine.client.ui

import rs.dusk.engine.client.ui.detail.InterfaceDetail

internal class InterfacePlayerHeadTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendPlayerHead(name, component)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendPlayerHead(id, component)
    }

    override fun expected(inter: InterfaceDetail, component: Int) {
        io.sendPlayerHead(inter, component)
    }

}
