package world.gregs.voidps.engine.entity.character.player.equip

enum class EquipType {
    None,
    TwoHanded,
    Sleeveless,
    FullFace,
    Hair,
    Mask;

    companion object {
        private val map = mapOf(
            "None" to None,
            "TwoHanded" to TwoHanded,
            "Sleeveless" to Sleeveless,
            "FullFace" to FullFace,
            "Hair" to Hair,
            "Mask" to Mask,
        )

        fun by(name: String): EquipType = map[name] ?: None
    }
}