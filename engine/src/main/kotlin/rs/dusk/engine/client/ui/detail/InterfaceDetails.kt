package rs.dusk.engine.client.ui.detail

import rs.dusk.engine.client.ui.InterfaceException

data class InterfaceDetails(
    private val interfaces: Map<String, InterfaceDetail>,
    private val names: Map<Int, String>
) {

    fun get(name: String) = interfaces[name] ?: throw IllegalNameException(name)

    fun getSafe(name: String) = interfaces[name] ?: InterfaceDetail(id = INVALID_ID, name = name)

    fun get(name: String, component: String): InterfaceComponentDetail? {
        val inter = getSafe(name)
        return inter.getComponent(component)
    }

    fun get(id: Int) = get(getName(id))

    fun getName(id: Int) = getNameOrNull(id) ?: throw IllegalNameException("$id")

    fun getNameOrNull(id: Int) = names[id]

    val size: Int = interfaces.count()

    class IllegalNameException(name: String) : InterfaceException(name)

    companion object {
        private const val INVALID_ID = -1
    }

}