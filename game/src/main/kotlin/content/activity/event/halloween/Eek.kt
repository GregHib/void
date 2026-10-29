package content.activity.event.halloween

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.random
import content.entity.player.dialogue.type.npc as dialogueNpc

class Eek : Script {

    init {
        itemOption("Talk-to", "eek") {
            when (random.nextInt(5)) {
                0 -> heroDialogue()
                1 -> legsDialogue()
                2 -> spiderDialogue()
                3 -> flyingDialogue()
                else -> manSpiderDialogue()
            }
        }
    }

    private suspend fun Player.npc(npcId: String, text: String) {
        dialogueNpc<Neutral>(npcId, text)
    }

    private suspend fun Player.npc(npcId: String, text: String, title: String) {
        dialogueNpc<Neutral>(npcId, text, title = title)
    }

    private suspend fun Player.heroDialogue() {
        npc("eek_the_spider_halloween_2009", "I bet you're a mighty hero!")
        npc("eek_the_spider_halloween_2009", "$name! Hero-of-the-humans!")
        npc("eek_the_spider_halloween_2009", "You are a mighty hero, aren't you? I bet you've slain dragons and all sorts of stuff.")
        choice {
            option("That's right. I'm awesome!") {
                player<Happy>("That's right. I'm awesome!")
                npc("eek_the_spider_halloween_2009", "I could be your sidekick! Player and Eek! We could have all sorts of adventures together!")
                choice {
                    option("Yes!") {
                        player<Happy>("Yes!")
                        npc(
                            "eek_the_spider_halloween_2009",
                            "Yay! $name and Eek! We'll travel all over the world fighting for justice! And freedom! And things like that!",
                        )
                        npc("eek_the_spider_halloween_2009", "Come on, Player! Let's have an adventure!")
                    }
                    option("No. It's too dangerous.") {
                        player<Happy>("No. It's too dangerous.")
                        npc("eek_the_spider_halloween_2009", "Aww.")
                        npc(
                            "eek_the_spider_halloween_2009",
                            "You know best, though. You're so BRAVE putting yourself in danger all by yourself!",
                        )
                    }
                }
            }
            option("I'm not a great hero. I'm just an ordinary person.") {
                player<Neutral>("I'm not a great hero. I'm just an ordinary person.")
                npc("eek_the_spider_halloween_2009", "Aww. I bet you'll be a mighty hero someday, though.")
                choice {
                    option("Yes, I'm trying to advance myself.") {
                        player<Neutral>("Yes, I'm trying to advance myself.")
                        npc("eek_the_spider_halloween_2009", "Brilliant! You can do it! Come on, let's have an adventure!")
                    }
                    option("I don't know. I'm not very ambitious.") {
                        player<Neutral>("I don't know. I'm not very ambitious.")
                        npc("eek_the_spider_halloween_2009", "Aww...well...never mind.")
                    }
                }
            }
        }
    }

    private suspend fun Player.legsDialogue() {
        npc("eek_the_spider_halloween_2009", "Hey, you've only got four legs. How do you manage? Don't you fall over?")
        choice {
            option("Actually, I've only got two legs.") {
                player<Neutral>("Actually, I've only got two legs.")
                npc("eek_the_spider_halloween_2009", "Someone has stolen your legs! This is a DISASTER!")
                npc("eek_the_spider_halloween_2009", "We've got to catch the leg thief!")
                choice {
                    option("No one stole my legs.") {
                        player<Neutral>("No one stole my legs.")
                        npc("eek_the_spider_halloween_2009", "You gave your legs away?")
                        npc("eek_the_spider_halloween_2009", "That is so heroic...giving your legs away to someone without legs.")
                        npc("eek_the_spider_halloween_2009", "You're my hero!")
                    }
                    option("They're probably long gone by now.") {
                        player<Neutral>("They're probably long gone by now.")
                        npc(
                            "eek_the_spider_halloween_2009",
                            "You're right. They probably used the machine to give themselves really long running-away type legs. We'll never catch them now.",
                        )
                    }
                }
            }
            option("Oh, it's not so bad.") {
                player<Neutral>("Oh, it's not so bad.")
                npc("eek_the_spider_halloween_2009", "Then again, your legs are much longer than mine.")
                npc("eek_the_spider_halloween_2009", "How's you get such long legs? Do you have some kind of leg-extending machine?")
                npc("eek_the_spider_halloween_2009", "Where is the leg-extending machine? I want to use it!")
                choice {
                    option("There is no leg-extending machine.") {
                        player<Neutral>("There is no leg-extending machine.")
                        npc("eek_the_spider_halloween_2009", "You would say that! You want to keep it for yourself!")
                        npc("eek_the_spider_halloween_2009", "Well that's okay. Maybe I don't even need your leg-extending machine!")
                        npc(
                            "eek_the_spider_halloween_2009",
                            "I can do special leg-stretching exercises! Make my legs longer naturally! No artificial legs for me!",
                        )
                        npc("eek_the_spider_halloween_2009", "You just wait! Soon I'll have the best legs ever!")
                    }
                    option("You can't use it. It's broken.") {
                        player<Neutral>("You can't use it. It's broken.")
                        npc("eek_the_spider_halloween_2009", "Oh! I wouldn't want to use it if it's broken.")
                        npc(
                            "eek_the_spider_halloween_2009",
                            "It might malfunction and give me tiny, tiny legs! Like a slug, only with tiny legs!",
                        )
                        npc(
                            "eek_the_spider_halloween_2009",
                            "Or it might make some legs longer than others so I wouldn't be able to walk and I'd fall over!",
                        )
                        npc("eek_the_spider_halloween_2009", "That would be TERRIBLE!")
                        npc("eek_the_spider_halloween_2009", "I'd better keep away from that machine!")
                    }
                }
            }
        }
    }

    private suspend fun Player.spiderDialogue() {
        npc("eek_the_spider_halloween_2009", "Hey, are you scared of spiders?")
        choice {
            option("Yes, I'm scared of spiders.") {
                player<Neutral>("Yes, I'm scared of spiders.")
                npc("eek_the_spider_halloween_2009", "Oh no! I'm sorry!")
                npc("eek_the_spider_halloween_2009", "I've got to try not to be scary.")
                npc("eek_the_spider_halloween_2009", "You should just pretend I'm not a spider! Like, maybe I'm an eight-legged mouse!")
                npc(
                    "eek_the_spider_halloween_2009",
                    "Hello, I'm an eight-legged mouse! Squeak squeak squeak, I like cheese, cats are bad, I'm definitely not a spider.",
                    title = "Eek the Eight-Legged Mouse",
                )
                npc("eek_the_spider_halloween_2009", "Is that better?")
                choice {
                    option("You still look like a spider.") {
                        player<Neutral>("You still look like a spider.")
                        npc("eek_the_spider_halloween_2009", "A spider? Oh no! I'm scared of spiders!")
                    }
                    option("That's much better.") {
                        player<Neutral>("That's much better.")
                        npc(
                            "eek_the_spider_halloween_2009",
                            "Yay! I'll be an eight-legged mouse forever until I forget.",
                            title = "Eek the Eight-Legged Mouse",
                        )
                        npc("eek_the_spider_halloween_2009", "...", title = "Eek the Eight-Legged Mouse")
                        npc("eek_the_spider_halloween_2009", "You know what? It's great being a spider.")
                    }
                }
            }
            option("No, I'm not scared of spiders.") {
                player<Neutral>("No, I'm not scared of spiders.")
                npc("eek_the_spider_halloween_2009", "Not even a bit?")
                npc("eek_the_spider_halloween_2009", "I bet I can make you scared of spiders!")
                npc("eek_the_spider_halloween_2009", "Boo! Raaar! I'm a scary spider!")
                npc("eek_the_spider_halloween_2009", "Next time you go to sleep I'm going to crawl on your face and EAT YOUR EYEBALLS!")
                choice {
                    option("I'm still not scared.") {
                        player<Neutral>("I'm still not scared.")
                        npc("eek_the_spider_halloween_2009", "Oh, wow! Even I was scared of me when I said that!")
                        npc("eek_the_spider_halloween_2009", "You're so brave! I bet you're not scared of anything!")
                    }
                    option("That was a bit scary.") {
                        player<Scared>("That was a bit scary.")
                        npc("eek_the_spider_halloween_2009", "Ha ha ha ha ha ha ha! I made you scared of me! I made you scared of me!")
                        npc("eek_the_spider_halloween_2009", "It's okay, I'm not really going to eat your eyeballs. Spiders don't do that.")
                        npc("eek_the_spider_halloween_2009", "Well...I don't think they do.")
                    }
                }
            }
        }
    }

    private suspend fun Player.flyingDialogue() {
        npc("eek_the_spider_halloween_2009", "I'm going to learn how to fly! I'm going to be a flying spider!")
        choice {
            option("How are you going to fly?") {
                player<Quiz>("How are you going to fly?")
                npc("eek_the_spider_halloween_2009", "I'm going to spin webs between my legs to make wings! Then I can fly!")
                choice {
                    option("Let's try it now!") {
                        player<Happy>("Let's try it now!")
                        npc("eek_the_spider_halloween_2009", "Um...okay! I'll spin the webs...and then you throw me in the air.")
                        npc("eek_the_spider_halloween_2009", "Okay. Three...")
                        npc("eek_the_spider_halloween_2009", "Two...")
                        npc("eek_the_spider_halloween_2009", "One...")
                        npc("eek_the_spider_halloween_2009", "Wait! I'm not ready!")
                        npc(
                            "eek_the_spider_halloween_2009",
                            "I don't think I've got the webs right. I'm not ready yet. We'll do it another time.",
                        )
                    }
                    option("Sounds dangerous.") {
                        player<Neutral>("Sounds dangerous.")
                        npc("eek_the_spider_halloween_2009", "It's not dangerous! What's the worst that could happen?")
                        npc("eek_the_spider_halloween_2009", "The worst that can happen is, like, the wings fail and I fall to my death.")
                        npc("eek_the_spider_halloween_2009", "Or I get eaten by a bird.")
                        npc("eek_the_spider_halloween_2009", "I'm going to rethink this.")
                    }
                }
            }
            option("Spiders can't fly!") {
                player<Laugh>("Spiders can't fly!")
                npc("eek_the_spider_halloween_2009", "Some spiders can fly! They use web strands to float in the air!")
                npc("eek_the_spider_halloween_2009", "I think I know more about spiders than you, seeing how I am one!")
                choice {
                    option("I'm sorry, Eek.") {
                        player<Sad>("I'm sorry, Eek.")
                        npc("eek_the_spider_halloween_2009", "That's okay.")
                        npc("eek_the_spider_halloween_2009", "I don't want to fly now. I'm not in the mood.")
                    }
                    option("I don't believe you.") {
                        player<Neutral>("I don't believe you.")
                        npc("eek_the_spider_halloween_2009", "I don't care. I'm not talking to you any more.")
                    }
                }
            }
        }
    }

    private suspend fun Player.manSpiderDialogue() {
        npc("eek_the_spider_halloween_2009", "Hey...there are so many humans in your world.")
        npc("eek_the_spider_halloween_2009", "Back in the Spider Realm there are only spiders.")
        npc("eek_the_spider_halloween_2009", "I guess that's why they call it the Spider Realm.")
        npc("eek_the_spider_halloween_2009", "Suppose I was bitten by a magically-irradiated man?")
        npc("eek_the_spider_halloween_2009", "I might become the Man-Spider! I'd gain all the powers of a man! Like um...")
        npc("eek_the_spider_halloween_2009", "Um...what can humans do that's special?")
        choice {
            option("Walk on two legs.") {
                player<Neutral>("Walk on two legs.")
                npc("eek_the_spider_halloween_2009", "Walk on two legs! Yes! Then I could use my other six legs to FIGHT CRIME!")
            }
            option("Use tools.") {
                player<Neutral>("Use tools.")
                npc("eek_the_spider_halloween_2009", "Yes! I could have all sorts of gadgets! Like a gadget that spins webs!")
                npc("eek_the_spider_halloween_2009", "Wait...I can already spin webs.")
                npc("eek_the_spider_halloween_2009", "Never mind!")
            }
            option("Project heat-rays from our eyes.") {
                player<Neutral>("Project heat-rays from our eyes.")
                npc("eek_the_spider_halloween_2009", "Oh! I didn't know humans could do that!")
                npc("eek_the_spider_halloween_2009", "Please don't fry me with your heat rays!")
                npc("eek_the_spider_halloween_2009", "I'm too young to fry!")
            }
            option("Nothing, really. Humans are pretty ordinary.") {
                player<Sad>("Nothing, really. Humans are pretty ordinary.")
                npc("eek_the_spider_halloween_2009", "Aww, don't say that. Everyone is special!")
            }
        }
    }
}
