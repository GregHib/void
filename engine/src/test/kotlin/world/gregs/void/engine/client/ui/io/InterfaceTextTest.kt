package world.gregs.void.engine.client.ui.io

import world.gregs.void.engine.client.ui.InterfaceSendTest
import world.gregs.void.engine.client.ui.detail.InterfaceComponentDetail

internal class InterfaceTextTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendText(name, component, "text")
    }

    override fun expected(component: InterfaceComponentDetail) {
        io.sendText(component, "text")
    }

}
