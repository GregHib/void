package content.skill.constitution.drink

import content.entity.combat.hit.directHit
import content.entity.effect.toxin.antiDisease
import content.entity.effect.toxin.antiPoison
import content.entity.effect.toxin.cureDisease
import content.entity.effect.toxin.poisonDamage
import content.entity.effect.toxin.poisoned
import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttackEnergy
import content.entity.player.effect.antifire
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import content.entity.player.effect.superAntifire
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import java.util.concurrent.TimeUnit

private fun Player.hasHolyItem() = equipped(EquipSlot.Cape).id.startsWith("prayer_cape") || carriesItem("holy_wrench")

/**
 * Overload applies the boost of all five extreme potions at once, so both share this definition.
 */
fun Player.extremeBoost(skill: Skill) {
    when (skill) {
        Skill.Magic -> levels.boost(Skill.Magic, 7)
        Skill.Ranged -> levels.boost(Skill.Ranged, 4, 0.1923)
        else -> levels.boost(skill, 5, 0.22)
    }
}

private fun Player.restoreAllSkills() {
    for (skill in Skill.all) {
        if (skill == Skill.Constitution) {
            continue
        }
        levels.restore(skill, 8, if (skill == Skill.Prayer && hasHolyItem()) 0.27 else 0.25)
    }
}

private fun MutableMap<String, Player.() -> Unit>.effect(vararg names: String, effect: Player.() -> Unit) {
    for (name in names) {
        require(put(name, effect) == null) { "Duplicate potion effect '$name'." }
    }
}

/**
 * Every dosed potion, keyed by its item id with the dose suffix removed. Membership is what marks
 * an item as a potion, so a potion with no effect yet is still registered with an empty one.
 */
object PotionEffects {
    val effects: Map<String, Player.() -> Unit> = buildMap {
        effect("antifire", "antifire_mix") { antifire(6) }
        effect("super_antifire") { superAntifire(6) }
        effect("overload") {
            set("overload_refreshes_remaining", 20)
            timers.start("overload")
        }
        effect("extreme_attack") { extremeBoost(Skill.Attack) }
        effect("extreme_strength") { extremeBoost(Skill.Strength) }
        effect("extreme_defence") { extremeBoost(Skill.Defence) }
        effect("extreme_magic") { extremeBoost(Skill.Magic) }
        effect("extreme_ranging") { extremeBoost(Skill.Ranged) }
        effect("attack_potion", "attack_mix") { levels.boost(Skill.Attack, 3, 0.1) }
        effect("strength_potion", "strength_mix") { levels.boost(Skill.Strength, 3, 0.1) }
        effect("defence_potion", "defence_mix", "defence_potion_stealing_creation") { levels.boost(Skill.Defence, 3, 0.1) }
        effect("magic_essence", "magic_essence_mix") { levels.boost(Skill.Magic, 3) }
        effect("agility_potion", "agility_mix") { levels.boost(Skill.Agility, 3) }
        effect("fishing_potion", "fishing_mix") { levels.boost(Skill.Fishing, 3) }
        effect("crafting_potion") { levels.boost(Skill.Crafting, 3) }
        effect("hunter_potion", "hunting_mix") { levels.boost(Skill.Hunter, 3) }
        effect("fletching_potion") { levels.boost(Skill.Fletching, 3) }
        effect("super_attack", "super_attack_mix", "super_attack_potion_stealing_creation", "cw_super_attack_potion") { levels.boost(Skill.Attack, 5, 0.15) }
        effect("super_strength", "super_strength_mix", "super_strength_potion_stealing_creation", "cw_super_strength_potion") { levels.boost(Skill.Strength, 5, 0.15) }
        effect("super_defence", "super_defence_mix", "cw_super_defence_potion") { levels.boost(Skill.Defence, 5, 0.15) }
        effect("super_magic_potion", "super_magic_mix", "magic_potion_stealing_creation", "cw_super_magic_potion") { levels.boost(Skill.Magic, 5, 0.15) }
        effect("super_ranging_potion", "super_ranging_mix", "ranging_potion_stealing_creation", "cw_super_ranging_potion") { levels.boost(Skill.Ranged, 4, 0.10) }
        effect("combat_potion", "combat_mix") {
            levels.boost(Skill.Attack, 3, 0.1)
            levels.boost(Skill.Strength, 3, 0.1)
        }
        effect("summoning_potion", "summoning_potion_stealing_creation") { levels.boost(Skill.Summoning, 7, 0.25) }
        effect("juju_mining_potion") { startJuju("juju_mining") }
        effect("juju_woodcutting_potion") { startJuju("juju_woodcutting") }
        effect("juju_farming_potion") { startJuju("juju_farming") }
        effect("juju_fishing_potion") { startJuju("juju_fishing") }
        effect("scentless_potion") { startJuju("scentless") }
        effect("saradomins_blessing") { startJuju("saradomins_blessing") }
        effect("guthixs_gift") { startJuju("guthixs_gift") }
        effect("zamoraks_favour") { startJuju("zamoraks_favour") }
        effect("relicyms_balm", "relicyms_mix") { cureDisease() }
        effect("sanfew_serum") {
            antiPoison(6)
            antiDisease(15)
            restoreAllSkills()
        }
        effect("zamorak_brew", "zamorak_mix") {
            levels.boost(Skill.Attack, 2, 0.2)
            levels.boost(Skill.Strength, 2, 0.12)
            levels.drain(Skill.Defence, 2, 0.1)
            val health = levels.get(Skill.Constitution)
            val damage = ((health / 100) * 10) + 20
            directHit(damage)
        }
        effect("saradomin_brew") {
            levels.boost(Skill.Constitution, 20, 0.15)
            levels.boost(Skill.Defence, 2, 0.2)
            levels.drain(Skill.Attack, 2, 0.1)
            levels.drain(Skill.Strength, 2, 0.1)
            levels.drain(Skill.Magic, 2, 0.1)
            levels.drain(Skill.Ranged, 2, 0.1)
        }
        effect("prayer_potion", "prayer_mix", "prayer_potion_stealing_creation") { levels.restore(Skill.Prayer, 7, if (hasHolyItem()) 0.27 else 0.25) }
        effect("super_prayer") { levels.restore(Skill.Prayer, 7, if (hasHolyItem()) 0.37 else 0.35) }
        effect("antipoison", "antipoison_mix") { antiPoison(90, TimeUnit.SECONDS) }
        effect("super_antipoison", "super_antipoison_mix") { antiPoison(6) }
        effect("antipoison+", "antidote+_mix") { antiPoison(9) }
        effect("antipoison++") { antiPoison(12) }
        effect("restore_potion", "restore_mix") {
            levels.restore(Skill.Attack, 10, 0.3)
            levels.restore(Skill.Strength, 10, 0.3)
            levels.restore(Skill.Defence, 10, 0.3)
            levels.restore(Skill.Magic, 10, 0.3)
            levels.restore(Skill.Ranged, 10, 0.3)
        }
        effect("super_restore", "super_restore_mix") { restoreAllSkills() }
        effect("energy_potion", "energy_mix", "energy_potion_stealing_creation") { runEnergy += (MAX_RUN_ENERGY / 100) * 10 }
        effect("super_energy", "super_energy_mix", "cw_super_energy_potion") { runEnergy += (MAX_RUN_ENERGY / 100) * 20 }
        effect("recover_special") {
            specialAttackEnergy = (specialAttackEnergy + (MAX_SPECIAL_ATTACK / 4)).coerceAtMost(MAX_SPECIAL_ATTACK)
            val percentage = ((specialAttackEnergy / MAX_SPECIAL_ATTACK.toDouble()) * 100).toInt()
            message("Your special attack energy is now $percentage%.")
            set("recover_special_delay", TimeUnit.SECONDS.toTicks(30) / 10)
            softTimers.start("recover_special")
        }
        // Drinkable, but with no effect of their own.
        effect("guthix_rest") {
            if (poisoned) {
                poisonDamage -= 10
            }
            runEnergy += (MAX_RUN_ENERGY / 100) * 5
            levels.boost(Skill.Constitution, 50, maximum = 50)
        }
        // Registered so a dose still reports itself, but their effects are not implemented.
        effect("juju_cooking_potion", "juju_hunter_potion") { }
        // Used on something else rather than drunk, so drinking them does nothing.
        effect("compost_potion", "guthix_balance", "serum_207", "serum_208", "olive_oil", "sacred_oil") { }
    }
}

/**
 * Applies the effect of drinking one dose of [potion], which may be any dose of the item id.
 */
fun Player.potionEffects(potion: String) {
    PotionEffects.effects[potion.substringBeforeLast('_')]?.invoke(this)
}
