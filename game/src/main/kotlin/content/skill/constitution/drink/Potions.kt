package content.skill.constitution.drink

import content.entity.combat.hit.directHit
import content.entity.effect.toxin.antiDisease
import content.entity.effect.toxin.antiPoison
import content.entity.effect.toxin.cureDisease
import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttackEnergy
import content.entity.player.effect.antifire
import content.entity.player.effect.energy.runEnergy
import content.entity.player.effect.superAntifire
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import java.util.concurrent.TimeUnit

class Potions : Script {

    init {
        consumed("*_4,*_3,*_2,*_1") { item, slot ->
            val doses = item.id.last().digitToInt()
            if (doses != 1) {
                message("You have ${doses - 1} ${"dose".plural(doses - 1)} of the potion left.")
                effects(item.id)
                return@consumed
            }
            message("You have finished your potion.")
            if (contains("smash_vials")) {
                inventory.remove(slot, item.id)
                message("You quickly smash the empty vial using the tick a Barbarian taught you.")
            }
            effects(item.id)
        }
    }

    fun Player.hasHolyItem() = equipped(EquipSlot.Cape).id.startsWith("prayer_cape") || carriesItem("holy_wrench")

    fun Player.effects(potion: String) {
        when {
            potion.startsWith("antifire") -> antifire(6)
            potion.startsWith("super_antifire") -> superAntifire(6)
            potion.startsWith("overload") -> {
                set("overload_refreshes_remaining", 20)
                timers.start("overload")
            }
            potion.startsWith("attack_") -> levels.boost(Skill.Attack, 3, 0.1)
            potion.startsWith("strength_") -> levels.boost(Skill.Strength, 3, 0.1)
            potion.startsWith("defence_") -> levels.boost(Skill.Defence, 3, 0.1)
            potion.startsWith("magic_essence") -> levels.boost(Skill.Magic, 3)
            potion.startsWith("agility_") -> levels.boost(Skill.Agility, 3)
            potion.startsWith("fishing_") -> levels.boost(Skill.Fishing, 3)
            potion.startsWith("crafting_") -> levels.boost(Skill.Crafting, 3)
            potion.startsWith("hunter_potion") || potion.startsWith("hunting_mix") -> levels.boost(Skill.Hunter, 3)
            potion.startsWith("fletching_") -> levels.boost(Skill.Fletching, 3)
            potion.startsWith("super_attack") -> levels.boost(Skill.Attack, 5, 0.15)
            potion.startsWith("super_strength") -> levels.boost(Skill.Strength, 5, 0.15)
            potion.startsWith("super_defence") -> levels.boost(Skill.Defence, 5, 0.15)
            potion.startsWith("super_magic_") -> levels.boost(Skill.Magic, 5, 0.15)
            potion.startsWith("super_ranging_") -> levels.boost(Skill.Ranged, 4, 0.10)
            potion.startsWith("combat_") -> {
                levels.boost(Skill.Attack, 3, 0.1)
                levels.boost(Skill.Strength, 3, 0.1)
            }
            potion.startsWith("summoning_") -> {
                levels.boost(Skill.Summoning, 7, 0.25)
                //            familiar.specialEnergy = (familiar.specialEnergy / 100) * 15
            }
            potion.startsWith("relicyms_") -> cureDisease()
            potion.startsWith("sanfew_serum") -> {
                antiPoison(6)
                antiDisease(15)
                Skill.all.filterNot { it == Skill.Constitution }.forEach { skill ->
                    levels.restore(skill, 8, if (skill == Skill.Prayer && hasHolyItem()) 0.27 else 0.25)
                }
            }
            potion.startsWith("zamorak_") -> {
                levels.boost(Skill.Attack, 2, 0.2)
                levels.boost(Skill.Strength, 2, 0.12)
                levels.drain(Skill.Defence, 2, 0.1)
                val health = levels.get(Skill.Constitution)
                val damage = ((health / 100) * 10) + 20
                directHit(damage)
            }
            potion.startsWith("saradomin_brew") -> {
                levels.boost(Skill.Constitution, 20, 0.15)
                levels.boost(Skill.Defence, 2, 0.2)
                levels.drain(Skill.Attack, 2, 0.1)
                levels.drain(Skill.Strength, 2, 0.1)
                levels.drain(Skill.Magic, 2, 0.1)
                levels.drain(Skill.Ranged, 2, 0.1)
            }
            potion.startsWith("prayer_") -> levels.restore(Skill.Prayer, 7, if (hasHolyItem()) 0.27 else 0.25)
            potion.startsWith("antipoison") -> antiPoison(90, TimeUnit.SECONDS)
            potion.startsWith("super_antipoison") -> antiPoison(6)
            potion.startsWith("antipoison++") -> antiPoison(12)
            potion.startsWith("antipoison+") -> antiPoison(9)
            potion.startsWith("restore_") -> {
                levels.restore(Skill.Attack, 10, 0.3)
                levels.restore(Skill.Strength, 10, 0.3)
                levels.restore(Skill.Defence, 10, 0.3)
                levels.restore(Skill.Magic, 10, 0.3)
                levels.restore(Skill.Ranged, 10, 0.3)
            }
            potion.startsWith("super_restore") -> {
                Skill.all.filterNot { it == Skill.Constitution }.forEach { skill ->
                    levels.restore(skill, 8, if (skill == Skill.Prayer && hasHolyItem()) 0.27 else 0.25)
                }
            }
            potion.startsWith("energy_") -> runEnergy = (runEnergy / 100) * 10
            potion.startsWith("super_energy_") -> runEnergy = (runEnergy / 100) * 20
            potion.startsWith("recover_special") -> {
                specialAttackEnergy = (specialAttackEnergy + (MAX_SPECIAL_ATTACK / 4)).coerceAtMost(MAX_SPECIAL_ATTACK)
                val percentage = ((specialAttackEnergy / MAX_SPECIAL_ATTACK.toDouble()) * 100).toInt()
                message("Your special attack energy is now $percentage%.")
                set("recover_special_delay", TimeUnit.SECONDS.toTicks(30) / 10)
                softTimers.start("recover_special")
            }
        }
    }
}
