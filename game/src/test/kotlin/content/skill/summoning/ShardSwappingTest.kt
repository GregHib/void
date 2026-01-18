package content.skill.summoning

import WorldTest
import interfaceOption
import npcOption
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.koin.test.get
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import kotlin.test.assertEquals

class ShardSwappingTest : WorldTest() {

    private lateinit var enums: EnumDefinitions
    private lateinit var pouchMap: Map<Int, Any>
    private lateinit var scrollMap: Map<Int, Any>

    @BeforeEach
    fun setup() {
        enums = get()
        pouchMap = enums.get("summoning_pouch_ids_1").map!!.filter { (index, _) -> index < 78 }
        scrollMap = enums.get("summoning_scroll_ids_1").map!!.filter { (index, _) -> index < 78 }
    }

    @TestFactory
    fun `Swap Pouches for Shards`() = pouchMap.map { (index, pouchId) ->
        val pouch = Item(ItemDefinitions.get(pouchId as Int).stringId)
        dynamicTest("Swap ${pouch.id}") {
            val player = createPlayer(emptyTile)
            val bogrog = createNPC("bogrog", emptyTile.addY(4))
            player.levels.set(Skill.Summoning, 99)
            val interactingSlot = index * 5 - 3
            val returnAmount = pouch.def["shard_refund_amount", -1]

            player.inventory.transaction {
                add(pouch.id, 1)
            }

            player.npcOption(bogrog, "Swap")
            tick(4)

            player.interfaceOption("summoning_trade_in", "pouch_trade_in", "Trade", item = pouch, slot = interactingSlot)
            tick()

            assertEquals(0, player.inventory.count(pouch.id))
            assertEquals(returnAmount, player.inventory.count("spirit_shards"))
        }
    }

    @TestFactory
    fun `Swap Scrolls for Shards`() = scrollMap.map { (index, scrollId) ->
        val scroll = Item(ItemDefinitions.get(scrollId as Int).stringId)
        dynamicTest("Swap ${scroll.id}") {
            val player = createPlayer(emptyTile)
            val bogrog = createNPC("bogrog", emptyTile.addY(4))
            player.levels.set(Skill.Summoning, 99)
            val interactingSlot = index * 5 - 3
            val numNeeded = scroll.def["summoning_refund_amount_inverse", 1]
            val returnAmount = scroll.def["shard_refund_amount", 1]

            player.inventory.transaction {
                add(scroll.id, numNeeded)
            }

            player.npcOption(bogrog, "Swap")
            tick(4)

            player.interfaceOption("summoning_trade_in", "scroll_tab", "Trade Scrolls")
            tick()

            player.interfaceOption("summoning_trade_in", "pouch_trade_in", "Trade-All", item = scroll, slot = interactingSlot)
            tick()

            assertEquals(0, player.inventory.count(scroll.id))
            assertEquals(returnAmount, player.inventory.count("spirit_shards"))
        }
    }
}
