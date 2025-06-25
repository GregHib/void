package content.skill.summoning

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.dialogue.type.intEntry
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import kotlin.math.min

private val logger = InlineLogger()

val enums: EnumDefinitions by inject()
val itemDefinitions: ItemDefinitions by inject()

val pouchInterfaceId = 672
val pouchComponentId = 16

val scrollInterfaceId = 666
val scrollComponentId = 16

val width = 8
val height = 10

val startingIndex = 1
val endingIndex = 78

val startingDungeoneeringIndex = 1100
val endingDungeoneeringIndex = 1159

objectOperate("Infuse-pouch") {
    openPouchCraftingInterface(player)
}

interfaceOption("Transform Scrolls", "scroll_creation_tab", "summoning_pouch_creation") {
    openScrollCraftingInterface(player)
}

interfaceOption("Infuse Pouches", "pouch_creation_tab", "summoning_scroll_creation") {
    openPouchCraftingInterface(player)
}

interfaceOption("Infuse*", "pouches", "summoning_pouch_creation") {
    // TODO: When dungeoneering support is implemented, this will need to change
    val enumIndex = (itemSlot + 3) / 5

    if (item.id.endsWith("_u")) {
        sendIngredientMessage(player, enumIndex)
        return@interfaceOption
    }

    when(option) {
        "Infuse" -> infusePouches(player, enumIndex, 1)
        "Infuse-5" -> infusePouches(player, enumIndex, 5)
        "Infuse-10" -> infusePouches(player, enumIndex, 10)
        "Infuse-X" -> {
            val total = intEntry("Enter amount:")
            infusePouches(player, enumIndex, total)
        }
        "Infuse-All" -> infusePouches(player, enumIndex, Int.MAX_VALUE)
    }
}

interfaceOption("List", "pouches", "summoning_pouch_creation") {
    // TODO: When dungeoneering support is implemented, this will need to change
    val enumIndex = (itemSlot + 3) / 5
    sendIngredientMessage(player, enumIndex)
}

/**
 * Crafts summoning familiar pouches.
 *
 * @param player: The player doing the crafting
 * @param enumIndex: The index in the "summoning_pouch_ids_*" enum of the pouch being crafted
 * @param amount: The amount of pouches the player is attempting to craft
 */
fun infusePouches(player: Player, enumIndex: Int, amount: Int) {
    val pouchItemId = enums.get("summoning_pouch_ids_1").getInt(enumIndex)
    val pouchItem = Item(itemDefinitions.get(pouchItemId).stringId)

    val shards = getShards(pouchItem)
    val charms = getCharms(pouchItem)
    val pouches = getPouches(pouchItem)
    val tertiaries = getTertiaries(pouchItem)

    val xpPerCraft = pouchItem.def.extras?.get("infuse_experience") as Double? ?: return
    val maxCraftable = maxCraftable(player, shards, charms, pouches, tertiaries) ?: return
    val amountToCraft = min(amount, maxCraftable)

    player.interfaces.close("summoning_pouch_creation")

    player.inventory.remove(shards.id, shards.amount * amountToCraft)
    player.inventory.remove(charms.id, charms.amount * amountToCraft)
    player.inventory.remove(pouches.id, pouches.amount * amountToCraft)
    tertiaries.forEach { tertiary -> player.inventory.remove(tertiary.id, tertiary.amount * amountToCraft) }

    player.anim("summoning_infuse")

    player.inventory.add(pouchItem.id, amountToCraft)
    player.exp(Skill.Summoning, xpPerCraft * amountToCraft)
}

/**
 * Gets the one or two tertiaries and amount needed to craft the given familiar pouch.
 *
 * @param pouch: The pouch being crafted.
 *
 * @return: A [List] of [Item]s with the tertiary ids and amounts needed to craft the given [pouch]
 */
fun getTertiaries(pouch: Item): List<Item> {
    val tertiaryItemId1 = pouch.def.extras?.get("summoning_pouch_req_item_id_1") as Int
    val tertiaryItemAmount1 = pouch.def.extras?.get("summoning_pouch_req_item_amount_1") as Int
    val tertiaryItemId2 = pouch.def.extras?.get("summoning_pouch_req_item_id_2") as Int?
    val tertiaryItemAmount2 = pouch.def.extras?.get("summoning_pouch_req_item_amount_2") as Int?

    val tertiaries = mutableListOf(Item(itemDefinitions.get(tertiaryItemId1).stringId, tertiaryItemAmount1))

    if (tertiaryItemId2 != null && tertiaryItemAmount2 != null)
        tertiaries.add(Item(itemDefinitions.get(tertiaryItemId2).stringId, tertiaryItemAmount2))

    return tertiaries.toList()
}

/**
 * Gets the shard and amount needed to craft the given familiar pouch.
 *
 * @param pouch: The pouch being crafted.
 *
 * @return: An [Item] with the charm id and amount needed to craft the given [pouch]
 */
fun getShards(pouch: Item): Item {
    val shardItemId = pouch.def.extras?.get("summoning_shard_id") as Int
    val shardAmount = pouch.def.extras?.get("summoning_shard_amount") as Int
    return Item(itemDefinitions.get(shardItemId).stringId, shardAmount)
}

/**
 * Gets the charm and amount needed to craft the given familiar pouch.
 *
 * @param pouch: The pouch being crafted.
 *
 * @return: An [Item] with the charm id and amount needed to craft the given [pouch]
 */
fun getCharms(pouch: Item): Item {
    val charmItemId = pouch.def.extras?.get("summoning_charm_id") as Int
    val charmAmount = pouch.def.extras?.get("summoning_charm_amount") as Int
    return Item(itemDefinitions.get(charmItemId).stringId, charmAmount)
}

/**
 * Gets the pouch and amount needed to craft the given familiar pouch.
 *
 * @param pouch: The pouch being crafted.
 *
 * @return: An [Item] with the pouch id and amount needed to craft the given [pouch]
 */
fun getPouches(pouch: Item): Item {
    val pouchItemId = pouch.def.extras?.get("summoning_pouch_id") as Int
    val pouchAmount = pouch.def.extras?.get("summoning_pouch_amount") as Int
    return Item(itemDefinitions.get(pouchItemId).stringId, pouchAmount)
}

/**
 * Gets the maximum number of pouches the player can craft with the items they currently have in their
 * inventory.
 *
 * @param player: The player
 * @param shards: The shard [Item] with the associated amount of shards needed to craft the pouch
 * @param charm: The charm [Item] with the associated amount of charms needed to craft the pouch
 * @param pouch: The pouch [Item] with the associated amount of pouches needed to craft the pouch
 * @param tertiaries: The list of 1 or more tertiary ingredient [Item]s needed for the pouch and the associated amount
 * needed to craft the pouch.
 *
 * @return: The total number of pouches the player can craft
 */
fun maxCraftable(player: Player, shards: Item, charm: Item, pouch: Item, tertiaries: List<Item>): Int? {
    if (tertiaries.isEmpty()) {
        logger.info { "No tertiaries were provided." }
        return null
    }

    val charmCount = player.inventory.count(charm.id).floorDiv(charm.amount)
    val shardCount = player.inventory.count(shards.id).floorDiv(shards.amount)
    val pouchCount = player.inventory.count(pouch.id).floorDiv(pouch.amount)
    var minTertiaries = Int.MAX_VALUE
    tertiaries.forEach { tertiary ->
        val count = player.inventory.count(tertiary.id).floorDiv(tertiary.amount)
        if (count < minTertiaries)
            minTertiaries = count
    }

    return minOf(charmCount, shardCount, pouchCount, minTertiaries)
}

/**
 * Sends a message to the given player with the ingredients needed for crafting a pouch.
 *
 * @param player: The player to send the message to
 * @param enumIndex: The index of the clicked pouch in the "summoning_pouch_ids_1" enum.
 */
fun sendIngredientMessage(player: Player, enumIndex: Int) {
    val realPouchId = enums.get("summoning_pouch_ids_1").getInt(enumIndex)
    val ingredientString = enums.get("summoning_pouch_crafting_ingredient_strings").getString(realPouchId)
    player.message(ingredientString)
}

/**
 * Opens the interface used for crafting summoning pouches.
 *
 * @param player: The [Player] to open the interface for
 */
fun openPouchCraftingInterface(player: Player) {
    player.interfaces.open("summoning_pouch_creation")
    player.sendScript(
        "populate_summoning_pouch_creation",
        InterfaceDefinition.pack(pouchInterfaceId, pouchComponentId),
        width,
        height,
        // TODO: Use starting/endingDungeoneeringIndex if the player is in a dungeon
        startingIndex,
        endingIndex,
        "Infuse<col=FF9040>",
        "Infuse-5<col=FF9040>",
        "Infuse-10<col=FF9040>",
        "Infuse-X<col=FF9040>",
        "Infuse-All<col=FF9040>",
        "List<col=FF9040>"
    )
    player.interfaceOptions.unlockAll("summoning_pouch_creation", "pouches", 0..400)
}

/**
 * Opens the interface used for crafting summoning scrolls.
 *
 * @param player: The [Player] to open the interface for
 */
fun openScrollCraftingInterface(player: Player) {
    player.interfaces.open("summoning_scroll_creation")
    player.sendScript(
        "populate_summoning_scroll_creation",
        InterfaceDefinition.pack(scrollInterfaceId, scrollComponentId),
        width,
        height,
        // TODO: Use starting/endingDungeoneeringIndex if the player is in a dungeon
        startingIndex,
        endingIndex,
        "Transform<col=FF9040>",
        "Transform-5<col=FF9040>",
        "Transform-10<col=FF9040>",
        "Transform-X<col=FF9040>",
        "Transform-All<col=FF9040>"
    )
    player.interfaceOptions.unlockAll("summoning_scroll_creation", "scrolls", 0..400)
}