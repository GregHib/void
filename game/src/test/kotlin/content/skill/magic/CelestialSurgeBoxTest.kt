package content.skill.magic

import world.gregs.voidps.engine.entity.item.Item

class CelestialSurgeBoxTest : DungeoneeringBoxTest() {
    override val spell: String = "surge"
    override val runes = listOf(Item("air_rune", 7), Item("blood_rune"), Item("death_rune"))
    override val box: String = "celestial_surgebox"
    override val mode: Boolean = true
}