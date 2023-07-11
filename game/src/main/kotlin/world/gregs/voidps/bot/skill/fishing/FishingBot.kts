package world.gregs.voidps.bot.skill.fishing

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.bot.*
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.skill.combat.hasExactGear
import world.gregs.voidps.bot.skill.combat.setupGear
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.data.config.GearDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.GearDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Catch
import world.gregs.voidps.engine.data.definition.data.Spot
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.world.interact.entity.death.weightedSample

val areas: AreaDefinitions by inject()
val tasks: TaskManager by inject()
val gear: GearDefinitions by inject()

onBot<TimerStop>({ timer == "fishing" }) { bot: Bot ->
    bot.resume(timer)
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
                name = "fish ${type.plural(2)} at ${area.name}".toLowerSpaceCase(),
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

suspend fun Bot.fish(map: AreaDefinition, option: String, bait: String, set: GearDefinition) {
    setupGear(set)
    goToArea(map)
    while (player.inventory.spaces > 0 && (bait == "none" || player.hasItem(bait))) {
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

fun Bot.isAvailableSpot(map: AreaDefinition, npc: NPC, option: String, bait: String): Boolean {
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
    val itemDefinitions: ItemDefinitions = get()
    val level = spot[option]?.bait?.get(bait)
        ?.minOf { itemDefinitions.get(it)["fishing", Catch.EMPTY].level }
        ?: return false
    return player.has(Skill.Fishing, level, false)
}