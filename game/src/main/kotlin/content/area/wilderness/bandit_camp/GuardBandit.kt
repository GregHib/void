package content.area.wilderness.bandit_camp

import world.gregs.voidps.engine.Script

class GuardBandit : Script {

    init {
        huntPlayer("guard_bandit", "aggressive") {
            say("You shall not pass!")
        }
    }
}
