package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasUseLevel
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
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
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.random.nextInt

val ammoDefinitions: AmmoDefinitions by inject()

combatSwing("hand_cannon", "range") { player ->
    val ammo = player.equipped(EquipSlot.Ammo)
    val weapon = player.weapon
    if (!player.hasUseLevel(Skill.Ranged, ammo)) {
        player.message("You are not high enough level to use this item.")
        player.message("You need to have a Ranged level of ${ammo.def.get<Int>("secondary_use_level")}.")
        delay = -1
        return@combatSwing
    }
    val group = weapon.def["ammo_group", ""]
    if (!ammoDefinitions.get(group).items.contains(ammo.id)) {
        player.message("You can't use that ammo with your cannon.")
        delay = -1
        return@combatSwing
    }
    if (!player.equipment.remove(ammo.id, if (player.specialAttack) 2 else 1)) {
        player.message("There is no ammo left in your quiver.")
        delay = -1
        return@combatSwing
    }
    player.ammo = ammo.id
    if (!player.specialAttack) {
        player.setAnimation("hand_cannon_shoot")
        player.setGraphic("hand_cannon_shoot")
        player.shoot(id = player.ammo, target = target)
        player.hit(target)
        delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
        explode(player, 0.005)
    } else {
        if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
            delay = -1
            return@combatSwing
        }
        player.setAnimation("hand_cannon_shoot")
        player.setGraphic("hand_cannon_shoot")
        player.shoot(id = player.ammo, target = target)
        player.hit(target)
        player.softQueue("hit", 2) {
            player.setAnimation("hand_cannon_special")
            player.setGraphic("hand_cannon_special")
            player.shoot(id = player.ammo, target = target)
            player.hit(target, delay = if (player.attackType == "rapid") 1 else 2)
        }
        delay = player["attack_speed", 4] - if (player.attackType == "rapid") 1 else 0
        explode(player, 0.05)
    }
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