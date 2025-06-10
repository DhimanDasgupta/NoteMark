package com.dhimandasgupta.notemark.common.extensions

fun String.isValidEmail(): Boolean {
    // The regex pattern for a practical email validation
    val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".toRegex()
    return this.matches(emailRegex)
}

/**
 * A simple username validation.
 * Allows alphanumeric chars, underscore, and hyphen. Length 3-20.
 */
fun String.isSimpleUsernameValid(): Boolean {
    val usernameRegex = """^[a-zA-Z0-9_-]{3,20}$""".toRegex()
    return this.matches(usernameRegex)
}

/**
 * Checks if the String is a strong password based on the defined regex.
 *
 * A strong password requires:
 * - At least 8 characters
 * - At least one lowercase letter
 * - At least one uppercase letter
 * - At least one digit
 * - At least one special character from the set @$!%*?&
 *
 * @return True if the string meets the criteria, false otherwise.
 */
fun String.isValidPassword(): Boolean {
    // Regex explanation:
    // ^                  # start-of-string
    // (?=.*[a-z])        # a lowercase letter must occur at least once
    // (?=.*[A-Z])        # an uppercase letter must occur at least once
    // (?=.*[0-9])        # a digit must occur at least once
    // (?=.*[@$!%*?&])    # a special character must occur at least once
    // .{8,}              # anything, at least eight places long
    val passwordRegex = """^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&]).{8,}""".toRegex()
    return this.matches(passwordRegex)
}