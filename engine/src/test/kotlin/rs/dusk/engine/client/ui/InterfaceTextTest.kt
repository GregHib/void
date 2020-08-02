package rs.dusk.engine.client.ui

internal class InterfaceTextTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendText(name, component, "text")
    }

    override fun sendIdString(id: Int, component: String): Boolean {
        return manager.sendText(id, component, "text")
    }

    override fun sendStringId(name: String, id: Int): Boolean {
        return manager.sendText(name, id, "text")
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendText(id, component, "text")
    }

    override fun expected(inter: Interface, component: Int) {
        io.sendText(inter, component, "text")
    }

}
