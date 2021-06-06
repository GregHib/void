import world.gregs.voidps.bot.Task
import world.gregs.voidps.bot.TaskManager
import world.gregs.voidps.bot.bank.*
import world.gregs.voidps.bot.buyItem
import world.gregs.voidps.bot.hasCoins
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.has
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.utility.func.plural
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.bank.has
import world.gregs.voidps.world.activity.skill.fishing.fish.Catch
import world.gregs.voidps.world.activity.skill.fishing.spot.FishingSpot
import world.gregs.voidps.world.activity.skill.fishing.spot.RegularFishingSpot
import world.gregs.voidps.world.activity.skill.fishing.tackle.Bait
import world.gregs.voidps.world.activity.skill.fishing.tackle.Tackle

val areas: Areas by inject()
val tasks: TaskManager by inject()

on<ActionFinished>({ type == ActionType.Fishing }) { bot: Bot ->
    bot.resume("fishing")
}

on<World, Startup> {
    for (area in areas.getTagged("fish")) {
        val spaces = area.tags.firstOrNull { it.startsWith("spaces_") }?.removePrefix("spaces_")?.toIntOrNull() ?: 1
        val type = RegularFishingSpot.values().firstOrNull { area.tags.contains(it.id) } ?: continue
        for ((key, values) in type.tackle) {
            for ((bait, catches) in values.second) {
                val range = when {
                    type == RegularFishingSpot.SmallNetBait && key == "Net" -> 0 until 20
                    type == RegularFishingSpot.SmallNetBait && key == "Bait" -> 5 until 20
                    type == RegularFishingSpot.LureBait && key == "Lure" -> if (bait == Bait.Feather) 20 until 40 else 38 until 40
                    type == RegularFishingSpot.LureBait && key == "Bait" -> 25 until 35
                    key == "Cage" -> 40 until 50
                    key == "Harpoon" -> 35 until 99
                    type == RegularFishingSpot.Crayfish && key == "Cage" -> 0 until 15
                    type == RegularFishingSpot.SmallNetHarpoon && key == "Net" -> 62 until 99
                    type == RegularFishingSpot.BigNetHarpoon && key == "Harpoon" -> 76 until 99
                    else -> continue
                }
                val task = Task(
                    name = "fish ${type.id.plural(2).toLowerCase()} at ${area.name}".replace("_", " "),
                    block = {
                        while (player.levels.getMax(Skill.Fishing) < range.last + 1) {
                            fish(area, type, key, bait)
                        }
                    },
                    area = area.area,
                    spaces = spaces,
                    requirements = listOf(
                        { player.levels.getMax(Skill.Fishing) in range },
                        { hasUsableTackleAndBait(values.first, bait, catches) || hasCoins(2000) }
                    )
                )
                tasks.register(task)
            }
        }
    }
}

suspend fun Bot.fish(map: MapArea, type: FishingSpot, option: String, bait: Bait) {
    setupInventory(type, option, bait)
    goToArea(map)
    while (player.inventory.isNotFull() && (bait == Bait.None || player.has(bait.id))) {
        val spot = player.viewport.npcs.current
            .filter { isAvailableSpot(map, it, type, option, bait) }
            .minByOrNull { spot -> tile.distanceTo(spot) }
        if (spot == null) {
            await("tick")
            if (player.inventory.spaces < 4) {
                break
            }
            continue
        }
        player.instructions.emit(InteractNPC(spot.index, spot.def.options.indexOf(option) + 1))
        await("fishing")
    }
}

fun Bot.isAvailableSpot(map: MapArea, npc: NPC, type: FishingSpot, option: String, bait: Bait): Boolean {
    if (!map.area.contains(npc.tile)) {
        return false
    }
    if (!npc.def.options.contains(option)) {
        return false
    }
    val spot = FishingSpot.get(npc) ?: return false
    if (type != spot) {
        return false
    }
    val catches = type.tackle[option]?.second?.get(bait) ?: return false
    val level: Int = catches.minOf { it.level }
    return player.has(Skill.Fishing, level, false)
}

suspend fun Bot.setupInventory(spot: FishingSpot, option: String, bait: Bait) {
    val (tackles, _) = spot.tackle[option] ?: return

    if (!tackles.any { tackle -> player.has(tackle.id) } && player.bank.getItems().none { item -> tackles.any { tackle -> tackle.id == item.name } }) {
        buyItem(tackles.first().id)
    }
    if (bait != Bait.None && !player.has(bait.id) && player.bank.getItems().none { item -> bait.id == item.name }) {
        buyItem(bait.id, 100)
    }

    val hasTackle = tackles.any { tackle -> player.has(tackle.id) }
    val hasBait = bait == Bait.None || player.has(bait.id)
    if (hasTackle && hasBait && player.inventory.spaces > 10) {
        return
    }
    val equipped = tackles.contains(Tackle.BarbTailHarpoon) && player.equipped(EquipSlot.Weapon).name == Tackle.BarbTailHarpoon.id
    openBank()
    depositAll()
    val tackle = player.bank.getItems()
        .firstOrNull { item -> tackles.any { tackle -> tackle.id == item.name } }
    if (!equipped && tackle != null) {
        withdraw(tackle.name)
    }
    val bait = player.bank.getItems()
        .firstOrNull { item -> bait.id == item.name }
    if (bait != null) {
        withdrawAll(bait.name)
    }
    closeBank()
}

fun Bot.hasUsableTackleAndBait(tackles: List<Tackle>, bait: Bait, catches: List<Catch>): Boolean {
    return tackles.any { tackle -> player.has(tackle.id, banked = true) } &&
            player.has(bait.id, banked = true) &&
            catches.any { catch -> player.has(Skill.Fishing, catch.level, message = false) }
}