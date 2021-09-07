package world.gregs.voidps.engine.entity.item.drop

enum class TableType {
    First,
    All;

    companion object {
        fun byName(name: String) = values().first { it.name.toLowerCase() == name }
    }
}