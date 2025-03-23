package world.gregs.voidps.engine.entity.character.player.chat.clan

enum class ClanRank(val value: Int, val string: String) {
    None(-1, "No-one"),
    Anyone(-128, "Anyone"),
    Friend(0, "Any friends"),
    Recruit(1, "Recruit+"),
    Corporeal(2, "Corporal+"),
    Sergeant(3, "Sergeant+"),
    Lieutenant(4, "Lieutenant+"),
    Captain(5, "Captain+"),
    General(6, "General+"),
    Owner(7, "Only me"),
    Admin(127, "");

    companion object {
        fun from(option: String) : ClanRank {
            return entries.firstOrNull { it.string == option } ?: None
        }
        private val ranks = mapOf(
            "Anyone" to Anyone,
            "Friend" to Friend,
            "Recruit" to Recruit,
            "Corporeal" to Corporeal,
            "Sergeant" to Sergeant,
            "Lieutenant" to Lieutenant,
            "Captain" to Captain,
            "General" to General,
            "Owner" to Owner,
            "Admin" to Admin,
        )
        fun by(name: String) : ClanRank = ranks.getOrDefault(name, None)
    }
}