package rs.dusk.engine.client.ui

import rs.dusk.engine.client.ui.detail.InterfaceDetail

internal class InterfaceSpriteTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendSprite(name, component, 123)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendSprite(id, component, 123)
    }

    override fun expected(inter: InterfaceDetail, component: Int) {
        io.sendSprite(inter, component, 123)
    }

}
