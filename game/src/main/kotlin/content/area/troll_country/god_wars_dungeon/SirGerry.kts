package content.area.troll_country.god_wars_dungeon

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Upset
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.inventoryItem
import content.quest.messageScroll
import content.quest.questCompleted
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace

npcOperate("Talk-to", "sir_gerry_*") {
    if (player.ownsItem("knights_notes") || player["godwars_knights_notes", false]) {
        player.message("He's still alive, but in no condition to talk. He appears to be almost unconscious.")
        return@npcOperate
    }
    player<Quiz>("Who are you? What are you doing here in the snow?")
    npc<Scared>("My name is...Sir Gerry. I am...a member of a secret...society of knights. My time is short and I need...your help.")
    if (player.questCompleted("recruitment_drive")) {
        player<Quiz>("A secret society of knights? You don't mean the Temple Knights, do you?")
        npc<Happy>("Yes! Praise Saradomin! You...have been sent in...my hour of need. Please, take...this scroll to Sir Tiffy in Falador park... You should not...read it.")
    } else {
        player<Quiz>("A secret society of knights? What a surprise! Is there an old charter or decree that says if you're a knight you have to belong to a secret order?")
        npc<Upset>("I'm sorry, my friend... I do not understand your meaning. Please, time is short... Take this scroll to Sir Tiffy. You will find him in Falador park... You should not...read it... It contains information for his eyes only.")
    }
    statement("The knight hands you a scroll.")
    player.inventory.add("knights_notes")
}

objectOperate("Search", "godwars_knight*") {
    if (player.ownsItem("knights_notes") || player.ownsItem("knights_notes_opened")) {
        player.message("You find nothing of value on the knight.")
        return@objectOperate
    }
    player.message("You find some handwritten notes on the knight.")
    player.inventory.add("knights_notes")
}

inventoryItem("Open", "knights_notes") {
    choice("The scroll is sealed. Do you still want to open it?") {
        option("Yes") {
            if (player.inventory.replace(item.id, "knights_notes_opened")) {
                player.message("You break the wax seal and open the scroll.")
                open(player)
            }
        }
        option("No")
    }
}

objectOperate("Tie-rope", "godwars_hole") {
    if (!player.inventory.contains("rope")) {
        return@objectOperate
    }
    if (player["godwars_knights_notes", false] || player.ownsItem("knights_notes") || player.ownsItem("knights_notes_opened")) {
        player.inventory.remove("rope")
        player["godwars_entrance_rope"] = true
    } else {
        npc<Scared>("sir_gerry", "Cough... Hey, over here.")
    }
}

inventoryItem("Read", "knights_notes_opened") {
    open(player)
}

fun open(player: Player) {
    player.messageScroll(
        lines = listOf(
            "",
            "",
            "My friend, you were right to send me to investigate the",
            "dwarf's drunken claims, for I have discovered a treasure",
            "beyond our wildest dreams. The aviansie are alive, and I",
            "suspect they still guard the Godsword! Beneath the remnants",
            "of the temple a great battle is being fought between followers",
            "of Bandos, Armadyl, Saradomin and Zamorak. My command was",
            "slaughtered and I am grievously wounded. YOU MUST PREVENT",
            "THE GODSWORD FROM FALLING INTO THE WRONG HANDS! I do not",
            "know how I am going to get this message to you, why is that",
            "talking skull never around when he's needed? Your comrade,",
            "Sir Gerry.",
        ),
    )
}
