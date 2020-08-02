package rs.dusk.engine.client.ui

internal class InterfacePlayerHeadTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendPlayerHead(name, component)
    }

    override fun sendIdString(id: Int, component: String): Boolean {
        return manager.sendPlayerHead(id, component)
    }

    override fun sendStringId(name: String, id: Int): Boolean {
        return manager.sendPlayerHead(name, id)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendPlayerHead(id, component)
    }

    override fun expected(inter: Interface, component: Int) {
        io.sendPlayerHead(inter, component)
    }

}
