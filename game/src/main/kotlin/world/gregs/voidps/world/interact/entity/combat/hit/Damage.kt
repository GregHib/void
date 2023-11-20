package world.gregs.voidps.world.interact.entity.combat.hit

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.skill.summoning.isFamiliar
import world.gregs.voidps.world.interact.entity.combat.Bonus
import world.gregs.voidps.world.interact.entity.combat.Equipment
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.combat.Weapon
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.prayer.Prayer
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.player.effect.freeze
import world.gregs.voidps.world.interact.entity.player.toxin.poison
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.nextInt

object Damage {
    private val logger = InlineLogger()

    /**
     * Rolls a real hit against [target] without modifiers
     * @return damage or -1 if unsuccessful
     */
    fun roll(source: Character, target: Character, type: String, weapon: Item, spell: String = "", special: Boolean = false): Int {
        if (!Hit.success(source, target, type, weapon, special)) {
            return -1
        }
        val baseMaxHit = maximum(source, type, weapon, spell)
        source["max_hit"] = baseMaxHit
        return random.nextInt(baseMaxHit + 1)
    }

    /**
     * Calculates the base maximum damage before modifications are applied
     */
    fun maximum(source: Character, type: String, weapon: Item, spell: String = ""): Int {
        val strengthBonus = Weapon.strengthBonus(source, type, weapon)
        if (source is NPC) {
            return source.def["max_hit_$type", 0]
        }
        if (type == "magic") {
            val damage = get<SpellDefinitions>().get(spell).maxHit
            return if (damage == -1) 0 else damage
        }

        val skill = when (type) {
            "range" -> Skill.Ranged
            "blaze" -> Skill.Magic
            else -> Skill.Strength
        }
        return 5 + (Hit.effectiveLevel(source, skill, type, accuracy = false) * strengthBonus) / 64
    }

    /**
     * Applies modifiers to a [maximum]
     */
    fun modify(source: Character, target: Character, type: String, baseMaxHit: Int, weapon: Item, spell: String = "", special: Boolean = false): Int {
        var damage = baseMaxHit
        if (type == "magic" && source.hasClock("charge") && source is Player && Equipment.wearingMatchingArenaGear(source)) {
            damage += 100
        }
        if (source is Player) {
            damage = (damage * Bonus.slayer(source, target, type, damage = true)).toInt()
        }
        if (source is NPC && type == "dragonfire" && source.isFamiliar) {
            damage = (damage * 0.7).toInt()
        } else if (source is Player && type == "icy_breath" && Equipment.fireResistantShield(source.equipped(EquipSlot.Shield).id)) {
            damage = 100
        } else if (source is Player && type == "dragonfire") {
            damage = (damage * Bonus.dragonfire(source, target)).toInt()
        }

        if (target is Player && target.equipped(EquipSlot.Shield).id == "divine_spirit_shield") {
            val points = target.levels.get(Skill.Prayer)
            val drain = ceil((damage * 0.3) / 20.0).toInt()
            if (points > drain) {
                target.levels.drain(Skill.Prayer, drain)
                damage = (damage * 0.7).toInt()
            }
        } else if (target is Player && target.equipped(EquipSlot.Shield).id == "elysian_spirit_shield" && random.nextDouble() < 0.7) {
            damage = (damage * 0.75).toInt()
        }

        if (type == "magic" && weapon.def["magic_damage", 0] > 0) {
            val damageMultiplier = 1.0 + (weapon.def["magic_damage", 0] / 100.0)
            damage = (damage * damageMultiplier).roundToInt()
        }

        if (type == "melee" && special && weapon.def["special_damage", 0.0] > 0.0) {
            damage = (damage * weapon.def["special_damage", 0.0]).toInt()
        }

        if (type == "melee" && target.softTimers.contains("power_of_light")) {
            damage = (damage * 0.5).toInt()
        }
        target.clear("protected_damage")

        if (type == "range" && damage > 0 && source is Player && source.ammo == "diamond_bolts_e" && random.nextDouble() < 0.1) {
            damage = (damage * 1.15).toInt()
            target.setGraphic("armour_piercing")
            source.playSound("armour_piercing", delay = 40)
        } else if (type == "range" && damage > 0 && source is Player && source.ammo == "onyx_bolts_e" && random.nextDouble() < if (target is Player) 0.1 else 0.11) {
            damage = (damage * 1.2).toInt()
            target.setGraphic("life_leech")
            source.playSound("life_leech", delay = 40)
            source.start("life_leech", 1)
        }

        if (type == "range" && weapon.id == "guthix_bow" && special) {
            damage = (damage * 1.5).toInt()
        } else if (type == "range" && weapon.id.endsWith("morrigans_throwing_axe") && special) {
            damage = (damage * 1.2).toInt()
        } else if (type == "range" && weapon.id == "zaniks_crossbow" && special) {
            if (target is NPC) {
                damage += random.nextInt(30..150)
            } else if (target is Player && (Prayer.hasActive(target) || Equipment.hasGodArmour(target))) {
                damage += random.nextInt(0..150)
            }
        } else if (type == "range" && Weapon.isOutlier(special, weapon.id)) {
            val strengthBonus = Weapon.strengthBonus(source, type, weapon)
            damage = (0.5 + (source.levels.get(Skill.Ranged) + 10) * strengthBonus / 64).toInt()
            if (weapon.id == "rune_thrownaxe" || (weapon.id == "magic_shortbow" && target is Player)) {
                damage += 1
            }
        } else if (type == "range" && weapon.id.startsWith("dark_bow") && special && source is Player) {
            val dragon = source.ammo == "dragon_arrow"
            damage = (damage * if (dragon) 1.5 else 1.3).toInt().coerceAtLeast(if (dragon) 80 else 50)
        }


        if (source is NPC && Prayer.usingProtectionPrayer(source, target, type)) {
            target["protected_damage"] = damage
            damage = 0
        } else if (source is Player && Prayer.usingProtectionPrayer(source, target, type) && !Prayer.hitThroughProtectionPrayer(source, target, type, weapon, special)) {
            target["protected_damage"] = damage
            damage = (damage * if (target is Player) 0.6 else 0.0).toInt()
        }

        if (type == "range" && damage > 0 && source is Player && source.ammo == "jade_bolts_e" && random.nextDouble() < 0.05) {
            val duration = TimeUnit.SECONDS.toTicks(5)
            target.freeze(duration)
            source.start("delay", duration)
            target.setGraphic("earths_fury")
            source.playSound("earths_fury", delay = 40)
        } else if (type == "range" && damage > 0 && source is Player && source.ammo == "topaz_bolts_e" && random.nextDouble() < 0.04) {
            target.levels.drain(Skill.Magic, 1)
            target.setGraphic("down_to_earth")
            source.playSound("down_to_earth", delay = 40)
        } else if (type == "range" && damage > 0 && source is Player && source.ammo == "sapphire_bolts_e" && random.nextDouble() < 0.05) {
            val amount = floor(source["range", 0] * 0.05).toInt()
            target.levels.drain(Skill.Prayer, amount)
            source.levels.restore(Skill.Prayer, amount / 2)
            target.setGraphic("clear_mind")
            source.playSound("clear_mind", delay = 40)
        } else if (type == "range" && damage > 0 && source is Player && source.ammo == "emerald_bolts_e" && random.nextDouble() < if (target is Player) 0.54 else 0.55) {
            source.poison(target, 50)
            target.setGraphic("magical_poison")
            source.playSound("magical_poison", delay = 40)
        } else if (type == "range" && damage > 0 && source is Player && source.ammo == "dragon_bolts_e" && !Equipment.dragonFireImmune(target) && random.nextDouble() < 0.06) {
            target.directHit(source, source.levels.get(Skill.Ranged) * 2, "dragonfire", weapon)
            target.setGraphic("dragons_breath")
            source.playSound("dragons_breath", delay = 40)
        }


        if (type == "melee" && source is Player && Equipment.isTzhaarWeapon(weapon.id) && source.equipped(EquipSlot.Amulet).id == "berserker_necklace") {
            damage = (damage * 1.2).toInt()
        } else if (type == "melee" && weapon.id.startsWith("dharoks_greataxe") && source.contains("dharoks_set_effect")) {
            val lost = (source.levels.getMax(Skill.Constitution) - source.levels.get(Skill.Constitution)) / 1000.0
            val max = source.levels.getMax(Skill.Constitution) / 1000.0
            damage = (damage * (1 + lost * max)).toInt()
        } else if (type == "melee" && source.contains("veracs_set_effect") && target.hasClock("veracs_effect")) {
            damage += 10
        } else if (type == "melee" && weapon.id.startsWith("armadyl_godsword") && special) {
            damage = (damage * 1.25).toInt()
        } else if (type == "melee" && weapon.id.startsWith("bandos_godsword") && special) {
            damage = (damage * 1.1).toInt()
        } else if (type == "melee" && Weapon.isDemonbane(weapon) && Target.isDemon(target) && !special) {
            damage = (damage * 1.6).toInt()
        } else if (type == "melee" && weapon.id == "gadderhammer" && Target.isShade(target)) {
            damage = (damage * if (random.nextDouble() < 0.05) 2.0 else 1.25).toInt()
        } else if (type == "melee" && weapon.id == "keris" && Target.isKalphite(target)) {
            damage = (damage * if (random.nextDouble() < 0.51) 3.0 else 1.0 + 1.0 / 3.0).toInt()
        } else if (type == "range" && damage > 0 && source is Player && source.ammo == "opal_bolts_e" && random.nextDouble() < 0.05) {
            damage += (source.levels.get(Skill.Ranged) * 0.1).toInt()
            target.setGraphic("lucky_lightning")
            source.playSound("lucky_lightning", delay = 40)
        } else if (!Equipment.isWatery(target) && type == "range" && damage > 0 && source is Player && source.ammo == "pearl_bolts_e" && random.nextDouble() < 0.06) {
            damage += (source.levels.get(Skill.Ranged) * if (Equipment.isFirey(target)) 1.0 / 15.0 else 0.05).toInt()
            target.setGraphic("sea_curse")
            source.playSound("sea_curse", delay = 40)
        } else if (type == "range" && damage > 0 && source is Player && source.ammo == "ruby_bolts_e" && random.nextDouble() < if (target is Player) 0.11 else 0.06) {
            damage = (source.levels.get(Skill.Constitution) * 0.2).toInt()
            val drain = floor(source.levels.get(Skill.Constitution) * 0.1).toInt()
            source.levels.drain(Skill.Constitution, drain)
            target.setGraphic("blood_forfeit")
            source.playSound("blood_forfeit", delay = 40)
        }

        if (type == "magic" && source is Player && target is NPC && target.id == "ice_strykewyrm") {
            val fireCape = source.equipped(EquipSlot.Cape).id == "fire_cape"
            if (fireCape) {
                damage += 40
            }
            if (source.spell.startsWith("fire_")) {
                damage = (damage * if (fireCape) 2.0 else 1.5).toInt()
            }
        }
        if (source["castle_wars_brace", false] && Weapon.isFlagHolder(target)) {
            damage = (damage * 1.2).toInt()
        } else if (source is Player && source.equipped(EquipSlot.Ring).id.startsWith("ferocious_ring")) {
            val areas: AreaDefinitions = get()
            val area = areas["kuradals_dungeon"]
            if (source.tile in area && target.tile in area) {
                damage = (damage * 1.04).toInt()
            }
        }
        if (spell == "magic_dart") {
            damage = source.levels.get(Skill.Magic) + 100
        } else if (type == "magic" && weapon.id.startsWith("saradomin_sword")) {
            damage = 160
        }

        if (type == "magic" && spell.endsWith("_bolt") && source is Player && source.equipped(EquipSlot.Hands).id == "chaos_gauntlets") {
            damage += 30
        }

        if (target is NPC && target.def.has("damage_cap")) {
            damage = damage.coerceAtMost(target.def["damage_cap"])
        } else if (target is NPC && (target.id == "magic_dummy" || target.id == "melee_dummy")) {
            damage = damage.coerceAtMost(target.levels.get(Skill.Constitution) - 1)
        }

        if (source["debug", false]) {
            val strengthBonus = Weapon.strengthBonus(source, type, weapon)
            val message =
                "Max damage: $damage ($type, $strengthBonus str, ${if (type == "magic") source.spell else if (weapon.isEmpty()) "unarmed" else weapon.id}${if ((source as? Player)?.specialAttack == true) ", special" else ""})"
            source.message(message)
            logger.debug { message }
        }
        return damage
    }

}

/**
 * Damages player closing any interfaces they have open
 */
fun Character.damage(damage: Int, delay: Int = 0, type: String = "damage") {
    strongQueue("hit", delay) {
        directHit(damage, type)
    }
}