package rs.dusk.engine.client.ui

class InterfacesLookup(
    private val interfaces: Map<Int, Interface>,
    private val names: Map<String, Int>
) {

    fun get(id: Int) = interfaces[id] ?: Interface(id, null)

    fun get(name: String) = get(getId(name))

    fun getSafe(name: String) = get(getIdOrNull(name) ?: INVALID_ID)

    private fun getId(name: String) = getIdOrNull(name) ?: throw IllegalNameException(name)

    private fun getIdOrNull(name: String) = names[name]

    class IllegalNameException(name: String) : InterfaceException(name)

    companion object {
        private const val INVALID_ID = -1
    }

}