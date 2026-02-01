package content.bot.interact.path

import content.bot.action.NavigationShortcut
import content.bot.fact.Condition
import content.bot.fact.Fact
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraphTest {

    @Test
    fun `Shortest path is found`() {
        /*

               B
          10 / |
            A  | 1
           1 \ |
               E

         */
        val builder = Graph.Builder()
        val a = 0
        val b = 1
        val c = 2
        builder.addEdge(a, b, 10)
        builder.addEdge(a, c, 1)
        builder.addEdge(c, b, 1)
//        builder.print()

        val output = mutableListOf<Int>()
        val success = builder.build().find(Player(), output, a, b)
        assertTrue(success)
        assertEquals(listOf(1, 2), output)
    }

    @Test
    fun `Branching path`() {
        /*
                  6
               B-----E
           2 / |5  / |  \ 9
            A  |  /3 |1  C
           8 \ | /   |  / 3
               D-----F
                  2
         */
        val builder = Graph.Builder()
        val a = 0
        val b = 1
        val c = 2
        val d = 3
        val e = 4
        val f = 5
        val ab = builder.addEdge(a, b, 2)
        builder.addEdge(a, d, 8)
        builder.addEdge(b, e, 6)
        val bd = builder.addEdge(b, d, 5)
        builder.addEdge(d, e, 3)
        val df = builder.addEdge(d, f, 2)
        builder.addEdge(e, f, 1)
        builder.addEdge(e, c, 9)
        val fc = builder.addEdge(f, c, 3)
//        builder.print()

        val output = mutableListOf<Int>()
        val success = builder.build().find(Player(), output, a, c)
        assertTrue(success)
        assertEquals(listOf(ab, bd, df, fc), output)
    }

    @Test
    fun `Differing starting location`() {
        /*
              1
           -------
          / 2     \
          G---D   |
       2  |   | 1 |
          F   B--/
       3  |  /
          C / 1
       4  |/
          A
         */
        val builder = Graph.Builder()
        val a = 0
        val b = 1
        val c = 2
        val d = 3
        val e = 4
        val f = 5
        val g = 6
        builder.addEdge(a, b, 1)
        builder.addEdge(c, a, 4)
        val bd = builder.addEdge(b, d, 1)
        builder.addEdge(b, e, 3)
        builder.addEdge(f, c, 5)
        builder.addEdge(d, g, 2)
        val gb = builder.addEdge(g, b, 1)
        val fg = builder.addEdge(f, g, 2)
//        builder.print()

        val output = mutableListOf<Int>()
        val success = builder.build().find(Player(), output, f, d)
        assertTrue(success)
        assertEquals(listOf(fg, gb, bd), output)
    }

    @Test
    fun `Condition is avoided if failed`() {
        /*
                  3
               B-----C
           7 / |8  / |
            A  |  /2 |6
           1 \ | /   |
               E-----D
                 7-X
         */
        val builder = Graph.Builder()
        val a = 0
        val b = 1
        val c = 2
        val d = 3
        val e = 4
        builder.addEdge(a, b, 7)
        val ae = builder.addEdge(a, e, 1)
        builder.addEdge(b, e, 8)
        builder.addEdge(b, c, 3)
        val cd = builder.addEdge(c, d, 6)
        val ec = builder.addEdge(e, c, 2)
        builder.addEdge(e, d, 7, conditions = listOf(Condition.Equals(Fact.PlayerTile, Tile(100))))
//        builder.print()

        val output = mutableListOf<Int>()
        val success = builder.build().find(Player(), output, a, d)
        assertTrue(success)
        assertEquals(listOf(ae, ec, cd), output)
    }

    @Test
    fun `Can only traverse in one direction`() {
        /*
               B
           1 /
            A
         */
        val builder = Graph.Builder()
        val a = 0
        val b = 1
        builder.addEdge(a, b, 10)
//        builder.print()

        val output = mutableListOf<Int>()
        val success = builder.build().find(Player(), output, b, a)
        assertFalse(success)
    }


    @Test
    fun `Multiple starting points`() {
        /*
              1
           -------
          / 2     \
          G---D   |
       2  |   | 1 |
          F   B--/
       3  |  /
          C / 1
       4  |/
          A
         */
        val builder = Graph.Builder()
        val a = 0
        val b = 1
        val c = 2
        val d = 3
        val e = 4
        val f = 5
        val g = 6
        val ab = builder.addEdge(a, b, 1)
        builder.addEdge(c, a, 4)
        val bd = builder.addEdge(b, d, 1)
        builder.addEdge(b, e, 3)
        builder.addEdge(f, c, 5)
        builder.addEdge(d, g, 2)
        builder.addEdge(g, b, 1)
        builder.addEdge(f, g, 2)
//        builder.print()

        val output = mutableListOf<Int>()
        val success = builder.build().find(Player(), output, setOf(f, a), d)
        assertTrue(success)
        assertEquals(listOf(ab, bd), output)
    }

    @Test
    fun `Find returns shortest path`() {
        val builder = Graph.Builder()

        val a = Tile(0)
        val b = Tile(1)
        val c = Tile(2)

        builder.addBiEdge(a, b, weight = 1, actions = emptyList())
        builder.addBiEdge(b, c, weight = 1, actions = emptyList())
        builder.addBiEdge(a, c, weight = 10, actions = emptyList())

        val graph = builder.build()
        val player = Player()
        player.tile = a

        val path = mutableListOf<Int>()
        val found = graph.find(player, path, start = 0, target = 2)

        assertTrue(found)
        assertEquals(2, path.size, "Shortest path should be two edges, not direct edge")
    }

    @Test
    fun `Find respects edge conditions`() {
        val builder = Graph.Builder()

        val a = Tile(0)
        val b = Tile(1)

        builder.addEdge(
            from = a,
            to = b,
            weight = 1,
            actions = emptyList(),
            conditions = listOf(Condition.Equals(Fact.PlayerTile, Tile(100)))
        )

        val graph = builder.build()
        val player = Player()
        player.tile = a

        val path = mutableListOf<Int>()
        val found = graph.find(player, path, start = 0, target = 1)

        assertFalse(found, "Edge condition blocks traversal")
        assertTrue(path.isEmpty())
    }

    @Test
    fun `Starting points include nearby tiles`() {
        val builder = Graph.Builder()

        val a = Tile(0)
        val b = Tile(20)
        val c = Tile(100)

        builder.add(a)
        builder.add(b)
        builder.add(c)

        val graph = builder.build()
        val player = Player()
        player.tile = Tile(10)

        val starts = graph.startingPoints(player)

        assertTrue(starts.contains(0))
        assertTrue(starts.contains(1))
        assertFalse(starts.contains(2))
    }

    @Test
    fun `Shortcut adds starting point when requirements met`() {
        Areas.set(mapOf("town" to AreaDefinition("town", Rectangle(75, 75, 75, 75), setOf())), mapOf(), mapOf())

        val shortcut = NavigationShortcut(
            id = "teleport",
            weight = 1,
            requires = listOf(Condition.Equals(Fact.PlayerTile, Tile(50, 50))),
            produces = setOf(Condition.Area(Fact.PlayerTile, "town"))
        )

        val builder = Graph.Builder()
        val edge = builder.add(shortcut)

        val graph = builder.build()
        val player = Player()
        player.tile = Tile(50, 50)

        val starts = graph.startingPoints(player)

        assertTrue(starts.contains(edge))
    }

    @Test
    fun `Path reconstruction produces correct edge order`() {
        val builder = Graph.Builder()

        val a = Tile(0)

        val e1 = builder.addEdge(0, 1, weight = 1)
        val e2 = builder.addEdge(1, 2, weight = 1)

        val graph = builder.build()
        val player = Player()
        player.tile = a

        val path = mutableListOf<Int>()
        val found = graph.find(player, path, start = 0, target = 2)

        assertTrue(found)
        assertEquals(listOf(e1, e2), path)
    }
}