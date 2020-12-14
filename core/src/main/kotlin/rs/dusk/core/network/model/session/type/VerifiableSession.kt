package rs.dusk.core.network.model.session.type

/**
 * The representation of a session that is verifiable.
 * The criteria for verifiability is that:
 *
 *
 *      1. The session is verified on both endpoints
 *      2. The session object is swapped with another one upon successful verification
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since April 08, 2020
 */
interface VerifiableSession {

    /**
     * The time in ms for verification, after which [VerifiableSession#timeout()] will be invoked
     */
    fun verificationTimeout(): Long

    /**
     * When the verification criteria have been met, this function is invoked
     */
    fun onSuccession()

    /**
     * When the timeout interval has lapsed, this function is invoked
     */
    fun onTimeout()
}