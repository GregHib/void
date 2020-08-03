package rs.dusk.engine.client.ui

internal class InterfaceSettingsTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendSetting(name, component, 12, 34, 56)
    }

    override fun sendIdString(id: Int, component: String): Boolean {
        return manager.sendSetting(id, component, 12, 34, 56)
    }

    override fun sendStringId(name: String, id: Int): Boolean {
        return manager.sendSetting(name, id, 12, 34, 56)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendSetting(id, component, 12, 34, 56)
    }

    override fun expected(inter: Interface, component: Int) {
        io.sendSettings(inter, component, 12, 34, 56)
    }

}
