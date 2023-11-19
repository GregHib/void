package world.gregs.voidps.world.interact.entity.player.combat.range

import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

object Ammo {
    fun required(item: Item) = !item.id.startsWith("crystal_bow") &&
            item.id != "zaryte_bow" &&
            !item.id.endsWith("sling") &&
            !item.id.endsWith("chinchompa")

    fun remove(player: Player, target: Character, ammo: String, required: Int) {
        if (ammo == "bolt_rack") {
            player.softQueue("ammo") {
                player.equipment.remove(ammo, required)
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
}

var Player.ammo: String
    get() = get("ammo", "")
    set(value) = set("ammo", value)

val ItemDefinition.ammo: Set<String>
    get() = getOrNull("ammo") ?: emptySet()