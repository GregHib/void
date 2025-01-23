package world.gregs.voidps.world.activity.skill.smithing

import com.github.michaelbull.logging.InlineLogger
import net.pearx.kasechange.toSentenceCase
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Smithing
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.type.intEntry
import world.gregs.voidps.world.interact.dialogue.type.statement

val types = listOf(
    "dagger",
    "hatchet",
    "mace",
    "med_helm",
    "bolts_unf",
    "sword",
    "dart_tip",
    "nails",
    "wire",
    "spit",
    "studs",
    "arrowtips",
    "scimitar",
    "limbs",
    "longsword",
    "knife",
    "full_helm",
    "sq_shield",
    "grapple_tip",
    "warhammer",
    "battleaxe",
    "chainbody",
    "kiteshield",
    "claws",
    "2h_sword",
    "plateskirt",
    "platelegs",
    "platebody",
    "pickaxe",
    "crossbow_bolt",
    "crossbow_limbs"
)

val itemDefinitions: ItemDefinitions by inject()
val interfaceDefinitions: InterfaceDefinitions by inject()
val logger = InlineLogger()

val white = 32767
val black = 0
val orange = 30309
val green = 992

interfaceOption(id = "smithing") {
    val metal: String = player["smithing_metal"] ?: return@interfaceOption
    val type = component.substringBeforeLast('_')
    val amount = when {
        component.endsWith("_1") -> 1
        component.endsWith("_5") -> 5
        component.endsWith("_x") -> intEntry("Enter amount:")
        component.endsWith("_all") -> Int.MAX_VALUE
        else -> return@interfaceOption
    }
    smith(player, metal, type, amount)
}

itemOnObjectOperate("*_bar", "anvil*") {
    if (!player.inventory.contains("hammer")) {
        statement("You need a hammer to work the metal with.")
        return@itemOnObjectOperate
    }
    player.open("smithing")
    val bars = player.inventory.count(item.id)
    val metal = item.id.removeSuffix("_bar")
    player["smithing_metal"] = metal
    player.interfaces.sendText("smithing", "title", "${metal.toSentenceCase()} Smithing")
    for (type in types) {
        val componentDefinition = interfaceDefinitions.getComponent("smithing", type)
        val itemDefinition = itemDefinitions.get("${metal}_$type")
        val id = itemDefinition.id
        if (id != -1) {
            val amount = componentDefinition?.getOrNull("amount") ?: 1
            player.interfaces.sendItem("smithing", type, id, amount)
            val smithing: Smithing = itemDefinition["smithing"]
            player.interfaces.sendColour("smithing", "${type}_name", if (player.has(Skill.Smithing, smithing.level)) white else black)
        }
        val required = componentDefinition?.getOrNull("bars") ?: 1
        player.interfaces.sendColour("smithing", "${type}_bar", if (bars < required) orange else green)
    }

    player.interfaces.sendVisibility("smithing", "wire_bronze", metal == "bronze")
    player.interfaces.sendVisibility("smithing", "spit_iron", metal == "iron")
    player.interfaces.sendVisibility("smithing", "studs_steel", metal == "steel")
    player.interfaces.sendVisibility("smithing", "bullseye_lantern", metal == "steel")
    player.interfaces.sendItem("smithing", "lantern", itemDefinitions.get("bullseye_lantern_frame").id, 1)
    player.interfaces.sendVisibility("smithing", "grapple", metal == "mithril")
    player.interfaces.sendVisibility("smithing", "darts", player.quest("tourist_trap") == "completed")
    player.interfaces.sendVisibility("smithing", "claw", player.quest("death_plateau") == "completed")
    player.interfaces.sendVisibility("smithing", "pickaxes", player.quest("perils_of_ice_mountain") == "completed")
}


itemOnObjectOperate("hammer", "anvil*", arrive = false) {
    player.message("To smith metal equipment, you must use the metal bar on the anvil.")
}

interfaceClose("smithing") { player ->
    player.sendScript("clear_dialogues")
}

suspend fun SuspendableContext<Player>.smith(player: Player, metal: String, type: String, amount: Int) {
    val item = if (metal == "steel" && type == "lantern") {
        "bullseye_lantern_frame"
    } else if (metal == "mithril" && type == "grapple") {
        "mithril_grapple_tip"
    } else {
        "${metal}_$type"
    }
    val itemDefinition = itemDefinitions.get(item)
    val smithing: Smithing = itemDefinition.getOrNull("smithing") ?: return
    val component = interfaceDefinitions.getComponent("smithing", type)
    val quantity = component?.getOrNull("amount") ?: 1
    val bars = component?.getOrNull("bars") ?: 1
    val bar = "${metal}_bar"
    val actualAmount = amount.coerceAtMost(player.inventory.count(bar) / bars)
    player.closeMenu()
    player.softTimers.start("smithing")
    if (actualAmount <= 0) {
        statement("You don't have enough $metal bars to make a $type.")
        player.softTimers.stop("smithing")
        return
    }
    smith(smithing, metal, bars, quantity, type, item, actualAmount, true)
}

suspend fun SuspendableContext<Player>.smith(
    smithing: Smithing,
    metal: String,
    bars: Int,
    quantity: Int,
    type: String,
    item: String,
    count: Int,
    first: Boolean
) {
    if (count <= 0) {
        player.softTimers.stop("smithing")
        return
    }
    if (!player.inventory.contains("hammer")) {
        player.message("You need a Hammer to smith items.")
        player.softTimers.stop("smithing")
        return
    }

    if (!player.has(Skill.Smithing, smithing.level, message = false)) {
        val name = item.removeSuffix("_unf")
        statement("You need a Smithing level of ${smithing.level} to make${name.an()} ${name.toTitleCase()}.")
        player.softTimers.stop("smithing")
        return
    }

    val bar = "${metal}_bar"
    player.anim("smith_item")
    player.weakQueue("smithing", if (first) 0 else 5) {
        player.inventory.transaction {
            remove(bar, bars)
            add(item, quantity)
        }
        when (player.inventory.transaction.error) {
            is TransactionError.Deficient -> player.message("You do not have enough bars to smith this item.")
            TransactionError.None -> {
                player.exp(Skill.Smithing, smithing.xp)
                smith(smithing, metal, bars, quantity, type, item, count - 1, false)
                val name = type.removeSuffix("_unf").replace("_", " ")
                player.message("You hammer the $metal and make${name.an()} ${name}.")
            }
            else -> logger.warn { "Error smithing ${this@smith} $item ${player.inventory.transaction.error} ${player.inventory.items.contentToString()}" }
        }
    }
}