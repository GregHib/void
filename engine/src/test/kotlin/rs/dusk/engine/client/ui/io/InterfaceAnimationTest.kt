package rs.dusk.engine.client.ui.io

import rs.dusk.engine.client.ui.InterfaceSendTest
import rs.dusk.engine.client.ui.detail.InterfaceDetail

internal class InterfaceAnimationTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendAnimation(name, component, 123)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendAnimation(id, component, 123)
    }

    override fun expected(inter: InterfaceDetail, component: Int) {
        io.sendAnimation(inter, component, 123)
    }

}
