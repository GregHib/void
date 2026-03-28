package content.area.kharidian_desert.pollnivneach.dungeon

import WorldTest
import dialogueContinue
import dialogueOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class PollnivneachDungeonTest : WorldTest() {

    @Test
    fun `Kill monstrous cave crawler`() {
        val player = createPlayer(Tile(3319, 4370, 0))
        player["auto_retaliate"] = true
        player["god_mode"] = true
        player["insta_kill"] = true

        val barrier = GameObjects.find(Tile(3318, 4371), "pollnivneach_dungeon_barrier_north")
        player.interactObject(barrier, "Pass")
        tick(2)

        player.dialogueContinue(1)
        player.dialogueOption("line1")

        tick(15)

        assertEquals(Tile(3319, 4370), player.tile)
        assertEquals(true, player["killed_monstrous_cave_crawler", false])

        player.interactObject(barrier, "Pass")
        tick(2)
        assertEquals(Tile(3319, 4372), player.tile)
    }

    @Test
    fun `Kill kurask overlord`() {
        val player = createPlayer(Tile(3277, 4371, 0))
        player["auto_retaliate"] = true
        player["god_mode"] = true
        player["insta_kill"] = true

        val barrier = GameObjects.find(Tile(3276, 4372), "pollnivneach_dungeon_barrier_north")
        player.interactObject(barrier, "Pass")
        tick(2)

        player.dialogueContinue(1)
        player.dialogueOption("line1")

        tick(15)

        assertEquals(Tile(3277, 4371), player.tile)
        assertEquals(true, player["killed_kurask_overlord", false])

        player.interactObject(barrier, "Pass")
        tick(2)
        assertEquals(Tile(3277, 4373), player.tile)
    }

    @Test
    fun `Kill mightiest turoth`() {
        val player = createPlayer(Tile(3275, 4335, 0))
        player.equipment.set(EquipSlot.Weapon.index, "leaf_bladed_sword")
        player["auto_retaliate"] = true
        player["god_mode"] = true
        player["insta_kill"] = true

        val barrier = GameObjects.find(Tile(3274, 4334), "pollnivneach_dungeon_barrier_north")
        player.interactObject(barrier, "Pass")
        tick(2)

        player.dialogueContinue(1)
        player.dialogueOption("line1")

        tick(20)

        assertEquals(Tile(3275, 4335), player.tile)
        assertEquals(true, player["killed_turoth_mightiest", false])

        player.interactObject(barrier, "Pass")
        tick(2)
        assertEquals(Tile(3275, 4333), player.tile)
    }

    @Test
    fun `Kill basilisk boss`() {
        val player = createPlayer(Tile(3315, 4335, 0))
        player.equipment.set(EquipSlot.Shield.index, "mirror_shield")
        player["auto_retaliate"] = true
        player["god_mode"] = true
        player["insta_kill"] = true

        val barrier = GameObjects.find(Tile(3314, 4334), "pollnivneach_dungeon_barrier_north")
        player.interactObject(barrier, "Pass")
        tick(2)

        player.dialogueContinue(1)
        player.dialogueOption("line1")

        tick(20)

        assertEquals(Tile(3315, 4335), player.tile)
        assertEquals(true, player["killed_basilisk_boss", false])

        player.interactObject(barrier, "Pass")
        tick(2)
        assertEquals(Tile(3315, 4333), player.tile)
    }
}
