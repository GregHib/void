package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import world.gregs.voidps.cache.definition.data.FontDefinition
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventories
import world.gregs.voidps.engine.inv.restrict.NoRestrictions
import world.gregs.voidps.engine.inv.stack.ItemStackingRule

abstract class MagicSpellTest : KoinTest {

    private lateinit var itemDefinitions: ItemDefinitions
    private lateinit var inventoryDefinitions: InventoryDefinitions
    protected lateinit var interfaceDefinitions: InterfaceDefinitions
    private lateinit var fontDefinitions: FontDefinitions
    private var information: Array<Any> = Array(16) { 0 }
    private val itemDefs = Array(100) { ItemDefinition.EMPTY }
    private val itemIds: MutableMap<String, Int> = mutableMapOf()
    private var itemId = 0

    @BeforeEach
    fun start() {
        interfaceDefinitions = InterfaceDefinitions(arrayOf(InterfaceDefinition(0, components = mutableMapOf(0 to InterfaceComponentDefinition(0, information = information)))))
        interfaceDefinitions.ids = mapOf("unknown_spellbook" to 0)
        interfaceDefinitions.componentIds = mapOf(
            "unknown_spellbook_spell" to 0,
            "unknown_spellbook_spell_bolt" to 0,
            "unknown_spellbook_spell_blast" to 0,
            "unknown_spellbook_spell_surge" to 0,
            "unknown_spellbook_spell_wave" to 0,
        )
        inventoryDefinitions = InventoryDefinitions(emptyArray())
        inventoryDefinitions.ids = emptyMap()
        itemDefinitions = ItemDefinitions(itemDefs)
        itemDefinitions.ids = itemIds
        fontDefinitions = FontDefinitions(arrayOf(FontDefinition(0, (0..200).map { 1.toByte() }.toByteArray()))).apply { ids = mapOf("p12_full" to 0) }
        startKoin {
            modules(module {
                single { itemDefinitions }
                single { interfaceDefinitions }
                single { fontDefinitions }
            })
        }
    }

    @AfterEach
    fun stop() {
        stopKoin()
    }

    fun setLevel(level: Int) {
        information[5] = level
    }

    fun setItems(vararg items: Item) {
        for (index in items.indices) {
            val item = items[index]
            information[8 + index * 2] = itemId
            information[9 + index * 2] = item.amount
            addItemDef(ItemDefinition(stringId = item.id, stackable = 1))
        }
    }

    fun setItems(vararg items: Pair<Item, ItemDefinition>) {
        for (index in items.indices) {
            val (item, def) = items[index]
            information[8 + index * 2] = itemId
            information[9 + index * 2] = item.amount
            addItemDef(if (def == ItemDefinition.EMPTY) ItemDefinition(stringId = item.id) else def)
        }
    }

    fun addItemDef(definition: ItemDefinition) {
        val id = itemId++
        definition.id = id
        itemIds[definition.stringId] = id
        itemDefs[id] = definition
    }

    private val normalStackRule = object : ItemStackingRule {
        override fun stackable(id: String): Boolean {
            return itemDefinitions.get(id).stackable == 1
        }
    }

    fun player(): Player {
        val player = Player(
            inventories = Inventories(mapOf("inventory" to Array(28) { Item.EMPTY.copy() }, "worn_equipment" to Array(12) { Item.EMPTY.copy() }))
        )
        player.interfaces = Interfaces(player, definitions = interfaceDefinitions)
        player.inventories.definitions = inventoryDefinitions
        player.inventories.itemDefinitions = itemDefinitions
        player.inventories.validItemRule = NoRestrictions
        player.inventories.events = player
        player.inventories.normalStack = normalStackRule
        player.inventories.start()
        player.levels.link(player, PlayerLevels(player.experience))
        return player
    }
}