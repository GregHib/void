package world.gregs.voidps.network.login.registration

/**
 * Validates the fields of an account creation form
 */
object RegistrationValidator {

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$")
    private val passwordRegex = Regex("^[A-Za-z0-9]+$")
    private val guessable = setOf(
        "password", "password1", "123456", "12345678", "123123", "111111", "000000",
        "qwerty", "abc123", "runescape", "letmein", "iloveyou",
    )

    const val MAX_EMAIL_LENGTH = 254
    const val MAX_LOCAL_PART_LENGTH = 64
    const val MIN_PASSWORD_LENGTH = 5
    const val MAX_PASSWORD_LENGTH = 20
    const val MIN_AGE = 13

    fun email(email: String): Int {
        if (email.length > MAX_EMAIL_LENGTH || localPart(email).length > MAX_LOCAL_PART_LENGTH) {
            return RegistrationResponse.INVALID_EMAIL
        }
        if (!emailRegex.matches(email)) {
            return RegistrationResponse.INVALID_EMAIL
        }
        return RegistrationResponse.SUCCESS
    }

    fun password(password: String, email: String): Int {
        if (password.length !in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH) {
            return RegistrationResponse.PASSWORD_LENGTH
        }
        if (!passwordRegex.matches(password)) {
            return RegistrationResponse.PASSWORD_CHARACTERS
        }
        val lower = password.lowercase()
        if (lower in guessable || lower == localPart(email).lowercase()) {
            return RegistrationResponse.PASSWORD_GUESSABLE
        }
        return RegistrationResponse.SUCCESS
    }

    fun age(age: Int): Int {
        if (age < MIN_AGE) {
            return RegistrationResponse.REFUSED
        }
        return RegistrationResponse.SUCCESS
    }

    fun localPart(email: String): String = email.substringBefore('@')
}
