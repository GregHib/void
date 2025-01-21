package world.gregs.voidps.world.interact.entity.player.combat.range

import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.skill.slayer.undead
import world.gregs.voidps.world.interact.entity.combat.Equipment
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import world.gregs.voidps.world.interact.entity.effect.freeze
import world.gregs.voidps.world.interact.entity.player.toxin.poison
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit

object Ammo {
    fun required(item: Item) = item.def["ammo_group", "none"] != "none" && !item.id.endsWith("chinchompa")

    fun requiredAmount(weapon: Item, special: Boolean) = if (weapon.id.startsWith("dark_bow") || (weapon.id.startsWith("magic_shortbow") && special)) 2 else 1

    fun remove(player: Player, target: Character, ammo: String, required: Int) {
        if (ammo == "" || ammo == "zaryte_arrow" || ammo == "sling_rock" || ammo == "special_arrow") {
            return
        } else if(ammo == "mud_pie" || ammo.endsWith("_tar")) {
            player.equipment.remove(ammo, required)
            return
        } else if (ammo == "bolt_rack" || ammo == "hand_cannon_shot" || ammo.endsWith("chinchompa")) {
            player.softQueue("ammo") {
                player.equipment.remove(ammo, required)
                if (!player.equipment.contains(ammo)) {
                    player.message("That was your last one!")
                }
            }
            return
        }
        when {
            player.equipped(EquipSlot.Cape).id == "avas_attractor" && !exceptions(ammo) -> remove(player, target, ammo, required, 0.6, 0.2)
            player.equipped(EquipSlot.Cape).id == "avas_accumulator" && !exceptions(ammo) -> remove(player, target, ammo, required, 0.72, 0.08)
            player.equipped(EquipSlot.Cape).id == "avas_alerter" -> remove(player, target, ammo, required, 0.8, 0.0)
            else -> remove(player, target, ammo, required, 0.0, 1.0)
        }
    }

    private fun exceptions(ammo: String) = ammo == "silver_bolts" || ammo == "bone_bolts"

    private fun remove(player: Player, target: Character, ammo: String, required: Int, recoverChance: Double, dropChance: Double) {
        val random = random.nextDouble()
        if (random <= recoverChance) return
        player.softQueue("remove_ammo") {
            player.equipment.remove(ammo, required)
            if (!player.equipment.contains(ammo)) {
                player.message("That was your last one!")
            }

            if (random > 1.0 - dropChance && !get<Collisions>().check(target.tile.x, target.tile.y, target.tile.level, CollisionFlag.FLOOR)) {
                get<FloorItems>().add(target.tile, ammo, required, revealTicks = 100, disappearTicks = 200, owner = player)
            }
        }
    }

    fun enchantedBoltEffects(
        source: Character,
        target: Character,
        type: String,
        weapon: Item,
        baseDamage: Int
    ): Int {
        if (source !is Player || baseDamage < 0 || type != "range") {
            return baseDamage
        }
        var damage = baseDamage
        when {
            source.ammo == "opal_bolts_e" && chance(source, target, "lucky_lightning", 0.05) -> {
                damage += (source.levels.get(Skill.Ranged) * 0.1).toInt()
            }
            source.ammo == "jade_bolts_e" && chance(source, target, "earths_fury", 0.05) -> {
                val duration = TimeUnit.SECONDS.toTicks(5)
                target.freeze(duration)
                source["delay"] = duration
            }
            source.ammo == "pearl_bolts_e" && ((target as? Player)?.equipped(EquipSlot.Weapon)?.id ?: "") != "staff_of_water" && chance(source, target, "sea_curse", 0.06) -> {
                damage += (source.levels.get(Skill.Ranged) * 1.0 / if (Target.isFirey(target)) 15.0 else 20.0).toInt()
            }
            source.ammo == "topaz_bolts_e" && chance(source, target, "down_to_earth", 0.04) -> {
                target.levels.drain(Skill.Magic, 1)
            }
            source.ammo == "sapphire_bolts_e" && chance(source, target, "clear_mind", 0.05) -> {
                val amount = (source["range_attack", 0] * 0.05).toInt()
                target.levels.drain(Skill.Prayer, amount)
                source.levels.restore(Skill.Prayer, amount / 2)
            }
            source.ammo == "emerald_bolts_e" && chance(source, target, "magical_poison", if (target is Player) 0.54 else 0.55) -> {
                source.poison(target, 50)
            }
            source.ammo == "ruby_bolts_e" && chance(source, target, "blood_forfeit", if (target is Player) 0.11 else 0.06) -> {
                damage = (source.levels.get(Skill.Constitution) * 0.2).toInt()
                val drain = (source.levels.get(Skill.Constitution) * 0.1).toInt()
                source.levels.drain(Skill.Constitution, drain)
            }
            source.ammo == "diamond_bolts_e" && chance(source, target, "armour_piercing", 0.1) -> {
                damage = (damage * 1.15).toInt()
            }
            source.ammo == "dragon_bolts_e" && !Equipment.dragonFireImmune(target) && chance(source, target, "dragons_breath", 0.06) -> {
                target.directHit(source, source.levels.get(Skill.Ranged) * 2, "dragonfire", weapon)
            }
            source.ammo == "onyx_bolts_e" && !target.undead && chance(source, target, "life_leech", if (target is Player) 0.1 else 0.11) -> {
                damage = (damage * 1.2).toInt()
                source.start("life_leech", 1)
            }
        }
        return damage
    }

    private fun chance(source: Player, target: Character, name: String, chance: Double): Boolean {
        if (random.nextDouble() < chance) {
            target.setGraphic(name)
            source.playSound(name, delay = 40)
            return true
        }
        return false
    }
}

var Character.ammo: String
    get() = get("ammo", "")
    set(value) = set("ammo", value)