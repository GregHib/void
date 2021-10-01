package world.gregs.voidps.world.community.assist

import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.handle.WalkHandler
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.mockStackableItem
import world.gregs.voidps.world.script.playerOption

internal class RequestAssistTest : WorldMock() {

    @Test
    fun `Grant exp to assistant`() = runBlocking(Dispatchers.Default) {
        every { get<InterfaceDecoder>().get(192) } returns InterfaceDefinition( // Spell book
            components = mapOf(40 to InterfaceComponentDefinition(
                anObjectArray4758 = arrayOf(-1, -1, -1, -1, -1, 1, -1, -1, 563, 1, 556, 3, 554, 1, -1, -1)
            ))
        )
        mockStackableItem(556) // air rune
        val assistant = createPlayer("assistant", Tile(100, 100))
        val receiver = createPlayer("receiver", Tile(100, 101))
        receiver.levels.setOffset(Skill.Magic, 25)
        receiver.inventory.add("fire_rune")
        receiver.inventory.add("air_rune", 3)
        receiver.inventory.add("law_rune")

        receiver.playerOption(assistant, "Req Assist")
        assistant.playerOption(receiver, "Req Assist")
        tick(1)
        assistant.interfaceOption("assist_xp", "magic", "Toggle Skill On / Off")
        receiver.interfaceOption("modern_spellbook", "varrock_teleport", "Cast")
        tick(5)

        assertTrue(assistant.experience.get(Skill.Magic) > 0.0)
        assertEquals(0.0, receiver.experience.get(Skill.Magic))
    }

    @Test
    fun `Assistance stops when more than 20 tiles away`() = runBlocking(Dispatchers.Default) {
        every { get<InterfaceDecoder>().get(192) } returns InterfaceDefinition( // Spell book
            components = mapOf(40 to InterfaceComponentDefinition(
                anObjectArray4758 = arrayOf(-1, -1, -1, -1, -1, 1, -1, -1, 563, 1, 556, 3, 554, 1, -1, -1)
            ))
        )
        mockStackableItem(556) // air rune
        val assistant = createPlayer("assistant", Tile(100, 100))
        val receiver = createPlayer("receiver", Tile(100, 101))
        receiver.levels.setOffset(Skill.Magic, 25)
        receiver.inventory.add("fire_rune")
        receiver.inventory.add("air_rune", 3)
        receiver.inventory.add("law_rune")


        receiver.playerOption(assistant, "Req Assist")
        assistant.playerOption(receiver, "Req Assist")
        tick(1)
        assistant.interfaceOption("assist_xp", "magic", "Toggle Skill On / Off")
        WalkHandler().validate(receiver, Walk(100, 122))
        tick(22)
        receiver.interfaceOption("modern_spellbook", "varrock_teleport", "Cast")
        tick(5)

        assertTrue(receiver.experience.get(Skill.Magic) > 0.0)
        assertEquals(0.0, assistant.experience.get(Skill.Magic))
    }

}