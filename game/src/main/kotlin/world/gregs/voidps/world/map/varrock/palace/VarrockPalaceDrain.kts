import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.sendVariable
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.replace
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.activity.bank.hasBanked
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Suspicious
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.sound.playSound

val logger = InlineLogger()

on<ObjectOption>({ obj.id == "varrock_palace_drain" && option == "Search" }) { player: Player ->
    player.setAnimation("climb_down")
    if (player["demon_slayer_drain_dislodged", false] || player.hasBanked("silverlight_key_sir_prysin")) {
        player.message("Nothing interesting seems to have been dropped down here today.")
    } else {
        player<Talking>("That must be the key Sir Prysin dropped.")
        player<Suspicious>("""
            I don't seem to be able to reach it. I wonder if I can
            dislodge it somehow. That way it may go down into the
            sewers.
        """)
    }
}

on<Registered>({ it["demon_slayer_drain_dislodged", false] }) { player: Player ->
    player.sendVariable("demon_slayer_drain_dislodged")
}

on<InterfaceOnObject>({ obj.id == "varrock_palace_drain" && item.id.endsWith("of_water") }) { player: Player ->
    val replacement = when {
        item.id.startsWith("bucket_of") -> "bucket"
        item.id.startsWith("jug_of") -> "jug"
        item.id.startsWith("pot_of") -> "empty_pot"
        item.id.startsWith("bowl_of") -> "bowl"
        else -> return@on
    }
    if (!player.inventory.replace(itemSlot, item.id, replacement)) {
        logger.warn { "Issue emptying ${item.id} -> $replacement" }
        return@on
    }
    player["demon_slayer_drain_dislodged"] = true
    player.message("You pour the liquid down the drain.")
    player.setAnimation("toss_water")
    player.setGraphic("toss_water")
    player.playSound("demon_slayer_drain")
    player.playSound("demon_slayer_key_fall")
    player.weakQueue("demon_slayer_dislodge_key") {
        player<Cheerful>("""
            OK, I think I've washed the key down into the sewer.
            I'd better go down and get it!
        """)
    }
}

on<ObjectOption>({ def.stringId == "demon_slayer_rusty_key" && option == "Take" }) { player: Player ->
    if (player.inventory.add("silverlight_key_sir_prysin")) {
        item("You pick up an old rusty key.", "silverlight_key_sir_prysin", 400)
        player["demon_slayer_drain_dislodged"] = false
    }
}
