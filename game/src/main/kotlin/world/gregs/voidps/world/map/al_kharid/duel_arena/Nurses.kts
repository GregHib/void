package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Uncertain
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate({ (target.id == "sabreen" || target.id == "a_abla") && option == "Talk-to" }) { player: Player ->
    player<Cheerful>("Hi!")
    npc<Cheerful>("Hi. How can I help?")
    choice {
        option<Uncertain>("Can you heal me?") {
            heal()
        }
        fighters()
        often()
    }
}

npcOperate({ (target.id == "sabreen" || target.id == "a_abla") && option == "Heal" }) { player: Player ->
    heal()
}