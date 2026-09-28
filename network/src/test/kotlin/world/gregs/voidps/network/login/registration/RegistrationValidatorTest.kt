package world.gregs.voidps.network.login.registration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class RegistrationValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = ["bob@example.com", "first.last+tag@sub.example.co.uk", "A1_b-c%d@host-name.org"])
    fun `Valid emails`(email: String) {
        assertEquals(RegistrationResponse.SUCCESS, RegistrationValidator.email(email))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "bob", "bob@", "@example.com", "bob@example", "bob example@site.com", "bob@exa mple.com", "bob@@example.com"])
    fun `Invalid emails`(email: String) {
        assertEquals(RegistrationResponse.INVALID_EMAIL, RegistrationValidator.email(email))
    }

    @ParameterizedTest
    @CsvSource(
        "abcd, 30",
        "abcdefghijklmnopqrstu, 30",
        "abc de, 31",
        "abc-de1, 31",
        "password, 32",
        "Password1, 32",
        "bob, 30",
        "bobby, 32",
        "s3cure, 2",
        "abcde, 2",
        "abcdefghijklmnopqrst, 2",
    )
    fun `Password rules`(password: String, expected: Int) {
        assertEquals(expected, RegistrationValidator.password(password, "bobby@example.com"))
    }

    @ParameterizedTest
    @CsvSource("12, 9", "0, 9", "13, 2", "99, 2")
    fun `Age rules`(age: Int, expected: Int) {
        assertEquals(expected, RegistrationValidator.age(age))
    }
}
