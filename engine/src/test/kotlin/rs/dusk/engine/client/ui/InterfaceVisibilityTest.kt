package rs.dusk.engine.client.ui

internal class InterfaceVisibilityTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendVisibility(name, component, true)
    }

    override fun sendIdString(id: Int, component: String): Boolean {
        return manager.sendVisibility(id, component, true)
    }

    override fun sendStringId(name: String, id: Int): Boolean {
        return manager.sendVisibility(name, id, true)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendVisibility(id, component, true)
    }

    override fun expected(inter: Interface, component: Int) {
        io.sendVisibility(inter, component, true)
    }

}
