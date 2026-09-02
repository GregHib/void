package content.activity.evil_tree

import WorldTest
import dialogueContinue
import dialogueOption
import npcOption
import objectOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

internal class EvilTreeTest : WorldTest() {

    @AfterEach
    fun resetEvilTree() {
        EvilTreeState.reset()
    }

    @Test
    fun `Nurturing a sapling grows it and gives farming experience`() {
        val player = createPlayer(emptyTile.add(1, 3))
        val seedling = sapling("normal")

        player.objectOption(seedling, "Nurture")
        tickIf { GameObjects.getLayer(EvilTreeState.centre, ObjectLayer.GROUND)?.id == "evil_tree_seedling" }

        assertEquals("evil_tree_sapling", EvilTreeState.tree.id)
        // seed_health of two nurtures, each paying nurture_xp / 10 / seed_health
        assertEquals(20.0, player.experience.get(Skill.Farming))
    }

    @Test
    fun `A sapling grows on its own`() {
        settings["events.evilTree.growthTicks"] = "10"
        sapling("normal")
        World.timers.start("evil_tree")

        tick(TICK + TICK)

        assertEquals("evil_tree_sapling", EvilTreeState.tree.id)
    }

    @Test
    fun `Nurturing the last stage grows the tree and spawns roots`() {
        val player = createPlayer(emptyTile.add(1, 3))
        val young = sapling("normal", "evil_tree_young_large", emptyTile)

        player.objectOption(young, "Nurture")
        tickIf { !EvilTreeState.grown }

        assertEquals("evil_tree_normal_full", EvilTreeState.tree.id)
        assertEquals(150, EvilTreeState.health)
        assertEquals("leprechaun_panic", EvilTreeState.leprechaun.transformId)
        // A root bursts out of each of the four sides
        tickIf { EvilTreeState.roots.size < 4 }
        assertEquals(4, EvilTreeState.roots.size)
    }

    @Test
    fun `Chopping the tree damages it and gives woodcutting experience`() {
        val player = createPlayer(emptyTile.add(1, 3))
        player.levels.set(Skill.Woodcutting, 99)
        player.inventory.add("dragon_hatchet")
        val tree = grownTree("normal")

        player.objectOption(tree, "Chop")
        tickIf { EvilTreeState.health > 145 }

        assertTrue(player.experience.get(Skill.Woodcutting) > 0)
        assertTrue(EvilTreeState.health <= 145)
    }

    @Test
    fun `Losing health swaps the tree for the damaged models`() {
        grownTree("normal")

        burnTo(90) // above a third of 150
        assertEquals("evil_tree_normal_half", EvilTreeState.tree.id)

        burnTo(30)
        assertEquals("evil_tree_normal_weak", EvilTreeState.tree.id)
    }

    @Test
    fun `Roots burst out of the ground and settle into the world`() {
        matureTree("normal")

        assertEquals(4, EvilTreeState.roots.size)
        for (root in EvilTreeState.roots.values) {
            assertEquals(root.obj, GameObjects.getLayer(root.obj.tile, ObjectLayer.GROUND))
            assertTrue(GameObjects.contains(root.obj))
        }
    }

    @Test
    fun `Chopping a root gives kindling`() {
        matureTree("normal")
        val player = chopper(emptyTile.add(1, 4))
        val root = EvilTreeState.roots.getValue("north").obj

        player.objectOption(root, "Chop")
        tickIf { !player.inventory.contains("evil_tree_kindling") }

        assertTrue(player.inventory.contains("evil_tree_kindling"))
        assertTrue(player.experience.get(Skill.Woodcutting) > 0)
    }

    @Test
    fun `A root dies once it has been chopped through`() {
        matureTree("normal")
        val player = chopper(emptyTile.add(1, 4))
        val root = EvilTreeState.roots.getValue("north").obj

        player.objectOption(root, "Chop")
        tickIf(limit = 200) { EvilTreeState.roots.containsKey("north") }

        assertFalse(EvilTreeState.roots.containsKey("north"))
        assertNull(GameObjects.getLayer(root.tile, ObjectLayer.GROUND))
        assertEquals(3, player.inventory.count("evil_tree_kindling"))
    }

    @Test
    fun `Burning kindling gives firemaking experience and lights a fire`() {
        val player = createPlayer(emptyTile.add(1, 3))
        player.levels.set(Skill.Firemaking, 99)
        player.inventory.add("tinderbox")
        player.inventory.add("evil_tree_kindling", 2)
        val tree = grownTree("normal")

        player.objectOption(tree, "Light fire")
        tickIf { player.inventory.count("evil_tree_kindling") == 2 }

        assertEquals(200.0, player.experience.get(Skill.Firemaking))
        assertEquals(1, EvilTreeState.fires.size)
    }

    @Test
    fun `Lit fires burn the tree down over time`() {
        grownTree("normal")
        EvilTreeState.fires["zero"] = createObject("evil_tree_fire", emptyTile.add(-1, 1))
        World.timers.start("evil_tree")

        tick(TICK)

        assertEquals(149, EvilTreeState.health)
    }

    @Test
    fun `Lightning strikes cut the tree down after thirty minutes`() {
        grownTree("normal")
        World.timers.start("evil_tree")

        EvilTreeState.grownTick = GameLoop.tick.toLong() - TimeUnit.MINUTES.toTicks(10)
        tick(TICK)
        assertEquals(75, EvilTreeState.health)

        EvilTreeState.grownTick = GameLoop.tick.toLong() - TimeUnit.MINUTES.toTicks(20)
        tick(TICK)
        assertEquals(37, EvilTreeState.health)

        EvilTreeState.grownTick = GameLoop.tick.toLong() - TimeUnit.MINUTES.toTicks(30)
        tick(TICK)
        assertEquals(0, EvilTreeState.health)
    }

    @Test
    fun `A dead tree leaves a stump and schedules the next spawn`() {
        grownTree("normal")

        burnTo(0)

        assertEquals("evil_tree_normal_death", EvilTreeState.tree.id)
        assertTrue(World.timers.contains("evil_tree_spawn"))

        tick(EvilTree.DEATH_TICKS + TICK)
        assertEquals("evil_tree_normal_stump", EvilTreeState.tree.id)
    }

    @Test
    fun `Handing in kindling pays coins logs and evil tree magic`() {
        val player = createPlayer(emptyTile.add(-1, 0))
        player.levels.set(Skill.Woodcutting, 99)
        player.inventory.add("evil_tree_kindling", 100)
        grownTree("normal")

        claim(player)

        assertEquals(0, player.inventory.count("evil_tree_kindling"))
        assertEquals(185, player.inventory.count("coins")) // half of 370
        assertEquals(12, player.inventory.count("logs_noted")) // half of 24
        assertEquals(100, player["evil_tree_kindling_handed", 0])
        assertEquals(180, player["evil_tree_buff", 0]) // half of six minutes, in seconds
    }

    @Test
    fun `Handing in kindling is capped each day`() {
        val player = createPlayer(emptyTile.add(-1, 0))
        player.levels.set(Skill.Woodcutting, 99)
        player.inventory.add("evil_tree_kindling", 28)
        grownTree("normal")
        player["evil_tree_day"] = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        player["evil_tree_kindling_handed"] = 200

        claim(player)

        assertEquals(28, player.inventory.count("evil_tree_kindling"))
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Rewards can be taken from the stump`() {
        val player = createPlayer(emptyTile.add(1, 3))
        player.levels.set(Skill.Woodcutting, 99)
        player.inventory.add("evil_tree_kindling", 200)
        player["evil_tree_rewards"] = true
        val stump = sapling("normal", "evil_tree_normal_stump", emptyTile)

        player.objectOption(stump, "Take-rewards")
        tickIf { player.inventory.contains("evil_tree_kindling") }

        assertEquals(370, player.inventory.count("coins"))
        assertEquals(24, player.inventory.count("logs_noted"))
        assertFalse(player["evil_tree_rewards", false])
    }

    @Test
    fun `The leprechaun lends a hatchet and a tinderbox`() {
        val player = createPlayer(emptyTile.add(-1, 0))
        grownTree("normal")

        player.npcOption(EvilTreeState.leprechaun, "Talk-to")
        tickIf { player.dialogue == null }
        player.dialogueContinue()
        player.dialogueOption("line2")
        player.dialogueContinue(2)

        assertTrue(player.inventory.contains("bronze_hatchet"))
        assertTrue(player.inventory.contains("tinderbox"))
    }

    @Test
    fun `Interacting with a third tree in a day is refused`() {
        val player = createPlayer(emptyTile)
        player["evil_tree_day"] = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        player["evil_tree_trees"] = 2
        player["evil_tree_spawn_id"] = 0
        EvilTreeState.spawnId = 7

        assertFalse(player.interact())
        assertEquals(2, player["evil_tree_trees", 0])
    }

    @Test
    fun `Interacting twice with the same tree only counts once`() {
        val player = createPlayer(emptyTile)
        EvilTreeState.spawnId = 3

        assertTrue(player.interact())
        assertTrue(player.interact())
        assertEquals(1, player["evil_tree_trees", 0])
        assertTrue(player["evil_tree_rewards", false])
    }

    private fun sapling(type: String, id: String = "evil_tree_seedling", tile: Tile = emptyTile.add(1, 1)): GameObject {
        EvilTreeState.reset()
        EvilTreeState.spawnTile = emptyTile
        EvilTreeState.type = type
        EvilTreeState.spawnId++
        EvilTreeState.tree = createObject(id, tile)
        EvilTreeState.leprechaun = createNPC("leprechaun_evil_tree", emptyTile.add(-1, -1))
        return EvilTreeState.tree
    }

    private fun grownTree(type: String): GameObject {
        sapling(type, "evil_tree_${type}_full", emptyTile)
        EvilTreeState.maxHealth = 150
        EvilTreeState.health = 150
        EvilTreeState.grownTick = GameLoop.tick.toLong()
        return EvilTreeState.tree
    }

    /**
     * Grows a tree the whole way through the world tick, so its roots burst out and settle the same
     * way they do in game rather than being placed into [EvilTreeState] by hand.
     */
    private fun matureTree(type: String): GameObject {
        settings["events.evilTree.growthTicks"] = "10"
        sapling(type, "evil_tree_${type}_full", emptyTile)
        EvilTreeState.maxHealth = 150
        EvilTreeState.health = 150
        EvilTreeState.grownTick = GameLoop.tick.toLong()
        World.timers.start("evil_tree")
        for (row in Tables.get("evil_branches").rows()) {
            val tile = emptyTile.add(row.int("deltaX"), row.int("deltaY"))
            GameObjects.add(row.obj("spawn"), tile, rotation = row.int("dir"), ticks = EvilTree.ROOT_BURST_TICKS)
        }
        tickIf(limit = 100) { EvilTreeState.roots.size < 4 }
        // Roots respawn on a timer, which would fight the tests that chop one down
        World.timers.clear("evil_tree")
        return EvilTreeState.tree
    }

    private fun chopper(tile: Tile): Player {
        val player = createPlayer(tile)
        player.levels.set(Skill.Woodcutting, 99)
        player.inventory.add("dragon_hatchet")
        return player
    }

    /**
     * Burns the tree down to [health] through a single tick of one lit fire, so the tree
     * takes the damage through the same path players do.
     */
    private fun burnTo(health: Int) {
        EvilTreeState.health = health + 1
        EvilTreeState.fires["zero"] = createObject("evil_tree_fire", emptyTile.add(-1, 1))
        World.timers.startIfAbsent("evil_tree")
        tick(TICK)
        EvilTreeState.fires.clear()
        assertEquals(health, EvilTreeState.health)
    }

    private fun claim(player: Player) {
        player.npcOption(EvilTreeState.leprechaun, "Talk-to")
        tickIf { player.dialogue == null }
        player.dialogueContinue()
        player.dialogueOption("line3")
        player.dialogueContinue()
    }

    companion object {
        private const val TICK = EvilTree.TICK_INTERVAL + 1
    }
}
