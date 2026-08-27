package content.area.kandarin.yanille

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class GuardCaptainYanille : Script {
    init {
        npcOperate("Talk-to", "guard_captain") {
            when (quest("hand_in_the_sand")) {
                "investigate_hand" -> handDelivery()
                "ask_wizards" -> wizardsAreToBlame()
                else -> {
                    player<Quiz>("Excuse me...")
                    npc<Drunk>("I don' need a hand drinkin me beer, go 'way!")
                    player<Quiz>("But...")
                    npc<Drunk>("Talk to tha' hand coz thish face ain't lishtnin'!")
                }
            }
        }
    }

    private suspend fun Player.handDelivery() {
        if (!inventory.contains("sandy_hand")) {
            statement(
                "Perhaps you should be carrying the hand that Bert gave you as " +
                    "evidence of the crime.",
            )
            return
        }
        if (!inventory.contains("beer")) {
            npc<Drunk>("Need more beer...")
            return
        }
        player<Neutral>("Sir? I have some more beer for you...")
        item(item = "beer", text = "You give the beer to the Guard Captain who takes a large gulp.")
        if (!inventory.remove("beer")) {
            return
        }
        sound("handsand_gulp")
        npc<Drunk>("Ahh... jus' wha' I need, now, wha' did you wanna know?")
        player<Sad>("I've come to report that Bert, the sandman, found a hand in the sand pit.")
        npc<Happy>("Lucky for him, means he can get even more work done.")
        player<Shock>("But aren't you going to find out who it ... belonged to?")
        if (!inventory.remove("sandy_hand")) {
            return
        }
        set("hand_in_the_sand", "ask_wizards")
        sound("handsand_drop_hand")
        addOrDrop("beer_hand")
        item(
            item = "beer_hand",
            text = "You hand the... hand... to the Guard Captain, he " +
                "fumbles with it, drops it in his beer, fishes it out and " +
                "hands it back.",
        )
        npc<Drunk>(
            "Oops, No 'arm done. S'prob'ly a wizard, i's always the " +
                "wizards fault, go ask them, jus' ring the bell outshide the " +
                "guild and talk to the first pointy hatted ninny you shee!",
        )
        player<Neutral>("Err... ok, I'll go ring the bell and talk to a wizard then.")
    }

    private suspend fun Player.wizardsAreToBlame() {
        player<Neutral>("Hello Sir!")
        npc<Drunk>(
            "Go 'way! This pint'sh nearly finished! Unlessh you got " +
                "more that ish....? Wizards, s'all the wizard's fault...prob'ly " +
                "that Zavistic one, he'sh the worsht!",
        )
        if (ownsItem("beer_hand")) {
            player<Neutral>("I think I should go talk to the wizards in the guild before he makes me buy him more beer!")
            return
        }
        npc<Drunk>("E're, you left this 'and in me beer!")
        if (inventory.spaces < 1) {
            npc<Drunk>("No good if you don' have space fer it in yer invent'ry, come back when you do.")
            return
        }
        sound("handsand_take_hand")
        addOrDrop("beer_hand")
        statement("The Guard Captain fishes the hand out of his beer and hands it to you.")
    }
}
