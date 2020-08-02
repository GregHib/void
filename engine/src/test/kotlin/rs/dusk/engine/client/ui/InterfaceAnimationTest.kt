package rs.dusk.engine.client.ui

internal class InterfaceAnimationTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendAnimation(name, component, 123)
    }

    override fun sendIdString(id: Int, component: String): Boolean {
        return manager.sendAnimation(id, component, 123)
    }

    override fun sendStringId(name: String, id: Int): Boolean {
        return manager.sendAnimation(name, id, 123)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendAnimation(id, component, 123)
    }

    override fun expected(inter: Interface, component: Int) {
        io.sendAnimation(inter, component, 123)
    }

}
