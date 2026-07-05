package content.skill.summoning

import content.entity.combat.hit.directHit
import content.entity.effect.toxin.curePoison
import content.entity.effect.toxin.poison
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.queue.queue
import kotlin.math.ceil

/** Flight time (client ticks; 30 = 1 game tick) of the spirit scorpion's slow venom bolt to its owner. */
private const val VENOM_SHOT_FLIGHT = 60

/**
 * Instant self/owner boost and heal familiar specials (Stony Shell, Testudo, Magic Focus, Healing
 * Aura, Blood Drain, ...). Each registers as an [FamiliarSpecialMoves.instant] move and runs through
 * [castFamiliarSpecial], so a scroll + points are spent on use.
 */
class FamiliarBoostSpecials : Script {
    init {
        // Single-skill flat boosts.
        FamiliarSpecialMoves.instant("granite_crab_familiar") { boost(Skill.Defence, 4, "stony_shell", "stony_shell") }
        FamiliarSpecialMoves.instant("war_tortoise_familiar") { boost(Skill.Defence, 8, "testudo", "testudo", "testudo_owner") }
        FamiliarSpecialMoves.instant("obsidian_golem_familiar") { boost(Skill.Strength, 9, "volcanic_strength", "volcanic_strength") }
        FamiliarSpecialMoves.instant("wolpertinger_familiar") { boost(Skill.Magic, 7, "magic_focus", "magic_focus") }

        // Abyssal Stealth - two skills at once.
        FamiliarSpecialMoves.instant("abyssal_lurker_familiar") {
            familiarSelfSpecial(anim = "abyssal_stealth", sourceGfx = "abyssal_stealth", playerGfx = "abyssal_stealth_owner") {
                levels.boost(Skill.Agility, 4)
                levels.boost(Skill.Thieving, 4)
            }
        }

        // Unburden - restore run energy by ~half the Agility level.
        FamiliarSpecialMoves.instant("bull_ant_familiar") {
            familiarSelfSpecial(anim = "unburden", sourceGfx = "unburden") {
                restoreRunEnergy()
            }
        }

        // Tireless Run - Agility +2 and restore run energy by half the (boosted) Agility level.
        // Refuses, charging nothing, when run energy is already full.
        FamiliarSpecialMoves.instant("spirit_terrorbird_familiar") {
            if (runEnergy >= MAX_RUN_ENERGY) {
                message("You're already full run energy.")
                return@instant false
            }
            familiarSelfSpecial(anim = "tireless_run", sourceGfx = "tireless_run", playerGfx = "tireless_run_owner") {
                levels.boost(Skill.Agility, 2)
                runEnergy = (runEnergy + levels.get(Skill.Agility) * 50).coerceAtMost(MAX_RUN_ENERGY)
            }
        }

        // Elemental titans - Titan's Constitution: Defence +12.5% and heal a tenth of max life
        // points, each titan with its own colours. Refuses, charging nothing, at full health.
        for (tier in listOf("fire", "ice", "moss")) {
            FamiliarSpecialMoves.instant("${tier}_titan_familiar") {
                if (levels.get(Skill.Constitution) >= levels.getMax(Skill.Constitution)) {
                    message("You're already at full life points!")
                    return@instant false
                }
                familiarSelfSpecial(anim = "titans_constitution_$tier", sourceGfx = "titans_constitution_$tier", playerGfx = "titans_constitution_${tier}_owner") {
                    levels.boost(Skill.Defence, multiplier = 0.125)
                    levels.restore(Skill.Constitution, multiplier = 0.1)
                }
            }
        }

        // Healing Aura - heal the owner by 15% of their max life points. Refuses at full health.
        FamiliarSpecialMoves.instant("unicorn_stallion_familiar") {
            if (levels.get(Skill.Constitution) >= levels.getMax(Skill.Constitution)) {
                message("You're already at full life points!")
                return@instant false
            }
            familiarSelfSpecial(anim = "healing_aura", sourceGfx = "healing_aura", playerGfx = "healing_aura_owner") {
                levels.restore(Skill.Constitution, ceil(levels.getMax(Skill.Constitution) * 0.15).toInt())
            }
        }

        // Blood Drain - the leech draws the poison from its owner's blood, restoring every drained
        // stat by 2 + 20% of its level, then takes a 25 life-point bite in payment.
        FamiliarSpecialMoves.instant("bloated_leech_familiar") {
            familiarSelfSpecial(anim = "blood_drain", sourceGfx = "blood_drain", playerGfx = "blood_drain_owner") {
                follower?.shoot("blood_drain_proj", this, height = 15, endHeight = 16)
                curePoison()
                for (skill in Skill.values()) {
                    levels.restore(skill, 2, 0.2)
                }
                directHit(25, "damage")
            }
        }

        // Thieving Fingers - boosts Thieving by 2, on top of the magpie's passive invisible +3
        // (see FamiliarBoosts).
        FamiliarSpecialMoves.instant("magpie_familiar") {
            familiarSelfSpecial(anim = "thieving_fingers", sourceGfx = "thieving_fingers", playerGfx = "thieving_fingers_owner") {
                levels.boost(Skill.Thieving, 2)
            }
        }

        // Insane Ferocity - the honey badger enrages the owner, boosting Attack and Strength (+5 and
        // 15% each) at the cost of Ranged, Magic and Defence (-10% each). The stat changes decay
        // naturally over time, and recasting simply refreshes them.
        FamiliarSpecialMoves.instant("honey_badger_familiar") {
            familiarSelfSpecial(anim = "insane_ferocity", sourceGfx = "insane_ferocity", playerGfx = "insane_ferocity_owner") {
                levels.boost(Skill.Attack, amount = 5, multiplier = 0.15)
                levels.boost(Skill.Strength, amount = 5, multiplier = 0.15)
                levels.drain(Skill.Ranged, multiplier = 0.1)
                levels.drain(Skill.Magic, multiplier = 0.1)
                levels.drain(Skill.Defence, multiplier = 0.1)
            }
        }

        // Venom Shot - charge the next ranged attack to poison its target. The charge is set here and
        // spent in [venomShot] on the owner's next damaging ranged hit.
        FamiliarSpecialMoves.instant("spirit_scorpion_familiar") {
            if (this["familiar_venom_shot_charged", false]) {
                message("Your familiar's venom shot is already charged.")
                return@instant false
            }
            val familiar = follower ?: return@instant false
            // The scorpion charges the owner's next ranged shot: it plays its wind-up animation +
            // graphic, then once the graphic finishes fires a slow venom bolt at the owner. Explicit
            // start/end heights keep the bolt travelling from the scorpion to the owner's body.
            familiar.watch(this)
            familiar.anim("venom_shot")
            familiar.gfx("venom_shot")
            val windUp = GraphicDefinitions.get("venom_shot")["ticks", 0]
            queue("venom_shot_charge", windUp) {
                familiar.shoot("venom_shot_proj", this, flightTime = VENOM_SHOT_FLIGHT, height = 20, endHeight = 35)
            }
            set("familiar_venom_shot_charged", true)
            true
        }

        combatAttack(handler = ::venomShot)
    }

    /** Consumes the spirit scorpion's charged Venom Shot: poison the target of the next ranged hit. */
    private fun venomShot(source: Player, attack: CombatAttack) {
        if (attack.damage <= 0 || attack.type != "range" || !source["familiar_venom_shot_charged", false]) {
            return
        }
        source.clear("familiar_venom_shot_charged")
        source.poison(attack.target, 60)
    }

    /** Boosts [skill] by [amount] above max with the familiar's [anim]/[sourceGfx] and owner [playerGfx] flourish. */
    private fun Player.boost(skill: Skill, amount: Int, anim: String? = null, sourceGfx: String? = null, playerGfx: String? = null): Boolean = familiarSelfSpecial(anim = anim, sourceGfx = sourceGfx, playerGfx = playerGfx) { levels.boost(skill, amount) }

    private fun Player.restoreRunEnergy() {
        runEnergy = (runEnergy + levels.getMax(Skill.Agility) * 50).coerceAtMost(MAX_RUN_ENERGY)
    }
}
