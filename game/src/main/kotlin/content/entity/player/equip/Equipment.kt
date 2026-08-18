package content.entity.player.equip

import content.entity.combat.hit.Hit
import content.entity.effect.transform
import content.entity.player.effect.antifire
import content.entity.player.effect.superAntifire
import content.skill.magic.spell.spell
import content.skill.melee.weapon.Weapon
import content.skill.melee.weapon.combatStyle
import content.skill.prayer.protectMagic
import content.skill.summoning.isFamiliar
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import kotlin.math.ceil

object Equipment {

    fun voidEffectiveLevelModifier(skill: Skill, character: Character, baseLevel: Int): Int {
        var level = baseLevel
        if (skill != Skill.Magic && hasVoidEffect(character)) {
            level = (level * 1.1).toInt()
        }
        if (skill == Skill.Ranged && hasEliteVoidEffect(character)) {
            level = (level * 1.025).toInt()
        }
        return level
    }

    fun shieldDamageReductionModifiers(source: Character, target: Character, type: String, baseDamage: Int): Int {
        if (baseDamage <= 0) {
            return baseDamage
        }
        var damage = baseDamage
        if (source is NPC && type == "dragonfire" && source.isFamiliar) {
            damage = (damage * 0.7).toInt()
        }
        // A fire resistant shield doesn't stop a wyvern's icy breath outright, the rare hit that
        // gets past it is capped instead.
        if (type == "icy_breath" && target is Player && fireResistantShield(target.equipped(EquipSlot.Shield).id)) {
            damage = damage.coerceAtMost(100)
        }
        if (source is Player && target is NPC && target.id.startsWith("tormented_demon") && !target.contains("shield_cooldown")) {
            damage = (damage * 0.25).toInt()
        }
        if (Hit.meleeType(type) && target.softTimers.contains("power_of_light")) {
            damage = (damage * 0.5).toInt()
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
        return damage
    }

    fun damageModifiers(source: Character, target: Character, type: String, baseDamage: Int): Int {
        if (source !is Player) {
            return baseDamage
        }
        var damage = baseDamage
        if (source["castle_wars_brace", false] && Weapon.isFlagHolder(target)) {
            damage = (damage * 1.2).toInt()
        } else if (source.equipped(EquipSlot.Ring).id.startsWith("ferocious_ring")) {
            val area = Areas["kuradals_dungeon"]
            if (source.tile in area && target.tile in area) {
                damage = (damage * 1.04).toInt()
            }
        } else if (type == "magic" && target is NPC && target.transform == "ice_strykewyrm") {
            val fireCape = source.equipped(EquipSlot.Cape).id == "fire_cape"
            if (fireCape) {
                damage += 40
            }
            if (source.spell.startsWith("fire_")) {
                damage = (damage * if (fireCape) 2.0 else 1.5).toInt()
            }
        }
        return damage
    }

    fun isCorpbaneWeapon(weapon: String) = weapon.contains("_spear") || weapon.contains("_halberd")

    fun isTzhaarWeapon(weapon: String) = weapon == "toktz_xil_ak" || weapon == "tzhaar_ket_om" || weapon == "tzhaar_ket_em" || weapon == "toktz_xil_ek"

    fun isEarmuffs(hat: String) = hat == "masked_earmuffs" || hat == "earmuffs" || hat.startsWith("slayer_helmet") || hat.startsWith("full_slayer_helmet")

    fun isNosePeg(hat: String) = hat == "nose_peg" || hat.startsWith("slayer_helmet") || hat.startsWith("full_slayer_helmet")

    fun isFaceMask(hat: String) = hat == "face_mask" || hat.startsWith("slayer_helmet") || hat.startsWith("full_slayer_helmet")

    fun dragonFireImmune(target: Character) = target.protectMagic() || antiDragonShield(target) || target.antifire || target.superAntifire

    fun antiDragonShield(target: Character): Boolean {
        if (target !is Player) {
            return false
        }
        val shield = target.equipped(EquipSlot.Shield).id
        return shield.startsWith("dragonfire_shield") || shield.startsWith("anti_dragon_shield")
    }

    fun hasGodArmour(player: Player) = false

    fun fireResistantShield(shield: String) = shield == "elemental_shield" || shield == "mind_shield" || shield == "body_shield" || shield.startsWith("dragonfire_shield")

    fun wearingMatchingArenaGear(player: Player, spell: String): Boolean = isMatchingArenaSpell(spell, player.equipped(EquipSlot.Cape).id)
    fun isMatchingArenaSpell(spell: String, cape: String): Boolean = isSaradomin(spell, cape) || isGuthix(spell, cape) || isZamorak(spell, cape)
    fun isSaradomin(spell: String, cape: String): Boolean = spell == "saradomin_strike" && cape == "saradomin_cape"
    fun isGuthix(spell: String, cape: String): Boolean = spell == "claws_of_guthix" && cape == "guthix_cape"
    fun isZamorak(spell: String, cape: String): Boolean = spell == "flames_of_zamorak" && cape == "zamorak_cape"

    fun hasVoidEffect(character: Character) = character.contains("void_set_effect") || hasEliteVoidEffect(character)

    fun hasEliteVoidEffect(character: Character) = character.contains("elite_void_set_effect")

    fun bonus(source: Character, target: Character, type: String, offense: Boolean): Int = if (offense) {
        style(source, if (source is NPC) "attack_bonus" else "${combatStyle(type, source)}_attack")
    } else {
        style(target, "${combatStyle(type, source)}_defence")
    }

    private fun style(character: Character, style: String): Int = if (character is NPC) character.def[style, 0] else character[style] ?: 0

    private fun combatStyle(type: String, character: Character) = if (type == "range" || type == "magic") type else (if (character is NPC) type else character.combatStyle).removePrefix("typeless_")
}
