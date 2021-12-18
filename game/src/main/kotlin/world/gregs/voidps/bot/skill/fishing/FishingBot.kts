import world.gregs.voidps.bot.Task
import world.gregs.voidps.bot.TaskManager
import world.gregs.voidps.bot.hasCoins
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.skill.combat.hasExactGear
import world.gregs.voidps.bot.skill.combat.setupGear
import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.hasItem
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.GearDefinitions
import world.gregs.voidps.engine.entity.definition.config.GearDefinition
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.engine.utility.weightedSample
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.world.activity.skill.fishing.spot.FishingSpot
import world.gregs.voidps.world.activity.skill.fishing.spot.RegularFishingSpot
import world.gregs.voidps.world.activity.skill.fishing.tackle.Bait

val areas: Areas by inject()
val tasks: TaskManager by inject()
val gear: GearDefinitions by inject()

on<ActionFinished>({ type == ActionType.Fishing }) { bot: Bot ->
    bot.resume("fishing")
}

on<World, Startup> {
    for (area in areas.getTagged("fish")) {
        val spaces: Int = area["spaces", 1]
        val type = RegularFishingSpot.values().firstOrNull { area.tags.contains(it.id) } ?: continue
        val sets = gear.get("fishing").filter { it["spot", ""] == type.id }
        for (set in sets) {
            val option = set["action", ""]
            val baitName = set.inventory.firstOrNull { it.amount > 1 }?.id ?: "none"
            val bait = Bait.values().firstOrNull { it.id == baitName  } ?: Bait.None
            val task = Task(
                name = "fish ${type.id.plural(2).lowercase()} at ${area.name}".replace("_", " "),
                block = {
                    while (player.levels.getMax(Skill.Fishing) < set.levels.last + 1) {
                        fish(area, type, option, bait, set)
                    }
                },
                area = area.area,
                spaces = spaces,
                requirements = listOf(
                    { player.levels.getMax(Skill.Fishing) in set.levels },
                    { hasExactGear(set) || hasCoins(2000) }
                )
            )
            tasks.register(task)
        }
    }
}

suspend fun Bot.fish(map: MapArea, type: FishingSpot, option: String, bait: Bait, set: GearDefinition) {
    setupGear(set)
    goToArea(map)
    while (player.inventory.isNotFull() && (bait == Bait.None || player.hasItem(bait.id))) {
        val spots = player.viewport.npcs.current
            .filter { isAvailableSpot(map, it, type, option, bait) }
            .map { it to tile.distanceTo(it) }
        val spot = weightedSample(spots, invert = true)
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