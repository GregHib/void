package content.skill.summoning

import content.entity.effect.stun
import content.entity.effect.toxin.poison
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.mode.combat.CombatDamage
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.type.random

/** How long (game ticks) a minotaur's Bull Rush stuns its target, keeping it from acting. */
private const val BULL_RUSH_STUN_TICKS = 5

/**
 * Combat familiar special moves - the cast-button specials that target an npc (or player). Each
 * registers into [FamiliarSpecialMoves]; the dispatcher runs it through [castFamiliarSpecial] so a
 * scroll + special-move points are spent only on a successful cast.
 */
class FamiliarCombatSpecials : Script {
    init {
        // Spirit wolf - Howl: no damage, the target flees from the familiar for a few tiles.
        FamiliarSpecialMoves.npc("spirit_wolf_familiar") { target ->
            if (!familiarCanSpecial(target) || target !is NPC) {
                return@npc false
            }
            val familiar = follower ?: return@npc false
            familiar.watch(target)
            familiar.anim("spirit_wolf_howl")
            familiar.gfx("spirit_wolf_howl")
            val flight = familiar.shoot("spirit_wolf_howl_proj", target, height = 0, endHeight = 0)
            // Flee only once the tornado actually reaches the npc, not the instant it's cast.
            target.queue("howl_retreat", CLIENT_TICKS.toTicks(flight)) {
                mode = Retreat(this, familiar)
            }
            true
        }

        // Straightforward "fire a projectile, deal up to maxHit" combat specials.
        FamiliarSpecialMoves.npc("desert_wyrm_familiar") { target ->
            familiarSpecialHit(target, maxHit = 5, anim = "electric_lash", sourceGfx = "electric_lash", projectile = "electric_lash_proj")
        }
        FamiliarSpecialMoves.npc("barker_toad_familiar") { target ->
            familiarSpecialHit(target, maxHit = 8, sourceGfx = "toad_bark", targetGfx = "toad_bark_hit")
        }
        FamiliarSpecialMoves.npc("thorny_snail_familiar") { target ->
            familiarSpecialHit(target, maxHit = 8, anim = "slime_spray", sourceGfx = "slime_spray", projectile = "slime_spray_proj", targetGfx = "slime_spray_hit")
        }
        FamiliarSpecialMoves.npc("arctic_bear_familiar") { target ->
            familiarSpecialHit(target, maxHit = 15, anim = "arctic_blast", sourceGfx = "arctic_blast", projectile = "arctic_blast_proj", targetGfx = "arctic_blast_hit")
        }
        FamiliarSpecialMoves.npc("granite_lobster_familiar") { target ->
            familiarSpecialHit(target, maxHit = 14, anim = "crushing_claw", sourceGfx = "crushing_claw", projectile = "crushing_claw_proj")
        }
        FamiliarSpecialMoves.npc("dreadfowl_familiar") { target ->
            familiarSpecialHit(target, maxHit = 3, anim = "dreadfowl_strike", sourceGfx = "dreadfowl_strike", projectile = "dreadfowl_strike_proj")
        }

        // Combat specials that also drain one of the target's stats.
        FamiliarSpecialMoves.npc("spirit_jelly_familiar") { target ->
            familiarSpecialHit(target, maxHit = 13, anim = "dissolve", projectile = "dissolve_proj").alsoDrain(target, Skill.Attack, 3)
        }
        FamiliarSpecialMoves.npc("spirit_larupia_familiar") { target ->
            familiarSpecialHit(target, maxHit = 10, anim = "rending", sourceGfx = "rending", projectile = "rending_proj").alsoDrain(target, Skill.Strength, 1)
        }
        FamiliarSpecialMoves.npc("evil_turnip_familiar") { target ->
            val cast = familiarSpecialHit(target, maxHit = 10, anim = "evil_flames", projectile = "evil_flames_proj", targetGfx = "evil_flames_hit").alsoDrain(target, Skill.Magic, 1)
            if (cast) {
                levels.restore(Skill.Constitution, 2)
            }
            cast
        }
        FamiliarSpecialMoves.npc("abyssal_parasite_familiar") { target ->
            familiarSpecialHit(target, maxHit = 7, anim = "abyssal_drain", sourceGfx = "abyssal_drain", projectile = "abyssal_drain_proj").alsoDrain(target, Skill.Prayer, random.nextInt(3) + 1)
        }

        // Poisonous Blast - small hit with a 50% chance to poison.
        FamiliarSpecialMoves.npc("stranger_plant_familiar") { target ->
            val cast = familiarSpecialHit(target, maxHit = 2, anim = "poisonous_blast", projectile = "poisonous_blast_proj", targetGfx = "poisonous_blast_hit")
            if (cast && random.nextInt(2) == 0) {
                follower?.poison(target, 20)
            }
            cast
        }

        // Boil - geyser titan ranged hit (approximates the live def-bonus formula with a flat max).
        FamiliarSpecialMoves.npc("geyser_titan_familiar") { target ->
            familiarSpecialHit(target, maxHit = 20, anim = "boil", sourceGfx = "boil", projectile = "boil_proj", targetGfx = "boil_hit")
        }

        // Vampyre Touch: 12 max, 40% chance to heal the owner 2.
        FamiliarSpecialMoves.npc("vampire_bat_familiar") { target ->
            val cast = familiarSpecialHit(target, maxHit = 12, anim = "vampire_touch", sourceGfx = "vampire_touch")
            if (cast && random.nextInt(100) < 40) {
                levels.restore(Skill.Constitution, 2)
            }
            cast
        }

        // Cockatrice family - Petrifying Gaze: drain a variant-specific stat by 3 then hit up to 10.
        val petrifyingGaze = mapOf(
            "spirit_cockatrice_familiar" to Skill.Defence,
            "spirit_guthatrice_familiar" to Skill.Attack,
            "spirit_saratrice_familiar" to Skill.Prayer,
            "spirit_zamatrice_familiar" to Skill.Strength,
            "spirit_pengatrice_familiar" to Skill.Magic,
            "spirit_coraxatrice_familiar" to Skill.Summoning,
            "spirit_vulatrice_familiar" to Skill.Ranged,
        )
        for ((familiar, skill) in petrifyingGaze) {
            FamiliarSpecialMoves.npc(familiar) { target ->
                familiarSpecialHit(target, maxHit = 10, anim = "petrifying_gaze", sourceGfx = "petrifying_gaze", projectile = "petrifying_gaze_proj", targetGfx = "petrifying_gaze_hit")
                    .alsoDrain(target, skill, 3)
            }
        }

        // Fireball Assault - the spirit Tz-Kih flings fire at up to two nearby foes.
        FamiliarSpecialMoves.instant("spirit_tz-kih_familiar") {
            familiarAoeSpecial(maxTargets = 2, maxHit = 7, radius = 3, anim = "fireball_assault", targetGfx = "fireball_assault_hit")
        }

        // Sandstorm - the spirit kalphite blasts every foe around it (up to six, big max hit).
        FamiliarSpecialMoves.instant("spirit_kalphite_familiar") {
            familiarAoeSpecial(maxTargets = 6, maxHit = 20, radius = 6, anim = "sandstorm", sourceGfx = "sandstorm", projectile = "sandstorm_proj")
        }

        // Explode - the giant chinchompa detonates, hitting everything around it, then is consumed by
        // the blast. Fired from the cast button, or (like 2009scape) auto-triggered on ~1/10 of the
        // familiar's own attacks - see [autoExplode].
        FamiliarSpecialMoves.instant("giant_chinchompa_familiar") {
            chinchompaExplode()
        }
        npcCombatDamage("giant_chinchompa_familiar", handler = ::autoExplode)

        // Minotaur family - Bull Rush: a ranged charge whose max hit scales with metal tier, stunning
        // the target on a real cast so it can't act for a few ticks (as in the live game).
        val bullRush = mapOf(
            "bronze_minotaur_familiar" to 4,
            "iron_minotaur_familiar" to 6,
            "steel_minotaur_familiar" to 9,
            "mithril_minotaur_familiar" to 13,
            "adamant_minotaur_familiar" to 16,
            "rune_minotaur_familiar" to 20,
        )
        for ((familiar, maxHit) in bullRush) {
            FamiliarSpecialMoves.npc(familiar) { target ->
                val cast = familiarSpecialHit(target, maxHit = maxHit, type = "range", anim = "bull_rush", sourceGfx = "bull_rush", projectile = "bull_rush_proj")
                if (cast) {
                    follower?.stun(target, BULL_RUSH_STUN_TICKS)
                }
                cast
            }
        }
    }

    /**
     * The Explode special: the giant chinchompa detonates around itself for up to nine hits, then is
     * consumed by the blast a few ticks later (once the hits have landed). Returns false, charging
     * nothing, when there is nothing nearby to hit.
     */
    private fun Player.chinchompaExplode(): Boolean {
        val cast = familiarAoeSpecial(maxTargets = 9, maxHit = 12, radius = 6, anim = "chinchompa_explode", sourceGfx = "chinchompa_explode")
        if (cast) {
            follower?.say("Squeak!")
            // Let the hits land (they self-delay ~3 ticks) before the familiar vanishes.
            queue("chinchompa_explode", 4) { dismissFamiliar() }
        }
        return cast
    }

    /**
     * ~1 in 10 of the attacks landed on the giant chinchompa make it auto-fire Explode. Unlike the
     * cast button this is free: it detonates directly, bypassing the scroll/points gate, so no scroll,
     * points, or owner scroll-throw flourish are involved.
     */
    private fun autoExplode(familiar: NPC, damage: CombatDamage) {
        if (random.nextInt(10) != 0) {
            return
        }
        val owner = Players.indexed(familiar["owner_index", -1]) ?: return
        if (owner.follower?.index != familiar.index) {
            return
        }
        // Several hits can land on the familiar in quick succession; the special cooldown clock keeps
        // those from each firing their own detonation, as it does for the cast-button path.
        if (owner.hasClock("familiar_special_delay")) {
            return
        }
        owner.start("familiar_special_delay", 3)
        owner.chinchompaExplode()
    }

    /** Drains [skill] on [target] by [amount] but only when the hit actually landed ([cast] true). */
    private fun Boolean.alsoDrain(target: Character, skill: Skill, amount: Int): Boolean {
        if (this) {
            target.levels.drain(skill, amount)
        }
        return this
    }
}
