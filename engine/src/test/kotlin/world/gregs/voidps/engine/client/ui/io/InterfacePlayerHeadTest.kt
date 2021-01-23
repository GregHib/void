package world.gregs.voidps.engine.client.ui.io

import world.gregs.voidps.engine.client.ui.InterfaceSendTest
import world.gregs.voidps.engine.client.ui.detail.InterfaceComponentDetail

internal class InterfacePlayerHeadTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendPlayerHead(name, component)
    }

    override fun expected(component: InterfaceComponentDetail) {
        io.sendPlayerHead(component)
    }

}
