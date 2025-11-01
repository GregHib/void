package content.area.misthalin.barbarian_village.stronghold_of_security

import content.entity.player.dialogue.DoorHead
import content.entity.player.dialogue.Surprised
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.random

class StrongholdOfSecurityDoors : Script {

    init {
        objectOperate("Open", "gate_of_war*") { (target) ->
            if (target.tile.y == 5238 && tile.y > target.tile.y) {
                npc<DoorHead>("gate_of_war", "Greetings Adventurer. This place is kept safe by the spirits within the doors. As you pass through you will be asked questions about security. Hopefully you will learn much from us.")
                npc<DoorHead>("gate_of_war", "Please pass through and begin your adventure, beware of the various monsters that dwell within.")
                set("stronghold_safe_space", false)
            }
            openDoor(target, "unlocked_emote_flap")
            if (target.tile.y == 5238 && tile.y > target.tile.y) {
                player<Surprised>("Oh my! I just got sucked through that door... what a weird feeling! Still, I guess I should expect it as these evidently aren't your average kind of doors.... they talk and look creepy!")
            }
        }

        objectOperate("Open", "rickety_door*") { (target) ->
            openDoor(target, "unlocked_emote_slap_head")
        }

        objectOperate("Open", "oozing_barrier*") { (target) ->
            openDoor(target, "unlocked_emote_idea")
        }

        objectOperate("Open", "portal_of_death*") { (target) ->
            openDoor(target, "unlocked_emote_stomp")
        }
    }

    suspend fun Player.openDoor(target: GameObject, variable: String) {
        // If player is in safe space and security questions are active
        if (get("stronghold_safe_space", false) && Settings["strongholdOfSecurity.quiz", false]) {
            // And they haven't completed the level (and questions on completion is active)
            if (Settings["strongholdOfSecurity.quiz.complete", false] || !get(variable, false)) {
                randomQuestion(target, target.id.removeSuffix("_mirrored"))
                return
            }
        }
        enterDoor(target)
    }

    suspend fun Player.enterDoor(target: GameObject) {
        anim("stronghold_of_security_door")
        sound("stronghold_of_security_through_door")
        delay()
        when (target.rotation) {
            0 -> if (tile.x >= target.tile.x) {
                tele(target.tile.addX(-1))
            } else {
                tele(target.tile)
            }
            1 -> if (tile.y > target.tile.y) {
                tele(target.tile)
            } else {
                tele(target.tile.addY(1))
            }
            2 -> if (tile.x <= target.tile.x) {
                tele(target.tile.addX(1))
            } else {
                tele(target.tile)
            }
            else -> if (tile.y >= target.tile.y) {
                tele(target.tile.addY(-1))
            } else {
                tele(target.tile)
            }
        }
        set("stronghold_safe_space", !get("stronghold_safe_space", false))
        anim("stronghold_of_security_door_appear")
    }

    suspend fun Player.randomQuestion(target: GameObject, door: String) {
        when (random.nextInt(29)) {
            0 -> {
                npc<DoorHead>(door, "To pass you must answer me this: What do you do if someone asks you for your password or bank PIN to make you a member for free?")
                choice {
                    option("Give them the information they asked for.") {
                        npc<DoorHead>(door, "Wrong! Membership requires a subscription paid or a bond to be redeemed - they are trying to trick you. Never give your account details to anyone. Press the 'Report Abuse' button and fill in the offending player's name and the correct category.")
                    }
                    option("Don't tell them anything and ignore them.") {
                        npc<DoorHead>(door, "Quite good. But we should try to stop scammers. So please report them using the 'Report Abuse' button.")
                        enterDoor(target)
                    }
                    option("Don't tell them anything and click the 'Report Abuse' button.") {
                        npc<DoorHead>(door, "Correct! Press the 'Report Abuse' Button and fill in the offending player's name and correct category.")
                        enterDoor(target)
                    }
                }
            }
            1 -> {
                npc<DoorHead>(door, "To pass you must answer me this: You have been offered a free giveaway or double XP invitation via in-game chat or email. What should you do?")
                choice {
                    option("Report the incident and do not click any links.") {
                        npc<DoorHead>(door, "Correct! This is an attempt to obtain your account details. If it happened in the game, report it via the Report Abuse option.")
                        enterDoor(target)
                    }
                    option("Respond quickly so as not to miss the offer.") {
                        npc<DoorHead>(door, "Wrong! ${Settings["server.company"]} will NEVER make offers like this - it is an attempt to obtain your account details. If it happens in the game, report via the Report Abuse option.")
                    }
                }
            }
            2 -> {
                npc<DoorHead>(door, "To pass you must answer me this: You have been offered a free giveaway or double XP invitation via social media or a livestream. What should you do?")
                choice {
                    option("Respond quickly so as not to miss the offer.") {
                        npc<DoorHead>(door, "Wrong! ${Settings["server.company"]} will NEVER make offers like this - it is an attempt to obtain your account details. Please report the incident using whatever reporting methods offered by the site you are on.")
                    }
                    option("Report the incident and do not click any links.") {
                        npc<DoorHead>(door, "Correct! This is an attempt to obtain your account details and should be reported to the relevant organisations via their own reporting systems.")
                        enterDoor(target)
                    }
                }
            }
            3 -> {
                npc<DoorHead>(door, "To pass you must answer me this: Is it safe to get someone to level your account?")
                choice {
                    option("Yes, so long as you change your password when they have finished.") {
                        npc<DoorHead>(door, "Wrong! If you allow someone to use your account they may steal it. Alternatively they may use macros/bots to increase your level, and thus get the account banned.")
                    }
                    option("Yes, so long as they don't raise it's levels by too much.") {
                        npc<DoorHead>(door, "Wrong! If you allow someone to use your account they may steal it. Alternatively they may use macros/bots to increase your level, and thus get the account banned.")
                    }
                    option("No, you should never allow anyone to use your account.") {
                        npc<DoorHead>(door, "Correct! Allowing someone else to use your account means they may steal it, or may get it banned by cheating on it.")
                        enterDoor(target)
                    }
                }
            }
            4 -> {
                npc<DoorHead>(door, "To pass you must answer me this: What is the best security step you can take to keep your registered email secure?")
                choice {
                    option("Set up two-factor authentication with my email provider.") {
                        npc<DoorHead>(door, "Correct! Enabling two-factor authentication on your email account wil help protect your email from hijackers.")
                        enterDoor(target)
                    }
                    option("Have a complicated password set to my email.") {
                        npc<DoorHead>(door, "Wrong! Complicated passwords are always a good idea but they are not the very best security step you can take to keep your email secure.")
                    }
                    option("Use an email address just for ${Settings["server.name"]}.") {
                        npc<DoorHead>(door, "Wrong! Even if you don't use your email address elsewhere there is still a risk that somebody could access it.")
                    }
                }
            }
            5 -> {
                npc<DoorHead>(door, "To pass you must answer me this: What is the best way to secure your account?")
                choice {
                    option("A long and complicated password because you have good memory.") {
                        npc<DoorHead>(door, "Incorrect! While a strong password is recommended, two-factor authentication adds extra levels of security.")
                    }
                    option("Two-factor authentication on your account and your registered email.") {
                        npc<DoorHead>(door, "Correct! Using each of these features will maximise your account security.")
                        enterDoor(target)
                    }
                }
            }
            6 -> {
                npc<DoorHead>(door, "To pass you must answer me this: How do I set a bank PIN?")
                choice {
                    option("Talk to any banker.") {
                        npc<DoorHead>(door, "Correct! Simply talking to a banker will give you the option to set a bank PIN. Never use personal details for passwords or bank PINs!")
                        enterDoor(target)
                    }
                    option("Use the account management section on the website.") {
                        npc<DoorHead>(door, "Wrong! Your password can be changed from the account management section, but you must talk to a banker to set a bank PIN. Never use personal details for passwords or bank PINs!")
                    }
                }
            }
            7 -> {
                npc<DoorHead>(door, "To pass you must answer me this: What should I do if I receive an email asking me to verify my identity or account details due to suspicious activity?")
                choice {
                    option("Email them back with the information it asks for.") {
                        npc<DoorHead>(door, "Wrong! ${Settings["server.company"]} will NEVER email to ask for account details, so don't send any info back.")
                    }
                    option("Click the links in the email to visit the website.") {
                        npc<DoorHead>(door, "Wrong! ${Settings["server.company"]} will NEVER email to ask for account details, and the links may lead to a fake website.")
                    }
                    option("Delete it - it is fake!") {
                        npc<DoorHead>(door, "Correct! ${Settings["server.company"]} will NEVER email you unless you've used the website to change your account. Account details can only be verified through using account recovery systems on the ${Settings["server.name"]} website, NOT through responding to emails.")
                        enterDoor(target)
                    }
                }
            }
            8 -> {
                npc<DoorHead>(door, "To pass you must answer me this: Who can I give my password to?")
                choice {
                    option("My friends.") {
                        npc<DoorHead>(door, "Wrong! Your password should be kept secret from everyone. You should *never* give it out under any circumstances.")
                    }
                    option("My brother or sister.") {
                        npc<DoorHead>(door, "Wrong! Your password should be kept secret from everyone. You should *never* give it out under any circumstances.")
                    }
                    option("Nobody.") {
                        npc<DoorHead>(door, "Correct! Your password should be kept secret from everyone. You should *never* give it out under any circumstances.")
                        enterDoor(target)
                    }
                }
            }
            9 -> {
                npc<DoorHead>(door, "To pass you must answer me this: What do I do if a moderator asks me for my account details?")
                choice {
                    option("Tell them whatever they want to know.") {
                        npc<DoorHead>(door, "Wrong! Never give your account details to anyone! This includes things like account creation details, contact details and passwords. Never use personal details for passwords or bank PINs!")
                    }
                    option("Politely tell them no, and ignore them.") {
                        npc<DoorHead>(door, "Okay! Don't just tell them the details. But reporting the incident to ${Settings["server.company"]} would help. Use the Report Abuse button. Never use personal details for passwords or bank PINs!")
                        enterDoor(target)
                    }
                    option("Politely tell them no, then use the 'Report Abuse' button.") {
                        npc<DoorHead>(door, "Correct! Report any attempt to gain your account details as it is a very serious breach of ${Settings["server.name"]}'s rules. Never use personal details for passwords or bank PINs!")
                        enterDoor(target)
                    }
                }
            }
            10 -> {
                npc<DoorHead>(door, "To pass you must answer me this: A player trades you some valuable items, provides you with a bond, then asks if you want to share your account so he can help you make progress. How do you respond?")
                choice {
                    option("Decline the offer and report that player.") {
                        npc<DoorHead>(door, "Correct! Never share your login details with another player.")
                        enterDoor(target)
                    }
                    option("Give the player access since they are a higher level.") {
                        npc<DoorHead>(door, "Incorrect! Never share your login details with another player.")
                    }
                    option("Tell them you'll trade your account info for theirs.") {
                        npc<DoorHead>(door, "Incorrect! This option still puts your account and items at risk!")
                    }
                }
            }
            11 -> {
                npc<DoorHead>(door, "To pass you must answer me this: Where is it safe to use my ${Settings["server.name"]} password?")
                choice {
                    option("On ${Settings["server.name"]} and all fansites.") {
                        npc<DoorHead>(door, "Wrong! Always use a unique password purely for your ${Settings["server.name"]} account.")
                    }
                    option("Only on the ${Settings["server.name"]} website.") {
                        npc<DoorHead>(door, "Correct! Always make sure you are entering your password only on the ${Settings["server.name"]} website as other sites may try to steal it.")
                        enterDoor(target)
                    }
                    option("On all websites I visit.") {
                        npc<DoorHead>(door, "Wrong! This is very insecure and will may lead to your account being stolen.")
                    }
                }
            }
            12 -> {
                npc<DoorHead>(door, "To pass you must answer me this: Whose responsibility is it to keep your account secure?")
                choice {
                    option("Me.") {
                        npc<DoorHead>(door, "Correct! Make sure to use the tools ${Settings["server.company"]} recommend, such as two-factor authentication options.")
                        enterDoor(target)
                    }
                    option("${Settings["server.company"]}.") {
                        npc<DoorHead>(door, "Incorrect! ${Settings["server.company"]} can offer tools such as two-factor authentication options, but you muse make sure you use them correctly to keep your info safe!")
                    }
                    option("My internet provider.") {
                        npc<DoorHead>(door, "Incorrect! Your internet service provider may offer security advice, but use the tips on the ${Settings["server.name"]} website to stay secure.")
                    }
                }
            }
            13 -> {
                npc<DoorHead>(door, "To pass you must answer me this: Psst! Adventurer! I've got a special offer for you, but you're going to have to trust me. If you give me some gold coins, I'll give you back twice whatever you gave me! How does that sound?")
                choice {
                    option("No way! You'll just take my gold for your own! Reported!") {
                        println("Option 1")
                        npc<DoorHead>(door, "Correct! If it sounds too good to be true, it probably is! Be wary of these types of scams.")
                        println("Enter door")
                        enterDoor(target)
                    }
                    option("I'm not sure... but giving a few coins to test it won't hurt.") {
                        npc<DoorHead>(door, "Incorrect! Do not trust players asking for gold offering to return a higher amount.")
                    }
                    option("WoW! You're so generous, thank you! Here's all my gold.") {
                        npc<DoorHead>(door, "Incorrect! Do not trust players asking for gold offering to return a higher amount.")
                    }
                }
            }
            14 -> {
                npc<DoorHead>(door, "To pass you must answer me this: Is it okay to buy an Old School RuneScape account?")
                choice {
                    option("Yes if it is from someone you know.") {
                        npc<DoorHead>(door, "Wrong! If you buy an account, the person who originally made it may take it back, and you will lose anything you paid for it.")
                    }
                    option("Yes if you pay for it with GP.") {
                        npc<DoorHead>(door, "Wrong! If you buy an account, the person who originally made it may take it back, and you will lose anything you paid for it.")
                    }
                    option("No, you should never buy an account.") {
                        npc<DoorHead>(door, "Correct! Buying accounts is against the rules. Also you could lose the account if the original owner takes it back.")
                        enterDoor(target)
                    }
                }
            }
            15 -> {
                npc<DoorHead>(door, "To pass you must answer me this: My friend asks me to for my password so that he can do a difficult quest for me. Do I give it to him?")
                choice {
                    option("Yes. He is my best friends and I've already spent ages trying this quest.") {
                        npc<DoorHead>(door, "Wrong! Don't give your password to anyone otherwise you can lose everything you have worked so hard for.")
                    }
                    option("Don't give them my password.") {
                        npc<DoorHead>(door, "Correct! You can make it alone and the success will taste even better. Don't forget you can ask people for advice too!")
                        enterDoor(target)
                    }
                    option("Let them do the quest, but in the same room the whole time.") {
                        npc<DoorHead>(door, "Wrong! Never let anyone use your account for any reason - they might try to keep it by changing the password! You'd be held responsible if they broke the rules on your account too.")
                    }
                }
            }
            16 -> {
                npc<DoorHead>(door, "To pass you must answer me this: A player tells you to search for a video online, click the link in the description and comment on the forum post to win a cash prize. What do you do?")
                choice {
                    option("Do what they ask, using the provided link in the video description.") {
                        npc<DoorHead>(door, "Incorrect! Don't trust these types of link even if they look similar to the ${Settings["server.name"]} website.")
                    }
                    option("Report the player for phishing.") {
                        npc<DoorHead>(door, "Correct! Always be wary of these links and double check you are on the official ${Settings["server.name"]} website before entering your login details!")
                        enterDoor(target)
                    }
                    option("Tell your friends so they can get free gold too.") {
                        npc<DoorHead>(door, "Incorrect! Don't trust these types of link even if they look similar to the ${Settings["server.name"]} website.")
                    }
                }
            }
            17 -> {
                npc<DoorHead>(door, "To pass you must answer me this: Adventurer, I'll trade items with you for an amazing price, but you've got to come immediately to a particular place on a different game world. Hurry up! Come now before you lose out! What do you say?")
                choice {
                    option("Okay, I'll take my valuables there now so we can trade.") {
                        npc<DoorHead>(door, "Incorrect! Do not trust players trying to rush you into going somewhere unfamiliar. It's often an attempt to lure you into taking items somewhere dangerous, where you'll lose them.")
                    }
                    option("Nope, you're tricking me into going somewhere dangerous.") {
                        npc<DoorHead>(door, "Correct! If it sounds too good to be true, it probably is! Be wary of these types of scams.")
                        enterDoor(target)
                    }
                }
            }
            18 -> {
                npc<DoorHead>(door, "To pass you must answer me this: Which of these is an important characteristic of a secure password?")
                choice {
                    option("It incorporates your real name or birthday.") {
                        npc<DoorHead>(door, "Incorrect! Using personal details as your password makes it easier to guess!")
                    }
                    option("It's never used on other websites or accounts.") {
                        npc<DoorHead>(door, "Correct! Make sure to use a unique password to keep your account secure.")
                        enterDoor(target)
                    }
                    option("It's never changed over many months or years.") {
                        npc<DoorHead>(door, "Incorrect! You should change your password frequently.")
                    }
                }
            }
            19 -> {
                npc<DoorHead>(door, "To pass you must answer me this: You're watching a stream by someone claiming to be ${Settings["server.company"]} offering double XP. What do you do?")
                choice {
                    option("Click the link! I love double XP!") {
                        npc<DoorHead>(door, "Incorrect! This is a common phishing method and puts your account at risk!")
                    }
                    option("Report the stream. Real ${Settings["server.company"]} streams have a 'varified' mark.") {
                        npc<DoorHead>(door, "Correct! This is a common phishing method and puts your account at risk!")
                        enterDoor(target)
                    }
                    option("Ignore it.") {
                        npc<DoorHead>(door, "Incorrect! Well done for avoiding the phishing attempt but make sure to report these wherever possible to help other players.")
                    }
                }
            }
            20 -> {
                npc<DoorHead>(door, "To pass you must answer me this: Will ${Settings["server.company"]} prevent me from saying my PIN in game?")
                choice {
                    option("Yes.") {
                        npc<DoorHead>(door, "Wrong! ${Settings["server.company"]} does NOT block your PIN so don't type it! Anyone asking you to say your PIN is trying to trick you.")
                    }
                    option("No.") {
                        npc<DoorHead>(door, "Correct! ${Settings["server.company"]} will not block your PIN so don't type it! Anyone asking you to say your PIN is trying to trick you.")
                        enterDoor(target)
                    }
                }
            }
            21 -> {
                npc<DoorHead>(door, "To pass you must answer me this: A website claims that they can make me a player moderator. What should I do?")
                choice {
                    option("Nothing, it's a fake.") {
                        npc<DoorHead>(door, "Correct! Remember that moderators are hand picked by ${Settings["server.company"]} and contact is made through the game inbox only.")
                        enterDoor(target)
                    }
                    option("Give them my account info and password.") {
                        npc<DoorHead>(door, "Wrong! This will almost certainly lead to your account being hijacked. No website can make you a moderator as they are hand picked by ${Settings["server.company"]}.")
                    }
                }
            }
            22 -> {
                npc<DoorHead>(door, "To pass you must answer me this: What do I do if I think I have a keylogger or virus?")
                choice {
                    option("Virus scan my device then change my password.") {
                        npc<DoorHead>(door, "Correct! Removing the keylogger must be the priority, otherwise anything you type can be given away. Remember to change your password and bank PIN afterwards.")
                        enterDoor(target)
                    }
                    option("Change my password then virus scan my device.") {
                        npc<DoorHead>(door, "Wrong! If you change your password while you still have the keylogger, it will still be insecure. Remove the keylogger first. Never use personal details for passwords or bank PINs!")
                    }
                    option("Nothing, it will go away on its own.") {
                        npc<DoorHead>(door, "Wrong! This could mean your account may be accessed by someone else. Remove the keylogger then change your password. Never use personal details for passwords or bank PINs!")
                    }
                }
            }
            23 -> {
                npc<DoorHead>(door, "To pass you must answer me this: What should you do if another player messages you recommending a website to purchase items and/or gold?")
                choice {
                    option("Check out the website, it never huts to look around!") {
                        npc<DoorHead>(door, "Incorrect! Websites offering these services should not be trusted!")
                    }
                    option("Visit the website in a private browser for added security.") {
                        npc<DoorHead>(door, "Incorrect! Websites offering these services should not be trusted!")
                    }
                    option("Do no visit the website and report the player who messaged you.") {
                        npc<DoorHead>(door, "Correct! Buying and selling items and gold is against the rules and results in a permanent ban!")
                        enterDoor(target)
                    }
                }
            }
            24 -> {
                npc<DoorHead>(door, "To pass you must answer me this: What do you do if someone asks you for your password or bank PIN to make you a player moderator?")
                choice {
                    option("Don't give them the information and send an 'Abuse report'") {
                        npc<DoorHead>(door, "Correct! Press the 'Report Abuse' button and fill in the offending player's name and the correct category.")
                        enterDoor(target)
                    }
                    option("Don't tell them anything and ignore them.") {
                        npc<DoorHead>(door, "Quite good. But we should try to stop scammers. So please report them using the 'Report Abuse' button.")
                        enterDoor(target)
                    }
                    option("Give them the information they asked for.") {
                        npc<DoorHead>(door, "Wrong! ${Settings["server.company"]} never ask for your account information - especially to become a player moderator. Press the 'Report Abuse' button and fill in the offending player's name and the correct category.")
                    }
                }
            }
            25 -> {
                npc<DoorHead>(door, "To pass you must answer me this: What is an example of a good bank PIN?")
                choice {
                    option("Your real life bank PIN.") {
                        npc<DoorHead>(door, "This is a bad idea as if someone happens to find out your bank PIN on ${Settings["server.name"]}, they then have your real life bank PIN! Never use personal details for passwords or bank PINs!")
                    }
                    option("Your birthday.") {
                        npc<DoorHead>(door, "Not a good idea. You know how many presents you get for your birthday, so you can imagine how many people know this date. Never use personal details for passwords or bank PINs.")
                    }
                    option("The birthday of a famous person or event.") {
                        npc<DoorHead>(door, "Well done! Unless you tell someone, they are unlikely to guess who or what you have chosen, and you can always look it up. Never use personal details for passwords or bank PINs!")
                        enterDoor(target)
                    }
                }
            }
            26 -> {
                npc<DoorHead>(door, "To pass you must answer me this: What should you do if your real-life friend asks for your password so he can check your stats?")
                choice {
                    option("Give them your password since they're a friend in real life.") {
                        npc<DoorHead>(door, "Incorrect! Don't trust anybody with your account login details!")
                    }
                    option("Don't give out your password to anyone. Not even close friends.") {
                        npc<DoorHead>(door, "Correct! Doing so could result in losing your items and gold and puts your account at risk.")
                        enterDoor(target)
                    }
                    option("Log in for your friend and let them play.") {
                        npc<DoorHead>(door, "Incorrect! Never allow anybody access to your account.")
                    }
                }
            }
            27 -> {
                npc<DoorHead>(door, "To pass you must answer me this: A player starts asking you about very specific details linked to your account, such as when you created your account, your birthday date, internet provider etc. How should you react?")
                choice {
                    option("Be friendly and answer the questions.") {
                        npc<DoorHead>(door, "Incorrect! These details can be used within the account recovery system and possibly compromise your account.")
                    }
                    option("Don't share your information and report the player.") {
                        npc<DoorHead>(door, "Correct! These details could've been used within the account recovery system and possibly compromise your account.")
                        enterDoor(target)
                    }
                    option("Answer questions and ask the player back for their details.") {
                        npc<DoorHead>(door, "Incorrect! These details can be used within the account recovery system and possibly compromise your account. If you're asking the player back, you're unknowingly commiting the same offence.")
                    }
                }
            }
            else -> {
                npc<DoorHead>(door, "To pass you must answer me this: How do I remove a hijacker from my account?")
                choice {
                    option("Ask on social media.") {
                        npc<DoorHead>(door, "Wrong! Visiting ${Settings["server.website", "${Settings["server.name"]}'s website"]} and using the account recovery system will allow you to kick hijackers off your account by locking it as stolen.")
                    }
                    option("Email ${Settings["server.name"]}.") {
                        npc<DoorHead>(door, "Wrong! Visiting ${Settings["server.website", "${Settings["server.name"]}'s website"]} and using the account recovery system will allow you to kick hijackers off your account by locking it as stolen.")
                    }
                    option("Use the Account Recovery system.") {
                        npc<DoorHead>(door, "Correct! Visiting ${Settings["server.website", "${Settings["server.name"]}'s website"]} and using the account recovery system will allow you to kick hijackers off your account by locking it as stolen.")
                        enterDoor(target)
                    }
                }
            }
        }
    }
}
