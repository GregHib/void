package world.gregs.void.engine.client.ui.io

import world.gregs.void.engine.client.ui.InterfaceSendTest
import world.gregs.void.engine.client.ui.detail.InterfaceComponentDetail

internal class InterfaceVisibilityTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendVisibility(name, component, true)
    }

    override fun expected(component: InterfaceComponentDetail) {
        io.sendVisibility(component, true)
    }

}
