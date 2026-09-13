package content.skill.constitution

import WorldTest
import content.skill.constitution.drink.potionEffects
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.ItemDefinitions

internal class ConsumableItemsTest : WorldTest() {

    /**
     * `excess` is the item left after a dose is drunk and `empty` is the container it came in, so
     * both have to name a real item or the consumable silently breaks.
     */
    @Test
    fun `Every excess and empty item exists`() {
        val missing = mutableListOf<String>()
        for (definition in ItemDefinitions.definitions) {
            for (param in listOf("excess", "empty")) {
                val target: String = definition.getOrNull(param) ?: continue
                if (!ItemDefinitions.ids.containsKey(target)) {
                    missing.add("${definition.stringId} $param = \"$target\"")
                }
            }
        }
        assertEquals(emptyList<String>(), missing)
    }

    /**
     * A dosed potion is recognised by its base name, so one without an effect branch is drunk
     * silently.
     */
    @Test
    fun `Every dosed potion has an effect`() {
        val bases = mutableSetOf<String>()
        for (definition in ItemDefinitions.definitions) {
            val id = definition.stringId
            if (id.endsWith("_noted") || id.length < 3 || id[id.length - 2] != '_') {
                continue
            }
            if (id.last() !in '1'..'5') {
                continue
            }
            if (!definition.contains("heals") && !definition.contains("excess")) {
                continue
            }
            bases.add(id.substringBeforeLast('_'))
        }

        val player = createPlayer(emptyTile)
        val missing = (bases - NOT_POTIONS).filterNot { player.potionEffects("${it}_1") }
        assertEquals(emptyList<String>(), missing, "dosed potions with no effect")
    }

    private companion object {
        /**
         * Items shaped like a dosed potion without being one: food and cocktails that come in
         * portions, and a second copy of an item whose name happens to end in a digit.
         */
        private val NOT_POTIONS = setOf(
            "blurberry_special",
            "cooked_crab_meat",
            "cooked_karambwan",
            "drunk_dragon",
            "easter_egg",
            "food_class",
            "fruit_blast",
            "jug_of_bad_wine",
            "pineapple_punch",
            "short_green_guy",
        )
    }
}
