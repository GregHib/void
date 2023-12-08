package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.prayer.Prayer
import world.gregs.voidps.world.interact.entity.player.combat.range.Ammo
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import kotlin.random.nextInt

object Weapon {
    fun specialRatingModifiers(source: Character, type: String, weapon: Item, special: Boolean, rating: Int): Int {
        if (type == "melee" && special && weapon.id == "dragon_halberd" && source["second_hit", false]) {
            return (rating * 0.75).toInt()
        } else if (type == "melee" && special) {
            return (rating * weapon.def["special_accuracy_mod", 1.0]).toInt()
        }
        return rating
    }

    fun guaranteedChance(source: Character, target: Character, type: String, weapon: Item, special: Boolean): Boolean {
        if (type == "melee" && source.contains("veracs_set_effect") && random.nextInt(4) == 0) {
            target.start("veracs_effect", 1)
            return true
        } else if(weapon.id.startsWith("bone_dagger")) {
            val last = target.attackers.lastOrNull()
            return last != null && last != source
        } else if (type == "magic" && special && weapon.id == "korasis_sword") {
            return true
        } else if (type == "range" && special && (weapon.id.startsWith("magic_longbow") || weapon.id.startsWith("magic_composite_bow") || weapon.id == "seercull")) {
            return true
        }
        return false
    }

    fun invalidateChance(source: Character, target: Character, type: String, weapon: Item, special: Boolean): Boolean {
        return type == "melee" && target.hasClock("spear_wall")
    }

    fun chinchompaChance(source: Character, target: Character, type: String, weapon: Item, chance: Double): Double {
        if (type != "range" || source == target || !weapon.id.endsWith("chinchompa")) {
            return chance
        }
        val distance = source.tile.distanceTo(target)
        return when (source.attackType) {
            "short_fuse" -> when {
                distance <= 3 -> 1.0
                distance <= 6 -> 0.75
                else -> 0.5
            }
            "medium_fuse" -> when {
                distance <= 3 -> 0.75
                distance <= 6 -> 1.0
                else -> 0.75
            }
            "long_fuse" -> when {
                distance <= 3 -> 0.5
                distance <= 6 -> 0.75
                else -> 1.0
            }
            else -> 0.0
        }
    }

    fun isFlagHolder(target: Character): Boolean = target is Player && (target.equipped(EquipSlot.Weapon).id == "zamorak_flag" || target.equipped(EquipSlot.Weapon).id == "saradomin_flag")

    fun isDemonbane(item: Item) = item.id == "silverlight" || item.id == "darklight" || item.id == "holy_water"

    fun isOutlier(special: Boolean, id: String): Boolean = (special && id.startsWith("magic") || id == "seercull" || id == "rune_thrownaxe") || id == "ogre_bow"

    fun isBowOrCrossbow(item: Item) = item.id.endsWith("bow") || item.id == "seercull" || item.id.endsWith("longbow_sighted")

    fun type(character: Character, weapon: Item = character.weapon): String {
        if (character.spell.isNotBlank()) {
            return "magic"
        }
        return when (weapon.def["weapon_style", 0]) {
            13, 16, 17, 18, 19 -> "range"
            20 -> if (character.attackType == "aim_and_fire") "range" else "melee"
            21 -> when (character.attackType) {
                "flare" -> "range"
                "blaze" -> "blaze"
                else -> "melee"
            }
            else -> "melee"
        }
    }

    fun strengthBonus(source: Character, type: String, weapon: Item?) = when {
        type == "blaze" -> weapon?.def?.getOrNull("magic_strength") ?: 0
        // Is thrown or no ammo required
        type == "range" && source is Player && weapon != null && (weapon.id == source.ammo || !Ammo.required(weapon)) -> weapon.def["ranged_strength", 0]
        else -> source[if (type == "range") "ranged_strength" else "strength", 0]
    } + 64

    fun specialDamageModifiers(weapon: Item, special: Boolean, baseDamage: Int): Int {
        if (!special) {
            return baseDamage
        }
        var damage = baseDamage
        val modifier1 = weapon.def["special_damage_mod_1", 0.0]
        if (modifier1 > 0) {
            damage = (damage * modifier1).toInt()
        }
        val modifier2 = weapon.def["special_damage_mod_2", 0.0]
        if (modifier2 > 0) {
            damage = (damage * modifier2).toInt()
        }
        return damage
    }

    fun weaponDamageModifiers(
        source: Character,
        target: Character,
        type: String,
        weapon: Item,
        special: Boolean,
        baseDamage: Int
    ): Int {
        var damage = baseDamage
        if (type == "melee" && source is Player) {
            if (weapon.id == "keris" && Target.isKalphite(target)) {
                damage = (damage * (1.0 / 3.0) + if (random.nextDouble() < 0.51) 3.0 else 1.0).toInt()
            } else if (weapon.id.startsWith("ivandis_flail") && Target.isVampyre(target)) {
                damage = (damage * 1.2).toInt()
            } else if (Equipment.isTzhaarWeapon(weapon.id) && source.equipped(EquipSlot.Amulet).id == "berserker_necklace") {
                damage = (damage * 1.2).toInt()
            } else if (isDemonbane(weapon) && Target.isDemon(target) && !special) {
                damage = (damage * 1.6).toInt()
            } else if (weapon.id == "gadderhammer" && Target.isShade(target)) {
                damage = (damage * if (random.nextDouble() < 0.05) 2.0 else 1.25).toInt()
            } else if (weapon.id.startsWith("dragon_claws") && special) {
                damage -= 10
            }
        } else if (type == "range" && source is Player) {
            damage = Ammo.enchantedBoltEffects(source, target, type, weapon, baseDamage)
            if (weapon.id == "zaniks_crossbow" && special) {
                if (target is NPC) {
                    damage += random.nextInt(30..150)
                } else if (target is Player && (Prayer.hasAnyActive(target) || Equipment.hasGodArmour(target))) {
                    damage += random.nextInt(0..150)
                }
            } else if (weapon.id.startsWith("dark_bow") && special) {
                val dragon = source.ammo == "dragon_arrow"
                damage = (damage * if (dragon) 1.5 else 1.3).toInt().coerceAtLeast(if (dragon) 80 else 50)
            } else if (isOutlier(special, weapon.id)) {
                val strengthBonus = strengthBonus(source, type, weapon)
                damage = (0.5 + (source.levels.get(Skill.Ranged) + 10) * strengthBonus / 64).toInt()
                if (weapon.id == "rune_thrownaxe" || (weapon.id == "magic_shortbow" && target is Player)) {
                    damage += 1
                }
            }
        }
        return damage
    }
}

val Character.fightStyle: String
    get() = Weapon.type(this)

var Character.weapon: Item
    get() = get("weapon", Item.EMPTY)
    set(value) = set("weapon", value)

var Character.attackRange: Int
    get() = get("attack_range", if (this is NPC) def["attack_range", 1] else 1)
    set(value) = set("attack_range", value)

// E.g "accurate"
val Character.attackStyle: String
    get() = get("attack_style", "")

// E.g "flick"
val Character.attackType: String
    get() = get("attack_type", "")

// E.g "crush"
val Character.combatStyle: String
    get() = get("combat_style", "")

