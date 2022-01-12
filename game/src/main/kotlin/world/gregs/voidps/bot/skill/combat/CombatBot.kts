import kotlinx.coroutines.CancellationException
import world.gregs.voidps.bot.*
import world.gregs.voidps.bot.item.pickup
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.cancel
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.skill.combat.hasExactGear
import world.gregs.voidps.bot.skill.combat.setAttackStyle
import world.gregs.voidps.bot.skill.combat.setupGear
import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.combatLevel
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.*
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.toIntRange
import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.engine.utility.weightedSample
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.ammo
import world.gregs.voidps.world.interact.entity.combat.attackers
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes
import kotlin.random.Random

val areas: Areas by inject()
val tasks: TaskManager by inject()
val floorItems: FloorItems by inject()

on<ActionFinished>({ type == ActionType.Combat }) { bot: Bot ->
    bot.resume("combat")
}

on<CombatSwing> { bot: Bot ->
    val player = bot.player
    if (player.levels.getPercent(Skill.Constitution) < 50.0) {
        val food = player.inventory.getItems().firstOrNull { it.def.has("heals") } ?: return@on
        bot.inventoryOption(food.id, "Eat")
    }
}

on<ActionFinished>({ type == ActionType.Dying }) { bot: Bot ->
    bot.clear("area")
    bot.cancel(CancellationException("Died."))
}

on<World, Startup> {
    for (area in areas.getTagged("combat_training")) {
        val spaces: Int = area["spaces", 1]
        val types = area["npcs", emptyList<String>()].toSet()
        val range = area["levels", "1-5"].toIntRange()
        val skills = listOf(Skill.Attack, Skill.Strength, Skill.Defence, Skill.Ranged, Skill.Magic).shuffled().take(spaces)
        for (skill in skills) {
            val task = Task(
                name = "train ${skill.name.lowercase()} killing ${types.joinToString(", ")} at ${area.name}".replace("_", " "),
                block = {
                    while (player.levels.getMax(skill) < range.last + 1) {
                        fight(area, skill, types)
                    }
                },
                area = area.area,
                spaces = 1,
                requirements = listOf(
                    { player.levels.getMax(skill) in range },
                    { hasExactGear(skill) || hasCoins(2000) }
                )
            )
            tasks.register(task)
        }
    }
}

suspend fun Bot.fight(map: MapArea, skill: Skill, races: Set<String>) {
    setupGear(skill)
    goToArea(map)
    setAttackStyle(skill)
    while (player.inventory.isNotFull() && player.isRangedNotOutOfAmmo(skill) && player.isMagicNotOutOfRunes(skill)) {
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
        npcOption(target, "Attack")
        await("combat", timeout = 30)
        target.getOrNull<Tile>("death_tile")?.let {
            pickupItems(it, 4)
        }
        equipAmmo(skill)
        // TODO on death run back and pickup stuff
    }
}

fun Player.isRangedNotOutOfAmmo(skill: Skill): Boolean {
    if (skill != Skill.Ranged) {
        return true
    }
    return has(EquipSlot.Ammo)
}

fun Player.isMagicNotOutOfRunes(skill: Skill): Boolean {
    if (skill != Skill.Magic) {
        return true
    }
    val spell = spell
    return Runes.hasSpellRequirements(this, spell)
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
    if (player.attackers.isNotEmpty()) {
        return player.attackers.contains(npc)
    }
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

fun Bot.equipAmmo(skill: Skill) {
    if (skill == Skill.Ranged) {
        val ammo = player.equipped(EquipSlot.Ammo)
        if (ammo.isEmpty()) {
            val weapon = player.equipped(EquipSlot.Weapon)
            player.inventory.getItems()
                .firstOrNull { player.hasRequirements(it) && weapon.def.ammo.contains(it.id) }
                ?.let {
                    equip(it.id)
                }
        } else if (player.inventory.contains(ammo.id)) {
            equip(ammo.id)
        }
    }
}