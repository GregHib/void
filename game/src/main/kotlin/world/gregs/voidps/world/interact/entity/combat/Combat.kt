package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.remove
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.weaponStyle
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.tick.delay
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile
import kotlin.random.Random
import kotlin.random.nextInt

val Character.height: Int
    get() = (this as? NPC)?.def?.getOrNull("height") ?: ShootProjectile.DEFAULT_HEIGHT

fun canAttack(source: Character, target: Character): Boolean {
    if (target is NPC) {
        if (target.def.combat <= 0) {
            return false
        }
        if (target.def.options[1] != "Attack") {
            return false
        }
        if (get<NPCs>().indexed(target.index) == null) {
            return false
        }
    }
    if (source.hasEffect("dead") || target.hasEffect("dead")) {
        return false
    }
    if (target.inSingleCombat && target.hasEffect("in_combat") && !target.attackers.contains(source)) {
        if (target is NPC) {
            (source as? Player)?.message("Someone else is fighting that.")
        } else {
            (source as? Player)?.message("That player is already under attack.")
        }
        return false
    }
    if (source.inSingleCombat && source.hasEffect("in_combat") && !source.attackers.contains(target)) {
        (source as? Player)?.message("You are already in combat.")
        return false
    }
    // PVP area, slayer requirements, in combat etc..
    return true
}

val Character.fightStyle: String
    get() = getWeaponType(this, (this as? Player)?.weapon)

fun getWeaponType(source: Character, weapon: Item?): String {
    if (source.spell.isNotBlank()) {
        return "magic"
    }
    return when (weapon?.def?.weaponStyle()) {
        13, 16, 17, 18, 19 -> "range"
        20 -> if (source.attackType == "aim_and_fire") "range" else "melee"
        21 -> when (source.attackType) {
            "flare" -> "range"
            "blaze" -> "blaze"
            else -> "melee"
        }
        else -> "melee"
    }
}

fun Character.hit(
    target: Character,
    weapon: Item? = (this as? Player)?.weapon,
    type: String = getWeaponType(this, weapon),
    delay: Int = if (type == "melee") 0 else 2,
    spell: String = (this as? Player)?.spell ?: "",
    special: Boolean = (this as? Player)?.specialAttack ?: false,
    damage: Int = hit(this, target, type, weapon, spell)
): Int {
    val damage = damage.coerceAtMost(target.levels.get(Skill.Constitution))
    events.emit(CombatAttack(target, type, damage, weapon, spell, special))
    if (delay == 0) {
        hit(this@hit, target, damage, type, weapon, spell, special)
        return damage
    }
    target.delay(delay) {
        hit(this@hit, target, damage, type, weapon, spell, special)
    }
    return damage
}

fun Character.hit(damage: Int, type: String = "damage") {
    hit(this, this, damage, type)
}

fun hit(source: Character, target: Character, damage: Int, type: String = "damage", weapon: Item? = null, spell: String = "", special: Boolean = false) {
    target.events.emit(CombatHit(source, type, damage, weapon, spell, special))
}

fun ammoRequired(item: Item) = !item.id.startsWith("crystal_bow") && item.id != "zaryte_bow" && !item.id.endsWith("sling") && !item.id.endsWith("chinchompa")

fun getStrengthBonus(source: Character, type: String, weapon: Item?): Int {
    return if (type == "blaze") {
        weapon?.def?.getOrNull("blaze_str") ?: 0
    } else if (type == "range" && source is Player && weapon != null && (weapon.id == source.ammo || !ammoRequired(weapon))) {
        weapon.def["range_str", 0]
    } else {
        source[if (type == "range") "range_str" else "str", 0]
    }
}

fun getMaximumHit(source: Character, target: Character? = null, type: String, weapon: Item?, spell: String = "", special: Boolean = false): Int {
    val strengthBonus = getStrengthBonus(source, type, weapon) + 64
    val baseMaxHit = if (source is NPC) {
        source.def["max_hit_$type", 0].toDouble()
    } else {
        if (type == "magic") {
            val damage = get<SpellDefinitions>().get(spell).maxHit
            if (damage == -1) 0.0 else damage.toDouble()
        } else {
            val skill = when (type) {
                "range" -> Skill.Ranged
                "blaze" -> Skill.Magic
                else -> Skill.Strength
            }
            5.0 + (getEffectiveLevel(source, skill, accuracy = false) * strengthBonus) / 64
        }
    }

    val modifier = HitDamageModifier(target, type, strengthBonus, baseMaxHit, weapon, spell, special)
    source.events.emit(modifier)
    source["max_hit"] = modifier.damage.toInt()
    return modifier.damage.toInt()
}

fun getMinimumHit(source: Character, target: Character? = null, type: String, weapon: Item?, spell: String, special: Boolean): Int {
    return 0
}

fun getEffectiveLevel(source: Character, skill: Skill, accuracy: Boolean): Int {
    val level = source.levels.get(skill).toDouble()
    val mod = HitEffectiveLevelModifier(skill, accuracy, level)
    source.events.emit(mod)
    return mod.level.toInt()
}

fun getRating(source: Character, target: Character?, type: String, weapon: Item?, special: Boolean): Int {
    val offense = source == target
    var level = if (target == null) 8 else {
        val skill = when {
            !offense -> Skill.Defence
            type == "range" -> Skill.Ranged
            type == "magic" || type == "blaze" -> if (offense && target is Player) Skill.Defence else Skill.Magic
            else -> Skill.Attack
        }
        getEffectiveLevel(target, skill, offense)
    }
    val override = HitEffectiveLevelOverride(target, type, !offense, level)
    source.events.emit(override)
    level = override.level
    val style = if (source is NPC && offense) "att_bonus" else if (type == "range" || type == "magic") type else target?.combatStyle ?: ""
    val equipmentBonus = if (target is NPC) target.def[if (offense) style else "${style}_def", 0] else target[if (offense) style else "${style}_def", 0]
    val rating = level * (equipmentBonus + 64.0)
    val modifier = HitRatingModifier(target, type, offense, rating, weapon, special)
    source.events.emit(modifier)
    return modifier.rating.toInt()
}

fun hitChance(source: Character, target: Character?, type: String, weapon: Item?, special: Boolean = false): Double {
    val offensiveRating = getRating(source, source, type, weapon, special)
    val defensiveRating = getRating(source, target, type, weapon, special)
    val chance = if (offensiveRating > defensiveRating) {
        1.0 - (defensiveRating + 2.0) / (2.0 * (offensiveRating + 1.0))
    } else {
        offensiveRating / (2.0 * (defensiveRating + 1.0))
    }
    val modifier = HitChanceModifier(target, type, chance, weapon, special)
    source.events.emit(modifier)
    return modifier.chance
}

fun successfulHit(source: Character, target: Character?, type: String, weapon: Item?, special: Boolean): Boolean {
    val verac = if (source is Player) source.hasFullVeracs() else if (source is NPC) source.id == "verac" else false
    val veracs = verac && Random.nextDouble() < 0.25
    if (veracs) {
        return true
    }

    return Random.nextDouble() < hitChance(source, target, type, weapon, special)
}

private fun Player.hasFullVeracs(): Boolean {
    return notBroken(equipped(EquipSlot.Hat).id, "veracs_helm") &&
            notBroken(equipped(EquipSlot.Hat).id, "veracs_flail") &&
            notBroken(equipped(EquipSlot.Hat).id, "veracs_brassard") &&
            notBroken(equipped(EquipSlot.Hat).id, "veracs_plateskirt")
}

private fun notBroken(id: String, prefix: String): Boolean {
    return id.startsWith(prefix) && !id.endsWith("broken")
}

fun hit(source: Character, target: Character?, type: String, weapon: Item?, spell: String = "", special: Boolean = false): Int {
    return if (successfulHit(source, target, type, weapon, special)) {
        val maxHit = getMaximumHit(source, target, type, weapon, spell, special)
        val minHit = getMinimumHit(source, target, type, weapon, spell, special)
        Random.nextInt(minHit..maxHit)
    } else {
        -1
    }
}

fun removeAmmo(player: Player, target: Character, ammo: String, required: Int) {
    if (ammo == "bolt_rack") {
        player.delay {
            player.equipment.remove(ammo, required)
        }
        return
    }
    when {
        player.equipped(EquipSlot.Cape).id == "avas_attractor" && !exceptions(ammo) -> remove(player, target, ammo, required, 0.6, 0.2)
        player.equipped(EquipSlot.Cape).id == "avas_accumulator" && !exceptions(ammo) -> remove(player, target, ammo, required, 0.72, 0.08)
        player.equipped(EquipSlot.Cape).id == "avas_alerter" -> remove(player, target, ammo, required, 0.8, 0.0)
        else -> remove(player, target, ammo, required, 0.0, 1.0)
    }
}

private fun exceptions(ammo: String) = ammo == "silver_bolts" || ammo == "bone_bolts"

private fun remove(player: Player, target: Character, ammo: String, required: Int, recoverChance: Double, dropChance: Double) {
    val random = Random.nextDouble()
    if (random > recoverChance) {
        player.delay {
            player.equipment.remove(ammo, required)
            if (!player.equipment.contains(ammo)) {
                player.message("That was your last one!")
            }

            if (random > 1.0 - dropChance && !get<Collisions>().check(target.tile.x, target.tile.y, target.tile.plane, CollisionFlag.WATER)) {
                get<FloorItems>().add(ammo, required, target.tile, 100, 200, player)
            }
        }
    }
}

var Character.attackers: MutableList<Character>
    get() = get("attackers")
    set(value) = set("attackers", value)

var Character.damageDealers: MutableMap<Character, Int>
    get() = get("damage_dealers")
    set(value) = set("damage_dealers", value)

val Character.inMultiCombat: Boolean
    get() = false

val Character.inSingleCombat: Boolean
    get() = !inMultiCombat

val ItemDefinition.ammo: Set<String>
    get() = getOrNull<ArrayList<String>>("ammo")?.toSet() ?: emptySet()

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
    get() = get("spell", get("autocast", ""))
    set(value) = set("spell", value)

var Player.weapon: Item
    get() = get("weapon", Item.EMPTY)
    set(value) = set("weapon", value)

var Player.ammo: String
    get() = get("ammo", "")
    set(value) = set("ammo", value)

var Character.attackRange: Int
    get() = get("attack_range", if (this is NPC) def["attack_range", 1] else 1)
    set(value) {
        val old = get("attack_range", 1)
        set("attack_range", value)
        events.emit(AttackDistance(old, value))
    }

val Player.spellBook: String
    get() = interfaces.get("spellbook_tab") ?: "unknown_spellbook"