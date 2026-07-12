package world.gregs.voidps.tools.photobooth

/**
 * The character appearance captured by the in-game photo booth (see Iconis.saveSnapshot).
 *
 * @param male gender model selection
 * @param looks 7 IdentityKit ids in BodyPart.index order (Hair, Beard, Chest, Arms, Hands, Legs, Feet); -1 = none
 * @param colours 5 body-colour byte indices in BodyColour order (Hair, Top, Legs, Feet, Skin)
 * @param equipment one equipIndex per worn equipment slot (list position = EquipSlot.index); -1 = empty
 * @param time epoch seconds the snapshot was taken
 */
data class PhotoSnapshot(
    val male: Boolean,
    val looks: IntArray,
    val colours: IntArray,
    val equipment: IntArray,
    val time: Long,
) {
    companion object {
        private fun ints(csv: String): IntArray =
            if (csv.isBlank()) IntArray(0) else csv.split(",").map { it.trim().toInt() }.toIntArray()

        /** Parses the comma-joined string forms persisted by the game. */
        fun parse(male: Boolean, looks: String, colours: String, equipment: String, time: Long) =
            PhotoSnapshot(male, ints(looks), ints(colours), ints(equipment), time)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PhotoSnapshot) return false
        return male == other.male && looks.contentEquals(other.looks) &&
            colours.contentEquals(other.colours) && equipment.contentEquals(other.equipment) && time == other.time
    }

    override fun hashCode(): Int {
        var result = male.hashCode()
        result = 31 * result + looks.contentHashCode()
        result = 31 * result + colours.contentHashCode()
        result = 31 * result + equipment.contentHashCode()
        result = 31 * result + time.hashCode()
        return result
    }
}
