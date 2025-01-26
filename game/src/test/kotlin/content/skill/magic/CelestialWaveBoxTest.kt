package content.skill.magic

import world.gregs.voidps.engine.entity.item.Item

class CelestialWaveBoxTest : DungeoneeringBoxTest() {
    override val spell: String = "wave"
    override val runes = listOf(Item("air_rune", 5), Item("blood_rune", 1))
    override val box: String = "celestial_surgebox"
    override val mode: Boolean = false
}