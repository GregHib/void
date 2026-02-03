package content.area.misthalin.wizards_tower

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script

class ProfessorOnglewip : Script {

    init {
        npcOperate("Talk-to", "professor_onglewip") { (target) ->
            player<Quiz>("Do you live here too?")
            npc<Happy>("Oh no, I come from the Gnome Stronghold. I've been sent here by King Narnode to learn about human magics.")
            player<Quiz>("So where's this Gnome Stronghold?")
            npc<Happy>("It's in the North West of the continent - a long way away. You should visit us there some time. The food's great, and the company's delightful.")
            player<Happy>("I'll try and make time for it. Sounds like a nice place.")
            npc<Happy>("Well, it's full of gnomes. How much nicer could it be?")
        }
    }
}
