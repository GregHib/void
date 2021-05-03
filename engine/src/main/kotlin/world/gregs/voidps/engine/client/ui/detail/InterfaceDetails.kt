package world.gregs.voidps.engine.client.ui.detail

import world.gregs.voidps.engine.client.ui.InterfaceException

class InterfaceDetails {

    private lateinit var interfaces: Map<String, InterfaceDetail>
    private lateinit var names: Map<Int, String>

    val size: Int
        get() = interfaces.count()

    fun load(interfaces: Map<String, InterfaceDetail>, names: Map<Int, String>) {
        this.interfaces = interfaces
        this.names = names
    }

    fun get(name: String) = interfaces[name] ?: throw IllegalNameException(name)

    fun getSafe(name: String) = interfaces[name] ?: InterfaceDetail(id = INVALID_ID, name = name)

    fun getComponent(name: String, component: String): InterfaceComponentDetail {
        return getComponentOrNull(name, component) ?: InterfaceComponentDetail(-1, "")
    }

    fun getComponentOrNull(name: String, component: String): InterfaceComponentDetail? {
        val inter = getSafe(name)
        return inter.getComponent(component)
    }

    fun get(id: Int) = get(getName(id))

    fun getName(id: Int) = getNameOrNull(id) ?: throw IllegalNameException("$id")

    fun getNameOrNull(id: Int) = names[id]

    class IllegalNameException(name: String) : InterfaceException(name)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterfaceDetails

        if (interfaces != other.interfaces) return false
        if (names != other.names) return false

        return true
    }

    override fun hashCode(): Int {
        var result = interfaces.hashCode()
        result = 31 * result + names.hashCode()
        return result
    }

    companion object {
        private const val INVALID_ID = -1
    }


}