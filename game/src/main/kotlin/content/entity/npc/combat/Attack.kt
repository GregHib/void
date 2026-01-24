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
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import world.gregs.voidps.engine.entity.character.mode.move.target.NPCCharacterTargetStrategy
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
) : Script {

    init {
        npcCombatSwing { primaryTarget ->
            val defId = if (primaryTarget is Player) {
                val def = def(primaryTarget)
                def["combat_def", def.stringId]
            } else {
                id
            }
            val definition = definitions.getOrNull(defId) ?: return@npcCombatSwing
            if (definition.attacks.isEmpty()) {
                return@npcCombatSwing
            }
            var attack = selectAttack(this, primaryTarget, definition) ?: return@npcCombatSwing
            // Source
            play(attack.anim)
            play(attack.gfx)
            play(attack.sounds)
            if (attack.say != "") {
                say(attack.say)
            }
            if (attack.approach) {
                if (tile.within(primaryTarget.tile, attack.range)) {
                    clear("attack_range")
                } else {
                    set("attack_range", attack.range)
                    set("next_attack", attack.id)
                    return@npcCombatSwing
                }
            }
            val targets = targets(primaryTarget, attack.multiTargetArea)
            // Target(s)
            for (target in targets) {
                target.play(attack.targetAnim)
                target.play(attack.targetGfx)
                target.play(attack.targetSounds)
                // Hit
                val delays = IntArray(attack.projectiles.size)
                val origin = attack.projectileOrigin
                for (i in attack.projectiles.indices) {
                    val projectile = attack.projectiles[i]
                    if (projectile.id == "") {
                        delays[i] = projectile.delay ?: 0
                        continue
                    }
                    val delay = when (origin) {
                        CombatDefinition.Origin.Centre -> nearestTile(this, target).shoot(id = projectile.id, target = target, delay = projectile.delay, curve = projectile.curve?.random(random), endHeight = projectile.endHeight)
                        else -> shoot(id = projectile.id, target = target, delay = projectile.delay, curve = projectile.curve?.random(random), endHeight = projectile.endHeight, tileOffsetX = attack.projectileOriginX, tileOffsetY = attack.projectileOriginY)
                    }
                    delays[i] = delay
                }
                var miss = true
                var delay = 0
                for (i in attack.targetHits.indices) {
                    val hit = attack.targetHits[i]
                    delay = delays.getOrNull(i) ?: -1
                    if (delay == -1) {
                        delay = if (Hit.meleeType(hit.offense)) 0 else 64
                    }
                    if (hit.delay != null) {
                        delay += hit.delay!!
                    }
                    var offense = hit.offense
                    var defence = hit.defence
                    if (offense == "random") {
                        offense = listOf("crush", "range", "magic").random(random)
                        defence = offense
                    }
                    val damage = if (hit.max == 0) {
                        hit(target = target, delay = delay, offensiveType = offense, defensiveType = defence, special = hit.special, spell = attack.id) // Reuse spell for attack name
                    } else {
                        val damage = Damage.roll(source = this, target = target, offensiveType = offense, weapon = Item.EMPTY, special = hit.special, defensiveType = defence, range = hit.min..hit.max, skipAccuracyRoll = !hit.accuracyRoll)
                        hit(target = target, delay = delay, offensiveType = offense, defensiveType = defence, special = hit.special, damage = damage, spell = attack.id)
                    }
                    if (damage > 0) {
                        miss = false
                    }
                }
                CombatApi.attack(this, target, "${definition.npc}:${attack.id}")
                val impactDelay = delays.firstOrNull()
                target.play(if (!attack.impactRegardless && miss) attack.missGfx else attack.impactGfx, impactDelay)
                target.play(if (!attack.impactRegardless && miss) attack.missSounds else attack.impactSounds, impactDelay)
            }
        }

        npcCombatAttack { context ->
            val attackName: String = context.spell
            val target = context.target
            val source = if (target is Player) def(target).stringId else id
            val definition = definitions.getOrNull(def["combat_def", source]) ?: return@npcCombatAttack
            val attack = definition.attacks[attackName] ?: return@npcCombatAttack
            val targets = targets(target, attack.multiTargetArea)
            for (target in targets) {
                if (!CombatApi.impact(this, target, "${definition.npc}:${attack.id}")) {
                    continue
                }
                // Impact
                target.play(attack.impactAnim)
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

    fun selectAttack(source: NPC, target: Character, definition: CombatDefinition): CombatDefinition.CombatAttack? {
        val distance = source.tile.distanceTo(target)
        val next: String? = source["next_attack"]
        if (next != null) {
            val attack = definition.attacks[next] ?: return null
            return if (withinRange(source, target, distance, attack)) attack else null
        }
        val validAttacks = mutableListOf<Pair<CombatDefinition.CombatAttack, Int>>()
        for (attack in definition.attacks.values) {
            if (!CombatApi.condition(source, target, attack.condition)) {
                continue
            }
            if (!attack.approach && !withinRange(source, target, distance, attack)) {
                continue
            }
            validAttacks.add(attack to attack.chance)
        }
        if (validAttacks.isEmpty()) {
            return null
        }
        return weightedSample(validAttacks)
    }

    fun withinRange(source: NPC, target: Character, distance: Int, attack: CombatDefinition.CombatAttack): Boolean {
        if (attack.range == 1 && (attack.targetHits.any { Hit.meleeType(it.offense) } || source.size > 1)) {
            return NPCCharacterTargetStrategy(source).reached(target)
        }
        return distance in 1..attack.range
    }

    @Suppress("UNCHECKED_CAST")
    private fun NPC.targets(target: Character, area: String): Set<Character> {
        if (area == "") {
            return setOf(target)
        }
        val area = Areas.getOrNull(area)?.area ?: return setOf(target)
        val list = mutableSetOf(target)
        for (zone in area.toZones(tile.level)) {
            list.addAll(Players.at(zone))
        }
        return list
    }

    private fun Character.play(anim: String) {
        if (anim != "") {
            anim(anim)
        }
    }

    @JvmName("playGfx")
    private fun Character.play(list: List<CombatGfx>, delay: Int? = null) {
        for (gfx in list) {
            if (gfx.area) {
                areaGfx(
                    id = gfx.id,
                    tile = if (gfx.offset != null) tile.add(gfx.offset!!) else tile,
                    delay = delay ?: gfx.delay ?: 0,
                    height = gfx.height ?: 0,
                )
            } else {
                gfx(id = gfx.id, delay = delay ?: gfx.delay)
            }
        }
    }

    @JvmName("playSounds")
    private fun Character.play(list: List<CombatDefinition.CombatSound>, delay: Int? = null) {
        for (sound in list) {
            if (sound.radius == 0) {
                sound(id = sound.id, delay = delay ?: sound.delay)
            } else {
                areaSound(
                    id = sound.id,
                    tile = if (sound.offset != null) tile.add(sound.offset!!) else tile,
                    radius = sound.radius,
                    delay = delay ?: sound.delay,
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
