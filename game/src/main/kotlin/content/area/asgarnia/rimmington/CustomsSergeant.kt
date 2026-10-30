package content.area.asgarnia.rimmington

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

class CustomsSergeant : Script {

    init {
        npcOperate("Talk-to", "customs_sergeant,customs_sergeant_2") { (target) ->
            talk(target.id)
        }

        objectOperate("Talk-to", "customs_sergeant_object,customs_sergeant_object_2") { (target) ->
            if (target.intId !in setOf(4340, 31459) || !target.tile.within(Tile(2965, 3196), 2)) {
                return@objectOperate
            }
            talk("customs_sergeant")
        }

        locker()
    }

    private suspend fun Player.talk(chathead: String) {
        if (quest("rocking_out") != "unstarted") {
            return
        }
        npc<Drunk>(chathead, "Zzzzzzzzzzzzzzzzzzz.")
        player<Shifty>("Ahem.")
        npc<Drunk>(chathead, "Push off, I'm busy.")
        player<Shifty>("Okay.")
        npc<Angry>(chathead, "Now!")
    }

    private fun locker() {
        objectOperate("Store", "rimmington_locker") {
            npc<Angry>("customs_sergeant", "Hey! Nobody stores anything in there unless they are under arrest.")
        }
    }
}
