package content.skill.melee.weapon

import content.entity.combat.Target
import content.entity.combat.attackers
import content.entity.player.combat.special.specialAttack
import content.entity.player.equip.Equipment
import content.skill.magic.spell.spell
import content.skill.prayer.Prayer
import content.skill.ranged.Ammo
import content.skill.ranged.ammo
import content.skill.slayer.categories
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.visual.update.HitSplat
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import kotlin.random.nextInt

object Weapon {
    val crossbows = setOf(
        "bronze_crossbow",
        "blurite_crossbow",
        "iron_crossbow",
        "steel_crossbow",
        "mithril_crossbow",
        "adamant_crossbow",
        "rune_crossbow",
    )

    fun hasGrapple(player: Player): Boolean {
        if (player.equipped(EquipSlot.Ammo).id != "mithril_grapple") {
            player.message("You need a mithril grapple tipped bolt with a rope to do that.")
            return false
        }
        if (!crossbows.contains(player.weapon.id)) {
            player.message("You need a crossbow equipped to do that.")
            return false
        }
        return true
    }

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
        } else if (weapon.id.startsWith("bone_dagger")) {
            val last = target.attackers.lastOrNull()
            return last != null && last != source
        } else if (type == "magic" && special && weapon.id == "korasis_sword") {
            return true
        } else if (type == "range" && special && (weapon.id.startsWith("magic_longbow") || weapon.id.startsWith("magic_composite_bow") || weapon.id == "seercull")) {
            return true
        } else if (type == "range" && source is NPC && target is Player && source.id.startsWith("thrower_troll") && target.equipped(EquipSlot.Shield).id != "fremennik_round_shield") {
            return true
        }
        return false
    }

    fun invalidateChance(source: Character, target: Character, type: String, weapon: Item, special: Boolean): Boolean {
        if (type == "melee" && target.hasClock("spear_wall")) {
            return true
        }
        if (target is NPC && target.id == "skeleton_warlock" && source is Player && source["restless_ghost_warlock", -1] != target.index) {
            return true
        }
        return false
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

    private fun isOutlier(special: Boolean, id: String): Boolean = (special && id.startsWith("magic") || id == "seercull" || id == "rune_throwing_axe") || id == "ogre_bow"

    fun mark(type: String): HitSplat.Mark = when (type) {
        "range" -> HitSplat.Mark.Range
        "melee", "scorch" -> HitSplat.Mark.Melee
        "magic", "blaze" -> HitSplat.Mark.Magic
        "poison" -> HitSplat.Mark.Poison
        "disease" -> HitSplat.Mark.Diseased
        "dragonfire", "damage" -> HitSplat.Mark.Regular
        "deflect" -> HitSplat.Mark.Reflected
        "healed" -> HitSplat.Mark.Healed
        else -> HitSplat.Mark.Regular
    }

    fun type(character: Character, weapon: Item = character.weapon): String {
        if (character.spell.isNotBlank()) {
            return "magic"
        }
        val definitions = get<WeaponStyleDefinitions>()
        if (character is NPC && !character.categories.contains("human")) {
            return when (character.combatStyle) {
                "range" -> "range"
                "magic" -> "magic"
                else -> "melee"
            }
        }
        val style = if (character is NPC) {
            definitions.get(character.def["weapon_style", "unarmed"])
        } else {
            definitions.get(weapon.def["weapon_style", 0])
        }
        return when (style.stringId) {
            "pie", "bow", "crossbow", "thrown", "chinchompa", "sling" -> "range"
            "fixed_device" -> if (character.attackType == "aim_and_fire") "range" else "melee"
            "salamander" -> when (character.attackType) {
                "blaze" -> "blaze"
                "scorch" -> "scorch"
                else -> "range"
            }
            else -> "melee"
        }
    }

    fun strengthBonus(source: Character, type: String, weapon: Item?) = when {
        type == "blaze" -> weapon?.def?.getOrNull<Double>("magic_strength")?.toInt() ?: 0
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
        baseDamage: Int,
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
                if (weapon.id == "rune_throwing_axe" || (weapon.id == "magic_shortbow" && target is Player)) {
                    damage += 1
                }
                damage = random.nextInt(0, damage + 1)
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

val Character.attackSpeed: Int
    get() = when {
        this is NPC -> def["attack_speed", get<CombatDefinitions>().get(def["combat_def", id]).attackSpeed]
        fightStyle == "magic" -> 5
        this is Player && specialAttack && weapon.id.startsWith("granite_maul") -> 1
        else -> weapon.def["attack_speed", 4] - (attackType == "rapid" || attackType == "medium_fuse").toInt()
    }

var Character.attackRange: Int
    get() = get("attack_range", if (this is NPC) def["attack_range", get<CombatDefinitions>().get(def["combat_def", id]).attackRange] else 1)
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
