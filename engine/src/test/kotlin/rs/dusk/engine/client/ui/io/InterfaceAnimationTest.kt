package rs.dusk.engine.client.ui.io

import rs.dusk.engine.client.ui.InterfaceSendTest
import rs.dusk.engine.client.ui.detail.InterfaceComponentDetail

internal class InterfaceAnimationTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendAnimation(name, component, 123)
    }

    override fun expected(component: InterfaceComponentDetail) {
        io.sendAnimation(component, 123)
    }

}
