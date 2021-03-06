package world.gregs.voidps.engine.client.ui.io

import world.gregs.voidps.engine.client.ui.InterfaceSendTest
import world.gregs.voidps.engine.client.ui.detail.InterfaceComponentDetail

internal class InterfaceItemTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendItem(name, component, 1234, 10)
    }

    override fun expected(component: InterfaceComponentDetail) {
        io.sendItem(component, 1234, 10)
    }

}
