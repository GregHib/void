package world.gregs.voidps.web.api.service

import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.AccountSummary
import world.gregs.voidps.web.api.model.Page
import world.gregs.voidps.web.api.model.PageRequest
import world.gregs.voidps.web.api.model.PlayerBosses
import world.gregs.voidps.web.api.model.PlayerEvent
import world.gregs.voidps.web.api.model.PlayerEventType
import world.gregs.voidps.web.api.model.PlayerProfile
import world.gregs.voidps.web.api.model.PlayerQuests
import world.gregs.voidps.web.api.model.PlayerSkills
import world.gregs.voidps.web.api.model.PlayerSuggestion
import world.gregs.voidps.web.api.model.QuestFilter
import world.gregs.voidps.web.api.model.SkillSort
import java.time.Instant

/**
 * Public player profiles — one resource, rendered by both the adventurer's log and the hiscores
 * player view. Modelling it once is deliberate: the two pages show the same account with different
 * emphasis, and a second profile model would drift from the first.
 *
 * Every method takes the [viewer] so an account that has opted out of public profiles is still
 * visible to its owner and to staff. A null [viewer] is an anonymous visitor.
 *
 * Names are matched case-insensitively.
 */
interface PlayerService {

    /** Typeahead. A null or blank [query] returns a default set of suggestions. */
    suspend fun search(query: String?, limit: Int): List<PlayerSuggestion>

    /**
     * @throws ApiException.NotFound when no such account exists
     * @throws ApiException.PrivateProfile when the account opted out and [viewer] may not override
     */
    suspend fun profile(name: String, viewer: AccountSummary? = null): PlayerProfile

    suspend fun skills(name: String, sort: SkillSort, viewer: AccountSummary? = null): PlayerSkills

    suspend fun bosses(name: String, viewer: AccountSummary? = null): PlayerBosses

    suspend fun quests(name: String, status: QuestFilter, viewer: AccountSummary? = null): PlayerQuests

    /** Newest first. A null [type] returns every kind. */
    suspend fun events(
        name: String,
        type: PlayerEventType? = null,
        since: Instant? = null,
        page: PageRequest = PageRequest(),
        viewer: AccountSummary? = null,
    ): Page<PlayerEvent>
}
