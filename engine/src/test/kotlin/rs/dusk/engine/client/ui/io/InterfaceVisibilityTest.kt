package rs.dusk.engine.client.ui.io

import rs.dusk.engine.client.ui.InterfaceSendTest
import rs.dusk.engine.client.ui.detail.InterfaceDetail

internal class InterfaceVisibilityTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendVisibility(name, component, true)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendVisibility(id, component, true)
    }

    override fun expected(inter: InterfaceDetail, component: Int) {
        io.sendVisibility(inter, component, true)
    }

}
