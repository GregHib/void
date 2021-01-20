package world.gregs.void.engine.client.ui.io

import world.gregs.void.engine.client.ui.InterfaceSendTest
import world.gregs.void.engine.client.ui.detail.InterfaceComponentDetail

internal class InterfaceSpriteTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendSprite(name, component, 123)
    }

    override fun expected(component: InterfaceComponentDetail) {
        io.sendSprite(component, 123)
    }

}
