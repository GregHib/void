package content.bot.action

import content.bot.fact.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows

class BehaviourFragmentTest {

    private fun fragment(fields: Map<String, Any> = emptyMap()) =
        BehaviourFragment(
            id = "test",
            capacity = 1,
            template = "tpl",
            type = "activity",
            fields = fields,
            weight = 1,
        )

    /*
        Actions
     */

    @Test
    fun `Nested clone action throws`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            actions = listOf(BotAction.Clone("x"))
        )
        assertThrows<IllegalArgumentException> {
            fragment.resolveActions(template, mutableListOf())
        }
    }

    @Test
    fun `Nested reference action throws`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            actions = listOf(
                BotAction.Reference(
                    BotAction.Reference(
                        BotAction.GoTo("x"),
                        emptyMap()
                    ),
                    emptyMap()
                )
            )
        )
        assertThrows<IllegalArgumentException> {
            fragment.resolveActions(template, mutableListOf())
        }
    }

    @Test
    fun `Wrong type in action field throws`() {
        val fragment = fragment(mapOf("dest" to 123))
        val template = BotActivity(
            id = "a",
            capacity = 1,
            actions = listOf(
                BotAction.Reference(
                    BotAction.GoTo("x"),
                    references = mapOf("go_to" to "dest")
                )
            )
        )

        assertThrows<IllegalArgumentException> {
            fragment.resolveActions(template, mutableListOf())
        }
    }

    @Test
    fun `No references uses default value`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            actions = listOf(
                BotAction.Reference(
                    BotAction.Wait(10),
                    references = emptyMap()
                )
            )
        )

        val actions = mutableListOf<BotAction>()
        fragment.resolveActions(template, actions)

        assertEquals(BotAction.Wait(10), actions.single())
    }

    @Test
    fun `Non-reference action is added`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            actions = listOf(
                BotAction.Wait(10)
            )
        )

        val actions = mutableListOf<BotAction>()
        fragment.resolveActions(template, actions)

        assertEquals(BotAction.Wait(10), actions.single())
    }

    @TestFactory
    fun `Resolve action references`() = listOf(
        Triple(BotAction.GoTo("default"), mapOf("go_to" to "lumbridge"), BotAction.GoTo("lumbridge")),
        Triple(BotAction.GoToNearest("default"), mapOf("go_to_nearest" to "lumbridge"), BotAction.GoToNearest("lumbridge")),
        Triple(BotAction.InterfaceOption(option = "click", id = "something"), mapOf("option" to "Open", "interface" to "bank"), BotAction.InterfaceOption(option = "Open", id = "bank")),
        Triple(BotAction.DialogueContinue(option = "click", id = "something"), mapOf("option" to "Option", "continue" to "now"), BotAction.DialogueContinue(option = "Option", id = "now")),
        Triple(BotAction.ItemOnItem(item = "default", on = "on"), mapOf("item" to "item", "on" to "another"), BotAction.ItemOnItem(item = "item", on = "another")),
        Triple(
            BotAction.InteractNpc(
                option = "talk",
                id = "default",
                delay = 2,
                radius = 10
            ),
            mapOf("option" to "Talk-to", "npc" to "bob", "delay" to 5, "radius" to 5),
            BotAction.InteractNpc(
                option = "Talk-to",
                id = "bob",
                delay = 5,
                radius = 5
            )
        ),
        Triple(
            BotAction.InteractObject(
                option = "interact",
                id = "default",
                delay = 2,
                radius = 10
            ),
            mapOf("option" to "Open", "object" to "door", "delay" to 5, "radius" to 5),
            BotAction.InteractObject(
                option = "Open",
                id = "door",
                delay = 5,
                radius = 5
            )
        ),
        Triple(BotAction.Wait(4), mapOf("wait" to 5), BotAction.Wait(5)),
    ).map { (default, values, expected) ->
        dynamicTest("Resolve ${default::class.simpleName} references") {
            val fields = values.mapKeys { "ref_${it.key}" }
            val fragment = fragment(fields)
            val references = values.map { it.key to "\$ref_${it.key}" }.toMap()
            val template = BotActivity(
                id = "a",
                capacity = 1,
                actions = listOf(
                    BotAction.Reference(
                        default,
                        references = references
                    )
                )
            )
            val actions = mutableListOf<BotAction>()
            fragment.resolveActions(template, actions)
            assertEquals(expected, actions.single())
        }
    }

    @Test
    fun `Resolve partial references`() {
        val fragment = fragment(mapOf("type" to "fun"))
        val template = BotActivity(
            id = "a",
            capacity = 1,
            requires = listOf(
                Condition.Reference(
                    "location", "default", references = mapOf(
                        "location" to $$"some_${type}_area"
                    )
                )
            )
        )
        val actions = mutableListOf<Condition>()
        fragment.resolveRequirements(actions, template.requires)
        assertEquals(Condition.Area(Fact.PlayerTile, "some_fun_area"), actions.single())
    }

    @Test
    fun `Resolve ending reference`() {
        val fragment = fragment(mapOf("area_type" to "fun"))
        val template = BotActivity(
            id = "a",
            capacity = 1,
            requires = listOf(
                Condition.Reference("location", "default",
                    references = mapOf(
                        "location" to $$"some_$area_type"
                    )
                )
            )
        )
        val actions = mutableListOf<Condition>()
        fragment.resolveRequirements(actions, template.requires)
        assertEquals(Condition.Area(Fact.PlayerTile, "some_fun"), actions.single())
    }

    /*
      Requirements
     */

    @TestFactory
    fun `Resolve requirement references`() = listOf(
        Triple(Condition.Reference("skill", "defence", min = 1, max = 120), mapOf("skill" to "attack", "min" to 5, "max" to 99), Condition.Range(Fact.AttackLevel, 5, 99)),
        Triple(Condition.Reference("variable", "default", value = 1), mapOf("variable" to "test", "value" to true), Condition.Equals(Fact.BoolVariable("test", null), true)),
        Triple(Condition.Reference("equips", "default", min = 1), mapOf("equips" to "item", "amount" to 10), Condition.AtLeast(Fact.EquipCount("item"), 10)),
        Triple(Condition.Reference("carries", "default", min = 1), mapOf("carries" to "item", "amount" to 10), Condition.AtLeast(Fact.InventoryCount("item"), 10)),
        Triple(Condition.Reference("inventory_space", min = 1), mapOf("inventory_space" to 10), Condition.AtLeast(Fact.InventorySpace, 10)),
        Triple(Condition.Reference("location", "default"), mapOf("location" to "area"), Condition.Area(Fact.PlayerTile, "area")),
        Triple(Condition.Reference("timer", "default"), mapOf("timer" to "tick"), Condition.Equals(Fact.HasTimer("tick"), true)),
        Triple(Condition.Reference("interface", "default"), mapOf("interface" to "id"), Condition.Equals(Fact.InterfaceOpen("id"), true)),
        Triple(Condition.Reference("clock", "default", min = 1), mapOf("clock" to "tock", "min" to 5), Condition.AtLeast(Fact.ClockRemaining("tock"), 5)),
    ).map { (reference, values, expected) ->
        dynamicTest("Resolve ${reference::class.simpleName} references") {
            val fields = values.mapKeys { "ref_${it.key}" }
            val fragment = fragment(fields)
            val references = values.map { it.key to "\$ref_${it.key}" }.toMap()
            val template = BotActivity(
                id = "a",
                capacity = 1,
                requires = listOf(
                    reference.copy(references = references)
                )
            )
            val actions = mutableListOf<Condition>()
            fragment.resolveRequirements(actions, template.requires)
            assertEquals(expected, actions.single())
        }
    }

    @Test
    fun `Missing requirement field throws`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            requires = listOf(
                Condition.Reference("skill", "attack", references = mapOf("skill" to "missing"))
            )
        )
        assertThrows<IllegalArgumentException> {
            fragment.resolveRequirements(mutableListOf(), template.requires)
        }
    }

    @Test
    fun `No requirement reference uses default value`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            requires = listOf(
                Condition.Reference("skill", "attack", references = emptyMap())
            )
        )

        val actions = mutableListOf<Condition>()
        fragment.resolveRequirements(actions, template.requires)

        assertEquals(Condition.Equals(Fact.AttackLevel, 1), actions.single())
    }

    @Test
    fun `Non-reference requirement is added`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            requires = listOf(
                Condition.Reference("skill", "attack")
            )
        )

        val actions = mutableListOf<Condition>()
        fragment.resolveRequirements(actions, template.requires)

        assertEquals(Condition.Equals(Fact.AttackLevel, 1), actions.single())
    }

    @Test
    fun `Any clone requirement throws`() {
        val fragment = fragment()
        val template = BotActivity(
            id = "a",
            capacity = 1,
            requires = listOf(
                Condition.Clone("x")
            )
        )
        assertThrows<IllegalArgumentException> {
            fragment.resolveRequirements(mutableListOf(), template.requires)
        }
    }

}