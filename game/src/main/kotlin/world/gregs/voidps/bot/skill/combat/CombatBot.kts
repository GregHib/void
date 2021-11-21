import world.gregs.voidps.bot.*
import world.gregs.voidps.bot.bank.closeBank
import world.gregs.voidps.bot.bank.depositAll
import world.gregs.voidps.bot.bank.openBank
import world.gregs.voidps.bot.item.pickup
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.skill.combat.setAttackStyle
import world.gregs.voidps.bot.skill.combat.setupCombatGear
import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.combatLevel
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.engine.utility.weightedSample
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.interact.entity.combat.ammo
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo.isBowOrCrossbow
import world.gregs.voidps.world.interact.entity.player.equip.hasRequirements
import kotlin.random.Random

val areas: Areas by inject()
val tasks: TaskManager by inject()
val floorItems: FloorItems by inject()

on<ActionFinished>({ type == ActionType.Combat }) { bot: Bot ->
    bot.resume("combat")
}

on<World, Startup> {
    for (area in areas.getTagged("combat_training")) {
        val spaces = area.tags.firstOrNull { it.startsWith("spaces_") }?.removePrefix("spaces_")?.toIntOrNull() ?: 1
        val types = area.tags.filterNot { it.startsWith("spaces_") || it.startsWith("level_range_") || it == "combat_training" }.toSet()
        val rangeString = area.tags.firstOrNull { it.startsWith("level_range_") }?.removePrefix("level_range_") ?: "1_5"
        val range = rangeString.split("_").first().toInt() until rangeString.split("_").last().toInt()
        for (skill in listOf(Skill.Attack, Skill.Range)) {
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
}

suspend fun Bot.fight(map: MapArea, skill: Skill, races: Set<String>) {
    setupCombatGear(skill, races)
    goToArea(map)
    setAttackStyle(skill)
    while (player.inventory.isNotFull() && player.isRangedNotOutOfAmmo(skill)) {
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

        if (skill == Skill.Range) {
            val ammo = player.equipped(EquipSlot.Ammo)
            if (player.inventory.contains(ammo.id)) {
                equip(ammo.id)
            }
        }

        /*
            TODO
                On death run back and pickup stuff
                food (food in banks to begin with)
         */

    }
    openBank()
    depositAll()
    closeBank()
}

fun Player.isRangedNotOutOfAmmo(skill: Skill): Boolean {
    if (skill != Skill.Range) {
        return true
    }
    val ammo = equipped(EquipSlot.Ammo)
    if (ammo.isEmpty()) {
        return false
    }
    return true
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
    if (skill == Skill.Range) {
        val all = player.bank.getItems()
            .union(player.equipment.getItems().toList())
            .union(player.inventory.getItems().toList())
        val weapons = all.filter(::isBowOrCrossbow)
        return all.any { item -> item.amount > 100 && player.hasRequirements(item) && weapons.any { it.def.ammo.contains(item.id) } }
    }
    return skill == Skill.Attack || skill == Skill.Strength || skill == Skill.Defence
}