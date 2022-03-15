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
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.hasItem
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.GearDefinitions
import world.gregs.voidps.engine.entity.definition.config.GearDefinition
import world.gregs.voidps.engine.entity.definition.data.Spot
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.engine.utility.weightedSample
import world.gregs.voidps.network.instruct.InteractNPC

val areas: Areas by inject()
val tasks: TaskManager by inject()
val gear: GearDefinitions by inject()

on<ActionFinished>({ type == ActionType.Fishing }) { bot: Bot ->
    bot.resume("fishing")
}

on<World, Registered> {
    for (area in areas.getTagged("fish")) {
        val spaces: Int = area["spaces", 1]
        val type: String = area.getOrNull("type") ?: continue
        val sets = gear.get("fishing").filter { it["spot", ""] == type }
        for (set in sets) {
            val option = set["action", ""]
            val bait = set.inventory.firstOrNull { it.first().amount > 1 }?.first()?.id ?: "none"
            val task = Task(
                name = "fish ${type.plural(2).lowercase()} at ${area.name}".replace("_", " "),
                block = {
                    while (player.levels.getMax(Skill.Fishing) < set.levels.last + 1) {
                        fish(area, option, bait, set)
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

suspend fun Bot.fish(map: MapArea, option: String, bait: String, set: GearDefinition) {
    setupGear(set)
    goToArea(map)
    while (player.inventory.isNotFull() && (bait == "none" || player.hasItem(bait))) {
        val spots = get<NPCs>()
            .filter { isAvailableSpot(map, it, option, bait) }
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

fun Bot.isAvailableSpot(map: MapArea, npc: NPC, option: String, bait: String): Boolean {
    if (!npc.tile.within(player.tile, Viewport.VIEW_RADIUS)) {
        return false
    }
    if (!map.area.contains(npc.tile)) {
        return false
    }
    if (!npc.def.options.contains(option)) {
        return false
    }
    val spot: Map<String, Spot> = npc.def["fishing", emptyMap()]
    val level = spot[option]?.minimumLevel(bait) ?: return false
    return player.has(Skill.Fishing, level, false)
}