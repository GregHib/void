package rs.dusk.engine.client.ui

internal class InterfaceItemTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendItem(name, component, 1234, 10)
    }

    override fun sendIdString(id: Int, component: String): Boolean {
        return manager.sendItem(id, component, 1234, 10)
    }

    override fun sendStringId(name: String, id: Int): Boolean {
        return manager.sendItem(name, id, 1234, 10)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendItem(id, component, 1234, 10)
    }

    override fun expected(inter: Interface, component: Int) {
        io.sendItem(inter, component, 1234, 10)
    }

}
