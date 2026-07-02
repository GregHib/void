package content.skill.summoning.familiar

import WorldTest
import content.skill.summoning.follower
import content.skill.summoning.summonFamiliar
import npcOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile

internal class DesertWyrmBurrowTest : WorldTest() {

    @Test
    fun `Burrow drops the nearest rock's ore`() {
        val player = createPlayer(Tile(3228, 3229))
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get("desert_wyrm_familiar"), restart = false)
        tick(2)
        createObject("copper_rocks_falador_mine_1", player.tile.addX(2))

        player.npcOption(player.follower!!, "Burrow")
        tick(12) // 8-tick burrow, call-back, then the floor-item queue flush

        val ore = (-2..2).flatMap { dx ->
            (-2..2).flatMap { dy -> FloorItems.at(player.tile.add(dx, dy)).filter { it.id == "copper_ore" } }
        }
        assertTrue(ore.isNotEmpty(), "burrow dropped copper ore near the player")
    }

    @Test
    fun `Burrow with no rocks nearby does nothing`() {
        val player = createPlayer(Tile(3228, 3229))
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get("desert_wyrm_familiar"), restart = false)
        tick(2)

        player.npcOption(player.follower!!, "Burrow")
        tick(12)

        val ore = floorItemsNear(player, "copper_ore")
        assertTrue(ore.isEmpty(), "no ore dropped when there are no rocks")
    }

    @Test
    fun `Burrow ignores silver rocks (above its tier)`() {
        val player = createPlayer(Tile(3228, 3229))
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get("desert_wyrm_familiar"), restart = false)
        tick(2)
        createObject("silver_rocks_dirt_1", player.tile.addX(2))

        player.npcOption(player.follower!!, "Burrow")
        tick(12)

        assertTrue(floorItemsNear(player, "silver_ore").isEmpty(), "the wyrm can't mine silver")
    }

    @Test
    fun `Burrow prefers the higher tier ore`() {
        val player = createPlayer(Tile(3228, 3229))
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get("desert_wyrm_familiar"), restart = false)
        tick(2)
        createObject("copper_rocks_falador_mine_1", player.tile.addX(1))
        createObject("iron_rocks_falador_mine_1", player.tile.addX(2))

        player.npcOption(player.follower!!, "Burrow")
        tick(12)

        assertTrue(floorItemsNear(player, "iron_ore").isNotEmpty(), "iron is preferred over copper")
        assertTrue(floorItemsNear(player, "copper_ore").isEmpty())
    }

    private fun floorItemsNear(player: world.gregs.voidps.engine.entity.character.player.Player, id: String) =
        (-2..2).flatMap { dx ->
            (-2..2).flatMap { dy -> FloorItems.at(player.tile.add(dx, dy)).filter { it.id == id } }
        }
}
