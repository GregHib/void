package content.area.asgarnia.burthorpe

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.Uncertain
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
        itemOnObjectOperate("amulet_of_glory", "fountain_of_heroes") {
            if (inventory.replace(it.slot, it.item.id, "amulet_of_glory_4")) {
                message("You dip the amulet in the fountain...")
                anim("human_pickupfloor")
                item("amulet_of_glory", 300, "You feel a power emanating from the fountain as it recharges your amulet. You can now rub the amulet to teleport and wear it to get more gems whilst mining.")
            }
        }

        npcOperate("Talk-to", "achietties") {
            npc<Talk>("Greetings. Welcome to the Heroes' Guild.")
            if (questCompleted("heroes_quest")) {
                return@npcOperate
            }
            npc<Talk>("Only the greatest heroes of this land may gain entrance to this guild.")
            player<Quiz>("I'm a hero. May I apply to join?")
            npc<Uncertain>("You're a hero? I've never heard of you.")
            statement("You do not meet all of the requirements to start the Heroes' Quest.")
        }
    }
}
