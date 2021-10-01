package world.gregs.voidps.world.activity.skill

import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.mockStackableItem

internal class MagicTest : WorldMock() {

    @Test
    fun `Teleport to another place`() = runBlocking(Dispatchers.Default) {
        mockStackableItem(556) // air_rune
        val tile = Tile(100, 100)
        val player = createPlayer("magician", tile)
        player.experience.set(Skill.Magic, experience)
        player.inventory.add("law_rune")
        player.inventory.add("air_rune", 3)
        player.inventory.add("fire_rune")

        player.interfaceOption("modern_spellbook", "varrock_teleport", "Cast")
        tickIf { player.tile == tile }

        assertTrue(player.inventory.isEmpty())
        assertTrue(player.experience.get(Skill.Magic) > experience)
        assertNotEquals(tile, player.tile)
    }

    @Test
    fun `Teleport with a tablet`() = runBlocking(Dispatchers.Default) {
        every { get<ItemDecoder>().get(8008) } returns ItemDefinition( // lumbridge_teleport
            id = 8008,
            options = arrayOf("Break", null, null, null, "Drop")
        )
        val tile = Tile(100, 100)
        val player = createPlayer("magician", tile)
        player.experience.set(Skill.Magic, experience)
        player.inventory.add("lumbridge_teleport")

        player.interfaceOption("inventory", "container", "Break", 0, Item("lumbridge_teleport"), 0)
        tick(5)

        assertTrue(player.inventory.isEmpty())
        assertFalse(player.experience.get(Skill.Magic) > experience)
        assertNotEquals(tile, player.tile)
    }

    companion object {
        private const val experience = 14000000.0
    }
}