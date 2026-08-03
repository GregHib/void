package content.area.asgarnia.burthorpe.warriors_guild

import WorldTest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

internal class AjjatTest : WorldTest() {

    @Test
    fun `Buy a skillcape of attack with a free hood`() {
        val player = createPlayer(emptyTile, "buyer")
        player.experience.set(Skill.Attack, 14_000_000.0)
        player.inventory.add("coins", 99_000)
        val ajjat = createNPC("ajjat", emptyTile.addY(1))

        player.npcOption(ajjat, "Talk-to")
        tick()
        player.dialogueContinue(4)
        player.dialogueOption(1)
        player.dialogueContinue(2)
        player.dialogueOption(2)
        player.dialogueContinue(2)

        println("DEBUG inv: " + player.inventory.items.filter { it.id.isNotEmpty() })
        println("DEBUG max: " + Skill.entries.associateWith { player.levels.getMax(it) })

        assertEquals(1, player.inventory.count("attack_cape"))
        assertEquals(1, player.inventory.count("attack_hood"))
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Cape is trimmed with a second level 99 skill`() {
        val player = createPlayer(emptyTile, "trimmed")
        player.experience.set(Skill.Attack, 14_000_000.0)
        player.experience.set(Skill.Strength, 14_000_000.0)
        player.inventory.add("coins", 99_000)
        val ajjat = createNPC("ajjat", emptyTile.addY(1))

        player.npcOption(ajjat, "Talk-to")
        tick()
        player.dialogueContinue(4)
        player.dialogueOption(1)
        player.dialogueContinue(2)
        player.dialogueOption(2)
        player.dialogueContinue(2)

        assertEquals(1, player.inventory.count("attack_cape_t"))
        assertEquals(1, player.inventory.count("attack_hood"))
    }

    @Test
    fun `No cape offer below level 99`() {
        val player = createPlayer(emptyTile, "novice")
        player.inventory.add("coins", 99_000)
        val ajjat = createNPC("ajjat", emptyTile.addY(1))

        player.npcOption(ajjat, "Talk-to")
        tick()
        player.dialogueContinue(3)
        player.dialogueOption(1)
        player.dialogueContinue(2)

        assertEquals(0, player.inventory.count("attack_cape"))
        assertEquals(99_000, player.inventory.count("coins"))
    }

    @Test
    fun `No cape without enough coins`() {
        val player = createPlayer(emptyTile, "broke")
        player.experience.set(Skill.Attack, 14_000_000.0)
        val ajjat = createNPC("ajjat", emptyTile.addY(1))

        player.npcOption(ajjat, "Talk-to")
        tick()
        player.dialogueContinue(4)
        player.dialogueOption(1)
        player.dialogueContinue(2)
        player.dialogueOption(2)
        player.dialogueContinue(2)

        assertEquals(0, player.inventory.count("attack_cape"))
        assertEquals(0, player.inventory.count("attack_hood"))
    }
}
