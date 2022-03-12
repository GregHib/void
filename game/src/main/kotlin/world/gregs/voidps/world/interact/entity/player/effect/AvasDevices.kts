import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.Job
import world.gregs.voidps.engine.tick.delay
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.toTicks
import world.gregs.voidps.network.visual.EquipSlot
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
    player["collect_junk_job"] = player.delay(TimeUnit.SECONDS.toTicks(90), true) {
        val junk = if (player.equipped(EquipSlot.Cape).id == "avas_attractor") attractor else accumulator
        val item = junk.random()
        if (!player.inventory.add(item)) {
            floorItems.add(item, 1, player.tile, 100, 200, player)
        }
    }
}

on<EffectStop>({ effect == "junk_collection" }) { player: Player ->
    player.remove<Job>("collect_junk_job")?.cancel()
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