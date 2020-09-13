package rs.dusk.engine.client.ui.io

import rs.dusk.engine.client.ui.InterfaceSendTest
import rs.dusk.engine.client.ui.detail.InterfaceComponentDetail

internal class InterfacePlayerHeadTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendPlayerHead(name, component)
    }

    override fun expected(component: InterfaceComponentDetail) {
        io.sendPlayerHead(component)
    }

}
