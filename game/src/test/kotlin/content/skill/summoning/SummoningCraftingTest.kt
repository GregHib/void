package content.skill.summoning

import WorldTest
import interfaceOption
import objectOption
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.koin.test.get
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.add
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class SummoningCraftingTest : WorldTest() {

    private lateinit var enums: EnumDefinitions
    private lateinit var nonDungeoneeringPouchMap: Map<Int, Any>
    private lateinit var nonDungeoneeringScollMap: Map<Int, Any>

    @BeforeEach
    fun setup() {
        enums = get()
        nonDungeoneeringPouchMap = enums.get("summoning_pouch_ids_1").map!!.filter { (index, _) -> index < 78 }
        nonDungeoneeringScollMap = enums.get("summoning_scroll_ids_1").map!!.filter { (index, _) -> index < 78 }
    }

    @TestFactory
    fun `Infuse Non-Dungeoneering Pouch`() = nonDungeoneeringPouchMap.map { (index, pouchId) ->
        val pouch = Item(ItemDefinitions.get(pouchId as Int).stringId)
        dynamicTest("Infuse ${pouch.id}") {
            val player = createPlayer(Tile(2523, 3056))
            player.levels.set(Skill.Summoning, 99)
            val originalXp = player.experience.get(Skill.Summoning)
            val obelisk = GameObjects.find(Tile(2521, 3055), "obelisk")
            val interactingSlot = index * 5 - 3

            val charmItemId: Int = pouch.def["summoning_charm_id"]
            val charmAmount: Int = pouch.def["summoning_charm_amount"]
            val charm = Item(ItemDefinitions.get(charmItemId).stringId, charmAmount)

            val shardItemId: Int = pouch.def["summoning_shard_id"]
            val shardAmount: Int = pouch.def["summoning_shard_amount"]
            val shard = Item(ItemDefinitions.get(shardItemId).stringId, shardAmount)

            val blankPouchItemId: Int = pouch.def["summoning_pouch_id"]
            val blankPouchAmount: Int = pouch.def["summoning_pouch_amount"]
            val blankPouch = Item(ItemDefinitions.get(blankPouchItemId).stringId, blankPouchAmount)

            val tertiaryItemId1: Int = pouch.def["summoning_pouch_req_item_id_1"]
            val tertiaryItemAmount1: Int = pouch.def["summoning_pouch_req_item_amount_1"]
            val tertiaryItemId2 = pouch.def["summoning_pouch_req_item_id_2", -1]
            val tertiaryItemAmount2 = pouch.def["summoning_pouch_req_item_amount_2", -1]

            val tertiaries = mutableListOf(Item(ItemDefinitions.get(tertiaryItemId1).stringId, tertiaryItemAmount1))

            if (tertiaryItemId2 != -1 && tertiaryItemAmount2 != -1) {
                tertiaries.add(Item(ItemDefinitions.get(tertiaryItemId2).stringId, tertiaryItemAmount2))
            }

            player.inventory.transaction {
                add(charm.id, charm.amount)
                add(shard.id, shard.amount)
                add(blankPouch.id, blankPouch.amount)
                add(tertiaries.toList())
            }

            player.objectOption(obelisk, "Infuse-pouch")
            tick()

            player.interfaceOption("summoning_pouch_creation", "pouches", "Infuse", item = pouch, slot = interactingSlot)
            tick()

            assertEquals(0, player.inventory.count(charm.id))
            assertEquals(0, player.inventory.count(shard.id))
            assertEquals(0, player.inventory.count(blankPouch.id))
            tertiaries.forEach { tertiary -> assertEquals(0, player.inventory.count(tertiary.id)) }
            assertEquals(1, player.inventory.count(pouch.id))
            assertNotEquals(originalXp, player.experience.get(Skill.Summoning))
        }
    }

    @TestFactory
    fun `Transform Non-Dungeoneering Scrolls`() = nonDungeoneeringScollMap.map { (index, scrollId) ->
        val scroll = Item(ItemDefinitions.get(scrollId as Int).stringId)
        val pouchId = enums.get("summoning_scroll_ids_2").getKey(scrollId)
        val pouch = Item(ItemDefinitions.get(pouchId).stringId)

        dynamicTest("Transform ${scroll.id}") {
            val player = createPlayer(Tile(2523, 3056))
            player.levels.set(Skill.Summoning, 99)
            val originalXp = player.experience.get(Skill.Summoning)
            val obelisk = GameObjects.find(Tile(2521, 3055), "obelisk")
            val interactingSlot = index * 5 - 3

            player.inventory.transaction {
                add(pouch.id, 1)
            }

            player.objectOption(obelisk, "Infuse-pouch")
            tick()

            player.interfaceOption("summoning_pouch_creation", "scroll_creation_tab", "Transform Scrolls")
            tick()

            player.interfaceOption("summoning_scroll_creation", "scrolls", "Transform", item = scroll, slot = interactingSlot)
            tick()

            assertEquals(0, player.inventory.count(pouch.id))
            assertEquals(10, player.inventory.count(scroll.id))
            assertNotEquals(originalXp, player.experience.get(Skill.Summoning))
        }
    }
}
