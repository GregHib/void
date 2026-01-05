package content.entity.npc.combat

import content.entity.combat.hit.Damage
import content.entity.combat.hit.Hit
import content.entity.combat.hit.hit
import content.entity.death.weightedSample
import content.entity.effect.freeze
import content.entity.effect.toxin.poison
import content.entity.gfx.areaGfx
import content.entity.proj.shoot
import net.pearx.kasechange.toPascalCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.CombatDefinition
import world.gregs.voidps.engine.data.config.CombatDefinition.CombatGfx
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class Attack(
    val definitions: CombatDefinitions,
    val areaDefinitions: AreaDefinitions,
    val players: Players,
) : Script {

    init {
        npcCombatSwing { target ->
            val distance = tile.distanceTo(target)
            val source = get("combat_def", if (target is Player) def(target).stringId else id)
            val definition = definitions.getOrNull(source) ?: return@npcCombatSwing
            if (distance > definition.retreatRange) {
                mode = Retreat(this, target)
                return@npcCombatSwing
            }
            if (definition.attacks.isEmpty()) {
                return@npcCombatSwing
            }
            val validAttacks = mutableListOf<Pair<CombatDefinition.CombatAttack, Int>>()
            for (attack in definition.attacks.values) {
                if (!CombatApi.condition(this, target, attack.condition)) {
                    continue
                }
                if (attack.range == 1 && !CharacterTargetStrategy(this).reached(target)) {
                    continue
                } else if (attack.range in 2..<distance) {
                    continue
                }
                validAttacks.add(attack to attack.chance)
            }
            val attack = weightedSample(validAttacks) ?: return@npcCombatSwing
            set("attack_name", attack.id)
            // Source
            play(attack.anim)
            play(attack.gfx)
            play(attack.sounds)
            if (attack.say != "") {
                say(attack.say)
            }
            val targets = targets(target, attack.targetMultiple, attack.targetArea)
            // Target
            for (target in targets) {
                target.play(attack.targetAnim)
                target.play(attack.targetGfx)
                target.play(attack.targetSounds)
                // Hit
                val delays = IntArray(attack.projectiles.size)
                val origin = attack.projectileOrigin
                for (i in attack.projectiles.indices) {
                    val projectile = attack.projectiles[i]
                    val delay = when (origin) {
                        CombatDefinition.Origin.Entity -> shoot(id = projectile.id, target = target, delay = projectile.delay, curve = projectile.curve?.random(random), endHeight = projectile.endHeight)
                        CombatDefinition.Origin.Tile -> tile.shoot(id = projectile.id, target = target, delay = projectile.delay, curve = projectile.curve?.random(random), endHeight = projectile.endHeight)
                        CombatDefinition.Origin.Centre -> nearestTile(this, target).shoot(id = projectile.id, target = target, delay = projectile.delay, curve = projectile.curve?.random(random), endHeight = projectile.endHeight)
                    }
                    delays[i] = delay
                }
                for (i in attack.targetHits.indices) {
                    val hit = attack.targetHits[i]
                    var delay = delays.getOrNull(i) ?: -1
                    if (delay == -1) {
                        delay = if (Hit.meleeType(hit.offense)) 0 else 64
                    }
                    if (hit.delay != null) {
                        delay = hit.delay!!
                    }
                    if (hit.max == 0) {
                        hit(target = target, delay = delay, offensiveType = hit.offense, defensiveType = hit.defence, special = hit.special)
                    } else {
                        val damage = Damage.roll(source = this, target = target, offensiveType = hit.offense, weapon = Item.EMPTY, special = hit.special, defensiveType = hit.defence, range = hit.min..hit.max)
                        hit(target = target, delay = delay, offensiveType = hit.offense, defensiveType = hit.defence, special = hit.special, damage = damage)
                    }
                }
                CombatApi.attack(this, target, "${definition.npc}:${attack.id}")
            }
        }

        npcCombatAttack { context ->
            val attackName: String = get("attack_name") ?: return@npcCombatAttack
            val target = context.target
            val source = if (target is Player) def(target).stringId else id
            val definition = definitions.getOrNull(source) ?: return@npcCombatAttack
            val attack = definition.attacks[attackName] ?: return@npcCombatAttack
            val targets = targets(target, attack.targetMultiple, attack.targetArea)
            for (target in targets) {
                if (!CombatApi.impact(this, target, "${definition.npc}:${attack.id}")) {
                    continue
                }
                // Impact
                target.play(attack.impactAnim)
                target.play(if (attack.impactRegardless || context.damage > 0) attack.impactGfx else attack.missGfx)
                target.play(if (attack.impactRegardless || context.damage > 0) attack.impactSounds else attack.missSounds)
                // Effects
                if (attack.impactRegardless || context.damage > 0) {
                    for (drain in attack.impactDrainSkills) {
                        when (drain.skill) {
                            "all" -> for (skill in Skill.all) {
                                target.levels.drain(skill, drain.amount, drain.multiplier)
                            }
                            "random" -> target.levels.drain(Skill.nonHealth.random(random), drain.amount, drain.multiplier)
                            else -> {
                                val skill = Skill.of(drain.skill.toPascalCase()) ?: continue
                                target.levels.drain(skill, drain.amount, drain.multiplier)
                            }
                        }
                    }
                    if (attack.impactFreeze != 0) {
                        target.freeze(attack.impactFreeze)
                    }
                    if (attack.impactPoison != 0) {
                        poison(target, attack.impactPoison)
                    }
                    if (attack.impactMessage != "") {
                        target.message(attack.impactMessage)
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun NPC.targets(target: Character, multiple: Boolean, area: String): List<Character> {
        if (!multiple || area == "" && targets.isEmpty()) {
            return listOf(target)
        }
        if (area == "") {
            return targets as List<Character>
        }
        val area = areaDefinitions.getOrNull(area)?.area ?: return listOf(target)
        val list = mutableListOf<Character>()
        for (zone in area.toZones(tile.level)) {
            list.addAll(players[zone])
        }
        return list
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
                areaGfx(
                    id = gfx.id,
                    tile = if (gfx.offset != null) tile.add(gfx.offset!!) else tile,
                    delay = gfx.delay ?: 0,
                    height = gfx.height ?: 0,
                )
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
                areaSound(
                    id = sound.id,
                    tile = if (sound.offset != null) tile.add(sound.offset!!) else tile,
                    radius = sound.radius,
                    delay = sound.delay
                )
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
