package world.gregs.voidps.world.map.lumbridge.combat_hall

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Interpolation
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.combat.hit.Damage
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.proj.shoot

on<ObjectOption>({ operate && target.id == "archery_target" && option == "Shoot-at" }, Priority.HIGH) { player: Player ->
    player.closeDialogue()
    player.face(target)
    arriveDelay()
    swing(player, target, 0)
}

fun swing(player: Player, obj: GameObject, delay: Int) {
    player.weakQueue("archery", delay) {
        val weapon = player.weapon
        if (player.fightStyle != "range") {
            player.message("You can only use Ranged against this target.")
            return@weakQueue
        }
        if (weapon.id != "training_bow") {
            player.message("You can only use a Training bow and arrows against this target.")
            return@weakQueue
        }
        if (player.underAttack) {
            player.message("You are already in combat.")
            return@weakQueue
        }
        player.ammo = ""
        val ammo = player.equipped(EquipSlot.Ammo)
        if (ammo.amount < 1) {
            player.message("There is no ammo left in your quiver.")
            return@weakQueue
        }
        val remaining = player.remaining("hit_delay")
        if (remaining <= 0) {
            player.ammo = "training_arrows"
            player.equipment.remove(player.ammo)
            player.face(obj)
            player.setAnimation("bow_shoot")
            player.setGraphic("training_arrows_shoot")
            val maxHit = Damage.maximum(player, type = "range", weapon = weapon)
            val hit = Damage.roll(player, type = "range", weapon = weapon)
            val height = Interpolation.lerp(hit, -1..maxHit, 0..20)
            player.shoot(id = player.ammo, obj.tile, endHeight = height)
            if (hit != -1) {
                player.exp(Skill.Ranged, hit / 2.5)
            }
            if (ammo.amount == 1) {
                player.message("That was your last one!")
            }
            val delay = weapon.def["attack_speed", 4]
            player.start("hit_delay", delay)
            swing(player, obj, delay)
        } else {
            swing(player, obj, remaining)
        }
    }
}