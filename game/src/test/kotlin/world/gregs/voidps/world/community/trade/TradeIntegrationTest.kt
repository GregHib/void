package world.gregs.voidps.world.community.trade

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.world.script.WorldMock

internal class TradeIntegrationTest : WorldMock() {

    lateinit var player1: Player
    lateinit var player2: Player

    override fun loadModules(): MutableList<Module> {
        val modules = super.loadModules()
        modules.add(module {
            single(override = true) {
                mockk<ContainerDecoder> {
                    every { get(any<Int>()) } answers { ContainerDefinition(id = arg(0)) }
                    every { get(93) } returns ContainerDefinition(id = 93, length = 28)
                    every { get(94) } returns ContainerDefinition(id = 94, length = 15)
                    every { get(90) } returns ContainerDefinition(id = 90, length = 28)
                }
            }
            single(override = true) {
                mockk<ItemDecoder> {
                    every { getOrNull(any()) } answers { ItemDefinition(id = arg(0)) }
                    every { get(any<Int>()) } answers { ItemDefinition(id = arg(0)) }
                    every { last } returns Short.MAX_VALUE.toInt()
                    every { get(995) } returns ItemDefinition(
                        id = 995,
                        name = "Coins",
                        stackable = 1
                    )
                }
            }
        })
        return modules
    }

    @BeforeEach
    override fun setup() {
        super.setup()
        player1 = createPlayer("1")
        player2 = createPlayer("2")
    }

    @Test
    fun test() {
        player1.inventory.add("coins", 1000)
        player1.events.emit(PlayerOption(player2, "Trade with", 4))
        player2.events.emit(PlayerOption(player1, "Trade with", 4))
        offerItem(player1, "Offer-10", 2, "coins", 0)
        acceptTrade(player1)
        acceptTrade(player2)
        tick()
        confirmTrade(player1)
        confirmTrade(player2)
        tick()
        assertEquals("coins", player1.inventory.getItemId(0))
        assertEquals(990, player1.inventory.getAmount(0))
        assertEquals("coins", player2.inventory.getItemId(0))
        assertEquals(10, player2.inventory.getAmount(0))
    }

    fun offerItem(player: Player, option: String, optionId: Int, item: String, slot: Int) {
        player.events.emit(InterfaceOption(336, "trade_side", 0, "offer", optionId, option, Item(item), slot))
    }

    fun acceptTrade(player: Player) {
        player.events.emit(InterfaceOption(335, "trade_main", 16, "accept", 0, "Accept", Item("", -1), -1))
    }

    fun confirmTrade(player: Player) {
        player.events.emit(InterfaceOption(334, "trade_confirm", 21, "accept", 0, "Accept", Item("", -1), -1))
    }

}