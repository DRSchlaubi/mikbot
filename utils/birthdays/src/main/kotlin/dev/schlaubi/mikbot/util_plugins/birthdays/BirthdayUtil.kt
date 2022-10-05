package dev.schlaubi.mikbot.util_plugins.birthdays

import dev.schlaubi.mikbot.util_plugins.birthdays.database.UserBirthday
import kotlinx.datetime.*
import java.time.Month

data class BirthdayContainer(
    val birthday: Instant,
    val nextBirthday: Instant,
    val dayDifference: Int,
    val newAge: Int
)

fun UserBirthday.calculate(): BirthdayContainer {
    val birthDay = time.toLocalDateTime(timeZone)
    val offsetNow = Clock.System.now().toLocalDateTime(timeZone)
    val thisYearBirthDay = birthDay.atYear(offsetNow.year)
    val nextBirthday = if (thisYearBirthDay > offsetNow) {
        thisYearBirthDay
    } else {
        birthDay.atYear(offsetNow.year + 1)
    }
    val nextAge = time.yearsUntil(nextBirthday.toInstant(TimeZone.UTC), TimeZone.UTC)
    val dayDifference = offsetNow.toInstant(timeZone).daysUntil(nextBirthday.toInstant(timeZone), timeZone)

    return BirthdayContainer(time, nextBirthday.toInstant(TimeZone.UTC), dayDifference, nextAge)
}

private fun LocalDateTime.atYear(year: Int): LocalDateTime {
    if (dayOfMonth == 29 && month == Month.FEBRUARY) { // handle leap years
        return LocalDateTime(year, Month.MARCH, 1, hour, minute, second, nanosecond)
    }
    return LocalDateTime(year, month, dayOfMonth, hour, minute, second, nanosecond)
}
