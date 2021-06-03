package world.gregs.voidps.world.activity.skill.fishing

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.entity.character.contain.has
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.inventoryFull
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Level.success
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.activity.skill.fishing.fish.Catch
import world.gregs.voidps.world.activity.skill.fishing.spot.FishingSpot
import world.gregs.voidps.world.activity.skill.fishing.tackle.Bait

on<NPCOption>({ option == "Net" }) { player: Player ->
    player.action(ActionType.Fishing) {
        try {
            var first = true
            val tile = npc.tile
            fishing@ while (isActive && player.awaitDialogues() && npc.tile == tile) {
                if (player.inventory.isFull()) {
                    player.message("Your inventory is too full to hold any more fish.")
                    break
                }

                val spot = FishingSpot.get(npc) ?: break
                val data = spot.tackle[option] ?: break

                val level = data.minOf { it.third.minOf { catch -> catch.level } }
                if (!player.has(Skill.Fishing, level, true)) {
                    break
                }

                val type = data.firstOrNull { (tackle, bait, catch) ->
                    player.has(tackle.id) && (bait == Bait.None || player.has(bait.id)) && player.has(Skill.Fishing, catch.minOf { it.level }, false)
                }
                if (type == null) {
                    player.message("You don't have bla bla, you need bla bla.")
                    break
                }
                val (tackle, bait, catches) = type

                if (first) {
                    player.message("You swing your thing at the water.")
                    first = false
                }
                player.face(npc)
                player.setAnimation("${tackle.id}_fish", walk = false, run = false)
                if (bait != Bait.None && !player.inventory.remove(bait.id)) {
                    break
                }
                delay(3)
                for (catch in catches) {
                    if (success(player.levels.get(Skill.Fishing), catch.chance)) {
                        if (catch.xp > 0.0) {
                            player.experience.add(Skill.Fishing, catch.xp)
                        }

                        if (!addCatch(player, catch)) {
                            break@fishing
                        }
                    }
                }
            }
        } finally {
            player.clearAnimation()
        }
    }
}

fun addCatch(player: Player, catch: Catch): Boolean {
    val added = player.inventory.add(catch.id)
    if (added) {
        player.message("You manage to catch a ${catch.id.replace("_", " ").toLowerCase()}.")
    } else {
        player.inventoryFull()
    }
    return added
}