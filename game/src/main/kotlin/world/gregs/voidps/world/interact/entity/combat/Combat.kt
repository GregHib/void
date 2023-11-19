package world.gregs.voidps.world.interact.entity.combat

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile

val Character.height: Int
    get() = (this as? NPC)?.def?.getOrNull("height") ?: ShootProjectile.DEFAULT_HEIGHT

fun Character.hit(
    target: Character,
    weapon: Item = this.weapon,
    type: String = Weapon.type(this, weapon),
    delay: Int = if (type == "melee") 0 else 2,
    spell: String = this.spell,
    special: Boolean = (this as? Player)?.specialAttack ?: false,
    damage: Int = Damage.roll(this, target, type, weapon, spell)
): Int {
    val damage = damage.coerceAtMost(target.levels.get(Skill.Constitution))
    events.emit(CombatAttack(target, type, damage, weapon, spell, special, TICKS.toClientTicks(delay)))
    val delay = delay
    if (target is Player) {
        target.strongQueue("hit", delay) {
            splat(this@hit, target, damage, type, weapon, spell, special)
        }
    } else if (target is NPC) {
        target.strongQueue("hit", delay) {
            splat(this@hit, target, damage, type, weapon, spell, special)
        }
    }
    return damage
}

fun Character.damage(damage: Int, delay: Int = 0, type: String = "damage") {
    if (this is Player) {
        strongQueue("hit", delay) {
            splat(source = this@damage, target = this@damage, damage, type)
        }
    } else if (this is NPC) {
        strongQueue("hit", delay) {
            splat(source = this@damage, target = this@damage, damage, type)
        }
    }
}

fun splat(source: Character, target: Character, damage: Int, type: String = "damage", weapon: Item? = null, spell: String = "", special: Boolean = false) {
    if (source.dead) {
        return
    }
    target.events.emit(CombatHit(source, type, damage, weapon, spell, special))
}

fun getEffectiveLevel(character: Character, skill: Skill, accuracy: Boolean): Int {
    val level = character.levels.get(skill).toDouble()
    val mod = HitEffectiveLevelModifier(skill, accuracy, level)
    character.events.emit(mod)
    return mod.level.toInt()
}

fun getRating(source: Character, target: Character?, type: String, weapon: Item?, special: Boolean, offense: Boolean): Int {
    var level = if (target == null) 8 else {
        val skill = when {
            !offense && type != "magic" -> Skill.Defence
            type == "range" -> Skill.Ranged
            type == "magic" || type == "blaze" -> Skill.Magic
            else -> Skill.Attack
        }
        getEffectiveLevel(if (offense) source else target, skill, offense)
    }
    val override = HitEffectiveLevelOverride(target, type, !offense, level)
    source.events.emit(override)
    level = override.level
    val equipmentBonus = getEquipmentBonus(source, target, type, offense)
    val rating = level * (equipmentBonus + 64.0)
    val modifier = HitRatingModifier(target, type, offense, rating, weapon, special)
    source.events.emit(modifier)
    return modifier.rating.toInt()
}

private fun getEquipmentBonus(source: Character, target: Character?, type: String, offense: Boolean): Int {
    val character = if (offense) source else target
    val style = if (source is NPC && offense) "att_bonus" else if (type == "range" || type == "magic") type else character?.combatStyle ?: ""
    return if (character is NPC) character.def[if (offense) style else "${style}_def", 0] else character?.getOrNull(if (offense) style else "${style}_def") ?: 0
}

var Character.attackers: MutableList<Character>
    get() = getOrPut("attackers") { ObjectArrayList() }
    set(value) = set("attackers", value)

var Character.damageDealers: MutableMap<Character, Int>
    get() = getOrPut("damage_dealers") { Object2IntOpenHashMap() }
    set(value) = set("damage_dealers", value)

// E.g "accurate"
val Character.attackStyle: String
    get() = get("attack_style", "")

// E.g "flick"
val Character.attackType: String
    get() = get("attack_type", "")

// E.g "crush"
val Character.combatStyle: String
    get() = get("combat_style", "")

var Character.spell: String
    get() = get("spell", get("autocast_spell", ""))
    set(value) = set("spell", value)

var Character.dead: Boolean
    get() = get("dead", false)
    set(value) {
        if (value) {
            set("dead", true)
        } else {
            clear("dead")
        }
    }

var Character.attackRange: Int
    get() = get("attack_range", if (this is NPC) def["attack_range", 1] else 1)
    set(value) = set("attack_range", value)

val Player.spellBook: String
    get() = interfaces.get("spellbook_tab") ?: "unknown_spellbook"