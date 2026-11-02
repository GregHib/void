package content.area.misthalin.varrock

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Drunks : Script {
    init {
        npcOperate("Talk-to", "woman_varrock,man_varrock_3,drunken_man_falador") {
            player<Happy>("Hello.")
            npc<Drunk>("... whassup?")
            player<Quiz>("Are you alright?")
            npc<Drunk>("... see ... two of you ... why there two of you?")
            player<Confused>("There's only one of me, friend.")
            npc<Drunk>("... no, two of you... you can't count...<br>... maybe you drunk too much...")
            player<Laugh>("Whatever you say, friend.")
            npc<Shock>("... giant hairy cabbages...")
        }
    }
}
