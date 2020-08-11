package rs.dusk.engine.entity.item

enum class EquipType(val id: Int) {
    None(-1),
    HoodedCape(0),
    TwoHanded(5),
    FullBody(6),
    Hair(8),
    Mask(11);

    companion object {
        fun by(id: Int): EquipType = values().firstOrNull { it.id == id } ?: None
    }
}