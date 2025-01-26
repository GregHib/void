package content.skill.magic

import world.gregs.voidps.engine.entity.item.Item

class MagicalBlastBoxTest : DungeoneeringBoxTest() {
    override val spell: String = "blast"
    override val runes = listOf(Item("air_rune", 3), Item("death_rune", 1))
    override val box: String = "magical_blastbox"
    override val mode: Boolean = true
}