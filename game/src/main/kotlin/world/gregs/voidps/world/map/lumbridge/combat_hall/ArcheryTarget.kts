package world.gregs.voidps.world.map.lumbridge.combat_hall

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.move.awaitWalk
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.obj.ObjectClick
import world.gregs.voidps.engine.entity.remaining
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.Maths
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot

on<ObjectClick>({ obj.id == "archery_target" && option == "Shoot-at" }, Priority.HIGH) { player: Player ->
    cancel()
    if (player.fightStyle != "range") {
        player.message("You can only use Ranged against this target.")
        return@on
    }
    val weapon = player.weapon
    if (weapon.id != "training_bow") {
        player.message("You can only use a Training bow and arrows against this target.")
        return@on
    }

    player.action(ActionType.Combat) {
        player.face(obj)
        while (isActive) {
            val targetTile = obj.tile.add(5, 0)
            if (player.tile != targetTile) {
                if (player.movement.path.state != Path.State.Complete) {
                    delay()
                    continue
                }
                player.dialogues.clear()
                player.awaitWalk(targetTile, cancelAction = false)
                continue
            } else if (player.remaining("skilling_delay") > 0L) {
                delay()
                continue
            } else if (player.hasEffect("in_combat")) {
                player.message("You are already in combat.")
                break
            }
            player.ammo = ""
            val ammo = player.equipped(EquipSlot.Ammo)
            if (ammo.amount < 1) {
                player.message("There is no ammo left in your quiver.")
                player.start("skilling_delay", -1, quiet = true)
                break
            }
            player.ammo = "training_arrows"
            player.equipment.remove(EquipSlot.Ammo.index, player.ammo)
            player.face(obj)
            player.setAnimation("bow_shoot")
            player.setGraphic("training_arrows_shoot")
            val maxHit = getMaximumHit(player, null, "range", weapon)
            val hit = hit(player, null, "range", weapon)
            val height = Maths.lerp(hit, -1..maxHit, 0..20)
            player.shoot(id = player.ammo, obj.tile, endHeight = height)
            if (hit != -1) {
                player.exp(Skill.Range, hit / 2.5)
            }
            player.start("skilling_delay", weapon.def["attack_speed", 4], quiet = true)
            if (ammo.amount == 1) {
                player.message("That was your last one!")
            }
        }
    }

}