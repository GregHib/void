package world.gregs.voidps.world.interact.entity.combat

import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.mockStackableItem
import world.gregs.voidps.world.script.npcOption

internal class CombatTest : WorldMock() {

    private lateinit var floorItems: FloorItems

    @BeforeEach
    fun start() {
        floorItems = get()
    }

    @Test
    fun `Kill rat with magic`() = runBlocking(Dispatchers.Default) {
        every { get<InterfaceDecoder>().get(192) } returns InterfaceDefinition( // Spell book
            components = mapOf(25 to InterfaceComponentDefinition(
                anObjectArray4758 = arrayOf(-1, -1, -1, -1, -1, 1, -1, -1, 556, 1, 558, 1, -1, -1, -1, -1)
            ))
        )
        mockStackableItem(558) // mind_rune
        val player = createPlayer("player", Tile(100, 100))
        val npc = createNPC("rat", Tile(100, 104))
        player.equipment.set(EquipSlot.Weapon.index, "staff_of_air")
        player.experience.set(Skill.Magic, experience)
        player.levels.boost(Skill.Magic, 25)
        player.inventory.add("mind_rune", 100)

        player.interfaceOption("modern_spellbook", "wind_strike", option = "Autocast")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val chunk = npc.tile.chunk
        tick(5)

        assertEquals(Tile(100, 100), player.tile)
        assertTrue(player.experience.get(Skill.Magic) > experience)
        assertTrue(floorItems[chunk].any { it.name == "bones" })
        assertTrue(player.inventory.getCount("mind_rune") < 100)
    }

    @Test
    fun `Kill rat with melee`() = runBlocking(Dispatchers.Default) {
        val player = createPlayer("player", Tile(100, 100))
        val npc = createNPC("rat", Tile(100, 104))
        val chunk = npc.tile.chunk

        player.equipment.set(EquipSlot.Weapon.index, "dragon_sword")
        player.experience.set(Skill.Attack, experience)
        player.experience.set(Skill.Strength, experience)
        player.experience.set(Skill.Defence, experience)
        player.levels.boost(Skill.Attack, 25)
        player.levels.boost(Skill.Strength, 25)

        player.interfaceOption("combat_styles", "style1")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        tick(5)

        assertNotEquals(Tile(100, 100), player.tile)
        assertTrue(player.experience.get(Skill.Attack) > experience)
        assertTrue(player.experience.get(Skill.Strength) > experience)
        assertTrue(player.experience.get(Skill.Defence) > experience)
        assertTrue(floorItems[chunk].any { it.name == "bones" })
    }

    @Test
    fun `Kill rat with range`() = runBlocking(Dispatchers.Default) {
        mockStackableItem(892) // rune_arrow
        every { get<ItemDecoder>().get(861) } returns ItemDefinition(params = HashMap(mapOf(686L to 16)))
        val player = createPlayer("player", Tile(100, 100))
        val npc = createNPC("rat", Tile(100, 104))

        player.equipment.set(EquipSlot.Weapon.index, "magic_shortbow")
        player.equipment.set(EquipSlot.Ammo.index, "rune_arrow", 100)
        player.experience.set(Skill.Range, experience)
        player.experience.set(Skill.Defence, experience)
        player.levels.boost(Skill.Range, 25)

        player.interfaceOption("combat_styles", "style1")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val chunk = npc.tile.chunk
        tick(5)

        val drop = floorItems[chunk]
        assertEquals(Tile(100, 100), player.tile)
        assertTrue(drop.any { it.name == "bones" })
        assertTrue(drop.any { it.name == "rune_arrow" })
        assertTrue(player.experience.get(Skill.Range) > experience)
        assertTrue(player.experience.get(Skill.Defence) > experience)
        assertTrue(player.inventory.getCount("rune_arrow") < 100)
    }

    companion object {
        private const val experience = 14000000.0
    }
}