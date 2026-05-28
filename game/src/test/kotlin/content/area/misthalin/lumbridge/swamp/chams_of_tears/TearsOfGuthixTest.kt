package content.area.misthalin.lumbridge.swamp.chams_of_tears

import FakeRandom
import WorldTest
import dialogueContinue
import dialogueOption
import itemOnItem
import itemOnNpc
import objectOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

class TearsOfGuthixTest : WorldTest() {

    override var loadNpcs = true

    @Test
    fun `Complete the quest`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int) = 0
        })
        val player = createPlayer(Tile(3251, 9517, 2))
        player.levels.set(Skill.Mining, 22)
        player["quest_points"] = 44
        player.inventory.add("sapphire_lantern_lit")
        player.inventory.add("mithril_pickaxe")
        player.inventory.add("chisel")

        val juna = GameObjects.find(Tile(3252, 9516, 2), "juna_base")
        player.objectOption(juna, "Talk-to")
        tick(1)
        player.dialogueContinue()
        player.dialogueOption("line1")
        player.dialogueContinue(5)

        assertEquals("stone_bowl", player["tears_of_guthix", "unstarted"])

        player.tele(3228, 9526, 2)
        var creature = NPCs.at(player.tile.regionLevel)
            .filter { it.id == "light_creature" }
            .minBy { it.tile.distanceTo(player.tile) }
        player.itemOnNpc(creature, 0)

        tick(40)

        assertEquals(Tile(3224, 9504, 2), player.tile)

        player.tele(3222, 9499, 2)
        val rocks = GameObjects.find(Tile(3221, 9499, 2), "magic_rocks_chasm_of_tears_1")
        player.objectOption(rocks, "Mine")
        tick(8)
        assertEquals(1, player.inventory.count("magical_stone"))
        player.itemOnItem(2, 3)
        tick(2)
        assertEquals(0, player.inventory.count("magical_stone"))
        assertEquals(1, player.inventory.count("stone_bowl"))

        player.tele(3224, 9505, 2)
        creature = createNPC("light_creature", Tile(3226, 9510, 2))
        player.itemOnNpc(creature, 0)
        tick(40)
        assertEquals(Tile(3224, 9530, 2), player.tile)

        player.tele(3251, 9517, 2)
        player.objectOption(juna, "Talk-to")
        tick(1)
        player.dialogueContinue(4)
        assertEquals(0, player.inventory.count("stone_bowl"))
        assertEquals(1000.0, player.experience.get(Skill.Crafting))
        assertEquals("completed", player["tears_of_guthix", "unstarted"])
    }

    @Test
    fun `Complete the minigame`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int) = 0
        })
        GameObjects.add("blue_tears", Tile(3257, 9520, 2), ObjectShape.WALL_DECOR_STRAIGHT_NO_OFFSET)

        val player = createPlayer(Tile(3251, 9516, 2))
        player["quest_points"] = 30
        player["tears_of_guthix"] = "completed"
        val juna = GameObjects.find(Tile(3252, 9516, 2), "juna_base")
        player.objectOption(juna, "Tell-story")
        tick(1)
        player.dialogueContinue(3)
        tick(6)

        val wall = GameObjects.find(Tile(3257, 9520, 2), "weeping_wall")
        player.objectOption(wall, "Collect-from")
        tick(40) // Times up

        assertEquals(24, player["tears_of_guthix_points", 0])
        assertEquals(Tile(3251, 9516, 2), player.tile)
        assertEquals(240.0, player.experience.get(Skill.Cooking))
    }
}
