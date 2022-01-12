package world.gregs.voidps.world.interact.entity.combat

import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.entity.character.Levels
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.get
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
        val chunk = npc["death_tile", npc.tile].chunk
        tick(5) // npc death

        assertEquals(Tile(100, 100), player.tile)
        assertTrue(player.experience.get(Skill.Magic) > experience)
        assertTrue(floorItems[chunk].any { it.id == "bones" })
        assertTrue(player.inventory.getCount("mind_rune") < 100)
    }

    @Test
    fun `Kill rat with melee`() = runBlocking(Dispatchers.Default) {
        val player = createPlayer("player", Tile(100, 100))
        val npc = createNPC("rat", Tile(100, 104))

        player.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        player.experience.set(Skill.Attack, experience)
        player.experience.set(Skill.Strength, experience)
        player.experience.set(Skill.Defence, experience)
        player.levels.boost(Skill.Attack, 25)
        player.levels.boost(Skill.Strength, 25)

        player.interfaceOption("combat_styles", "style1")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val chunk = npc["death_tile", npc.tile].chunk
        tick(5) // npc death

        assertNotEquals(Tile(100, 100), player.tile)
        assertTrue(player.experience.get(Skill.Attack) > experience)
        assertTrue(player.experience.get(Skill.Strength) > experience)
        assertTrue(player.experience.get(Skill.Defence) > experience)
        assertTrue(floorItems[chunk].any { it.id == "bones" })
    }

    @Test
    fun `Kill rat with range`() = runBlocking(Dispatchers.Default) {
        mockStackableItem(892) // rune_arrow
        every { get<ItemDecoder>().get(861) } returns ItemDefinition(params = HashMap(mapOf(686L to 16)), extras = mapOf("attack_range" to 7, "attack_speed" to 4))

        val player = createPlayer("player", Tile(100, 100))
        val npc = createNPC("rat", Tile(100, 104))

        player.equipment.set(EquipSlot.Weapon.index, "magic_shortbow")
        player.equipment.set(EquipSlot.Ammo.index, "rune_arrow", 100)
        player.experience.set(Skill.Ranged, experience)
        player.experience.set(Skill.Defence, experience)
        player.levels.boost(Skill.Ranged, 25)

        player.interfaceOption("combat_styles", "style1")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val chunk = npc["death_tile", npc.tile].chunk
        tick(5) // npc death

        val drops = floorItems[chunk]
        assertEquals(Tile(100, 100), player.tile)
        assertTrue(drops.any { it.id == "bones" })
        assertTrue(drops.any { it.id == "rune_arrow" })
        assertTrue(player.experience.get(Skill.Ranged) > experience)
        assertTrue(player.experience.get(Skill.Defence) > experience)
        assertTrue(player.inventory.getCount("rune_arrow") < 100)
    }

    @Test
    fun `Dragon dagger special attack`() = runBlocking(Dispatchers.Default) {
        val player = createPlayer("player", Tile(100, 100))
        val npc = createNPC("rat", Tile(100, 101))

        player.equipment.set(EquipSlot.Weapon.index, "dragon_dagger")

        var hits = 0
        npc.events.on<NPC, CombatHit> {
            hits++
        }

        player.interfaceOption("combat_styles", "special_attack_bar", "Use")
        player.npcOption(npc, "Attack")
        tick()

        assertEquals(2, hits)
    }

    @Test
    fun `Don't take damage with protection prayers`() = runBlocking(Dispatchers.Default) {
        every { get<EnumDecoder>().get(2279) } answers { // regular prayers enum
            EnumDefinition(id = arg(0), map = HashMap((0 until 30).associateWith { it }))
        }
        every { get<StructDecoder>().get(19) } answers { // protect from melee prayer information
            StructDefinition(arg(0), params = HashMap(mapOf(734L to "<br>Protect from Melee<br>")))
        }
        val player = createPlayer("player", Tile(100, 100))
        player.experience.set(Skill.Constitution, experience)
        val npc = createNPC("rat", Tile(100, 101))
        npc.levels.link(npc.events, object : Levels.Level {
            override fun getMaxLevel(skill: Skill): Int {
                return if (skill == Skill.Constitution) 10000 else 99
            }
        })
        var shouldHaveDamaged = false
        npc.events.on<NPC, HitDamageModifier>({ damage > 0 }, Priority.HIGHEST) {
            shouldHaveDamaged = true
        }

        player.interfaceOption("prayer_list", "regular_prayers", slot = 19, optionIndex = 0)
        player.npcOption(npc, "Attack")
        tickIf { !shouldHaveDamaged }

        assertEquals(990, player.levels.get(Skill.Constitution))
    }

    @Disabled("Real maps need real object definitions")
    @Test
    fun `Ranged attacks will run within distance and stop`() = runBlocking(Dispatchers.Default) {
        loadVarrock()
        mockStackableItem(892) // rune_arrow
        every { get<ItemDecoder>().get(861) } returns ItemDefinition(params = HashMap(mapOf(686L to 16)), extras = mapOf("attack_range" to 7, "attack_speed" to 4))
        val player = createPlayer("player", Tile(3228, 3415))
        val npc = createNPC("rat", Tile(3228, 3407))

        player.equipment.set(EquipSlot.Weapon.index, "magic_shortbow")
        player.equipment.set(EquipSlot.Ammo.index, "rune_arrow", 100)
        player.experience.set(Skill.Ranged, experience)
        player.levels.boost(Skill.Ranged, 25)

        // TODO fails because of mockking ObjectDecoder
        player.interfaceOption("combat_styles", "style1") // Long range
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        tick(5) // npc death

        assertEquals(Tile(3228, 3413), player.tile)
    }

    private fun loadVarrock() {
        // Region 12853
        val varrockTileData = CombatTest::class.java.getResourceAsStream("varrock_tiles.dat")?.readAllBytes()!!
        val varrockObjectData = CombatTest::class.java.getResourceAsStream("varrock_objects.dat")?.readAllBytes()!!
        every { get<Cache>().getFile(Indices.MAPS, "m50_53", any()) } returns varrockTileData
        every { get<Cache>().getFile(Indices.MAPS, "l50_53", any()) } returns varrockObjectData
    }

    companion object {
        private const val experience = 14000000.0
    }
}