package content.skill.smithing

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.dialogue.type.intEntry
import content.entity.player.dialogue.type.statement
import content.quest.quest
import net.pearx.kasechange.toSentenceCase
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Smithing
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.weakQueue

class Anvil(val interfaceDefinitions: InterfaceDefinitions) : Script {

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
        "crossbow_limbs",
    )

    val logger = InlineLogger()

    init {
        interfaceOption(id = "smithing:*") {
            val metal: String = get("smithing_metal") ?: return@interfaceOption
            val component = it.component
            val type = component.substringBeforeLast('_')
            val amount = when {
                component.endsWith("_1") -> 1
                component.endsWith("_5") -> 5
                component.endsWith("_x") -> intEntry("Enter amount:")
                component.endsWith("_all") -> Int.MAX_VALUE
                else -> return@interfaceOption
            }
            smith(metal, type, amount)
        }

        itemOnObjectOperate("*_bar", "anvil*") {
            if (!inventory.contains("hammer")) {
                statement("You need a hammer to work the metal with.")
                return@itemOnObjectOperate
            }
            open("smithing")
            val bars = inventory.count(it.item.id)
            val metal = it.item.id.removeSuffix("_bar")
            set("smithing_metal", metal)
            interfaces.sendText("smithing", "title", "${metal.toSentenceCase()} Smithing")
            for (type in types) {
                val componentDefinition = interfaceDefinitions.getComponent("smithing", type)
                val itemDefinition = ItemDefinitions.get("${metal}_$type")
                val id = itemDefinition.id
                if (id != -1) {
                    val amount = componentDefinition?.getOrNull("amount") ?: 1
                    interfaces.sendItem("smithing", type, id, amount)
                    val smithing: Smithing = itemDefinition["smithing"]
                    interfaces.sendColour("smithing", "${type}_name", if (has(Skill.Smithing, smithing.level)) Colours.WHITE else Colours.BLACK)
                }
                val required = componentDefinition?.getOrNull("bars") ?: 1
                interfaces.sendColour("smithing", "${type}_bar", if (bars < required) Colours.ORANGE else Colours.GREEN)
            }

            interfaces.sendVisibility("smithing", "wire_bronze", metal == "bronze")
            interfaces.sendVisibility("smithing", "spit_iron", metal == "iron")
            interfaces.sendVisibility("smithing", "studs_steel", metal == "steel")
            interfaces.sendVisibility("smithing", "bullseye_lantern", metal == "steel")
            interfaces.sendItem("smithing", "lantern", ItemDefinitions.get("bullseye_lantern_frame").id, 1)
            interfaces.sendVisibility("smithing", "grapple", metal == "mithril")
            interfaces.sendVisibility("smithing", "darts", quest("tourist_trap") == "completed")
            interfaces.sendVisibility("smithing", "claw", quest("death_plateau") == "completed")
            interfaces.sendVisibility("smithing", "pickaxes", quest("perils_of_ice_mountain") == "completed")
        }

        itemOnObjectOperate("hammer", "anvil*", arrive = false) {
            message("To smith metal equipment, you must use the metal bar on the anvil.")
        }

        interfaceClosed("smithing") {
            sendScript("clear_dialogues")
        }
    }

    suspend fun Player.smith(metal: String, type: String, amount: Int) {
        val item = if (metal == "steel" && type == "lantern") {
            "bullseye_lantern_frame"
        } else if (metal == "mithril" && type == "grapple") {
            "mithril_grapple_tip"
        } else {
            "${metal}_$type"
        }
        val itemDefinition = ItemDefinitions.get(item)
        val smithing: Smithing = itemDefinition.getOrNull("smithing") ?: return
        val component = interfaceDefinitions.getComponent("smithing", type)
        val quantity = component?.getOrNull("amount") ?: 1
        val bars = component?.getOrNull("bars") ?: 1
        val bar = "${metal}_bar"
        val actualAmount = amount.coerceAtMost(inventory.count(bar) / bars)
        closeMenu()
        softTimers.start("smithing")
        if (actualAmount <= 0) {
            statement("You don't have enough $metal bars to make a $type.")
            softTimers.stop("smithing")
            return
        }
        smith(smithing, metal, bars, quantity, type, item, actualAmount, true)
    }

    suspend fun Player.smith(
        smithing: Smithing,
        metal: String,
        bars: Int,
        quantity: Int,
        type: String,
        item: String,
        count: Int,
        first: Boolean,
    ) {
        if (count <= 0) {
            softTimers.stop("smithing")
            return
        }
        if (!inventory.contains("hammer")) {
            message("You need a Hammer to smith items.")
            softTimers.stop("smithing")
            return
        }

        if (!has(Skill.Smithing, smithing.level, message = false)) {
            val name = item.removeSuffix("_unf")
            statement("You need a Smithing level of ${smithing.level} to make${name.an()} ${name.toTitleCase()}.")
            softTimers.stop("smithing")
            return
        }

        val bar = "${metal}_bar"
        anim("smith_item")
        weakQueue("smithing", if (first) 0 else 5) {
            inventory.transaction {
                remove(bar, bars)
                add(item, quantity)
            }
            when (inventory.transaction.error) {
                is TransactionError.Deficient -> message("You do not have enough bars to smith this item.")
                TransactionError.None -> {
                    exp(Skill.Smithing, smithing.xp)
                    smith(smithing, metal, bars, quantity, type, item, count - 1, false)
                    val name = type.removeSuffix("_unf").replace("_", " ")
                    message("You hammer the $metal and make${name.an()} $name.")
                }
                else -> logger.warn { "Error smithing ${this@smith} $item ${inventory.transaction.error} ${inventory.items.contentToString()}" }
            }
        }
    }
}
