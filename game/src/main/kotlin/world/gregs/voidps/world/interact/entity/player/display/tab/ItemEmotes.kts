import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitInterfaces
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.player.cure
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption
import world.gregs.voidps.world.interact.entity.sound.playJingle
import kotlin.random.Random

on<ContainerOption>({ item.name == "toy_kite" && option == "Fly" }) { player: Player ->
    emote(player) {
        player.playAnimation("emote_fly_kite")
    }
}

on<ContainerOption>({ container == "worn_equipment" && item.name == "reindeer_hat" && option == "Emote" }) { player: Player ->
    emote(player) {
        player.setGraphic("emote_reindeer")
        player.playAnimation("emote_reindeer")
    }
}

on<ContainerOption>({ container == "inventory" && item.name == "prayer_book" && option == "Recite-prayer" }) { player: Player ->
    emote(player) {
        if (player.hasEffect("poison")) {
            val poisonDamage = player.getOrNull<Int>("poison_damage") ?: return@emote
            var points = (poisonDamage - 20) / 2
            var decrease = poisonDamage
            val prayer = player.levels.get(Skill.Prayer)
            if (points > prayer) {
                decrease = (prayer * 2) + 2
                points = prayer
            }
            if (points > 0) {
                player.levels.drain(Skill.Prayer, points)
                player["poison_damage"] = poisonDamage - decrease
                if (poisonDamage - decrease <= 10) {
                    player.cure()
                }
            }
        }
        player.playAnimation("emote_recite_prayer")
    }
}

on<ContainerOption>({ item.name == "rubber_chicken" && option == "Dance" }) { player: Player ->
    emote(player) {
        player.playAnimation("emote_chicken_dance")
    }
}

on<ContainerOption>({ container == "inventory" && item.name == "spinning_plate" && option == "Spin" }) { player: Player ->
    emote(player) {
        val drop = Random.nextBoolean()
        repeat(if (drop) 7 else 10) {
            player.playAnimation("emote_spinning_plate")
        }
        player.playAnimation("emote_spinning_plate_${if (drop) "drop" else "take"}")
        delay(1)
        player.playAnimation("emote_${if (drop) "cry" else "cheer"}")
    }
}

on<ContinueDialogue>({ name == "snow_globe" && component == "continue" }) { player: Player ->
    player.close("snow_globe")
    player.dialogues.resume(true)
}

on<ContainerOption>({ container == "inventory" && item.name == "snow_globe" && option == "Shake" }) { player: Player ->
    emote(player) {
        player.message("You shake the snow globe.")
        player.playAnimation("emote_shake_snow_globe")
        player.playJingle("harmony_snow_globe")
        player.dialogue {
            val result: Boolean = await("snow_globe")
            if (result) {
                emote(player, false) {
                    player.setGraphic("emote_snow_globe_flurry")
                    player.playAnimation("emote_trample_snow")
                    player.message("The snow globe fills your inventory with snow!")
                    player.inventory.add("snowball_2007_christmas_event", player.inventory.spaces)
                }
            }
        }
        player.open("snow_globe")
        player.awaitInterfaces()
        player.clearAnimation()
        player.dialogues.clear()
    }
}

on<ContainerOption>({ container == "inventory" && item.name == "yo-yo" && (option == "Play" || option == "Loop" || option == "Walk" || option == "Crazy") }) { player: Player ->
    emote(player) {
        player.playAnimation("emote_yoyo_${option.toLowerCase()}")
    }
}

fun emote(player: Player, check: Boolean = true, block: suspend Action.() -> Unit) {
    if (check && player.action.type == ActionType.Emote) {
        player.message("Please wait till you've finished performing your current emote.")
        return
    }
    player.action(ActionType.Emote) {
        withContext(NonCancellable) {
            block.invoke(this@action)
            delay(1)
        }
    }
}