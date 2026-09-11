package world.gregs.voidps.web.api.service

import world.gregs.voidps.web.api.model.WorldList
import world.gregs.voidps.web.api.model.WorldStatus

/**
 * The world list. Public, and the same data the developer panel's compact world list shows — there
 * is no staff-only twin to keep in step.
 */
interface WorldService {

    /** A null [status] returns every world, offline ones included. */
    suspend fun worlds(status: WorldStatus? = null): WorldList
}
