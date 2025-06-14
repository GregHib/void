package world.gregs.voidps.engine.entity.item.drop

enum class TableType {
    /**
     * Stop rolling after first item is awarded.
     */
    First,

    /**
     * All drops in the table are called whether an item is awarded or not
     */
    All,

    ;

    companion object {
        fun byName(name: String) = entries.first { it.name.lowercase() == name }
    }
}
