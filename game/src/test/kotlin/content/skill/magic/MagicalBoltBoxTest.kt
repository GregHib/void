package content.skill.magic

import world.gregs.voidps.engine.entity.item.Item

class MagicalBoltBoxTest : DungeoneeringBoxTest() {
    override val spell: String = "bolt"
    override val runes = listOf(Item("air_rune", 2), Item("chaos_rune"))
    override val box: String = "magical_blastbox"
    override val mode: Boolean = false
}