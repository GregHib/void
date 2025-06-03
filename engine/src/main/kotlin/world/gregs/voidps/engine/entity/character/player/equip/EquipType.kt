package world.gregs.voidps.engine.entity.character.player.equip

enum class EquipType {
    /**
     * Normal item
     */
    None,

    /**
     * Held in two hands
     */
    TwoHanded,

    /**
     * Covers the arms
     */
    Sleeveless,

    /**
     * Covers the entire face
     */
    FullFace,

    /**
     * Covers the scalp and hair
     */
    Hair,

    /**
     * Covers the jaw
     */
    Mask,

    /**
     * Hair is replaced with a middle-sized version in [AppearanceOverrides]
     * E.g. headbands or party-hats
     */
    HairMid,

    /**
     * Hair is replaced with a smaller version in [AppearanceOverrides]
     * E.g. wizard hats
     */
    HairLow;

    companion object {
        fun by(name: String): EquipType = when (name) {
            "TwoHanded" -> TwoHanded
            "Sleeveless" -> Sleeveless
            "FullFace" -> FullFace
            "Hair" -> Hair
            "Mask" -> Mask
            "HairMid" -> HairMid
            "HairLow" -> HairLow
            else -> None
        }
    }
}