import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNpcClick
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Died
import world.gregs.voidps.engine.entity.character.move.cantReach
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.character.update.visual.Hit
import world.gregs.voidps.engine.entity.character.update.visual.hit
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy
import world.gregs.voidps.engine.path.strat.CombatTargetStrategy.Companion.isWithinAttackDistance
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.math.floor

on<NPCClick>({ option == "Attack" }) { player: Player ->
    cancel = true
    player.closeDialogue()
    player.attack(npc, firstHit = {
        player.clear("spell")
    })
}

on<InterfaceOnNpcClick>({ name.endsWith("_spellbook") }) { player: Player ->
    cancel = true
    if (player.action.type == ActionType.Combat && player.getOrNull<NPC>("target") == npc) {
        player.spell = component
        player["attack_range"] = 8
        player["attack_speed"] = 5
    } else {
        player.attack(npc, start = {
            player["attack_range"] = 8
        }, firstHit = {
            player.spell = component
            player["attack_speed"] = 5
        })
    }
}

on<CombatSwing> { character: Character ->
    target.start("in_combat", 16, restart = true)
    if (target.inSingleCombat) {
        target.attackers.clear()
    }
    target.attackers.add(character)
}

on<CombatHit>({ it is Player && it.getVar("auto_retaliate", false) || it is NPC }) { character: Character ->
    if (character.levels.get(Skill.Constitution) <= 0) {
        return@on
    }
    delay(character, 1) {
        character.attack(source)
    }
}

fun Character.attack(target: Character, start: () -> Unit = {}, firstHit: () -> Unit = {}) {
    val source = this
    action(ActionType.Combat) {
        source["target"] = target
        val handler = target.events.on<Character, Died> {
            source.stop("in_combat")
            cancel(ActionType.Combat)
        }
        try {
            watch(target)
            var first = true
            start.invoke()
            while (isActive && (source is NPC || source is Player && source.awaitDialogues())) {
                if (!withinRange(source, target)) {
                    delay()
                    continue
                } else if (source.remaining("skilling_delay") > 0L) {
                    delay()
                    continue
                } else if (!canAttack(source, target)) {
                    break
                }
                if (first) {
                    firstHit.invoke()
                    first = false
                }
                val swing = CombatSwing(target)
                face(target)
                events.emit(swing)
                val nextDelay = swing.delay
                if (nextDelay == null || nextDelay < 0) {
                    break
                }
                start("skilling_delay", nextDelay, quiet = true)
            }
        } finally {
            target.events.remove(handler)
            clear("target")
            watch(null)
        }
    }
}

fun withinRange(source: Character, target: Character): Boolean {
    val range = if (source is NPC) source.def["attack_range", 1] else source["attack_range", 1]
    val maxDistance = (range + if (source.attackStyle == "long_range") 2 else 0).coerceAtMost(10)
    val closeCombat = maxDistance == 1
    if (!isWithinAttackDistance(source, target, maxDistance + if (source.movement.moving) 1 else 0, closeCombat)) {
        if (source.movement.steps.isNotEmpty()) {
            return false
        }
        val strategy = CombatTargetStrategy(target, maxDistance, closeCombat)
        if (source is Player) {
            source.dialogues.clear()
        }
        source.movement.strategy = strategy
        source.movement.action = {
            if (source is Player && (source.cantReach(strategy) || source.movement.result == null)) {
                source.message("You can't reach that.")
            }
        }
        return false
    }
    return true
}

on<CombatAttack>({ damage > 0 }) { player: Player ->
    if (type == "spell" || type == "blaze") {
        val base = definitions.get(spell).experience
        if (player.getVar("defensive_cast", false)) {
            player.exp(Skill.Magic, base + damage / 7.5)
            player.exp(Skill.Defence, damage / 10.0)
        } else {
            player.exp(Skill.Magic, base + damage / 5.0)
        }
    } else if (type == "range") {
        if (player.attackType == "long_range") {
            player.exp(Skill.Range, damage / 5.0)
            player.exp(Skill.Defence, damage / 5.0)
        } else {
            player.exp(Skill.Range, damage / 2.5)
        }
    } else if (type == "melee") {
        when (player.attackStyle) {
            "accurate" -> player.exp(Skill.Attack, damage / 2.5)
            "aggressive" -> player.exp(Skill.Strength, damage / 2.5)
            "controlled" -> {
                player.exp(Skill.Attack, damage / 7.5)
                player.exp(Skill.Strength, damage / 7.5)
                player.exp(Skill.Defence, damage / 7.5)
            }
            "defensive" -> player.exp(Skill.Defence, damage / 2.5)
        }
    }
    player.exp(Skill.Constitution, damage / 7.5)
}

val definitions: SpellDefinitions by inject()

on<CombatHit>({ damage >= 0 && !(type == "spell" && definitions.get(spell).maxHit == -1) }) { character: Character ->
    var damage = damage
    var soak = 0
    if (damage > 200) {
        val percent = when (type) {
            "melee" -> character["absorb_melee", 0] / 100.0
            "range" -> character["absorb_range", 0] / 100.0
            "spell" -> character["absorb_magic", 0] / 100.0
            else -> 0.0
        }
        soak = floor((damage - 200) * percent).toInt()
        damage -= soak
    }
    if (soak <= 0) {
        soak = -1
    }
    val dealers = character.get<MutableMap<Character, Int>>("damage_dealers")
    dealers[source] = dealers.getOrDefault(source, 0) + damage
    character.hit(
        source = source,
        amount = damage,
        mark = when (type) {
            "range" -> Hit.Mark.Range
            "melee" -> Hit.Mark.Melee
            "spell" -> Hit.Mark.Magic
            "poison" -> Hit.Mark.Poison
            "dragonfire", "damage" -> Hit.Mark.Regular
            else -> Hit.Mark.Missed
        },
        critical = (type == "melee" || type == "spell" || type == "range") && damage > (source["max_hit", 0] * 0.9),
        soak = soak
    )
    character.levels.drain(Skill.Constitution, damage)
}

on<CombatHit> { character: Character ->
    val name = (character as? NPC)?.def?.getOrNull("category") ?: "player"
    if (source is Player) {
        source.playSound("${name}_hit", delay = 40)
    }
    character.setAnimation("${name}_hit")
}