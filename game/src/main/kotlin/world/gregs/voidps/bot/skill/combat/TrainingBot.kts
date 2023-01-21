import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.bot.*
import world.gregs.voidps.bot.bank.withdrawAll
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.bot.navigation.cancel
import world.gregs.voidps.bot.navigation.goToArea
import world.gregs.voidps.bot.skill.combat.setAttackStyle
import world.gregs.voidps.bot.skill.combat.setAutoCast
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectClick
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.activity.bank.hasBanked
import world.gregs.voidps.world.interact.entity.combat.attackRange
import world.gregs.voidps.world.interact.entity.combat.attackers
import world.gregs.voidps.world.interact.entity.combat.spellBook

val areas: Areas by inject()
val tasks: TaskManager by inject()

on<World, Registered> {
    val area = areas["lumbridge_combat_tutors"] ?: return@on
    val range = 1..5
    val skills = listOf(Skill.Attack, Skill.Magic, Skill.Ranged)
    val melees = listOf(Skill.Attack, Skill.Strength, Skill.Defence)
    for (skill in skills) {
        val melee = skill == Skill.Attack
        val task = Task(
            name = "train ${if (melee) "melee" else skill.name} at ${area.name}".toLowerSpaceCase(),
            block = {
                val skill = if (melee) melees.filter { player.levels.getMax(it) in range }.random() else skill
                train(area, skill, range)
            },
            area = area.area,
            spaces = if (melee) 3 else 1,
            requirements = listOf(
                { if (melee) melees.any { player.levels.getMax(it) in range } else player.levels.getMax(skill) in range },
                { canGetGearAndAmmo(skill) }
            )
        )
        tasks.register(task)
    }
}

suspend fun Bot.train(map: MapArea, skill: Skill, range: IntRange) {
    setupGear(map, skill)
    if (skill == Skill.Magic) {
        setAutoCast("wind_strike")
    } else {
        player.clearVar("autocast")
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
            player.queue.clearWeak()
            player.walkTo(target.tile)
        }
    }
    while (player.levels.getMax(skill) < range.last + 1 && hasAmmo(skill)) {
        if (target is GameObject) {
            objectOption(target, "Shoot-at")
            await<Player, ObjectClick>()
            await("tick")
        } else if (target is NPC) {
            npcOption(target, "Attack")
//            await<Player, ActionStarted> { type == ActionType.Combat }
            await("tick")
        }
    }
}


suspend fun Bot.setupGear(area: MapArea, skill: Skill) {
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

fun Bot.isAvailableTarget(map: MapArea, npc: NPC, skill: Skill): Boolean {
    if (!npc.tile.within(player.tile, Viewport.VIEW_RADIUS)) {
        return false
    }
    if (npc.hasEffect("in_combat") && !npc.attackers.contains(player)) {
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


fun Bot.canGetGearAndAmmo(skill: Skill): Boolean {
    return when (skill) {
        Skill.Magic -> (player.hasBanked("air_rune") && player.hasBanked("mind_rune")) || !player.hasEffect("claimed_tutor_consumables") && player.spellBook == "modern_spellbook"
        Skill.Ranged -> (player.hasBanked("training_bow") && (player.hasBanked("training_arrows")) || !player.hasEffect("claimed_tutor_consumables"))
        else -> true
    }
}

fun Bot.hasAmmo(skill: Skill): Boolean = when (skill) {
    Skill.Ranged -> player.has(EquipSlot.Ammo)
    Skill.Magic -> player.inventory.contains("air_rune") && player.inventory.contains("mind_rune")
    else -> true
}
