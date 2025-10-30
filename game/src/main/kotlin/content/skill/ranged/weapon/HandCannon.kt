package content.skill.ranged.weapon

import content.entity.combat.combatSwing
import content.entity.combat.hit.damage
import content.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import content.skill.melee.weapon.attackType
import content.skill.melee.weapon.weapon
import content.skill.ranged.ammo
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import kotlin.random.nextInt

class HandCannon : Script {

    init {
        combatSwing("hand_cannon", "range") { player ->
            val ammo = player.equipped(EquipSlot.Ammo)
            player.ammo = ammo.id
            player.anim("hand_cannon_shoot")
            player.gfx("hand_cannon_shoot")
            val time = player.shoot(id = player.ammo, target = target)
            player.hit(target, delay = time)
            if (player.specialAttack) {
                val rapid = player.attackType == "rapid"
                player.strongQueue("hit", 2) {
                    player.anim("hand_cannon_special")
                    player.gfx("hand_cannon_special")
                    player.shoot(id = player.ammo, target = target)
                    player.hit(target, delay = if (rapid) 30 else 60)
                }
            }
            explode(player, if (player.specialAttack) 0.05 else 0.005)
        }
    }

    fun explode(player: Player, chance: Double) {
        if (random.nextDouble() >= chance || !player.equipment.remove(EquipSlot.Weapon.index, "hand_cannon")) {
            return
        }
        player.anim("hand_cannon_explode")
        player.gfx("hand_cannon_explode")
        player.weapon = Item.EMPTY
        player.damage(random.nextInt(10..160))
        player.message("Your hand cannon explodes!")
    }
}
