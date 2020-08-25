package rs.dusk.engine.client.ui.io

import rs.dusk.engine.client.ui.InterfaceSendTest
import rs.dusk.engine.client.ui.detail.InterfaceDetail

internal class InterfaceSettingsVarargTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendSettings(name, component, 12, 34, 56, 57, 58, 59)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendSettings(id, component, 12, 34, 56, 57, 58, 59)
    }

    override fun expected(inter: InterfaceDetail, component: Int) {
        io.sendSettings(inter, component, 12, 34, 503316480)
    }

}
