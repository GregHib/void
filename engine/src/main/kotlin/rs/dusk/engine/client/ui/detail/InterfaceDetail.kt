package rs.dusk.engine.client.ui.detail

import rs.dusk.engine.client.ui.InterfaceException

data class InterfaceDetail(
    val id: Int,
    val name: String = "",
    val type: String = "",
    val data: InterfaceData? = null,
    val components: Map<String, InterfaceComponentDetail> = mutableMapOf(),
    val componentNames: Map<Int, String> = components.map { it.value.id to it.key }.toMap()
) {

    init {
        components.values.forEach {
            it.parent = id
        }
    }

    class InvalidInterfaceException : InterfaceException()

    fun getIndex(resizable: Boolean): Int {
        return data?.getIndex(resizable) ?: throw InvalidInterfaceException()
    }

    fun getParent(resizable: Boolean): Int {
        return data?.getParent(resizable) ?: throw InvalidInterfaceException()
    }

    fun getComponent(name: String): InterfaceComponentDetail? = components[name]

    fun getComponentName(id: Int): String = componentNames[id] ?: ""

}