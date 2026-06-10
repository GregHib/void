package content.social.report

/**
 * Rules a player can be reported for breaking, grouped in the report interface
 * under Honour, Respect and Security
 * @param id The identifier sent by the client's report interface
 * @param title The rule name as shown on the report interface
 */
enum class Rule(val id: Int, val title: String) {
    BuyingOrSellingAnAccount(6, "Buying or selling an account"),
    EncouragingRuleBreaking(9, "Encouraging rule breaking"),
    StaffImpersonation(5, "Staff impersonation"),
    MacroingOrUseOfBots(7, "Macroing or use of bots"),
    Scamming(15, "Scamming"),
    ExploitingABug(4, "Exploiting a bug"),
    SeriouslyOffensiveLanguage(16, "Seriously offensive language"),
    Solicitation(17, "Solicitation"),
    DisruptiveBehaviour(18, "Disruptive behaviour"),
    OffensiveAccountName(19, "Offensive account name"),
    RealLifeThreats(20, "Real-life threats"),
    AskingForOrProvidingContactInformation(13, "Asking for or providing contact information"),
    BreakingRealWorldLaws(21, "Breaking real-world laws"),
    AdvertisingWebsites(11, "Advertising websites"),
    ;

    companion object {
        fun byId(id: Int): Rule? = entries.firstOrNull { it.id == id }
    }
}
