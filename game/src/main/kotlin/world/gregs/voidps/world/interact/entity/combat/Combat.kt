package world.gregs.voidps.world.interact.entity.combat

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.weaponStyle
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.random
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile

val Character.height: Int
    get() = (this as? NPC)?.def?.getOrNull("height") ?: ShootProjectile.DEFAULT_HEIGHT

fun canAttack(source: Character, target: Character): Boolean {
    if (target is NPC) {
        if (target.def.options[1] != "Attack") {
            return false
        }
        if (get<NPCs>().indexed(target.index) == null) {
            return false
        }
    }
    if (source.tile.level != target.tile.level) {
        return false
    }
    if (source.dead || target.dead) {
        return false
    }
    if (source is Player && target is Player) {
        if (!source.inWilderness) {
            source.message("You can only attack players in a player-vs-player area.")
            return false
        }
        if (!target.inWilderness) {
            source.message("That player is not in the wilderness.")
            return false
        }
        val range = getCombatRange(source)
        if (target.combatLevel !in range) {
            source.message("Your level difference is too great!")
            source.message("You need to move deeper into the Wilderness.")
            return false
        }
    }
    if (target.inSingleCombat && target.underAttack && !target.attackers.contains(source)) {
        if (target is NPC) {
            (source as? Player)?.message("Someone else is fighting that.")
        } else {
            (source as? Player)?.message("That player is already under attack.")
        }
        return false
    }
    if (source.inSingleCombat && source.underAttack && !source.attackers.contains(target)) {
        (source as? Player)?.message("You are already in combat.")
        return false
    }
    // PVP area, slayer requirements, in combat etc..
    return true
}

private fun getCombatRange(player: Player): IntRange {
    var diff = 0
    if (player.tile.x in 3008..3135 && player.tile.y in 9920..10367) {
        diff = (player.tile.y - 9920) / 8 + 1
    } else if (player.tile.x in 2944..3392 && player.tile.y in 3525..3967 && player["decrease_combat_attack_range", false]) {
        diff = (player.tile.y - 3520) / 8 + 1
    }
    diff = diff.coerceIn(0..60)
    val combatLevel = player.combatLevel
    val min = (combatLevel - (diff + (5 + combatLevel / 10))).coerceAtLeast(20)
    var max = (combatLevel + (diff + (5 + combatLevel / 10))).coerceAtMost(138)
    while (max < 139 && max - (diff + (5 + max / 10)) <= combatLevel) {
        max += 1
    }
    max -= 1
    return min..max
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
    damage: Int = rollHit(this, target, type, weapon, spell)
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

fun ammoRequired(item: Item) = !item.id.startsWith("crystal_bow") && item.id != "zaryte_bow" && !item.id.endsWith("sling") && !item.id.endsWith("chinchompa")

fun rollHit(source: Character, target: Character?, type: String, weapon: Item?, spell: String = "", special: Boolean = false): Int {
    if (!successfulHit(source, target, type, weapon, special)) {
        return -1
    }
    val strengthBonus = strengthBonus(source, type, weapon)
    val baseMaxHit = baseHit(source, type, spell, strengthBonus)
    val hit = random.nextInt(baseMaxHit.toInt() + 1)
    return modifiers(source, target, type, strengthBonus, hit.toDouble(), weapon, spell, special)
}

fun maximumHit(source: Character, target: Character? = null, type: String, weapon: Item?, spell: String = "", special: Boolean = false): Int {
    val strengthBonus = strengthBonus(source, type, weapon)
    val baseMaxHit = baseHit(source, type, spell, strengthBonus)
    return modifiers(source, target, type, strengthBonus, baseMaxHit, weapon, spell, special)
}

private fun strengthBonus(source: Character, type: String, weapon: Item?): Int {
    return if (type == "blaze") {
        weapon?.def?.getOrNull("blaze_str") ?: 0
    } else if (type == "range" && source is Player && weapon != null && (weapon.id == source.ammo || !ammoRequired(weapon))) {
        weapon.def["range_str", 0]
    } else {
        source[if (type == "range") "range_str" else "str", 0]
    } + 64
}

private fun baseHit(source: Character, type: String, spell: String, strengthBonus: Int): Double {
    return if (source is NPC) {
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
}

private fun modifiers(
    source: Character,
    target: Character?,
    type: String,
    strengthBonus: Int,
    baseMaxHit: Double,
    weapon: Item?,
    spell: String,
    special: Boolean
): Int {
    val modifier = HitDamageModifier(target, type, strengthBonus, baseMaxHit, weapon, spell, special)
    source.events.emit(modifier)
    source["max_hit"] = modifier.damage.toInt()
    return modifier.damage.toInt()
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

fun hitChance(source: Character, target: Character?, type: String, weapon: Item?, special: Boolean = false): Double {
    val offensiveRating = getRating(source, target, type, weapon, special, true)
    val defensiveRating = getRating(source, target, type, weapon, special, false)
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
    return random.nextDouble() < hitChance(source, target, type, weapon, special)
}

fun removeAmmo(player: Player, target: Character, ammo: String, required: Int) {
    if (ammo == "bolt_rack") {
        player.softQueue("ammo") {
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
    val random = random.nextDouble()
    if (random > recoverChance) {
        player.softQueue("remove_ammo") {
            player.equipment.remove(ammo, required)
            if (!player.equipment.contains(ammo)) {
                player.message("That was your last one!")
            }

            if (random > 1.0 - dropChance && !get<Collisions>().check(target.tile.x, target.tile.y, target.tile.level, CollisionFlag.FLOOR)) {
                get<FloorItems>().add(target.tile, ammo, required, revealTicks = 100, disappearTicks = 200, owner = player)
            }
        }
    }
}

val Character.underAttack: Boolean
    get() = hasClock("under_attack")

var Character.attackers: MutableList<Character>
    get() = getOrPut("attackers") { ObjectArrayList() }
    set(value) = set("attackers", value)

var Character.damageDealers: MutableMap<Character, Int>
    get() = getOrPut("damage_dealers") { Object2IntOpenHashMap() }
    set(value) = set("damage_dealers", value)

val Character.inWilderness: Boolean
    get() = get("in_wilderness", false)

val Character.inMultiCombat: Boolean
    get() = softTimers.contains("in_multi_combat")

val Character.inSingleCombat: Boolean
    get() = !inMultiCombat

val ItemDefinition.ammo: Set<String>
    get() = getOrNull("ammo") ?: emptySet()

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

var Character.weapon: Item
    get() = get("weapon", Item.EMPTY)
    set(value) = set("weapon", value)

var Player.ammo: String
    get() = get("ammo", "")
    set(value) = set("ammo", value)

var Character.dead: Boolean
    get() = get("dead", false)
    set(value) {
        if (value) {
            set("dead", true)
        } else {
            clear("dead")
        }
    }

internal val Character.retaliates: Boolean
    get() = if (this is NPC) {
        def["retaliates", true]
    } else {
        this["auto_retaliate", false]
    }

internal var Character.target: Character?
    get() = getOrNull("target")
    set(value) {
        if (value != null) {
            set("target", value)
        } else {
            clear("target")
        }
    }

var Character.attackRange: Int
    get() = get("attack_range", if (this is NPC) def["attack_range", 1] else 1)
    set(value) = set("attack_range", value)

val Player.spellBook: String
    get() = interfaces.get("spellbook_tab") ?: "unknown_spellbook"