package world.gregs.void.engine.client.ui.io

import world.gregs.void.engine.client.ui.InterfaceSendTest
import world.gregs.void.engine.client.ui.detail.InterfaceComponentDetail

internal class InterfacePlayerHeadTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendPlayerHead(name, component)
    }

    override fun expected(component: InterfaceComponentDetail) {
        io.sendPlayerHead(component)
    }

}
