package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.condition.BotAlliesOnTile
import content.bot.behaviour.condition.BotItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import set
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class BotSwitchSetupTest {

    private lateinit var player: Player
    private lateinit var bot: Bot

    @BeforeEach
    fun setup() {
        player = Player()
        bot = Bot(player)
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(InventoryDefinition(stringId = "worn_equipment", length = 14))
        player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = 28))
    }

    @Test
    fun `Gated condition false returns success without dispatch`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(stringId = "rune_scimitar", params = mapOf(Params.SLOT to EquipSlot.Weapon))),
            mapOf("rune_scimitar" to 0),
        )
        val action = BotSwitchSetup(
            equipment = mapOf(EquipSlot.Weapon to BotItem(setOf("rune_scimitar"), min = 1)),
            condition = BotAlliesOnTile(min = 5),
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Already equipped target item returns success`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(stringId = "rune_scimitar", params = mapOf(Params.SLOT to EquipSlot.Weapon))),
            mapOf("rune_scimitar" to 0),
        )
        player.equipment.set(EquipSlot.Weapon.index, "rune_scimitar")

        val action = BotSwitchSetup(
            equipment = mapOf(EquipSlot.Weapon to BotItem(setOf("rune_scimitar"), min = 1)),
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }

    @Test
    fun `Mismatch with no inventory match returns success`() {
        ItemDefinitions.set(
            arrayOf(
                ItemDefinition(stringId = "rune_scimitar", params = mapOf(Params.SLOT to EquipSlot.Weapon)),
                ItemDefinition(stringId = "rune_crossbow", params = mapOf(Params.SLOT to EquipSlot.Weapon)),
            ),
            mapOf("rune_scimitar" to 0, "rune_crossbow" to 1),
        )
        player.equipment.set(EquipSlot.Weapon.index, "rune_scimitar")

        val action = BotSwitchSetup(
            equipment = mapOf(EquipSlot.Weapon to BotItem(setOf("rune_crossbow"), min = 1)),
        )

        val state = action.update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertEquals(BehaviourState.Success, state)
    }
}
