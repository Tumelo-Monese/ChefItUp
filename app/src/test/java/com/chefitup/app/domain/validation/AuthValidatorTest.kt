package com.chefitup.app.domain.validation

import com.chefitup.app.R
import com.chefitup.app.domain.model.PasswordStrength
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun validateEmail_rejectsBlank() {
        assertThat(AuthValidator.validateEmail("")).isEqualTo(R.string.auth_error_empty_email)
    }

    @Test
    fun validateEmail_rejectsInvalid() {
        assertThat(AuthValidator.validateEmail("not-an-email"))
            .isEqualTo(R.string.auth_error_invalid_email)
    }

    @Test
    fun validateEmail_acceptsValid() {
        assertThat(AuthValidator.validateEmail("chef@chefitup.app")).isNull()
    }

    @Test
    fun validatePassword_requiresMinLengthOnRegister() {
        assertThat(AuthValidator.validatePassword("short"))
            .isEqualTo(R.string.auth_error_password_short)
    }

    @Test
    fun validatePassword_acceptsLongEnough() {
        assertThat(AuthValidator.validatePassword("SecurePass1!")).isNull()
    }

    @Test
    fun validateConfirmPassword_detectsMismatch() {
        assertThat(AuthValidator.validateConfirmPassword("Password1!", "Password2!"))
            .isEqualTo(R.string.auth_error_password_mismatch)
    }

    @Test
    fun validateUsername_rejectsInvalidCharacters() {
        assertThat(AuthValidator.validateUsername("ab"))
            .isEqualTo(R.string.auth_error_username_invalid)
        assertThat(AuthValidator.validateUsername("chef_user")).isNull()
    }

    @Test
    fun validatePhone_acceptsSouthAfricanFormats() {
        assertThat(AuthValidator.validatePhone("0821234567")).isNull()
        assertThat(AuthValidator.validatePhone("+27821234567")).isNull()
        assertThat(AuthValidator.validatePhone("12345"))
            .isEqualTo(R.string.auth_error_phone_invalid)
    }

    @Test
    fun passwordStrength_increasesWithComplexity() {
        assertThat(AuthValidator.passwordStrength("abcdef")).isEqualTo(PasswordStrength.WEAK)
        assertThat(AuthValidator.passwordStrength("abcdefgh")).isEqualTo(PasswordStrength.WEAK)
        assertThat(AuthValidator.passwordStrength("Abcdefgh")).isEqualTo(PasswordStrength.FAIR)
        assertThat(AuthValidator.passwordStrength("Abcdefgh1")).isEqualTo(PasswordStrength.GOOD)
        assertThat(AuthValidator.passwordStrength("Abcdefgh1!Long")).isEqualTo(PasswordStrength.STRONG)
    }
}
