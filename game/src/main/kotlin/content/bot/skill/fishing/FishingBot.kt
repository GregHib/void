package content.bot.skill.fishing

import content.bot.*
import content.bot.interact.navigation.await
import content.bot.interact.navigation.goToArea
import content.bot.interact.navigation.resume
import content.bot.skill.combat.hasExactGear
import content.bot.skill.combat.setupGear
import content.entity.death.weightedSample
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.data.config.GearDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.GearDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Catch
import world.gregs.voidps.engine.data.definition.data.Spot
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.network.client.instruction.InteractNPC
import world.gregs.voidps.engine.event.Script
@Script
class FishingBot {

    val areas: AreaDefinitions by inject()
    val tasks: TaskManager by inject()
    val gear: GearDefinitions by inject()
    
    init {
        timerStop("fishing") { player ->
            if (player.isBot) {
                player.bot.resume(timer)
            }
        }

        worldSpawn {
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
                            while (levels.getMax(Skill.Fishing) < set.levels.last + 1) {
                                bot.fish(area, option, bait, set)
                            }
                        },
                        area = area.area,
                        spaces = spaces,
                        requirements = listOf(
                            { levels.getMax(Skill.Fishing) in set.levels },
                            { bot.hasExactGear(set) || bot.hasCoins(2000) },
                        ),
                    )
                    tasks.register(task)
                }
            }
        }

    }

    suspend fun Bot.fish(map: AreaDefinition, option: String, bait: String, set: GearDefinition) {
        setupGear(set)
        goToArea(map)
        while (player.inventory.spaces > 0 && (bait == "none" || player.holdsItem(bait))) {
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
            player.instructions.send(InteractNPC(spot.index, spot.def.options.indexOf(option) + 1))
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
}
