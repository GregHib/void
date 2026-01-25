package content.area.misthalin.varrock.palace

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class VarrockPalaceDrain : Script {

    val logger = InlineLogger()

    init {
        playerSpawn {
            if (get("demon_slayer_drain_dislodged", false)) {
                sendVariable("demon_slayer_drain_dislodged")
            }
        }

        objectOperate("Search", "varrock_palace_drain*") {
            anim("climb_down")
            if (get("demon_slayer_drain_dislodged", false) || ownsItem("silverlight_key_sir_prysin")) {
                message("Nothing interesting seems to have been dropped down here today.")
            } else if (quest("demon_slayer") == "unstarted") {
                player<Shifty>("This is the drainpipe running from the kitchen sink to the sewer. I can see a key just inside the drain.")
            } else {
                player<Idle>("That must be the key Sir Prysin dropped.")
                player<Shifty>("I don't seem to be able to reach it. I wonder if I can dislodge it somehow. That way it may go down into the sewers.")
            }
        }

        itemOnObjectOperate("*of_water", "varrock_palace_drain*") { (_, item, slot) ->
            val replacement = when {
                item.id.startsWith("bucket_of") -> "bucket"
                item.id.startsWith("jug_of") -> "jug"
                item.id.startsWith("pot_of") -> "empty_pot"
                item.id.startsWith("bowl_of") -> "bowl"
                else -> return@itemOnObjectOperate
            }
            if (!inventory.replace(slot, item.id, replacement)) {
                logger.warn { "Issue emptying ${item.id} -> $replacement" }
                return@itemOnObjectOperate
            }
            set("demon_slayer_drain_dislodged", true)
            message("You pour the liquid down the drain.")
            anim("toss_water")
            gfx("toss_water")
            sound("demon_slayer_drain")
            sound("demon_slayer_key_fall")
            if (quest("demon_slayer") == "key_hunt") {
                player<Happy>("OK, I think I've washed the key down into the sewer. I'd better go down and get it!")
            } else {
                player<Shifty>("I think that dislodged something from the drain. It's probably gone down to the sewers below.")
            }
        }

        objectOperate("Take", "demon_slayer_rusty_key") {
            if (inventory.add("silverlight_key_sir_prysin")) {
                set("demon_slayer_drain_dislodged", false)
                item("silverlight_key_sir_prysin", 400, "You pick up an old rusty key.")
            }
        }
    }
}
