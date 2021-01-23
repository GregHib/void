package world.gregs.voidps.engine.client.ui.io

import world.gregs.voidps.engine.client.ui.InterfaceSendTest
import world.gregs.voidps.engine.client.ui.detail.InterfaceComponentDetail

internal class InterfaceNPCHeadTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendNPCHead(name, component, 1234)
    }

    override fun expected(component: InterfaceComponentDetail) {
        io.sendNPCHead(component, 1234)
    }

}
