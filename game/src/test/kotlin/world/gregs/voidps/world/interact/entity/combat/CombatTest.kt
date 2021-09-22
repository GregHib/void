package world.gregs.voidps.world.interact.entity.combat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.npcOption

internal class CombatTest : WorldMock() {

    private lateinit var floorItems: FloorItems

    @BeforeEach
    fun start() {
        floorItems = get()
    }

    @Test
    fun `Kill rat with magic`() = runBlocking(Dispatchers.Default) {
        val player = createPlayer("player", Tile(100, 100))
        val npc = createNPC("rat", Tile(100, 104))
        val chunk = npc.tile.chunk

        player.equipment.set(EquipSlot.Weapon.index, "staff_of_air")
        player.experience.set(Skill.Magic, Double.MAX_VALUE)
        player.levels.boost(Skill.Magic, 25)
        player.inventory.add("mind_rune", 100)

        player.interfaceOption("modern_spellbook", "wind_strike", optionIndex = 0)
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        tick(5)


        assertTrue(floorItems[chunk].any { it.name == "bones" })
        assertTrue(player.inventory.getCount("mind_rune") < 100)
    }

    @Test
    fun `Kill rat with melee`() = runBlocking(Dispatchers.Default) {
        val player = createPlayer("player", Tile(100, 100))
        val npc = createNPC("rat", Tile(100, 104))
        val chunk = npc.tile.chunk

        player.equipment.set(EquipSlot.Weapon.index, "dragon_sword")
        player.experience.set(Skill.Attack, Double.MAX_VALUE)
        player.experience.set(Skill.Strength, Double.MAX_VALUE)
        player.levels.boost(Skill.Attack, 25)
        player.levels.boost(Skill.Strength, 25)

        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        tick(5)

        assertTrue(floorItems[chunk].any { it.name == "bones" })
    }
}