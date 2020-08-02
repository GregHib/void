package rs.dusk.engine.client.ui

internal class InterfaceNPCHeadTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendNPCHead(name, component, 1234)
    }

    override fun sendIdString(id: Int, component: String): Boolean {
        return manager.sendNPCHead(id, component, 1234)
    }

    override fun sendStringId(name: String, id: Int): Boolean {
        return manager.sendNPCHead(name, id, 1234)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendNPCHead(id, component, 1234)
    }

    override fun expected(inter: Interface, component: Int) {
        io.sendNPCHead(inter, component, 1234)
    }

}
