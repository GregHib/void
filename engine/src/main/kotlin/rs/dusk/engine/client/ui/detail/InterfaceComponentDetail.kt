package rs.dusk.engine.client.ui.detail

data class InterfaceComponentDetail(
    val id: Int,
    val name: String,
    var parent: Int = -1,
    val container: String = "",
    val primaryContainer: Boolean = true,
    val options: Array<String> = emptyArray()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterfaceComponentDetail

        if (id != other.id) return false
        if (name != other.name) return false
        if (parent != other.parent) return false
        if (container != other.container) return false
        if (primaryContainer != other.primaryContainer) return false
        if (!options.contentEquals(other.options)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + parent
        result = 31 * result + container.hashCode()
        result = 31 * result + primaryContainer.hashCode()
        result = 31 * result + options.contentHashCode()
        return result
    }
}