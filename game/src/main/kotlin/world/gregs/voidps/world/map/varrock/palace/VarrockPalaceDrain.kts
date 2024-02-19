package world.gregs.voidps.world.map.varrock.palace

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.activity.bank.ownsItem
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Suspicious
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.sound.playSound

val logger = InlineLogger()

objectOperate("Search", "varrock_palace_drain") {
    player.setAnimation("climb_down")
    if (player["demon_slayer_drain_dislodged", false] || player.ownsItem("silverlight_key_sir_prysin")) {
        player.message("Nothing interesting seems to have been dropped down here today.")
    } else if (player.quest("demon_slayer") == "unstarted") {
        player<Suspicious>("This is the drainpipe running from the kitchen sink to the sewer. I can see a key just inside the drain.")
    } else {
        player<Talking>("That must be the key Sir Prysin dropped.")
        player<Suspicious>("I don't seem to be able to reach it. I wonder if I can dislodge it somehow. That way it may go down into the sewers.")
    }
}

playerSpawn { player ->
    if (player["demon_slayer_drain_dislodged", false]) {
        player.sendVariable("demon_slayer_drain_dislodged")
    }
}

itemOnObjectOperate("*of_water", "varrock_palace_drain") {
    val replacement = when {
        item.id.startsWith("bucket_of") -> "bucket"
        item.id.startsWith("jug_of") -> "jug"
        item.id.startsWith("pot_of") -> "empty_pot"
        item.id.startsWith("bowl_of") -> "bowl"
        else -> return@itemOnObjectOperate
    }
    if (!player.inventory.replace(itemSlot, item.id, replacement)) {
        logger.warn { "Issue emptying ${item.id} -> $replacement" }
        return@itemOnObjectOperate
    }
    player["demon_slayer_drain_dislodged"] = true
    player.message("You pour the liquid down the drain.")
    player.setAnimation("toss_water")
    player.setGraphic("toss_water")
    player.playSound("demon_slayer_drain")
    player.playSound("demon_slayer_key_fall")
    player.weakQueue("demon_slayer_dislodge_key") {
        if (player.quest("demon_slayer") == "key_hunt") {
            player<Cheerful>("OK, I think I've washed the key down into the sewer. I'd better go down and get it!")
        } else {
            player<Suspicious>("I think that dislodged something from the drain. It's probably gone down to the sewers below.")
        }
    }
}

objectOperate("Take", "demon_slayer_rusty_key") {
    if (player.inventory.add("silverlight_key_sir_prysin")) {
        player["demon_slayer_drain_dislodged"] = false
        item("silverlight_key_sir_prysin", 400, "You pick up an old rusty key.")
    }
}
