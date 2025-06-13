package world.gregs.voidps.network.login.protocol.visual.update.player

enum class BodyPart(
    val slot: EquipSlot = EquipSlot.None,
    val index: Int = -1,
) {
    Head(EquipSlot.Hat),
    Back(EquipSlot.Cape),
    Neck(EquipSlot.Amulet),
    RightHand(EquipSlot.Weapon),
    Chest(EquipSlot.Chest, index = 2),
    LeftHand(EquipSlot.Shield),
    Arms(EquipSlot.Chest, index = 3),
    Legs(EquipSlot.Legs, index = 5),
    Hair(EquipSlot.Hat, index = 0),
    Hands(EquipSlot.Hands, index = 4),
    Feet(EquipSlot.Feet, index = 6),
    Beard(EquipSlot.Hat, index = 1),
    ;

    companion object {
        fun by(index: Int) = entries.firstOrNull { it.index == index }
        fun by(slot: EquipSlot) = entries.firstOrNull { it.slot == slot }
    }
}
