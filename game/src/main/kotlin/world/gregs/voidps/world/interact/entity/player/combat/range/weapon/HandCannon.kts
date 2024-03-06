package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.damage
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.random.nextInt

combatSwing("hand_cannon", "range") { player ->
    val ammo = player.equipped(EquipSlot.Ammo)
    player.ammo = ammo.id
    player.setAnimation("hand_cannon_shoot")
    player.setGraphic("hand_cannon_shoot")
    player.shoot(id = player.ammo, target = target)
    player.hit(target)
    if (player.specialAttack) {
        player.softQueue("hit", 2) {
            player.setAnimation("hand_cannon_special")
            player.setGraphic("hand_cannon_special")
            player.shoot(id = player.ammo, target = target)
            player.hit(target, delay = if (player.attackType == "rapid") 1 else 2)
        }
    }
    explode(player, if (player.specialAttack) 0.05 else 0.005)
    delay = player.weapon.def["attack_speed", 4] - (player.attackType == "rapid").toInt()
}

fun explode(player: Player, chance: Double) {
    if (random.nextDouble() >= chance || !player.equipment.remove(EquipSlot.Weapon.index, "hand_cannon")) {
        return
    }
    player.setAnimation("hand_cannon_explode")
    player.setGraphic("hand_cannon_explode")
    player.weapon = Item.EMPTY
    player.damage(random.nextInt(10..160))
    player.message("Your hand cannon explodes!")
}