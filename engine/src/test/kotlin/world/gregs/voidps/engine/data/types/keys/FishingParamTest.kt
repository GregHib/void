package world.gregs.voidps.engine.data.types.keys

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.engine.data.definition.data.Spot
import world.gregs.voidps.engine.data.param.NpcParams

class FishingParamTest {

    @Test
    fun `Encode and decode empty map`() {
        val codec = NpcParams.FishingParam()
        val original = emptyMap<String, Spot>()

        val writer = ArrayWriter(1)
        codec.write(writer, original)

        val reader = ArrayReader(writer.toArray())
        val decoded = codec.read(reader)

        assertEquals(original, decoded)
        assertTrue(decoded.isEmpty())
    }

    @Test
    fun `Encode and decode single spot with no tackle or bait`() {
        val codec = NpcParams.FishingParam()
        val original = mapOf(
            "lake_1" to Spot(
                tackle = ObjectArrayList(),
                bait = Object2ObjectOpenHashMap()
            )
        )

        val writer = ArrayWriter(100)
        codec.write(writer, original)

        val reader = ArrayReader(writer.toArray())
        val decoded = codec.read(reader)

        assertEquals(1, decoded.size)
        assertTrue(decoded.containsKey("lake_1"))
        assertTrue(decoded["lake_1"]!!.tackle.isEmpty())
        assertTrue(decoded["lake_1"]!!.bait.isEmpty())
    }

    @Test
    fun `Encode and decode single spot with tackle and bait`() {
        val codec = NpcParams.FishingParam()
        val tackle = ObjectArrayList<String>()
        tackle.add("rod")
        tackle.add("reel")

        val bait = Object2ObjectOpenHashMap<String, MutableList<String>>()
        bait["worms"] = mutableListOf("earthworm", "nightcrawler")
        bait["lures"] = mutableListOf("spinnerbait", "crankbait")

        val original = mapOf(
            "river_1" to Spot(tackle, bait)
        )

        val writer = ArrayWriter(200)
        codec.write(writer, original)

        val reader = ArrayReader(writer.toArray())
        val decoded = codec.read(reader)

        assertEquals(1, decoded.size)
        val spot = decoded["river_1"]!!
        assertEquals(2, spot.tackle.size)
        assertTrue(spot.tackle.contains("rod"))
        assertTrue(spot.tackle.contains("reel"))
        assertEquals(2, spot.bait.size)
        assertEquals(listOf("earthworm", "nightcrawler"), spot.bait["worms"])
        assertEquals(listOf("spinnerbait", "crankbait"), spot.bait["lures"])
    }

    @Test
    fun `Encode and decode multiple spots`() {
        val codec = NpcParams.FishingParam()

        val tackle1 = ObjectArrayList<String>()
        tackle1.add("fly_rod")

        val bait1 = Object2ObjectOpenHashMap<String, MutableList<String>>()
        bait1["flies"] = mutableListOf("dry_fly", "wet_fly")

        val tackle2 = ObjectArrayList<String>()
        tackle2.add("spinning_rod")
        tackle2.add("tackle_box")

        val bait2 = Object2ObjectOpenHashMap<String, MutableList<String>>()
        bait2["artificial"] = mutableListOf("jig", "plastic_worm")
        bait2["live"] = mutableListOf("minnow")

        val original = mapOf(
            "stream_1" to Spot(tackle1, bait1),
            "pond_2" to Spot(tackle2, bait2)
        )

        val writer = ArrayWriter(500)
        codec.write(writer, original)

        val reader = ArrayReader(writer.toArray())
        val decoded = codec.read(reader)

        assertEquals(2, decoded.size)

        val spot1 = decoded["stream_1"]!!
        assertEquals(1, spot1.tackle.size)
        assertEquals("fly_rod", spot1.tackle[0])
        assertEquals(1, spot1.bait.size)
        assertEquals(listOf("dry_fly", "wet_fly"), spot1.bait["flies"])

        val spot2 = decoded["pond_2"]!!
        assertEquals(2, spot2.tackle.size)
        assertEquals(2, spot2.bait.size)
        assertEquals(listOf("jig", "plastic_worm"), spot2.bait["artificial"])
        assertEquals(listOf("minnow"), spot2.bait["live"])
    }

    @Test
    fun `Roundtrip preserves data integrity`() {
        val codec = NpcParams.FishingParam()

        val tackle = ObjectArrayList<String>()
        tackle.add("item1")
        tackle.add("item2")
        tackle.add("item3")

        val bait = Object2ObjectOpenHashMap<String, MutableList<String>>()
        bait["type1"] = mutableListOf("a", "b", "c")
        bait["type2"] = mutableListOf("x")
        bait["type3"] = mutableListOf("p", "q")

        val original = mapOf(
            "spot_a" to Spot(tackle, bait)
        )

        // First roundtrip
        val writer1 = ArrayWriter(500)
        codec.write(writer1, original)
        val reader1 = ArrayReader(writer1.toArray())
        val decoded1 = codec.read(reader1)

        // Second roundtrip
        val writer2 = ArrayWriter(500)
        codec.write(writer2, decoded1)
        val reader2 = ArrayReader(writer2.toArray())
        val decoded2 = codec.read(reader2)

        // Both should be identical
        assertEquals(decoded1["spot_a"]!!.tackle, decoded2["spot_a"]!!.tackle)
        assertEquals(decoded1["spot_a"]!!.bait, decoded2["spot_a"]!!.bait)
    }
}