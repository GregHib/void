package rs.dusk.engine.client.ui

internal class InterfaceSpriteTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendSprite(name, component, 123)
    }

    override fun sendIdString(id: Int, component: String): Boolean {
        return manager.sendSprite(id, component, 123)
    }

    override fun sendStringId(name: String, id: Int): Boolean {
        return manager.sendSprite(name, id, 123)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendSprite(id, component, 123)
    }

    override fun expected(inter: Interface, component: Int) {
        io.sendSprite(inter, component, 123)
    }

}
