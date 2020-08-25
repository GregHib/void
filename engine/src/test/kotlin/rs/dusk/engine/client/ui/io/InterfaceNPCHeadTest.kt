package rs.dusk.engine.client.ui.io

import rs.dusk.engine.client.ui.InterfaceSendTest
import rs.dusk.engine.client.ui.detail.InterfaceDetail

internal class InterfaceNPCHeadTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendNPCHead(name, component, 1234)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendNPCHead(id, component, 1234)
    }

    override fun expected(inter: InterfaceDetail, component: Int) {
        io.sendNPCHead(inter, component, 1234)
    }

}
