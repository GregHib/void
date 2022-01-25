package world.gregs.voidps.world.community.assist

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.WalkHandler
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.playerOption

internal class RequestAssistTest : WorldTest() {

    @Test
    fun `Grant exp to assistant`() {
        val assistant = createPlayer("assistant", emptyTile)
        val receiver = createPlayer("receiver", emptyTile.addY(1))
        assistant.experience.set(Skill.Magic, 15000000.0)
        receiver.experience.set(Skill.Magic, 10000000.0)
        receiver.inventory.add("fire_rune")
        receiver.inventory.add("air_rune", 3)
        receiver.inventory.add("law_rune")

        receiver.playerOption(assistant, "Req Assist")
        assistant.playerOption(receiver, "Req Assist")
        tick()
        assistant.interfaceOption("assist_xp", "magic", "Toggle Skill On / Off")
        tick()
        receiver.interfaceOption("modern_spellbook", "varrock_teleport", "Cast")
        tickIf { receiver.tile == emptyTile.addY(1) }

        assertTrue(assistant.experience.get(Skill.Magic) > 15000000.0)
        assertEquals(10000000.0, receiver.experience.get(Skill.Magic))
    }

    @Test
    fun `Assistance stops when more than 20 tiles away`() {
        val assistant = createPlayer("assistant", emptyTile)
        val receiver = createPlayer("receiver", emptyTile.addY(1))
        receiver.levels.setOffset(Skill.Magic, 25)
        receiver.inventory.add("fire_rune")
        receiver.inventory.add("air_rune", 3)
        receiver.inventory.add("law_rune")

        receiver.playerOption(assistant, "Req Assist")
        assistant.playerOption(receiver, "Req Assist")
        assistant.interfaceOption("assist_xp", "magic", "Toggle Skill On / Off")
        WalkHandler().validate(receiver, Walk(emptyTile.x, emptyTile.y + 22))
        tickIf { receiver.tile != emptyTile.addY(22) }
        receiver.interfaceOption("modern_spellbook", "varrock_teleport", "Cast")
        tickIf { receiver.tile == emptyTile.addY(22) }

        assertTrue(receiver.experience.get(Skill.Magic) > 0.0)
        assertEquals(0.0, assistant.experience.get(Skill.Magic))
    }

}