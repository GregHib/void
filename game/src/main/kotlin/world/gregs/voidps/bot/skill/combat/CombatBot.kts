import world.gregs.voidps.bot.Task
import world.gregs.voidps.bot.TaskManager
import world.gregs.voidps.bot.bank.depositAll
import world.gregs.voidps.bot.bank.openBank
import world.gregs.voidps.bot.hasCoins
import world.gregs.voidps.bot.inventoryOption
import world.gregs.voidps.bot.item.pickup
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.skill.combat.setAttackStyle
import world.gregs.voidps.bot.skill.combat.setupCombatGear
import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.combatLevel
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.engine.utility.weightedSample
import world.gregs.voidps.network.instruct.InteractNPC
import kotlin.random.Random

val areas: Areas by inject()
val tasks: TaskManager by inject()
val floorItems: FloorItems by inject()
val definitions: NPCDefinitions by inject()

on<ActionFinished>({ type == ActionType.Combat }) { bot: Bot ->
    bot.resume("combat")
}

on<World, Startup> {
    for (area in areas.getTagged("combat_training")) {
        val spaces = area.tags.firstOrNull { it.startsWith("spaces_") }?.removePrefix("spaces_")?.toIntOrNull() ?: 1
        val types = area.tags.filterNot { it.startsWith("spaces_") || it == "combat_training" }.toSet()
        val combatLevel = types.minOf { definitions.get(it).combat }
        val minLevel = (combatLevel - 1) * 5
        val targetLevel = combatLevel * 5
        val range = minLevel until targetLevel
        val skill = Skill.Attack

        val task = Task(
            name = "train ${skill.name.toLowerCase()} killing ${types.joinToString(", ")} at ${area.name}".replace("_", " "),
            block = {
                while (player.levels.getMax(skill) < range.last + 1) {
                    fight(area, skill, types)
                }
            },
            area = area.area,
            spaces = spaces,
            requirements = listOf(
                { player.levels.getMax(skill) in range },
                { hasUsableWeaponAndAmmo(skill) || hasCoins(1000) }
            )
        )
        tasks.register(task)
    }
}

suspend fun Bot.fight(map: MapArea, skill: Skill, races: Set<String>) {
    setupCombatGear(skill, races)
    goToArea(map)
    setAttackStyle(skill)
    while (player.inventory.isNotFull() /*&& (player.equipped(EquipSlot.Weapon)ammo == null || player.hasItem(ammo))*/) {
        val targets = player.viewport.npcs.current
            .filter { isAvailableTarget(map, it, races) }
            .map { it to tile.distanceTo(it) }
        val target = weightedSample(targets, invert = true)
        if (target == null) {
            await("tick")
            if (player.inventory.spaces < 4) {
                break
            }
            continue
        }
        player.instructions.emit(InteractNPC(target.index, target.def.options.indexOf("Attack") + 1))
        await("combat")
        val tile = target["death_tile", target.tile]
        pickupItems(tile, 4)
    }
    openBank()
    depositAll()
}


suspend fun Bot.pickupItems(tile: Tile, amount: Int) {
    repeat(Random.nextInt(2, 8)) {
        if (player.inventory.contains("bones")) {
            inventoryOption("bones", "Bury")
            await("tick")
        }
        await("tick")
    }
    repeat(amount) {
        val item = floorItems[tile].firstOrNull() ?: return@repeat
        pickup(item)
    }
}

fun Bot.isAvailableTarget(map: MapArea, npc: NPC, races: Set<String>): Boolean {
    if (npc.hasEffect("in_combat")) {
        return false
    }
    if (!npc.def.options.contains("Attack")) {
        return false
    }
    if (!races.contains(npc.def.name.toUnderscoreCase()) && !races.contains(npc.def["race", ""])) {
        return false
    }
    if (!map.area.contains(npc.tile)) {
        return false
    }
    val difference = npc.def.combat - player.combatLevel
    return difference < 5
}


fun Bot.hasUsableWeaponAndAmmo(skill: Skill): Boolean {
    return skill == Skill.Attack || skill == Skill.Strength || skill == Skill.Defence
}