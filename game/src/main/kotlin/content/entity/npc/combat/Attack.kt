package content.entity.npc.combat

import content.entity.combat.hit.Damage
import content.entity.combat.hit.hit
import content.entity.death.weightedSample
import content.entity.effect.freeze
import content.entity.effect.toxin.poison
import content.entity.gfx.areaGfx
import content.entity.proj.shoot
import content.skill.slayer.categories
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.CombatDefinition
import world.gregs.voidps.engine.data.config.CombatDefinition.CombatGfx
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile

class Attack(
    val definitions: CombatDefinitions
) : Script {

    init {
        /*
            TODO
                how to handle default npcs
                replace projectile delay with calc


         */
        npcCombatSwing { target ->
            val distance = tile.distanceTo(target)
            if (distance > def["attack_radius", 8]) {
                mode = Retreat(this, target)
                return@npcCombatSwing
            }
            val source = if (target is Player) def(target).stringId else id
            val attackList = definitions.get(source)
            val attack = when (attackList.size) {
                0 -> return@npcCombatSwing
                1 -> {
                    val attack = attackList.first()
                    if (attack.range == 1 && !CharacterTargetStrategy(this).reached(target)) {
                        return@npcCombatSwing
                    } else if (attack.range > distance) {
                        return@npcCombatSwing
                    }
                    attack
                }
                else -> {
                    val canMelee = CharacterTargetStrategy(this).reached(target)
                    val list = attackList.filter { (it.range == 1 && canMelee) || it.range > distance }
                    weightedSample(list.map { it to it.chance }) ?: return@npcCombatSwing
                }
            }
            set("attack_name", attack.id)
            // Source
            play(attack.anim)
            play(attack.gfx)
            play(attack.sounds)
            // Target
            target.play(attack.targetAnim)
            target.play(attack.targetGfx)
            target.play(attack.targetSounds)
            // Hit
            val delays = IntArray(attack.projectiles.size)
            for (i in attack.projectiles.indices) {
                val projectile = attack.projectiles[i]
                val delay = when (projectile.origin) {
                    CombatDefinition.ProjectileOrigin.Entity -> shoot(id = projectile.id, tile = target.tile, delay = projectile.delay, curve = projectile.curve, endHeight = projectile.endHeight)
                    CombatDefinition.ProjectileOrigin.Tile -> tile.shoot(id = projectile.id, tile = target.tile, delay = projectile.delay, curve = projectile.curve, endHeight = projectile.endHeight)
                    CombatDefinition.ProjectileOrigin.Centre -> nearestTile(this, target).shoot(id = projectile.id, tile = target.tile, delay = projectile.delay, curve = projectile.curve, endHeight = projectile.endHeight)
                }
                delays[i] = delay
            }
            for (i in attack.targetHits.indices) {
                val hit = attack.targetHits[i]
                var delay = delays.getOrNull(i) ?: -1
                if (delay == -1) {
                    delay = if (hit.offense == "melee") 0 else 64
                }
                val damage = Damage.roll(source = this, target = target, offensiveType = hit.offense, weapon = Item.EMPTY, spell = hit.spell, special = hit.special, defensiveType = hit.defence, range = hit.min..hit.max)
                hit(target = target, delay = delay, offensiveType = hit.offense, defensiveType = hit.defence, spell = hit.spell, special = hit.special, damage = damage)
            }
        }

        npcCombatAttack { context ->
            val attackName: String = get("attack_name") ?: return@npcCombatAttack
            val target = context.target
            val source = if (target is Player) def(target).stringId else id
            val attackList = definitions.get(source)
            val attack = attackList.firstOrNull { it.id == attackName } ?: return@npcCombatAttack
            // Impact
            target.play(attack.impactAnim)
            target.play(if (context.damage == 0 && attack.missGfx.isNotEmpty()) attack.missGfx else attack.impactGfx)
            target.play(if (context.damage == 0 && attack.missSounds.isNotEmpty()) attack.missSounds else attack.impactSounds)
            // Effects
            for (drain in attack.impactDrainSkills) {
                if (drain.skill == "all") {
                    for (skill in Skill.all) {
                        target.levels.drain(skill, drain.amount, drain.multiplier)
                    }
                } else {
                    target.levels.drain(Skill.of(drain.skill) ?: continue, drain.amount, drain.multiplier)
                }
            }
            if (attack.impactFreeze != 0) {
                freeze(target, attack.impactFreeze)
            }
            if (attack.impactPoison != 0) {
                poison(target, attack.impactPoison)
            }
            if (attack.impactMessage != "") {
                target.message(attack.impactMessage)
            }
        }
    }

    private fun Character.play(anim: String) {
        if (anim != "") {
            anim(anim)
        }
    }

    @JvmName("playGfx")
    private fun Character.play(list: List<CombatGfx>) {
        for (gfx in list) {
            if (gfx.area) {
                areaGfx(id = gfx.id, tile = tile, delay = gfx.delay ?: 0)
            } else {
                gfx(id = gfx.id, delay = gfx.delay)
            }
        }
    }

    @JvmName("playSounds")
    private fun Character.play(list: List<CombatDefinition.CombatSound>) {
        for (sound in list) {
            if (sound.radius == 0) {
                sound(id = sound.id, delay = sound.delay)
            } else {
                areaSound(id = sound.id, tile = tile, radius = sound.radius, delay = sound.delay)
            }
        }
    }

    /**
     * Tile the kbd dragon breath originates from.
     * Looks weird imo, but it's the same as OSRS.
     */
    private fun nearestTile(source: Character, target: Character): Tile {
        val half = source.size / 2
        val centre = source.tile.add(half, half)
        val direction = target.tile.delta(centre).toDirection()
        return centre.add(direction).add(direction)
    }
}
