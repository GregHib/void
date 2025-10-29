package content.entity.player.effect

import content.entity.player.inv.inventoryItem
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.inventoryChanged
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import java.util.concurrent.TimeUnit

@Script
class AvasDevices : Api {

    val attractor = setOf(
        "iron_arrow",
        "iron_ore",
        "iron_med_helm",
        "iron_full_helm",
        "iron_dart",
        "iron_dart_p",
        "iron_dart_p+",
        "iron_dart_p++",
        "iron_knife",
        "iron_knife_p",
        "iron_knife_p+",
        "iron_knife_p++",
        "iron_bar",
        "iron_bolts",
        "iron_bolts_p",
        "iron_bolts_p+",
        "iron_bolts_p++",
        "iron_arrowtips",
        "steel_med_helm",
        "steel_full_helm",
        "toy_mouse",
    )

    val accumulator = setOf(
        "steel_arrow",
        "iron_ore",
        "steel_med_helm",
        "steel_full_helm",
        "steel_dart",
        "steel_dart_p",
        "steel_dart_p+",
        "steel_dart_p++",
        "steel_knife",
        "steel_knife_p",
        "steel_knife_p+",
        "steel_knife_p++",
        "steel_bar",
        "steel_bolts",
        "steel_bolts_p",
        "steel_bolts_p+",
        "steel_bolts_p++",
        "steel_arrowtips",
        "steel_med_helm",
        "steel_full_helm",
        "steel_hatchet",
        "steel_nails",
        "broken_arrow",
        "toy_mouse",
    )

    val floorItems: FloorItems by inject()

    init {
        playerSpawn(::update)

        timerStart("junk_collection") { TimeUnit.SECONDS.toTicks(90) }

        timerTick("junk_collection") {
            val junk = if (equipped(EquipSlot.Cape).id == "avas_attractor") attractor else accumulator
            val item = junk.random()
            if (!inventory.add(item)) {
                floorItems.add(tile, item, revealTicks = 100, disappearTicks = 200, owner = this)
            }
            return@timerTick Timer.CONTINUE
        }

        inventoryChanged("worn_equipment", EquipSlot.Chest) { player ->
            if (item.def["material", ""] == "metal" || fromItem.def["material", ""] == "metal") {
                update(player)
            }
        }

        itemAdded("avas_*", EquipSlot.Cape, "worn_equipment") { player ->
            update(player)
        }

        inventoryItem("Toggle", "avas_*", "worn_equipment") {
            player["collect_junk"] = !player["collect_junk", false]
            update(player)
        }

        inventoryItem("Toggle", "avas_*", "inventory") {
            player["collect_junk"] = !player["collect_junk", false]
            update(player)
        }
    }

    fun update(player: Player) {
        if (player["collect_junk", false] && player.equipped(EquipSlot.Cape).id.startsWith("avas_") && player.equipped(EquipSlot.Chest).def["material", ""] != "metal") {
            player.timers.startIfAbsent("junk_collection")
        } else {
            player.timers.stop("junk_collection")
        }
    }
}
