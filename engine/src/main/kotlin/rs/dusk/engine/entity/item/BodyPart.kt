package rs.dusk.engine.entity.item

enum class BodyPart(val slot: EquipSlot = EquipSlot.None, val index: Int = -1) {
    Hat(EquipSlot.Hat),
    Cape(EquipSlot.Cape),
    Amulet(EquipSlot.Amulet),
    Weapon(EquipSlot.Weapon),
    Chest(EquipSlot.Chest, index = 2),
    Shield(EquipSlot.Shield),
    Arms(EquipSlot.Chest, index = 3),
    Legs(EquipSlot.Legs, index = 5),
    Hair(EquipSlot.Hat, index = 0),
    Bracelet(EquipSlot.Hands, index = 4),
    Feet(EquipSlot.Feet, index = 6),
    Beard(EquipSlot.Hat, index = 1),
    Aura(EquipSlot.Aura);

    companion object {
        val all = values()
        fun by(index: Int) = all.firstOrNull { it.index == index }
        fun by(slot: EquipSlot) = all.firstOrNull { it.slot == slot }
    }
}