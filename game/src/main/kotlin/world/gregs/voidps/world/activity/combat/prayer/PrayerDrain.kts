package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.hasVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.Job
import world.gregs.voidps.engine.timer.timer
import world.gregs.voidps.world.interact.entity.sound.playSound

on<EffectStart>({ effect == "prayer_drain" }) { player: Player ->
    player["prayer_drain_tick_job"] = player.timer(1, loop = true) {
        val equipmentBonus = player["prayer", 0]
        var prayerDrainCounter = player["prayer_drain_counter", 0]

        prayerDrainCounter += getTotalDrainEffect(player)
        val prayerDrainResistance = 60 + (equipmentBonus * 2)
        while (prayerDrainCounter > prayerDrainResistance) {
            player.levels.drain(Skill.Prayer, 1)
            prayerDrainCounter -= prayerDrainResistance
            if (player.levels.get(Skill.Prayer) == 0) {
                player.playSound("prayer_drain")
                player.message("You have run out of Prayer points; you can recharge at an altar.")
                player.clearVar(player.getActivePrayerVarKey())
                player.setVar(PrayerConfigs.USING_QUICK_PRAYERS, false)
                break
            }
        }
        player["prayer_drain_counter", true] = prayerDrainCounter
    }
}

on<EffectStop>({ effect == "prayer_drain" }) { player: Player ->
    player.remove<Job>("prayer_drain_tick_job")?.cancel()
}

val prayerDrainEffects = mapOf(
    "Rapid Restore" to 1,
    "Protect Item" to 2,
    "Rapid Heal" to 2,
    "Burst of Strength" to 3,
    "Clarity of Thought" to 3,
    "Mystic Will" to 3,
    "Retribution" to 3,
    "Sharp Eye" to 3,
    "Thick Skin" to 3,
    "Hawk Eye" to 6,
    "Improved Reflexes" to 6,
    "Mystic Lore" to 6,
    "Redemption" to 6,
    "Rock Skin" to 6,
    "Superhuman Strength" to 6,
    "Eagle Eye" to 12,
    "Mystic Might" to 12,
    "Protect from Magic" to 12,
    "Protect from Melee" to 12,
    "Protect from Missiles" to 12,
    "Protect from Summoning" to 12,
    "Steel Skin" to 12,
    "Ultimate Strength" to 12,
    "Incredible Reflexes" to 12,
    "Rapid Renewal" to 14,
    "Rigour" to 16,
    "Chivalry" to 18,
    "Smite" to 18,
    "Augury" to 18,
    "Piety" to 20
)

val curseDrainEffects = mapOf(
    "Berserker" to 2,
    "Protect Item" to 2,
    "Wrath" to 3,
    "world.gregs.voidps.world.activity.combat.prayer.Leech Attack" to 10,
    "world.gregs.voidps.world.activity.combat.prayer.Leech Defence" to 10,
    "world.gregs.voidps.world.activity.combat.prayer.Leech world.gregs.voidps.world.interact.entity.player.energy.Energy" to 10,
    "world.gregs.voidps.world.activity.combat.prayer.Leech Magic" to 10,
    "world.gregs.voidps.world.activity.combat.prayer.Leech Ranged" to 10,
    "world.gregs.voidps.world.activity.combat.prayer.Leech Special Attack" to 10,
    "world.gregs.voidps.world.activity.combat.prayer.Leech Strength" to 10,
    "Deflect Magic" to 12,
    "Deflect Melee" to 12,
    "Deflect Missiles" to 12,
    "Deflect Summoning" to 12,
    "Sap Mage" to 14,
    "Sap Ranger" to 14,
    "Sap Spirit" to 14,
    "Sap Warrior" to 14,
    "Soul Split" to 16,
    "Turmoil" to 16
)

fun getTotalDrainEffect(player: Player): Int {
    val effects = if (player.isCurses()) curseDrainEffects else prayerDrainEffects
    val listKey = player.getActivePrayerVarKey()
    var total = 0
    for ((effect, drain) in effects) {
        if (player.hasVar(listKey, effect)) {
            total += drain
        }
    }
    return total
}