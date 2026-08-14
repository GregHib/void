package content.area.asgarnia.burthorpe

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class HeroesGuild : Script {

    init {
        itemOnObjectOperate("amulet_of_glory*", "fountain_of_heroes") {
            var found = false
            for (index in inventory.items.indices) {
                val item = inventory[index]
                if (item.id.startsWith("amulet_of_glory") && item.id != "amulet_of_glory_4" && inventory.replace(index, item.id, if (item.id.startsWith("amulet_of_glory_t")) "amulet_of_glory_t_4" else "amulet_of_glory_4")) {
                    found = true
                }
            }
            if (found) {
                message("You dip the amulet in the fountain...")
                anim("climb_down")
                item("amulet_of_glory", "You feel a power emanating from the fountain as it recharges your amulet. You can now rub the amulet to teleport and wear it to get more gems whilst mining.")
            } else {
                message("You don't have any amulets of glory that the fountain can recharge.")
            }
        }

        npcOperate("Talk-to", "achietties") {
            npc<Neutral>("Greetings. Welcome to the Heroes' Guild.")
            if (questCompleted("heroes_quest")) {
                return@npcOperate
            }
            npc<Neutral>("Only the greatest heroes of this land may gain entrance to this guild.")
            player<Quiz>("I'm a hero. May I apply to join?")
            npc<Confused>("You're a hero? I've never heard of you.")
            statement("You do not meet all of the requirements to start the Heroes' Quest.")
        }
    }
}
