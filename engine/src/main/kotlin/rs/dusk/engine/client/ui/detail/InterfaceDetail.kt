package rs.dusk.engine.client.ui.detail

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import rs.dusk.engine.client.ui.InterfaceException

data class InterfaceDetail(
    val id: Int,
    val name: String = "",
    val type: String = "",
    val data: InterfaceData? = null,
    val components: BiMap<String, InterfaceComponentDetail> = HashBiMap.create()
) {

    constructor(id: Int, name: String = "", type: String = "", data: InterfaceData? = null, components: Map<String, InterfaceComponentDetail>) : this(id, name, type, data, HashBiMap.create(components))

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

    fun containsComponent(component: InterfaceComponentDetail): Boolean = components.inverse().containsKey(component)

    fun getComponent(name: String): InterfaceComponentDetail? = components[name]

    fun getComponentName(id: Int): String = components.filter { it.value.id == id }.values.firstOrNull()?.name ?: ""

}