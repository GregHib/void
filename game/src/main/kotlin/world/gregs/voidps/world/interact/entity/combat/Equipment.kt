package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.player.combat.consume.drink.antifire
import world.gregs.voidps.world.interact.entity.player.combat.consume.drink.superAntifire
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.prayer.protectMagic

object Equipment {

    // TODO other staves and npcs
    fun isFirey(target: Character): Boolean = target is Player && target.equipped(EquipSlot.Weapon).id == "staff_of_fire"

    fun isWatery(target: Character): Boolean = target is Player && target.equipped(EquipSlot.Weapon).id == "staff_of_water"

    fun isTzhaarWeapon(weapon: String) = weapon == "toktz_xil_ak" || weapon == "tzhaar_ket_om" || weapon == "tzhaar_ket_em" || weapon == "toktz_xil_ek"

    fun dragonFireImmune(target: Character) = target.protectMagic() ||
            (target is Player && (target.equipped(EquipSlot.Shield).id.startsWith("dragonfire_shield") ||
                    target.equipped(EquipSlot.Shield).id.startsWith("anti_dragon_shield") ||
                    target.antifire ||
                    target.superAntifire))

    fun hasGodArmour(player: Player) = false

    fun fireResistantShield(shield: String) = shield == "elemental_shield" || shield == "mind_shield" || shield == "body_shield" || shield == "dragonfire_shield"

    fun wearingMatchingArenaGear(player: Player): Boolean = isMatchingArenaSpell(player.spell, player.equipped(EquipSlot.Cape).id)
    fun isMatchingArenaSpell(spell: String, cape: String): Boolean = isSaradomin(spell, cape) || isGuthix(spell, cape) || isZamorak(spell, cape)
    fun isSaradomin(spell: String, cape: String): Boolean = spell == "saradomin_strike" && cape == "saradomin_cape"
    fun isGuthix(spell: String, cape: String): Boolean = spell == "claws_of_guthix" && cape == "guthix_cape"
    fun isZamorak(spell: String, cape: String): Boolean = spell == "flames_of_zamorak" && cape == "zamorak_cape"

    fun hasVoidEffect(character: Character) = character.contains("void_set_effect") || character.contains("elite_void_set_effect")
    fun bonus(source: Character, target: Character, type: String, offense: Boolean): Int {
        return if (offense) {
            style(source, type, if (source is NPC) "att_bonus" else combatStyle(type, source))
        } else {
            style(target, type, "${combatStyle(type, target)}_def")
        }
    }

    private fun style(character: Character, type: String, style: String = combatStyle(type, character)): Int {
        return if (character is NPC) character.def[style, 0] else character.getOrNull(style) ?: 0
    }

    private fun combatStyle(type: String, character: Character) = if (type == "range" || type == "magic") type else character.combatStyle
}