package content.area.misthalin.paterdomus

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.member.misc.pipProgress
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.type.Tile

class TempleDoor : Script {

    init {

        objectOperate("Knock-at", "priestperiltempledoor*") { (target) ->
            sound("knock_knock")
            when {
                pipProgress >= 4 && tile.x == 3406 -> {
                    statement("You knock at the door.<br>The door swings open as you knock.")
                    openTempleDoor(target.y)
                }
                pipProgress >= 4 -> {
                    statement("You knock at the door, but nobody outside replies. You feel a little foolish.")
                }
                pipProgress == 3 -> droneVoicesProgress3()
                pipProgress == 2 -> droneVoicesProgress2()
                pipProgress == 1 -> droneVoicesProgress1()
                else -> statement("You knock at the door...<br>Doesn't seem like anyone's home.")
            }
        }
    }

    // ===== Per-stage dialogues (the two voices: Drezel = red, prompter = blue) =====

    private suspend fun Player.droneVoicesProgress3() {
        statement(" You knock at the door...<br>" +
                "You hear a voice from inside.<br>" +
                "<col=000080>You again? What do you want now?")
        player<Happy>("I killed that dog for you.")
        statement("<col=000080>HAHAHAHAHA!<br>" +
                "<col=000080>Really? Hey, that's great!<br>" +
                "<col=800000>Yeah thanks a lot buddy!<br>" +
                "<col=800000>HAHAHAHAHAHA")
        player<Quiz>("What's so funny?")
        statement("<col=000080>HAHAHAHA nothing buddy! We're just so grateful to you!<br>" +
                "<col=000080>HAHAHA<br>" +
                "<col=800000>Yeah, maybe you should go tell the King what a great job you did<br>" +
                "<col=800000>buddy! HAHAHA")
    }

    private suspend fun Player.droneVoicesProgress2() {
        statement("You knock at the door...<br>" +
                "You hear a voice from inside.<br>" +
                "<col=000080>Hello?")
        player<Quiz>("What am I supposed to be doing again?")
        statement("<col=000080>Who are you?<br>" +
                "<col=800000>(SHHHH! It's the adventurer!)<br>" +
                "<col=800000>I want you to go kill the horrible dog in the basement for me!")
        statement(" <col=800000>You can use the entrance in the mausoleum out there. You don't <col=800000>need to come inside to do it.")
        statement("<col=800000>You'll do this for good old Delzig won't ya buddy?<br>" +
                "<col=000080>(Drezel!)<br>" +
                "<col=800000>*cough* for good old Drezel right buddy?")
    }

    private suspend fun Player.droneVoicesProgress1() {
        statement("You knock at the door...<br>You hear a voice from inside.<br><col=000080>Who are you and what do you want?")
        player<Neutral>("Ummmm.....")
        choice {
            option<Neutral>("Roald sent me to check on Drezel.") {
                statement("<col=000080>(Psst... Hey... Who's Roald? Who's Drezel?)<br>" +
                        "<col=800000>(Uh... isn't Drezel that dude upstairs? Oh, wait, Roald's the King of Varrock right?)")
                statement("<col=000080>(He is??? Aw man...)<br>" +
                        "<col=000080>(Hey, you deal with this okay?)<br>" +
                        "<col=000080>He's just coming! Wait a second!")
                statement("<col=800000>Hello, my name is Drevil.<br>" +
                        "<col=000080>(Drezel!)<br>" +
                        "<col=800000>I mean Drezel. How can I help?")
                player<Neutral>("Well, as I said, the King sent me to check you're alright.")
                statement("<col=800000>And, uh, what would you do if everything wasn't okay with me?")
                player<Confused>("I'm not sure.<br>Ask you what help you need I suppose.")
                statement("<col=800000>Ah, good, well, I don't think...<br>" +
                        "<col=000080>(Psst... hey... the dog!)<br>" +
                        "<col=800000>OH! Yes, of course!<br>" +
                        "<col=800000>Will you do me a favour, adventurer?")
                choice {
                    option("Sure.") {
                        player<Happy>("Sure. I'm a helpful person!")
                        pipProgress = 2
                        statement(" <col=800000>HAHAHAHA! Really? Thanks buddy! You see that mausoleum out" +
                                "there? There's a horrible big dog underneath it that I'd like you to" +
                                "<col=800000>kill for me! It's been really bugging me! Barking all the time and" +
                                "<col=800000>stuff! Please kill it for me buddy!")
                        player<Happy>("Okey-dokey, one dead dog coming up.")
                    }
                    option("Nope.") {
                        player<Shifty>("No, something about all this is very suspicious...")
                        statement("<col=800000>Get lost then! I have important things to do, as sure as my name is" +
                                "<col=800000>Dibzil.<br>" +
                                "<col=000080>(Drezel!)<br>" +
                                "<col=800000>Drezel. Go away!")
                    }
                }
            }
            option("Hi, I just moved in next door...") {
                player<Happy>("Hi, I just moved in next door...<br>Can I borrow a cup of coffee?")
                statement("<col=000080>...<br>" +
                        "<col=000080>What next door???<br>" +
                        "<col=000080>What's coffee???<br>" +
                        "<col=000080>Who ARE you???")
            }
            option("I hear this is a place of historical interest.") {
                player<Happy>(
                    "I hear this place is of historic interest. Can I come in" +
                        "and have a wander around? Possibly look at some" +
                        "antiques or buy something from your gift shop?"
                )
                statement("<col=000080>(Pssst... Hey...)<br>" +
                        "<col=000080>(Is this place of historic interest?)<br>" +
                        "<col=800000>(I dunno. I guess it might be. Does it matter?)")
                statement("<col=000080>(I suppose not.)<br>" +
                        "<col=000080>Clear off! You can't come in!")
            }
            option<Neutral>("The council sent me to check your pipes.") {
                statement("<col=000080>They did? Ummm....<br>" +
                        "<col=000080>(Psst... are there any pipes in here, you reckon?)<br>" +
                        "<col=800000>(I dunno... don't think so...)<br>" +
                        "<col=000080>We don't have any thanks! Bye!")
            }
        }
    }

    // ===== Door-open helper (mirrors PriestInPeril.kt's Open handler) =====
    // Worth deduplicating with PriestInPeril.openTempleDoor if/when extracted.

    private suspend fun Player.openTempleDoor(doorY: Int) {
        sound("iron_door_open")
        walkTo(
            target = Tile(if (tile.x == 3406) 3407 else 3406, doorY),
            forceWalk = true,
            noCollision = true,
        )
        GameObjects.add(
            id = "inviswall",
            tile = Tile(3406, 3488),
            shape = ObjectShape.WALL_STRAIGHT,
            rotation = 2,
            ticks = 2,
        )
        GameObjects.add(
            id = "inviswall",
            tile = Tile(3406, 3489),
            shape = ObjectShape.WALL_STRAIGHT,
            rotation = 2,
            ticks = 2,
        )
        val leftDoor = GameObjects.find(Tile(3406, 3489), "priestperiltempledoorl")
        leftDoor.replace(
            id = "inactivetempledoorl",
            tile = Tile(3407, 3488),
            shape = leftDoor.shape,
            rotation = 1,
            ticks = 3,
        )
        val rightDoor = GameObjects.find(Tile(3406, 3488), "priestperiltempledoorr")
        rightDoor.replace(
            id = "inactivetempledoorr",
            tile = Tile(3407, 3489),
            shape = leftDoor.shape,
            rotation = 1,
            ticks = 3,
        )
        delay(2)
    }
}