package content.entity.player.effect

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import java.util.concurrent.TimeUnit

class AvasDevices(val floorItems: FloorItems) : Script {

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

        slotChanged("worn_equipment", EquipSlot.Chest) {
            if (it.item.def["material", ""] == "metal" || it.fromItem.def["material", ""] == "metal") {
                update(this)
            }
        }

        itemAdded("avas_*", "worn_equipment", EquipSlot.Cape) {
            update(this)
        }

        itemOption("Toggle", "avas_*", "*") {
            set("collect_junk", !get("collect_junk", false))
            update(this)
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
