package world.gregs.voidps.world.interact.entity.player.combat.consume.drink

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttackEnergy
import content.entity.player.effect.antifire
import content.entity.player.effect.superAntifire
import content.entity.player.effect.energy.runEnergy
import content.entity.effect.toxin.antiDisease
import content.entity.effect.toxin.antiPoison
import content.entity.effect.toxin.cureDisease
import java.util.concurrent.TimeUnit

consume("*_4", "*_3", "*_2", "*_1") { player ->
    effects(player, item.id)
    val doses = item.id.last().digitToInt()
    if (doses != 1) {
        player.message("You have ${doses - 1} ${"dose".plural(doses - 1)} of the potion left.")
        return@consume
    }
    player.message("You have finished your potion.")
    if (player.contains("smash_vials")) {
        player.inventory.remove(slot, item.id)
        player.message("You quickly smash the empty vial using the tick a Barbarian taught you.")
    }
}

fun hasHolyItem(player: Player) = player.equipped(EquipSlot.Cape).id.startsWith("prayer_cape") || player.holdsItem("holy_wrench")

fun effects(player: Player, potion: String) {
    when {
        potion.startsWith("antifire") -> player.antifire(6)
        potion.startsWith("super_antifire") -> player.superAntifire(6)
        potion.startsWith("overload") -> {
            player["overload_refreshes_remaining"] = 20
            player.timers.start("overload")
        }
        potion.startsWith("attack_") -> player.levels.boost(Skill.Attack, 3, 0.1)
        potion.startsWith("strength_") -> player.levels.boost(Skill.Strength, 3, 0.1)
        potion.startsWith("defence_") -> player.levels.boost(Skill.Defence, 3, 0.1)
        potion.startsWith("magic_essence") -> player.levels.boost(Skill.Magic, 3)
        potion.startsWith("agility_") -> player.levels.boost(Skill.Agility, 3)
        potion.startsWith("fishing_") -> player.levels.boost(Skill.Fishing, 3)
        potion.startsWith("crafting_") -> player.levels.boost(Skill.Crafting, 3)
        potion.startsWith("hunter_potion") || potion.startsWith("hunting_mix") -> player.levels.boost(Skill.Hunter, 3)
        potion.startsWith("fletching_") -> player.levels.boost(Skill.Fletching, 3)
        potion.startsWith("super_attack") -> player.levels.boost(Skill.Attack, 5, 0.15)
        potion.startsWith("super_strength") -> player.levels.boost(Skill.Strength, 5, 0.15)
        potion.startsWith("super_defence") -> player.levels.boost(Skill.Defence, 5, 0.15)
        potion.startsWith("super_magic_") -> player.levels.boost(Skill.Magic, 5, 0.15)
        potion.startsWith("super_ranging_") -> player.levels.boost(Skill.Ranged, 4, 0.10)
        potion.startsWith("combat_") -> {
            player.levels.boost(Skill.Attack, 3, 0.1)
            player.levels.boost(Skill.Strength, 3, 0.1)
        }
        potion.startsWith("summoning_") -> {
            player.levels.boost(Skill.Summoning, 7, 0.25)
//            player.familiar.specialEnergy = (player.familiar.specialEnergy / 100) * 15
        }
        potion.startsWith("relicyms_") -> player.cureDisease()
        potion.startsWith("sanfew_serum") -> {
            player.antiPoison(6)
            player.antiDisease(15)
            Skill.all.filterNot { it == Skill.Constitution }.forEach { skill ->
                player.levels.restore(skill, 8, if (skill == Skill.Prayer && hasHolyItem(player)) 0.27 else 0.25)
            }
        }
        potion.startsWith("zamorak_") -> {
            player.levels.boost(Skill.Attack, 2, 0.2)
            player.levels.boost(Skill.Strength, 2, 0.12)
            player.levels.drain(Skill.Defence, 2, 0.1)
            val health = player.levels.get(Skill.Constitution)
            val damage = ((health / 100) * 10) + 20
            player.directHit(damage)
        }
        potion.startsWith("saradomin_brew") -> {
            player.levels.boost(Skill.Constitution, 20, 0.15)
            player.levels.boost(Skill.Defence, 2, 0.2)
            player.levels.drain(Skill.Attack, 2, 0.1)
            player.levels.drain(Skill.Strength, 2, 0.1)
            player.levels.drain(Skill.Magic, 2, 0.1)
            player.levels.drain(Skill.Ranged, 2, 0.1)
        }
        potion.startsWith("prayer_") -> player.levels.restore(Skill.Prayer, 7, if (hasHolyItem(player)) 0.27 else 0.25)
        potion.startsWith("antipoison") -> player.antiPoison(90, TimeUnit.SECONDS)
        potion.startsWith("super_antipoison") -> player.antiPoison(6)
        potion.startsWith("antipoison++") -> player.antiPoison(12)
        potion.startsWith("antipoison+") -> player.antiPoison(9)
        potion.startsWith("restore_") -> {
            player.levels.restore(Skill.Attack, 10, 0.3)
            player.levels.restore(Skill.Strength, 10, 0.3)
            player.levels.restore(Skill.Defence, 10, 0.3)
            player.levels.restore(Skill.Magic, 10, 0.3)
            player.levels.restore(Skill.Ranged, 10, 0.3)
        }
        potion.startsWith("super_restore") -> {
            Skill.all.filterNot { it == Skill.Constitution }.forEach { skill ->
                player.levels.restore(skill, 8, if (skill == Skill.Prayer && hasHolyItem(player)) 0.27 else 0.25)
            }
        }
        potion.startsWith("energy_") -> player.runEnergy = (player.runEnergy / 100) * 10
        potion.startsWith("super_energy_") -> player.runEnergy = (player.runEnergy / 100) * 20
        potion.startsWith("recover_special") -> {
            player.specialAttackEnergy = (MAX_SPECIAL_ATTACK / 100) * 25
            val percentage = (player.specialAttackEnergy / MAX_SPECIAL_ATTACK) * 100
            if (percentage == 0) {
                player.message("Your special attack energy is now $percentage%.")
            }
            player["recover_special_delay"] = TimeUnit.SECONDS.toTicks(30) / 10
            player.softTimers.start("recover_special")
        }
    }
}