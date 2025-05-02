package content.area.misthalin.barbarian_village.stronghold_of_security

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Upset
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.items
import content.entity.player.dialogue.type.statement
import content.entity.sound.jingle
import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

objectOperate("Open", "gift_of_peace") {
    if (player["unlocked_emote_flap", false]) {
        statement("You have already claimed your reward from this level.")
        return@objectOperate
    }
    if (!player.inventory.add("coins", 2000)) {
        statement("Make some room in your inventory, then come back to us...")
        return@objectOperate
    }
    player.sound("stronghold_creaks")
    statement("The box hinges creak and appear to be forming audible words....")
    player.jingle("stronghold_of_security_gift_of_peace")
    player["unlocked_emote_flap"] = true
    item("coins_9", 400, "...congratulations adventurer, you have been deemed worthy of this reward. You have also unlocked the Flap emote!")
}

objectOperate("Search", "grain_of_plenty") {
    if (player["unlocked_emote_slap_head", false]) {
        statement("You have already claimed your reward from this level.")
        return@objectOperate
    }
    if (!player.inventory.add("coins", 3000)) {
        statement("Make some room in your inventory, then come back to us...")
        return@objectOperate
    }
    player.sound("stronghold_creaks")
    statement("The grain shifts in the sack, sighing audible words....")
    player.jingle("stronghold_of_security_grain_of_plenty")
    player["unlocked_emote_slap_head"] = true
    item("coins_9", 400, "...congratulations adventurer, you have been deemed worthy of this reward. You have also unlocked the Slap Head emote!")
}

objectOperate("Open", "box_of_health") {
    if (player["unlocked_emote_idea", false]) {
        statement("You have already claimed your reward from this level.")
        return@objectOperate
    }
    if (!player.inventory.add("coins", 5000)) {
        statement("Make some room in your inventory, then come back to us...")
        return@objectOperate
    }
    player.sound("stronghold_creaks")
    statement("The box hinges creak and appear to be forming audible words....")
    player.levels.restore(Skill.Constitution, 99)
    player.levels.restore(Skill.Prayer, 990)
    player.message("You feel refreshed and renewed.")
    player.jingle("stronghold_of_security_box_of_health")
    player["unlocked_emote_idea"] = true
    item("coins_10", 400, "...congratulations adventurer, you have been deemed worthy of this reward. You have also unlocked the Idea emote!")
}

objectOperate("Search", "cradle_of_life") {
    player.sound("stronghold_choir")
    statement("As your hand touches the cradle, you hear a voice in your head of a million dead adventurers...")
    if (player["unlocked_emote_stomp", false]) {
        if (player.ownsItem("fancy_boots") || player.ownsItem("fighting_boots")) {
            choice("Would you like to swap your boots to the other style?") {
                option<Happy>("Yes, I'd like the other pair, please.") {
                    if (player.inventory.contains("fancy_boots")) {
                        player.inventory.replace("fancy_boots", "fighting_boots")
                    } else if (player.inventory.contains("fighting_boots")) {
                        player.inventory.replace("fighting_boots", "fancy_boots")
                    } else {
                        statement("Your boots need to be in your inventory to swap them.") // Unknown message
                    }
                }
                option<Upset>("No thanks, I'll keep these.")
            }
            return@objectOperate
        }
        statement("You appear to have lost your boots.")
    }
    statement("Welcome adventurer... you have a choice.")
    items("fancy_boots", "fighting_boots", "You can choose between these two pairs of boots.") // Zoom 400
    statement("They will both protect your feet in exactly the same manner, however, they look very different. You can always come back and get another pair if you lose them, or even swap them for the other style!")
    choice {
        option<Happy>("I'll take the colourful ones.") {
            if (player.inventory.add("fancy_boots")) {
                player.jingle("stronghold_of_security_cradle_of_life")
                player["unlocked_emote_stomp"] = true
                item("fancy_boots", 400, "Congratulations! You have successfully navigated the Stronghold of Security and claimed your reward. You have unlocked the Stomp emote.")
            }
        }
        option<Happy>("I'll take the fighting ones.") {
            if (player.inventory.add("fighting_boots")) {
                player.jingle("stronghold_of_security_cradle_of_life")
                player["unlocked_emote_stomp"] = true
                item("fighting_boots", 400, "Congratulations! You have successfully navigated the Stronghold of Security and claimed your reward. You have unlocked the Stomp emote.")
            }
        }
    }
}

objectOperate("Search", "stronghold_dead_explorer") {
    player.anim("pick_pocket")
    player.sound("pick")
    if (player.holdsItem("stronghold_notes")) {
        player.message("You don't find anything.")
        return@objectOperate
    }
    if (!player.inventory.add("stronghold_notes")) {
        statement("I'd better make room in my inventory first!")
        return@objectOperate
    }
    statement("You rummage around in the dead explorer's bag.....")
    statement("You find a book of hand written notes.")
}
