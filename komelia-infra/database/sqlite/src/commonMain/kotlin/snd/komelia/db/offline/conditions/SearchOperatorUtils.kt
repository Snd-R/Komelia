package snd.komelia.db.offline.conditions

import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.TimeZone.Companion.UTC
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ExpressionWithColumnType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.notLike
import org.jetbrains.exposed.v1.core.or
import snd.komga.client.search.KomgaSearchOperator
import snd.komga.client.search.KomgaSearchOperator.IsFalse
import snd.komga.client.search.KomgaSearchOperator.IsNotNull
import snd.komga.client.search.KomgaSearchOperator.IsNull
import snd.komga.client.search.KomgaSearchOperator.IsTrue
import kotlin.time.Clock
import kotlin.time.Instant

fun KomgaSearchOperator.Equality<String>.toCondition(
    field: Column<String>,
    ignoreCase: Boolean = false,
) = when (this) {
    is KomgaSearchOperator.Is<String> ->
        if (ignoreCase) field.equalsIgnoreCase(this.value)
        else field.eq(this.value)

    is KomgaSearchOperator.IsNot<String> ->
        if (ignoreCase) field.notEqualsIgnoreCase(this.value)
        else field.neq(this.value)
}

fun <T> KomgaSearchOperator.Equality<T>.toCondition(
    field: Column<String>,
    ignoreCase: Boolean = false,
    converter: (T) -> String,
) = when (this) {
    is KomgaSearchOperator.Is<T> ->
        if (ignoreCase) field.equalsIgnoreCase(converter(this.value))
        else field.eq(converter(this.value))

    is KomgaSearchOperator.IsNot<T> ->
        if (ignoreCase) field.notEqualsIgnoreCase(converter(this.value))
        else field.neq(converter(this.value))
}

fun <T> KomgaSearchOperator.Equality<T>.toConditionNullable(
    field: Column<String?>,
    ignoreCase: Boolean = false,
    converter: (T) -> String,
) = when (this) {
    is KomgaSearchOperator.Is<T> ->
        if (ignoreCase) field.equalsIgnoreCaseNullable(converter(this.value))
        else field.eq(converter(this.value))

    is KomgaSearchOperator.IsNot<T> ->
        if (ignoreCase) field.notEqualsIgnoreCaseNullable(converter(this.value))
        else field.neq(converter(this.value))
}

fun KomgaSearchOperator.Date.toCondition(field: Column<String?>): Op<Boolean> =
    when (this) {
        is KomgaSearchOperator.After -> field.greater(this.dateTime.toLocalDateUtc().toString())
        is KomgaSearchOperator.Before -> field.less(this.dateTime.toLocalDateUtc().toString())
        is KomgaSearchOperator.IsInTheLast -> field.greater(
            Clock.System.now().toLocalDateUtc()
                .minus(duration.inWholeDays, DAY).toString()
        )

        is KomgaSearchOperator.IsNotInTheLast -> field.less(
            Clock.System.now().toLocalDateUtc()
                .minus(duration.inWholeDays, DAY).toString()
        )

        IsNull -> field.isNull()
        IsNotNull -> field.isNotNull()
    }

fun KomgaSearchOperator.Boolean.toCondition(field: Column<Boolean>) =
    when (this) {
        IsFalse -> field.eq(true)
        IsTrue -> field.eq(false)
    }

fun KomgaSearchOperator.NumericNullable<Int>.toCondition(field: Column<Int?>) =
    when (this) {
        is KomgaSearchOperator.Is<Int> -> field.eq(value)
        is KomgaSearchOperator.IsNot<Int> -> field.neq(value).or(field.isNull())
        is KomgaSearchOperator.GreaterThan -> field.greater(value)
        is KomgaSearchOperator.LessThan -> field.less(value)
        is KomgaSearchOperator.IsNullT -> field.isNull()
        is KomgaSearchOperator.IsNotNullT -> field.isNotNull()
    }

fun KomgaSearchOperator.Numeric<Float>.toCondition(field: Column<Float>) =
    when (this) {
        is KomgaSearchOperator.Is<Float> -> field.eq(value)
        is KomgaSearchOperator.IsNot<Float> -> field.neq(value)
        is KomgaSearchOperator.GreaterThan -> field.greater(value)
        is KomgaSearchOperator.LessThan -> field.less(value)
    }

fun KomgaSearchOperator.StringOp.toCondition(field: Column<String>) =
    when (this) {
        is KomgaSearchOperator.BeginsWith -> field.startsWithIgnoreCase(value)
        is KomgaSearchOperator.DoesNotBeginWith -> field.doesNotStartWithIgnoreCase(value)
        is KomgaSearchOperator.EndsWith -> field.endsWithIgnoreCase(value)
        is KomgaSearchOperator.DoesNotEndWith -> field.doesNotEndWithIgnoreCase(value)
        is KomgaSearchOperator.Contains -> field.containsIgnoreCase(value)
        is KomgaSearchOperator.DoesNotContain -> field.notContainsIgnoreCase(value)
        is KomgaSearchOperator.Is<*> -> field.equalsIgnoreCase(value as String)
        is KomgaSearchOperator.IsNot<*> -> field.notEqualsIgnoreCase(value as String)
    }


private fun Instant.toLocalDateUtc() = this.toLocalDateTime(UTC).date

infix fun ExpressionWithColumnType<String?>.equalsIgnoreCaseNullable(t: String): Op<Boolean> = this.lowerCase().eq(t)
infix fun ExpressionWithColumnType<String?>.notEqualsIgnoreCaseNullable(t: String): Op<Boolean> = this.lowerCase().neq(t)

infix fun ExpressionWithColumnType<String>.equalsIgnoreCase(t: String): Op<Boolean> = this.lowerCase().eq(t)
infix fun ExpressionWithColumnType<String>.notEqualsIgnoreCase(t: String): Op<Boolean> = this.lowerCase().neq(t)

infix fun ExpressionWithColumnType<String>.startsWithIgnoreCase(t: String): Op<Boolean> =
    this.lowerCase().like("$t%")

infix fun ExpressionWithColumnType<String>.doesNotStartWithIgnoreCase(t: String): Op<Boolean> =
    this.lowerCase().notLike("$t%")

infix fun ExpressionWithColumnType<String>.endsWithIgnoreCase(t: String): Op<Boolean> =
    this.lowerCase().like("%$t")

infix fun ExpressionWithColumnType<String>.doesNotEndWithIgnoreCase(t: String): Op<Boolean> =
    this.lowerCase().notLike("%$t")

infix fun ExpressionWithColumnType<String>.containsIgnoreCase(t: String): Op<Boolean> =
    this.lowerCase().like("%$t%")

infix fun ExpressionWithColumnType<String>.notContainsIgnoreCase(t: String): Op<Boolean> =
    this.lowerCase().notLike("%$t%")
