package content.area.misthalin.barbarian_village

import content.entity.obj.objTeleport
import content.entity.obj.objTeleportLand
import content.entity.obj.objTeleportTakeOff
import content.entity.player.dialogue.DoorHead
import content.entity.player.dialogue.Surprised
import content.entity.player.dialogue.type.*
import content.entity.sound.jingle
import content.entity.sound.midi
import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue

objTeleportLand("Climb-down", "stronghold_of_security_entrance") {
    player.queue("stronghold_of_security_entrance") {
        statement("You squeeze through the hole and find a ladder a few feet down leading into the Stronghold of Security.")
    }
}

objTeleport("Enter", "stronghold_war_portal") {
    // TODO "You are not of sufficient experience to take the shortcut through this level."
    player.message("You enter the portal to be whisked through to the treasure room.", ChatType.Filter)
}

objTeleport("Enter", "stronghold_famine_portal") {
    player.message("You enter the portal to be whisked through to the treasure room.", ChatType.Filter)
}

objectOperate("Open", "gate_of_war*") {
    if (target.tile.y == 5238 && player.tile.y > target.tile.y) {
        npc<DoorHead>("gate_of_war", "Greetings Adventurer. This place is kept safe by the spirits within the doors. As you pass through you will be asked questions about security. Hopefully you will learn much from us.")
        npc<DoorHead>("gate_of_war", "Please pass through and begin your adventure, beware of the various monsters that dwell within.'")
    }
    player.anim("stronghold_of_security_door")
    player.sound("stronghold_of_security_through_door")
    delay()
    enterDoor()
    player.anim("stronghold_of_security_door_appear")
    player<Surprised>("Oh my! I just got sucked through that door... what a weird feeling! Still, I guess I should expect it as these evidently aren't your average kind of doors.... they talk and look creepy!")
}

objectOperate("Open", "gift_of_peace") {
    if (!player.inventory.add("coins", 2000)) {
        statement("Make some room in your inventory, then come back to us...")
        return@objectOperate
    }
//    statement("You have already claimed your reward from this level.")
    player.sound("1247")
    statement("The box hinges creak and appear to be forming audible words....")
    player.jingle("157")
    player.midi("147")
    statement("...congratulations adventurer, you have been deemed worthy of this reward. You have also unlocked the Flap emote!")
}

objectOperate("Open", "grain_of_plenty") {
    if (!player.inventory.add("coins", 3000)) {
        statement("Make some room in your inventory, then come back to us...")
        return@objectOperate
    }
//    statement("You have already claimed your reward from this level.")
    player.sound("1247")
    statement("The grain shifts in the sack, sighing audible words....")
    player.jingle("157")
    player.midi("147")
    statement("...congratulations adventurer, you have been deemed worthy of this reward. You have also unlocked the Slap Head emote!")
}

objectOperate("Open", "box_of_health") {
    if (!player.inventory.add("coins", 5000)) {
        statement("Make some room in your inventory, then come back to us...")
        return@objectOperate
    }
//    statement("You have already claimed your reward from this level.")
    player.sound("1247")
    statement("The box hinges creak and appear to be forming audible words....")
    player.message("You feel refreshed and renewed.")
    player.midi("147")
    player.jingle("177")
    statement("...congratulations adventurer, you have been deemed worthy of this reward. You have also unlocked the Idea emote!")
}

objectOperate("Open", "cradle_of_life") {
    if (!player.inventory.add("coins", 5000)) {
        statement("Make some room in your inventory, then come back to us...")
        return@objectOperate
    }
//    statement("You have already claimed your reward from this level.")
    player.sound("1246")
    statement("As your hand touches the cradle, you hear a voice in your head of a million dead adventurers...")
    statement("... welcome adventurer... you have learnt the value of securing your account, and may claim your prize...")
    items("9005", "9006", "You may claim these boots. They protect your feet equally, but they look very different.") // Zoom 400
    items("9006", "28672", "You can return here for more boots at any time, or to get replacements if you lose them.") // Zoom 400
    // TODO choice dialogue
    val choice = "9006"
    if (player.inventory.add(choice)) {
        player.midi("147")
        player.jingle("158")
        item(choice, 400, "You claim your prize: Fancier boots You have unlocked the 'Stamp Foot' emote.")
    }
}

/*
    Level 1
    You climb up the ladder which seems to twist and wind in all directions.
    Tele out to top

    Level 2
    You shin up the rope, squeeze through a passage then climb a ladder.
    You climb up the ladder which seems to twist and wind in all directions.
    Tele to stairs

    Level 3
    You shin up the rope, squeeze through a passage then climb a ladder.
    You climb up the ladder which seems to twist and wind in all directions.
    Tele to stairs

    Level 4
    You shin up the rope, squeeze through a passage then climb a ladder.
    You climb up the ladder which seems to twist and wind in all directions.
    Tele out to top

    You climb up the ladder to the level above.
    You climb up the ladder to the surface.
 */

objectOperate("Search", "stronghold_dead_explorer") {
    player.anim("pick_pocket")
    player.sound("2581")
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

// message_game	type=spam, message='You climb up the chain very very carefully, squeeze through a passage then climb a ladder.'
// message_game	type=spam, message='You climb up the ladder which seems to twist and wind in all directions.'
fun ObjectOption<Player>.enterDoor() {
    when (target.rotation) {
        0 -> if (player.tile.x >= target.tile.x) {
            player.tele(target.tile.addX(-1))
        } else {
            player.tele(target.tile)
        }
        1 -> if (player.tile.y > target.tile.y) {
            player.tele(target.tile)
        } else {
            player.tele(target.tile.addY(1))
        }
        2 -> if (player.tile.x < target.tile.x) {
            player.tele(target.tile)
        } else {
            player.tele(target.tile.addX(1))
        }
        3 -> if (player.tile.y >= target.tile.y) {
            player.tele(target.tile.addY(-1))
        } else {
            player.tele(target.tile)
        }
    }
}

objTeleportTakeOff("Climb-down", "stronghold_war_ladder_down") {
    // TODO Interface 579
    player.message("You climb down the ladder to the next level.")
//    cancel()
//    player<Shifty>("No thanks, I don't want to die!")
}