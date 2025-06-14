package content.bot.skill.combat

import content.bot.*
import content.bot.interact.bank.withdrawAll
import content.bot.interact.navigation.await
import content.bot.interact.navigation.cancel
import content.bot.interact.navigation.goToArea
import content.entity.combat.attackers
import content.entity.combat.inCombat
import content.entity.player.bank.ownsItem
import content.skill.magic.spell.spellBook
import content.skill.melee.weapon.attackRange
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

val areas: AreaDefinitions by inject()
val tasks: TaskManager by inject()

worldSpawn {
    val area = areas.getOrNull("lumbridge_combat_tutors") ?: return@worldSpawn
    val range = 1..5
    val skills = listOf(Skill.Attack, Skill.Magic, Skill.Ranged)
    val melees = listOf(Skill.Attack, Skill.Strength, Skill.Defence)
    for (skill in skills) {
        val melee = skill == Skill.Attack
        val task = Task(
            name = "train ${if (melee) "melee" else skill.name} at ${area.name}".toLowerSpaceCase(),
            block = {
                val actualSkill = if (melee) melees.filter { levels.getMax(it) in range }.random() else skill
                bot.train(area, actualSkill, range)
            },
            area = area.area,
            spaces = if (melee) 3 else 2,
            requirements = listOf(
                { if (melee) melees.any { levels.getMax(it) in range } else levels.getMax(skill) in range },
                { bot.canGetGearAndAmmo(skill) },
            ),
        )
        tasks.register(task)
    }
}

suspend fun Bot.train(map: AreaDefinition, skill: Skill, range: IntRange) {
    setupGear(map, skill)
    if (skill == Skill.Magic) {
        setAutoCast("wind_strike")
    } else {
        player.clear("autocast")
        setAttackStyle(skill)
    }
    var target: Any? = null
    while (target == null) {
        await("tick")
        target = if (skill == Skill.Ranged) {
            getObjects { it.id == "archery_target" }
                .randomOrNull()
        } else {
            get<NPCs>()
                .filter { isAvailableTarget(map, it, skill) }
                .randomOrNull()
        }
    }
    if (target is NPC) {
        if (!player.tile.within(target.tile, player.attackRange + 1)) {
            player.walkTo(target.tile)
            await("move")
        }
    }
    while (player.levels.getMax(skill) < range.last + 1 && hasAmmo(skill)) {
        if (target is GameObject) {
            objectOption(target, "Shoot-at")
            await<Player, ObjectOption<Player>>()
            await("tick")
        } else if (target is NPC) {
            npcOption(target, "Attack")
            while (player.mode is CombatMovement) {
                await("tick")
            }
            await("tick")
        }
    }
}

suspend fun Bot.setupGear(area: AreaDefinition, skill: Skill) {
    when (skill) {
        Skill.Magic -> {
            withdrawAll("air_rune", "mind_rune")
            goToArea(area)
            if (!player.inventory.contains("air_rune") && !player.inventory.contains("mind_rune")) {
                claim("mikasi")
            }
            if (!player.inventory.contains("air_rune") || !player.inventory.contains("mind_rune")) {
                cancel()
                return
            }
        }
        Skill.Ranged -> {
            withdrawAll("training_bow", "training_arrows")
            goToArea(area)
            if (!player.inventory.contains("training_bow") || !player.inventory.contains("training_arrows")) {
                claim("nemarti")
            }
            equip("training_bow")
            equip("training_arrows")
        }
        else -> {
            withdrawAll("training_sword", "training_shield")
            goToArea(area)
            if (!player.inventory.contains("training_sword")) {
                val tutor = get<NPCs>().first { it.tile.within(player.tile, Viewport.VIEW_RADIUS) && it.id == "harlan" }
                npcOption(tutor, "Talk-to")
                await<Player, InterfaceOpened> { id.startsWith("dialogue_") }
                await("tick")
                dialogueOption("continue")
                dialogueOption("line4")
                dialogueOption("continue")
                dialogueOption("continue")
                dialogueOption("continue")
            }
            equip("training_sword")
            equip("training_shield")
        }
    }
}

suspend fun Bot.claim(npc: String) {
    val tutor = get<NPCs>().first { it.tile.within(player.tile, Viewport.VIEW_RADIUS) && it.id == npc }
    npcOption(tutor, "Talk-to")
    await<Player, InterfaceOpened> { id.startsWith("dialogue_") }
    await("tick")
    dialogueOption("continue")
    dialogueOption("line3")
    dialogueOption("continue")
    dialogueOption("continue")
}

fun Bot.isAvailableTarget(map: AreaDefinition, npc: NPC, skill: Skill): Boolean {
    if (!npc.tile.within(player.tile, Viewport.VIEW_RADIUS)) {
        return false
    }
    if (npc.inCombat && !npc.attackers.contains(player)) {
        return false
    }
    if (!npc.def.options.contains("Attack")) {
        return false
    }
    if (!map.area.contains(npc.tile)) {
        return false
    }
    return npc.id == if (skill == Skill.Magic) "magic_dummy" else "melee_dummy"
}

fun Bot.canGetGearAndAmmo(skill: Skill): Boolean = when (skill) {
    Skill.Magic -> (player.ownsItem("air_rune") && player.ownsItem("mind_rune")) || player.remaining("claimed_tutor_consumables", epochSeconds()) <= 0 && player.spellBook == "modern_spellbook"
    Skill.Ranged -> (player.ownsItem("training_bow") && (player.ownsItem("training_arrows")) || player.remaining("claimed_tutor_consumables", epochSeconds()) <= 0)
    else -> true
}

fun Bot.hasAmmo(skill: Skill): Boolean = when (skill) {
    Skill.Ranged -> player.has(EquipSlot.Ammo)
    Skill.Magic -> player.inventory.contains("air_rune") && player.inventory.contains("mind_rune")
    else -> true
}
