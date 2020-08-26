package rs.dusk.engine.client.ui.io

import rs.dusk.engine.client.ui.InterfaceSendTest
import rs.dusk.engine.client.ui.detail.InterfaceComponentDetail

internal class InterfaceTextTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendText(name, component, "text")
    }

    override fun expected(component: InterfaceComponentDetail) {
        io.sendText(component, "text")
    }

}
