package content.area.kandarin.ardougne

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Afraid
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.Uncertain
import content.entity.player.dialogue.Upset
import content.entity.player.dialogue.type.*
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class Larry : Script {

    val npcs: NPCs by inject()
    val areas: AreaDefinitions by inject()

    init {
        npcOperate("Talk-to", "larry_ardougne_normal") {
            choice("I want to speak to Larry about:") {
//                option("Cold War") {}
                option("Penguin Hide and Seek") {
                    hideAndSeek()
                }
            }
        }
        npcOperate("Hide-n-Seek", "larry_ardougne_normal") {
            reward()
        }
    }

    private suspend fun Player.hideAndSeek() {
        if (!get("penguin_hide_and_seek_explained", false)) {
            explain()
            return
        }
        if (!ownsItem("spy_notebook")) {
            npc<Afraid>("Wait! Where is your notebook? Did it fall into enemy hands?")
            player<Talk>("I only turned around for a second...")
            npc<Upset>("Be more careful next time, who knows what would have happened if it go into the wrong hands!") // TODO proper message (not in osrs)
            if (inventory.add("spy_notebook")) {
                item("spy_notebook", 600, "Larry hands you a small notebook.")
            }
        }
        // https://youtu.be/s8tOI3CM9pc?si=Hv6ACh2QgWFLLAj8&t=264
        npc<Afraid>("Do you have news? Have you found more?")
        choice("Choose an option:") {
            option<Talk>("I've found more penguins.") {
                npc<Afraid>("More? They're spreading so quickly.")
                player<Happy>("I've found ${get("penguins_found_weekly", 0)} penguins this week.")
                choice("Choose an option:") {
                    havingTrouble()
                    claimReward()
                    option<Quiz>("What do I need to do again?") {
                        npc<Talk>("Weren't you listening the first time? Fine, I'll explain it again, but pay attention this time.")
                        npc<Shifty>("There are spies everywhere. I'm recruiting brave adventurers to find these penguins and tell me of their locations.")
                        info()
                        if (!questCompleted("lunar_diplomacy")) {
                            npc<Talk>("One last thing. If you ever discover a place called Lunar Isle, you might gain access to a spell that will help you get in touch with me from anywhere in the world. I can't say any more that that, there may be spies nearby.")
                        }
                    }
                    option<Talk>("Never mind.") {
                        npc<Upset>("FINE. Be that way.")
                    }
                }
            }
            havingTrouble()
            claimReward()
        }
    }

    private fun ChoiceBuilder2.claimReward(chuck: Boolean = false) {
        option("I want to claim my reward.") {
            reward(chuck)
        }
    }

    private suspend fun Player.reward(chuck: Boolean = false) {
        val points = get("penguin_points", 0)
        if (points <= 0) {
            npc<Talk>("You've found a lot of spies. But, you have no penguin points saved up. Keep looking!")
            return
        }
        if (chuck) {
            npc<Talk>("My, you have been working hard... You have $points Penguin Points.")
        } else {
            npc<Talk>("Well, you need rewarding for your hard work. You have $points Penguin ${"Point".plural(points)}.")
        }
        npc<Talk>("I can either reward you with coins or experience. Which would you prefer?")
        choice {
            option("Show me the money!") {
                player<Happy>("I want the cash reward.")
                val amount = points * 6500
                if (!inventory.add("coins", amount)) {
                    inventoryFull()
                    return@option
                }
                set("penguin_points", 0)
                statement("You have been awarded ${amount.toDigitGroupString()} coins!")
                npc<Happy>("Well done finding those penguins. Keep up the hard work, they'll keep moving around.")
            }
            option("Experience, all the way!") {
                player<Happy>("I want the experience reward.")
                val skill = skillLamp()
                val amount = levels.get(skill) * 25 * points
                exp(skill, amount.toDouble())
                set("penguin_points", 0)
                statement("You have been awarded ${amount.toDigitGroupString()} ${skill.name} experience!")
                npc<Happy>("Well done finding those penguins. Keep up the hard work, they'll keep moving around.")
            }
        }
    }

    private fun ChoiceBuilder2.havingTrouble() {
        option<Talk>("I'm having trouble finding the penguins; can I have a hint?") {
            for (i in 0 until 10) {
                if (!containsVarbit("penguins_found", "penguin_$i")) {
                    val penguin = npcs.firstOrNull { it.id == "hidden_penguin_$i" } ?: continue
                    val area = areas.get(penguin.tile.zone).firstOrNull { it.tags.contains("penguin_area") } ?: continue
                    val hint: String = area.getOrNull("hint") ?: continue
                    npc<Shifty>("I've heard there's a penguin located $hint")
                    return@option
                }
            }
            npc<Shifty>("I haven't heard of any penguins sightings recently, come back and ask me another time.") // TODO proper message
        }
    }

    private suspend fun Player.explain() {
        // https://youtu.be/ZZRctKBpy3o?si=Iw5wOLTaHVt-EABp&t=108
        npc<Afraid>("What do you want?")
        player<Uncertain>("Uh, I just wanted to as-")
        npc<Angry>("SHHHHHH! They're listening. Keep your voice down.")
        player<Talk>("*whispers* Who's listening?")
        npc<Shifty>("Never mind.")
        npc<Quiz>("Are you the inquisitive sort? Are you willing to go on an expedition for me?")
        player<Quiz>("What would I need to do on this expedition?")
        npc<Shifty>("The zoo has granted me permission to study penguins abroad. It's my, er...understanding that there are many penguins located around the world.")
        // https://www.youtube.com/watch?v=GMbrrm9YaW0
        player<Quiz>("Why do you want to find penguins around the world?")
        npc<Shifty>("I need to see if they're organis-, I mean, if they're migrating or something like that.")
        player<Uncertain>("You think they're organised? They're just penguins!")
        npc<Angry>("Do not underestimate them! They're clever and tricky and LISTENING! I know they're up to something.")
        npc<Happy>("That's why I'm recruiting brave adventurers to find these penguins and tell me of their locations.")
        player<Happy>("Well, I don't think they're organised, but I do travel all over the world. I could give you a hand finding them. What do I need to do?")
        info()
        player<Uncertain>("How much experience?")
        npc<Talk>("The more penguins you find, the more experience. Are you in?")
        player<Happy>("Great, I'll get started right away.")
        statement("To get 2 Penguin Points for deep cover penguins, you must complete the Cold War quest.")
        set("penguin_hide_and_seek_explained", true)
        npc<Happy>("Hold on, there. Take this notebook to record how many penguins you've found.")
        if (inventory.add("spy_notebook")) {
            item("spy_notebook", 600, "Larry hands you a small notebook.")
        }
    }

    private suspend fun Player.info() {
        npc<Happy>("Whenever you spot a penguin, spy on it. They're well trained and will change their positions every week, so keep your eyes peeled.")
        player<Uncertain>("What should I do after I've spied on them?")
        npc<Shifty>("I'll give you a notebook to record any penguins you've spotted. Each penguin is worth 1 Penguin Point.")
        npc<Shifty>("If you assist me further in my penguin investigations, I will award you 2 Penguin Points for any penguins you find further abroad.")
        npc<Shifty>("Report back here and I will reward you for your efforts. I will count up the Penguin Points you have earned and reward you with either a cash reward for 6,500gp per point, or experience in a skill of your choice.")
    }
}
