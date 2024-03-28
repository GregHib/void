package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.skill.summoning.isFamiliar
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.prayer.protectMagic
import world.gregs.voidps.world.interact.entity.player.effect.antifire
import world.gregs.voidps.world.interact.entity.player.effect.superAntifire
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
        var damage = baseDamage
        if (source is NPC && type == "dragonfire" && source.isFamiliar) {
            damage = (damage * 0.7).toInt()
        } else if (source is Player && type == "icy_breath" && fireResistantShield(source.equipped(EquipSlot.Shield).id)) {
            damage = 100
        } else if (source is Player && type == "dragonfire") {
            val metal = target is NPC && (target.id.contains("bronze") || target.id.contains("iron") || target.id.contains("steel"))
            var multiplier = 1.0

            val shield = source.equipped(EquipSlot.Shield).id
            if (shield == "anti_dragon_shield" || shield.startsWith("dragonfire_shield")) {
                multiplier -= if (metal) 0.6 else 0.8
                source.message("Your shield absorbs most of the dragon's fiery breath!", ChatType.Filter)
            }

            if (source.antifire || source.superAntifire) {
                multiplier -= if (source.superAntifire) 1.0 else 0.5
            }

            if (multiplier > 0.0) {
                val black = target is NPC && target.id.contains("black")
                if (!metal && !black && random.nextDouble() <= 0.1) {
                    multiplier -= 0.1
                    source.message("You manage to resist some of the dragon fire!", ChatType.Filter)
                } else {
                    source.message("You're horribly burnt by the dragon fire!", ChatType.Filter)
                }
            }
            damage = (damage * multiplier.coerceAtLeast(0.0)).toInt()
        }

        if (type == "melee" && target.softTimers.contains("power_of_light")) {
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
            val areas: AreaDefinitions = get()
            val area = areas["kuradals_dungeon"]
            if (source.tile in area && target.tile in area) {
                damage = (damage * 1.04).toInt()
            }
        } else if (type == "magic" && target is NPC && target.id == "ice_strykewyrm") {
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

    fun isTzhaarWeapon(weapon: String) = weapon == "toktz_xil_ak" || weapon == "tzhaar_ket_om" || weapon == "tzhaar_ket_em" || weapon == "toktz_xil_ek"

    fun dragonFireImmune(target: Character) = target.protectMagic() || antiDragonShield(target) || target.antifire || target.superAntifire

    fun antiDragonShield(target: Character): Boolean {
        if (target !is Player) {
            return false
        }
        val shield = target.equipped(EquipSlot.Shield).id
        return shield.startsWith("dragonfire_shield") || shield.startsWith("anti_dragon_shield")
    }

    fun hasGodArmour(player: Player) = false

    fun fireResistantShield(shield: String) = shield == "elemental_shield" || shield == "mind_shield" || shield == "body_shield" || shield == "dragonfire_shield"

    fun wearingMatchingArenaGear(player: Player, spell: String): Boolean = isMatchingArenaSpell(spell, player.equipped(EquipSlot.Cape).id)
    fun isMatchingArenaSpell(spell: String, cape: String): Boolean = isSaradomin(spell, cape) || isGuthix(spell, cape) || isZamorak(spell, cape)
    fun isSaradomin(spell: String, cape: String): Boolean = spell == "saradomin_strike" && cape == "saradomin_cape"
    fun isGuthix(spell: String, cape: String): Boolean = spell == "claws_of_guthix" && cape == "guthix_cape"
    fun isZamorak(spell: String, cape: String): Boolean = spell == "flames_of_zamorak" && cape == "zamorak_cape"

    fun hasVoidEffect(character: Character) = character.contains("void_set_effect") || hasEliteVoidEffect(character)

    fun hasEliteVoidEffect(character: Character) = character.contains("elite_void_set_effect")

    fun bonus(source: Character, target: Character, type: String, offense: Boolean): Int {
        return if (offense) {
            style(source, if (source is NPC) "attack_bonus" else "${combatStyle(type, source)}_attack")
        } else {
            style(target, "${combatStyle(type, target)}_defence")
        }
    }

    private fun style(character: Character, style: String): Int {
        return if (character is NPC) character.def[style, 0] else character[style] ?: 0
    }

    private fun combatStyle(type: String, character: Character) = if (type == "range" || type == "magic") type else character.combatStyle
}