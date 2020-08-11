package rs.dusk.engine.entity.item

enum class BodyPart(val slot: EquipSlot = EquipSlot.None, val index: Int = -1) {
    Hat(EquipSlot.Hat),
    Cape(EquipSlot.Cape),
    Amulet(EquipSlot.Amulet),
    Weapon(EquipSlot.Weapon),
    Chest(EquipSlot.Chest, 2),
    Shield(EquipSlot.Shield),
    Arms(EquipSlot.Chest, index = 3),
    Legs(EquipSlot.Legs, 5),
    Hair(index = 0),
    Bracelet(EquipSlot.Hands, 4),
    Feet(EquipSlot.Feet, 6),
    Beard(index = 1),
    Aura(EquipSlot.Aura);

    companion object {
        val all = values()
        fun by(index: Int) = all.firstOrNull { it.index == index }
        fun by(slot: EquipSlot) = all.firstOrNull { it.slot == slot }
    }
}