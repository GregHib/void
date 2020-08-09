package rs.dusk.engine.client.ui

import rs.dusk.engine.client.ui.detail.InterfaceDetail

internal class InterfaceItemTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendItem(name, component, 1234, 10)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendItem(id, component, 1234, 10)
    }

    override fun expected(inter: InterfaceDetail, component: Int) {
        io.sendItem(inter, component, 1234, 10)
    }

}
