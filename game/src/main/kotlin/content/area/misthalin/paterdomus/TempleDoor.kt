package content.area.misthalin.paterdomus

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound

class TempleDoor : Script {

    init {
        objectOperate("Knock-at", "priestperiltempledoor*") { (target) ->
            sound("knock_knock")
            when (quest("priest_in_peril")) {
                "dog_dead" -> droneVoicesProgress3()
                "kill_dog" -> droneVoicesProgress2()
                "find_drezel" -> droneVoicesProgress1()
                "unstarted" -> statement("You knock at the door...<br>Doesn't seem like anyone's home.")
                else -> if (tile.x == 3406) {
                    statement("You knock at the door.<br>The door swings open as you knock.")
                    enterDoor(target, delay = 2)
                } else {
                    statement("You knock at the door, but nobody outside replies. You feel a little foolish.")
                }
            }
        }
    }

    // ===== Per-stage dialogues (the two voices: Drezel = red, prompter = blue) =====

    private suspend fun Player.droneVoicesProgress3() {
        statement("You knock at the door...<br>You hear a voice from inside.<br><navy>You again? What do you want now?")
        player<Happy>("I killed that dog for you.")
        statement("<navy>HAHAHAHAHA!<br><navy>Really? Hey, that's great!<br><maroon>Yeah thanks a lot buddy!<br><maroon>HAHAHAHAHAHA")
        player<Quiz>("What's so funny?")
        statement("<navy>HAHAHAHA nothing buddy! We're just so grateful to you!<br><navy>HAHAHA<br><maroon>Yeah, maybe you should go tell the King what a great job you did<br><maroon>buddy! HAHAHA")
    }

    private suspend fun Player.droneVoicesProgress2() {
        statement("You knock at the door...<br>You hear a voice from inside.<br><navy>Hello?")
        player<Quiz>("What am I supposed to be doing again?")
        statement("<navy>Who are you?<br><maroon>(SHHHH! It's the adventurer!)<br><maroon>I want you to go kill the horrible dog in the basement for me!")
        statement(" <maroon>You can use the entrance in the mausoleum out there. You don't <maroon>need to come inside to do it.")
        statement("<maroon>You'll do this for good old Delzig won't ya buddy?<br><navy>(Drezel!)<br><maroon>*cough* for good old Drezel right buddy?")
    }

    private suspend fun Player.droneVoicesProgress1() {
        statement("You knock at the door...<br>You hear a voice from inside.<br><navy>Who are you and what do you want?")
        player<Neutral>("Ummmm.....")
        choice {
            option<Neutral>("Roald sent me to check on Drezel.") {
                statement("<navy>(Psst... Hey... Who's Roald? Who's Drezel?)<br><maroon>(Uh... isn't Drezel that dude upstairs? Oh, wait, Roald's the King of Varrock right?)")
                statement("<navy>(He is??? Aw man...)<br><navy>(Hey, you deal with this okay?)<br><navy>He's just coming! Wait a second!")
                statement("<maroon>Hello, my name is Drevil.<br><navy>(Drezel!)<br><maroon>I mean Drezel. How can I help?")
                player<Neutral>("Well, as I said, the King sent me to check you're alright.")
                statement("<maroon>And, uh, what would you do if everything wasn't okay with me?")
                player<Confused>("I'm not sure.<br>Ask you what help you need I suppose.")
                statement("<maroon>Ah, good, well, I don't think...<br><navy>(Psst... hey... the dog!)<br><maroon>OH! Yes, of course!<br><maroon>Will you do me a favour, adventurer?")
                choice {
                    option("Sure.") {
                        player<Happy>("Sure. I'm a helpful person!")
                        set("priest_in_peril", "kill_dog")
                        statement("<maroon>HAHAHAHA! Really? Thanks buddy! You see that mausoleum out there? There's a horrible big dog underneath it that I'd like you to <maroon>kill for me! It's been really bugging me! Barking all the time and <maroon>stuff! Please kill it for me buddy!")
                        player<Happy>("Okey-dokey, one dead dog coming up.")
                    }
                    option("Nope.") {
                        player<Shifty>("No, something about all this is very suspicious...")
                        statement("<maroon>Get lost then! I have important things to do, as sure as my name is<maroon>Dibzil.<br><navy>(Drezel!)<br><maroon>Drezel. Go away!")
                    }
                }
            }
            option("Hi, I just moved in next door...") {
                player<Happy>("Hi, I just moved in next door...<br>Can I borrow a cup of coffee?")
                statement("<navy>...<br><navy>What next door???<br><navy>What's coffee???<br><navy>Who ARE you???")
            }
            option("I hear this is a place of historical interest.") {
                player<Happy>("I hear this place is of historic interest. Can I come inand have a wander around? Possibly look at someantiques or buy something from your gift shop?")
                statement("<navy>(Pssst... Hey...)<br><navy>(Is this place of historic interest?)<br><maroon>(I dunno. I guess it might be. Does it matter?)")
                statement("<navy>(I suppose not.)<br><navy>Clear off! You can't come in!")
            }
            option<Neutral>("The council sent me to check your pipes.") {
                statement("<navy>They did? Ummm....<br><navy>(Psst... are there any pipes in here, you reckon?)<br><maroon>(I dunno... don't think so...)<br><navy>We don't have any thanks! Bye!")
            }
        }
    }
}
