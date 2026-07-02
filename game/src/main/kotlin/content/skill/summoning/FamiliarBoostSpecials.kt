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
import world.gregs.voidps.type.random
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
        FamiliarSpecialMoves.instant("war_tortoise_familiar") { boost(Skill.Defence, 9, "testudo", "testudo") }
        FamiliarSpecialMoves.instant("obsidian_golem_familiar") { boost(Skill.Strength, 9, sourceGfx = "volcanic_strength") }
        FamiliarSpecialMoves.instant("wolpertinger_familiar") { boost(Skill.Magic, 7, "magic_focus", "magic_focus") }

        // Abyssal Stealth - two skills at once.
        FamiliarSpecialMoves.instant("abyssal_lurker_familiar") {
            familiarSelfSpecial(anim = "abyssal_stealth") {
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

        // Tireless Run - Agility +2 and restore run energy.
        FamiliarSpecialMoves.instant("spirit_terrorbird_familiar") {
            familiarSelfSpecial(anim = "tireless_run", playerGfx = "tireless_run") {
                levels.boost(Skill.Agility, 2)
                restoreRunEnergy()
            }
        }

        // Elemental titans - Titan's Constitution: Defence +12.5% and heal 8.
        FamiliarSpecialMoves.instant("fire_titan_familiar", "moss_titan_familiar", "ice_titan_familiar") {
            familiarSelfSpecial {
                levels.boost(Skill.Defence, multiplier = 0.125)
                levels.restore(Skill.Constitution, 8)
            }
        }

        // Healing Aura - heal the owner by 15% of their max hitpoints.
        FamiliarSpecialMoves.instant("unicorn_stallion_familiar") {
            familiarSelfSpecial(anim = "healing_aura", playerGfx = "healing_aura") {
                levels.restore(Skill.Constitution, ceil(levels.getMax(Skill.Constitution) * 0.15).toInt())
            }
        }

        // Blood Drain - cure poison, restore drained stats by ~20%, then take 1-5 recoil damage.
        FamiliarSpecialMoves.instant("bloated_leech_familiar") {
            familiarSelfSpecial {
                curePoison()
                for (skill in Skill.values()) {
                    val offset = levels.get(skill) - levels.getMax(skill)
                    if (offset < 0) {
                        levels.restore(skill, ceil(levels.getMax(skill) * 0.2).toInt())
                    }
                }
                directHit(random.nextInt(5) + 1, "damage")
            }
        }

        // Thieving Fingers - the Thieving boost is a passive (see FamiliarBoosts); the special is
        // just the visual flourish, but still costs a scroll + points like the live game.
        FamiliarSpecialMoves.instant("magpie_familiar") {
            familiarSelfSpecial(anim = "thieving_fingers", sourceGfx = "thieving_fingers") {}
        }

        // Insane Ferocity - charge the next attack. The next-attack consumption is not wired yet, so
        // this currently sets the charge flag + plays the visuals only (TODO: buff the next swing).
        FamiliarSpecialMoves.instant("honey_badger_familiar") {
            if (this["familiar_insane_ferocity", false]) {
                message("Your familiar is already enraged.")
                return@instant false
            }
            familiarSelfSpecial(anim = "insane_ferocity", sourceGfx = "insane_ferocity") {
                set("familiar_insane_ferocity", true)
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

    /** Boosts [skill] by [amount] above max with the familiar's [anim]/[sourceGfx] flourish. */
    private fun Player.boost(skill: Skill, amount: Int, anim: String? = null, sourceGfx: String? = null): Boolean = familiarSelfSpecial(anim = anim, sourceGfx = sourceGfx) { levels.boost(skill, amount) }

    private fun Player.restoreRunEnergy() {
        runEnergy = (runEnergy + levels.getMax(Skill.Agility) * 50).coerceAtMost(MAX_RUN_ENERGY)
    }
}
