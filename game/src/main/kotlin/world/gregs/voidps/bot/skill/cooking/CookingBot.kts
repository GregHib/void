import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.bot.Task
import world.gregs.voidps.bot.TaskManager
import world.gregs.voidps.bot.clickInterface
import world.gregs.voidps.bot.getObject
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.skill.combat.getGear
import world.gregs.voidps.bot.skill.combat.getSuitableItem
import world.gregs.voidps.bot.skill.combat.hasExactGear
import world.gregs.voidps.bot.skill.combat.setupGear
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.config.GearDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.network.instruct.InteractDialogue
import world.gregs.voidps.network.instruct.InteractInterfaceObject

val areas: Areas by inject()
val tasks: TaskManager by inject()

on<EffectStop>({ effect == "cooking" }) { bot: Bot ->
    bot.resume(effect)
}

on<World, Registered> {
    for (area in areas.getTagged("cooking")) {
        val spaces: Int = area["spaces", 1]
        val type: String = area.getOrNull("type") ?: ""
        val task = Task(
            name = "cook on ${type.plural(2)} at ${area.name}".toLowerSpaceCase(),
            block = {
                val gear = getGear(Skill.Cooking) ?: return@Task
                val item = getSuitableItem(gear.inventory.first())
                while (player.levels.getMax(Skill.Cooking) < gear.levels.last + 1) {
                    cook(area, item, gear)
                }
            },
            area = area.area,
            spaces = spaces,
            requirements = listOf { hasExactGear(Skill.Cooking) }
        )
        tasks.register(task)
    }
}

suspend fun Bot.cook(map: MapArea, rawItem: Item, set: GearDefinition) {
    setupGear(set, buy = false)
    goToArea(map)
    if (player.inventory.contains(rawItem.id)) {
        val range = getObject { isRange(map, it) }
        if (range == null) {
            await("tick")
            return
        }
        // Use item on range
        player.instructions.emit(InteractInterfaceObject(range.def.id, range.tile.x, range.tile.y, 149, 0, rawItem.def.id, player.inventory.indexOf(rawItem.id)))
        await("tick")
        await("tick")
        if (rawItem.id == "raw_beef") {
            player.instructions.emit(InteractDialogue(228, 3, -1))
            await("tick")
        }
        // Select all
        clickInterface(916, 8, 0)
        await("tick")
        // First option
        player.instructions.emit(InteractDialogue(905, 14, -1))
    }
    var count = 0
    while (player.inventory.contains(rawItem.id)) {
        await("cooking")
        if (count++ > 28) {
            break
        }
    }
}

fun isRange(map: MapArea, obj: GameObject): Boolean {
    if (!map.area.contains(obj.tile)) {
        return false
    }
    return obj.id.startsWith("cooking_range")
}