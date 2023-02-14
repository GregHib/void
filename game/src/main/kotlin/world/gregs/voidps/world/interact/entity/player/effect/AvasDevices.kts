package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.contain.ItemChanged
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.stopTimer
import world.gregs.voidps.engine.timer.timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption
import java.util.concurrent.TimeUnit

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
    "toy_mouse"
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
    "toy_mouse"
)

val floorItems: FloorItems by inject()

on<EffectStart>({ effect == "junk_collection" }) { player: Player ->
    player.timer("collect_junk", TimeUnit.SECONDS.toTicks(90)) {
        val junk = if (player.equipped(EquipSlot.Cape).id == "avas_attractor") attractor else accumulator
        val item = junk.random()
        if (!player.inventory.add(item)) {
            floorItems.add(item, 1, player.tile, 100, 200, player)
        }
    }
}

on<EffectStop>({ effect == "junk_collection" }) { player: Player ->
    player.stopTimer("collect_junk")
}

on<Registered> { player: Player ->
    update(player)
}

on<ItemChanged>({
    container == "worn_equipment" &&
            (index == EquipSlot.Chest.index && (item.def["material", ""] == "metal" || oldItem.def["material", ""] == "metal")) ||
            (index == EquipSlot.Cape.index && (oldItem.id.startsWith("avas_") || item.id.startsWith("avas_")))
}) { player: Player ->
    update(player)
}

on<ContainerOption>({ (container == "inventory" || container == "worn_equipment") && option == "Toggle" && item.id.startsWith("avas_") }) { player: Player ->
    player["collect_junk", true] = !player["collect_junk", false]
    update(player)
}

fun update(player: Player) {
    if (player["collect_junk", false] && player.equipped(EquipSlot.Cape).id.startsWith("avas_") && player.equipped(EquipSlot.Chest).def["material", ""] != "metal") {
        player.start("junk_collection")
    } else if (player.hasEffect("junk_collection")) {
        player.stop("junk_collection")
    }
}