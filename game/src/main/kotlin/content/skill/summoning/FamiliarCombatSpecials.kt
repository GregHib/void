package content.skill.summoning

import content.entity.combat.attackers
import content.entity.combat.hit.hit
import content.entity.combat.target
import content.entity.effect.freeze
import content.entity.effect.stun
import content.entity.effect.toxin.poison
import content.entity.player.combat.special.specialAttackEnergy
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.mode.combat.CombatDamage
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.move
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

/** How long (game ticks) a minotaur's Bull Rush stuns its target, keeping it from acting. */
private const val BULL_RUSH_STUN_TICKS = 5

/** Bull Rush stuns on impact with a 1-in-[BULL_RUSH_STUN_CHANCE] chance (i.e. a third of the time). */
private const val BULL_RUSH_STUN_CHANCE = 3

/** How long (game ticks) the arctic bear's Arctic Blast stuns a small target on impact. */
private const val ARCTIC_BLAST_STUN_TICKS = 3

/** Arctic Blast stuns on impact with a 1-in-[ARCTIC_BLAST_STUN_CHANCE] chance, size-1 targets only. */
private const val ARCTIC_BLAST_STUN_CHANCE = 5

/** How long (game ticks) the praying mantis' Mantis Strike binds a small target in place on impact. */
private const val MANTIS_STRIKE_BIND_TICKS = 3

/** How much special attack energy (out of 1000) the lava titan's Ebon Thunder drains from a player. */
private const val EBON_THUNDER_ENERGY_DRAIN = 100

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
            familiarSpecialHit(target, maxHit = 50, anim = "electric_lash", sourceGfx = "electric_lash", projectile = "electric_lash_proj")
        }
        FamiliarSpecialMoves.npc("barker_toad_familiar") { target ->
            familiarSpecialHit(target, maxHit = 80, sourceGfx = "toad_bark", targetGfx = "toad_bark_hit")
        }
        FamiliarSpecialMoves.npc("thorny_snail_familiar") { target ->
            familiarSpecialHit(target, maxHit = 80, anim = "slime_spray", sourceGfx = "slime_spray", projectile = "slime_spray_proj", targetGfx = "slime_spray_hit")
        }
        // Arctic Blast also stuns small targets on impact, one time in five.
        FamiliarSpecialMoves.npc("arctic_bear_familiar") { target ->
            familiarSpecialHit(target, maxHit = 130, anim = "arctic_blast", sourceGfx = "arctic_blast", projectile = "arctic_blast_proj", targetGfx = "arctic_blast_hit") { hit ->
                if (hit.size <= 1 && random.nextInt(ARCTIC_BLAST_STUN_CHANCE) == 0) {
                    follower?.stun(hit, ARCTIC_BLAST_STUN_TICKS)
                }
            }
        }
        FamiliarSpecialMoves.npc("granite_lobster_familiar") { target ->
            familiarSpecialHit(target, maxHit = 96, anim = "crushing_claw", sourceGfx = "crushing_claw", projectile = "crushing_claw_proj").alsoDrain(target, Skill.Defence, multiplier = 0.05)
        }
        FamiliarSpecialMoves.npc("dreadfowl_familiar") { target ->
            familiarSpecialHit(target, maxHit = 30, anim = "dreadfowl_strike", sourceGfx = "dreadfowl_strike", projectile = "dreadfowl_strike_proj")
        }

        // Combat specials that also drain one of the target's stats.
        FamiliarSpecialMoves.npc("spirit_jelly_familiar") { target ->
            familiarSpecialHit(target, maxHit = 120, anim = "dissolve", projectile = "dissolve_proj").alsoDrain(target, Skill.Attack, 3)
        }
        FamiliarSpecialMoves.npc("spirit_larupia_familiar") { target ->
            familiarSpecialHit(target, maxHit = 120, anim = "rending", sourceGfx = "rending", projectile = "rending_proj").alsoDrain(target, Skill.Strength, 1)
        }
        // Evil Flames - the evil turnip breathes a magic fireball, lowering the target's Magic by 1.
        FamiliarSpecialMoves.npc("evil_turnip_familiar") { target ->
            familiarSpecialHit(target, maxHit = 100, anim = "evil_flames", sourceGfx = "evil_flames", projectile = "evil_flames_proj", targetGfx = "evil_flames_hit").alsoDrain(target, Skill.Magic, 1)
        }
        FamiliarSpecialMoves.npc("abyssal_parasite_familiar") { target ->
            familiarSpecialHit(target, maxHit = 95, anim = "abyssal_drain", sourceGfx = "abyssal_drain", projectile = "abyssal_drain_proj").alsoDrain(target, Skill.Prayer, random.nextInt(3) + 1)
        }

        // Poisonous Blast - small hit with a 50% chance to poison.
        FamiliarSpecialMoves.npc("stranger_plant_familiar") { target ->
            val cast = familiarSpecialHit(target, maxHit = 120, anim = "poisonous_blast", projectile = "poisonous_blast_proj", targetGfx = "poisonous_blast_hit")
            if (cast && random.nextInt(2) == 0) {
                follower?.poison(target, 20)
            }
            cast
        }

        // Boil - geyser titan ranged hit (approximates the live def-bonus formula with a flat max).
        FamiliarSpecialMoves.npc("geyser_titan_familiar") { target ->
            familiarSpecialHit(target, maxHit = 240, anim = "boil", sourceGfx = "boil", projectile = "boil_proj", targetGfx = "boil_hit")
        }

        // Doomsphere - the karamthulhu overlord's watery blast, also washing away 5% of the target's Magic.
        FamiliarSpecialMoves.npc("karamthulhu_overlord_familiar") { target ->
            familiarSpecialHit(target, maxHit = 78, anim = "doomsphere", sourceGfx = "doomsphere", projectile = "doomsphere_proj", targetGfx = "doomsphere_hit").alsoDrain(target, Skill.Magic, multiplier = 0.05)
        }

        // Spike Shot - the spirit dagannoth launches a spike, its hardest-hitting single shot.
        FamiliarSpecialMoves.npc("spirit_dagannoth_familiar") { target ->
            familiarSpecialHit(target, maxHit = 170, anim = "spike_shot", projectile = "spike_shot_proj", targetGfx = "spike_shot_hit")
        }

        // Swamp Plague - the swamp titan's bog blast also poisons the target.
        FamiliarSpecialMoves.npc("swamp_titan_familiar") { target ->
            val cast = familiarSpecialHit(target, maxHit = 110, anim = "swamp_plague", projectile = "swamp_plague_proj")
            if (cast) {
                follower?.poison(target, 80)
            }
            cast
        }

        // Ebon Thunder - the lava titan's bolt drains a player target's special attack energy on impact.
        FamiliarSpecialMoves.npc("lava_titan_familiar") { target ->
            familiarSpecialHit(target, maxHit = 140, anim = "ebon_thunder", sourceGfx = "ebon_thunder", projectile = "ebon_thunder_proj", targetGfx = "ebon_thunder_hit", onLand = ::drainSpecialEnergy)
        }
        FamiliarSpecialMoves.player("lava_titan_familiar") { target ->
            familiarSpecialHit(target, maxHit = 140, anim = "ebon_thunder", sourceGfx = "ebon_thunder", projectile = "ebon_thunder_proj", targetGfx = "ebon_thunder_hit", onLand = ::drainSpecialEnergy)
        }

        // Mantis Strike - the praying mantis' bolt binds small targets in place once it lands.
        FamiliarSpecialMoves.npc("praying_mantis_familiar") { target ->
            familiarSpecialHit(target, maxHit = 100, anim = "mantis_strike", sourceGfx = "mantis_strike", projectile = "mantis_strike_proj", targetGfx = "mantis_strike_hit") { hit ->
                if (hit.size <= 1) {
                    follower?.freeze(hit, MANTIS_STRIKE_BIND_TICKS)
                }
            }
        }

        // Deadly Claw - the talon beast rakes its target three times in quick succession.
        FamiliarSpecialMoves.npc("talon_beast_familiar") { target ->
            if (!familiarCanSpecial(target)) {
                return@npc false
            }
            val familiar = follower ?: return@npc false
            familiar.watch(target)
            repeat(3) { swipe ->
                familiar.hit(target, offensiveType = "magic", damage = random.nextInt(101), delay = 64 + swipe * 30)
            }
            if (familiar !in target.attackers) {
                target.attackers.add(familiar)
            }
            commandFamiliarAttack(target, silent = true)
            true
        }

        // Acorn Missile - the giant ent lobs acorns that also pelt anything adjacent to the target.
        FamiliarSpecialMoves.npc("giant_ent_familiar") { target ->
            val cast = familiarSpecialHit(target, maxHit = 100, anim = "acorn_missile", projectile = "acorn_missile_proj", targetGfx = "acorn_missile_hit")
            if (cast) {
                val familiar = follower
                if (familiar != null && target is NPC) {
                    for (tile in target.tile.spiral(1)) {
                        for (splashed in NPCs.at(tile)) {
                            if (splashed == target || !familiarCanSpecial(splashed, silent = true)) {
                                continue
                            }
                            familiar.hit(splashed, offensiveType = "magic", damage = random.nextInt(101))
                        }
                    }
                }
            }
            cast
        }

        // Famine - the ravenous locust swarms the target; a player target has some food eaten rotten.
        FamiliarSpecialMoves.npc("ravenous_locust_familiar") { target ->
            familiarSpecialHit(target, maxHit = 50, anim = "famine", sourceGfx = "famine", projectile = "famine_proj", targetGfx = "famine_hit")
        }
        FamiliarSpecialMoves.player("ravenous_locust_familiar") { target ->
            val cast = familiarSpecialHit(target, maxHit = 50, anim = "famine", sourceGfx = "famine", projectile = "famine_proj", targetGfx = "famine_hit")
            if (cast && rotFood(target)) {
                message("Your locust devours some of ${target.name}'s food.")
                target.message("${name}'s locust devours some of your food!")
            }
            cast
        }

        // Inferno - the forge regent's flare knocks another player's weapon and shield out of their
        // hands (into their inventory). Fails, charging nothing, if nothing could be unequipped.
        FamiliarSpecialMoves.player("forge_regent_familiar") { target ->
            if (!familiarCanSpecial(target)) {
                return@player false
            }
            val familiar = follower ?: return@player false
            var disarmed = false
            for (slot in intArrayOf(EquipSlot.Weapon.index, EquipSlot.Shield.index)) {
                if (target.equipment[slot].isEmpty()) {
                    continue
                }
                target.equipment.move(slot, target.inventory)
                if (target.equipment.transaction.error == TransactionError.None) {
                    disarmed = true
                }
            }
            if (!disarmed) {
                message("Your familiar couldn't disarm ${target.name}.")
                return@player false
            }
            familiar.watch(target)
            familiar.gfx("inferno")
            target.gfx("inferno_hit")
            target.message("${name}'s familiar burns the equipment from your hands!")
            true
        }

        // Vampyre Touch: 120 max, 40% chance to heal the owner 20.
        FamiliarSpecialMoves.npc("vampire_bat_familiar") { target ->
            val cast = familiarSpecialHit(target, maxHit = 120, anim = "vampire_touch", sourceGfx = "vampire_touch")
            if (cast && random.nextInt(100) < 40) {
                levels.restore(Skill.Constitution, 20)
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
                familiarSpecialHit(target, maxHit = 100, anim = "petrifying_gaze", sourceGfx = "petrifying_gaze", projectile = "petrifying_gaze_proj", targetGfx = "petrifying_gaze_hit")
                    .alsoDrain(target, skill, 3)
            }
            // The cockatrice's "Drain" right-click option casts Petrifying Gaze on the familiar's
            // current combat target (falling back to the owner's), through the same cast gate -
            // scroll, points, cooldown - as the summoning-orb button.
            npcOperate("Drain", familiar) { (clicked) ->
                if (clicked != follower) {
                    return@npcOperate
                }
                val enemy = (follower?.target ?: target) as? NPC
                if (enemy == null) {
                    message("Your familiar has no target to drain.")
                    return@npcOperate
                }
                castFamiliarSpecial { FamiliarSpecialMoves.npcTarget.getValue(familiar)(enemy) }
            }
        }

        // Fireball Assault - the spirit Tz-Kih flings fire at up to two nearby foes.
        FamiliarSpecialMoves.instant("spirit_tz-kih_familiar") {
            familiarAoeSpecial(maxTargets = 2, maxHit = 70, radius = 3, anim = "fireball_assault", targetGfx = "fireball_assault_hit")
        }

        // Sandstorm - the spirit kalphite blasts every foe around it (up to six, big max hit).
        FamiliarSpecialMoves.instant("spirit_kalphite_familiar") {
            familiarAoeSpecial(maxTargets = 6, maxHit = 200, radius = 6, anim = "sandstorm", sourceGfx = "sandstorm", projectile = "sandstorm_proj")
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
            "bronze_minotaur_familiar" to 80,
            "iron_minotaur_familiar" to 100,
            "steel_minotaur_familiar" to 120,
            "mithril_minotaur_familiar" to 160,
            "adamant_minotaur_familiar" to 200,
            "rune_minotaur_familiar" to 240,
        )
        for ((familiar, maxHit) in bullRush) {
            FamiliarSpecialMoves.npc(familiar) { target ->
                familiarSpecialHit(target, maxHit = maxHit, type = "range", anim = "bull_rush", sourceGfx = "bull_rush", projectile = "bull_rush_proj") { hit ->
                    // The charge only stuns about a third of the time, and only once it lands - the
                    // stun fires with the projectile's impact, not the instant the special is cast.
                    if (random.nextInt(BULL_RUSH_STUN_CHANCE) == 0) {
                        follower?.stun(hit, BULL_RUSH_STUN_TICKS)
                    }
                }
            }
        }
    }

    /**
     * The Explode special: the giant chinchompa detonates around itself for up to nine hits, then is
     * consumed by the blast a few ticks later (once the hits have landed). Returns false, charging
     * nothing, when there is nothing nearby to hit.
     */
    private fun Player.chinchompaExplode(): Boolean {
        val cast = familiarAoeSpecial(maxTargets = 9, maxHit = 120, radius = 6, anim = "chinchompa_explode", sourceGfx = "chinchompa_explode")
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

    /** Drains [skill] on [target] by [amount] (and/or [multiplier]) but only on a real cast (`this` true). */
    private fun Boolean.alsoDrain(target: Character, skill: Skill, amount: Int = 0, multiplier: Double = 0.0): Boolean {
        if (this) {
            target.levels.drain(skill, amount, multiplier)
        }
        return this
    }

    /** Ebon Thunder's on-impact effect: saps some of a player target's special attack energy. */
    private fun drainSpecialEnergy(target: Character) {
        if (target is Player) {
            target.specialAttackEnergy = (target.specialAttackEnergy - EBON_THUNDER_ENERGY_DRAIN).coerceAtLeast(0)
        }
    }

    /** Famine's food theft: turns the first edible item in [target]'s inventory rotten. */
    private fun rotFood(target: Player): Boolean {
        for (item in target.inventory.items) {
            if (item.isEmpty() || !item.def.options.contains("Eat")) {
                continue
            }
            target.inventory.replace(item.id, "rotten_food")
            return true
        }
        return false
    }
}
