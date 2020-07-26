package rs.dusk.engine.client.ui

data class Interface(
    val id: Int,
    val name: String? = null,
    val type: String? = null,
    val data: InterfaceData? = null,
    val components: Map<Int, String> = mapOf()
) {

    class InvalidInterfaceException : InterfaceException()

    fun getIndex(resizable: Boolean): Int {
        return data?.getIndex(resizable) ?: throw InvalidInterfaceException()
    }

    fun getParent(resizable: Boolean): Int {
        return data?.getParent(resizable) ?: throw InvalidInterfaceException()
    }

}