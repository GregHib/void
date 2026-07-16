package content.skill.summoning

import FakeRandom
import WorldTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

internal class CompostMoundGenerateTest : WorldTest() {

    private fun castOn(binVar: String, superRoll: Int): Player {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = superRoll
        })
        val player = createPlayer(Tile(3057, 3311))
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get("compost_mound_familiar"), restart = false)
        tick(2)
        player.set("summoning_special_points_remaining", 60)
        player.inventory.transaction { add("generate_compost_scroll", 2) }
        val bin = GameObjects.find(Tile(3056, 3312), "farming_compost_bin_falador")!!

        player.castFamiliarSpecial { FamiliarSpecialMoves.objectTarget.getValue("compost_mound_familiar").invoke(player, bin) }
        tick(5) // projectile flight then the queued fill
        return player
    }

    @Test
    fun `Generate Compost fills an empty bin with compost`() {
        val player = castOn("compost_bin_falador", superRoll = 1) // not 0 -> normal compost
        assertEquals("compost_15", player["compost_bin_falador", "empty"])
        assertEquals(1, player.inventory.count("generate_compost_scroll"), "one scroll spent")
    }

    @Test
    fun `Generate Compost can yield supercompost`() {
        val player = castOn("compost_bin_falador", superRoll = 0) // 0 -> supercompost
        assertEquals("supercompost_15", player["compost_bin_falador", "empty"])
    }

    @Test
    fun `Generate Compost rejects a non-empty bin`() {
        setRandom(FakeRandom())
        val player = createPlayer(Tile(3057, 3311))
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get("compost_mound_familiar"), restart = false)
        tick(2)
        player.set("summoning_special_points_remaining", 60)
        player.inventory.transaction { add("generate_compost_scroll", 2) }
        player["compost_bin_falador"] = "compostable_5"
        val bin = GameObjects.find(Tile(3056, 3312), "farming_compost_bin_falador")!!

        player.castFamiliarSpecial { FamiliarSpecialMoves.objectTarget.getValue("compost_mound_familiar").invoke(player, bin) }
        tick(5)

        assertEquals("compostable_5", player["compost_bin_falador", "empty"], "unchanged")
        assertEquals(2, player.inventory.count("generate_compost_scroll"), "no scroll spent on a bad target")
    }
}
