package world.gregs.voidps.world.map.musicians

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.interact.dialogue.Drunk
import world.gregs.voidps.world.interact.dialogue.Pleased
import world.gregs.voidps.world.interact.dialogue.Quiz
import world.gregs.voidps.world.interact.dialogue.RollEyes
import world.gregs.voidps.world.interact.dialogue.type.PlayerChoice
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "drunken_musician") {
    choice()
}

suspend fun CharacterContext.choice() {
    choice {
        option<Quiz>("Who are you?") {
            npc<Drunk>("Me? I'sh mooshian! Lemme her help youse relaxsh: sit down, reshst your weery limz an' stuff. You'll feel mush better. Like me, I ffeel great!")
            player<Quiz>("You're drunk, aren't you?")
            npc<Drunk>("I'm jus' relaxshed, mate.")
            player<Quiz>("I'm not sure I want to be as relaxed as you are.")
            npc<Drunk>("Youze'll never be as relaxshed as as I am, I worked hard to get this relaxshed.")
            player<RollEyes>("Clearly...")
            choice()
        }
        option("Can I ask you some questions about resting?") {
            resting()
        }
        exit()
    }
}

suspend fun CharacterContext.resting() {
    choice("Can I ask you some questions about resting?") {
        option("How does resting work?") {
            player<Quiz>("So how does resting work?")
            npc<Drunk>("Well, youze sit down and resht. Then you feel better. Mush better.")
            npc<Drunk>("If youze are lissening to my relaxshing moozik then iss even bettar. Relaxshing moozik, like mine.")
            player<Quiz>("Right; that's nice and clear. Thanks.")
            resting()
        }
        option<Pleased>("What's special about resting by a musician?") {
            npc<Drunk>("Moozik's great! My moozik is the bessht. Mush more relaxshing than those else.")
            resting()
        }
        option<Pleased>("Can you summarise the effects for me?") {
            npc<Drunk>("Yeshh, 'course. 'f youze sit down you resht. Moozik make reshting better.")
            resting()
        }
        exit()
    }
}

suspend fun PlayerChoice.exit(): Unit = option<Quiz>("That's all for now") {
    npc<Drunk>("Fanks. Sshtay relaxshed!")
}