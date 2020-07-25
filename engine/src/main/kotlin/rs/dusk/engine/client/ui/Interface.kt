package rs.dusk.engine.client.ui

data class Interface(val id: Int, private val data: InterfaceData?) {

    class InvalidInterfaceException : InterfaceException()

    fun getIndex(resizable: Boolean): Int {
        return data?.getIndex(resizable) ?: throw InvalidInterfaceException()
    }

    fun getParent(resizable: Boolean): Int {
        return data?.getParent(resizable) ?: throw InvalidInterfaceException()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Interface

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }

}