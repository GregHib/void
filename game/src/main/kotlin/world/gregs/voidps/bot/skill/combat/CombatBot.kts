package world.gregs.voidps.bot.skill.combat

import net.pearx.kasechange.toLowerSpaceCase
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.bot.*
import world.gregs.voidps.bot.item.pickup
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.cancel
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.equip.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirements
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.skill.slayer.race
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackers
import world.gregs.voidps.world.interact.entity.combat.underAttack
import world.gregs.voidps.world.interact.entity.death.Death
import world.gregs.voidps.world.interact.entity.death.weightedSample
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

val areas: AreaDefinitions by inject()
val tasks: TaskManager by inject()
val floorItems: FloorItems by inject()

onBot<VariableSet>({ key == "under_attack" && to == 0 }) { bot ->
    bot.resume("combat")
}

onBot<CombatSwing> { bot ->
    val player = bot.player
    if (player.levels.getPercent(Skill.Constitution) < 50.0) {
        val food = player.inventory.items.firstOrNull { it.def.contains("heals") } ?: return@onBot
        bot.inventoryOption(food.id, "Eat")
    }
}

onBot<Death> { bot ->
    bot.clear("area")
    bot.cancel()
}

worldSpawn {
    for (area in areas.getTagged("combat_training")) {
        val spaces: Int = area["spaces", 1]
        val types = area["npcs", emptyList<String>()].toSet()
        val range = area["levels", "1-5"].toIntRange()
        val skills = listOf(Skill.Attack, Skill.Strength, Skill.Defence, Skill.Ranged, Skill.Magic).shuffled().take(spaces)
        for (skill in skills) {
            val task = Task(
                name = "train ${skill.name} killing ${types.joinToString(", ")} at ${area.name}".toLowerSpaceCase(),
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

suspend fun Bot.fight(map: AreaDefinition, skill: Skill, races: Set<String>) {
    setupGear(skill)
    goToArea(map)
    setAttackStyle(skill)
    while (player.inventory.spaces > 0 && player.isRangedNotOutOfAmmo(skill) && player.isMagicNotOutOfRunes(skill)) {
        val targets = get<NPCs>()
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
        target.get<Tile>("death_tile")?.let {
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
    return Spell.removeRequirements(this, spell)
}

suspend fun Bot.pickupItems(tile: Tile, amount: Int) {
    repeat(random.nextInt(2, 8)) {
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

fun Bot.isAvailableTarget(map: AreaDefinition, npc: NPC, races: Set<String>): Boolean {
    if (!npc.tile.within(player.tile, Viewport.VIEW_RADIUS)) {
        return false
    }
    if (player.attackers.isNotEmpty()) {
        return player.attackers.contains(npc)
    }
    if (npc.underAttack) {
        return false
    }
    if (!npc.def.options.contains("Attack")) {
        return false
    }
    if (!races.contains(npc.def.name.toSnakeCase()) && !races.contains(npc.race)) {
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
            val ammoDefinitions: AmmoDefinitions = get()
            player.inventory.items
                .firstOrNull { player.hasRequirements(it) && ammoDefinitions.get(weapon.def["ammo_group", ""]).items.contains(it.id) }
                ?.let {
                    equip(it.id)
                }
        } else if (player.inventory.contains(ammo.id)) {
            equip(ammo.id)
        }
    }
}