package content.quest.member.tale_of_the_muspah

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.skillLamp
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

/**
 * The natural historian north of Rellekka rewards locating the Muspah statue after The Tale of the Muspah.
 * https://runescape.wiki/w/Transcript:The_Tale_of_the_Muspah#Natural_Historian
 */
class NaturalHistorianRellekka : Script {

    init {
        npcOperate("Talk-to", "natural_historian_rellekka_2") {
            if (get("muspah_statue_reported", false)) {
                npc<Happy>("How splendid to see you again. I can't wait to study the statue you found in the desert.")
                player<Quiz>("Are you still looking for yetis?")
                npc<Sad>("Yes, but I think I'll have to give up here. I'm sure the locals could help, but they won't even talk to me.")
                npc<Sad>("They just mutter about some kind of trials, then set me a bunch of impossible tasks!")
                player<Neutral>("Perhaps you'll have better luck with the desert statue.")
                npc<Happy>("I do hope so.")
                return@npcOperate
            }
            reward()
        }

        // 200 experience in any skill at or over level 10 https://runescape.wiki/w/The_Tale_of_the_Muspah?oldid=4169342
        itemOption("Rub", "antique_lamp_the_tale_of_the_muspah") { (item, slot) ->
            val skill = skillLamp()
            if (levels.get(skill) < 10) {
                statement("<red>This skill is not high enough to gain experience from this lamp.")
                return@itemOption
            }
            if (inventory.remove(slot, item.id)) {
                exp(skill, 200.0)
                statement("<blue>Your wish has been granted!<br><black>You have been awarded 200 ${skill.name} experience!")
            }
        }
    }

    private suspend fun Player.reward() {
        player<Happy>("Hello, I've come to claim a reward.")
        npc<Quiz>("How wonderful. And what is this reward for?")
        player<Happy>("I've managed to locate the statue of the Muspah.")
        npc<Happy>("Of course! The mysterious desert statue. Now, if you could just pinpoint its location on my map for me...")
        if (inventory.isFull()) {
            inventoryFull()
            return
        }
        statement("You mark the location of the Muspah statue on the historian's map.")
        npc<Happy>("That's wonderful, now we'll be able to study it when the opportunity arises.")
        npc<Happy>("As promised, here's a token of our thanks!")
        if (!inventory.add("antique_lamp_the_tale_of_the_muspah")) {
            inventoryFull()
            return
        }
        set("muspah_statue_reported", true)
        item("antique_lamp_the_tale_of_the_muspah", "The historian carefully hands you an antique lamp.")
    }
}
