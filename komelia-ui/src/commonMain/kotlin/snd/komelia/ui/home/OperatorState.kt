package snd.komelia.ui.home

import kotlinx.coroutines.flow.MutableStateFlow
import snd.komga.client.search.KomgaSearchOperator
import snd.komga.client.search.KomgaSearchOperator.GreaterThan
import snd.komga.client.search.KomgaSearchOperator.Is
import snd.komga.client.search.KomgaSearchOperator.IsNot
import snd.komga.client.search.KomgaSearchOperator.IsNotNullT
import snd.komga.client.search.KomgaSearchOperator.IsNullT
import snd.komga.client.search.KomgaSearchOperator.LessThan
import snd.komga.client.search.KomgaSearchOperator.NumericNullable
import kotlin.time.Duration
import kotlin.time.Instant

interface OperatorState<T>

open class EqualityOpState<T>(
    initial: KomgaSearchOperator.Equality<T>? = null
) : OperatorState<T> {
    val operator: MutableStateFlow<Op>
    val value: MutableStateFlow<T?>

    init {
        val (op, value) = when (initial) {
            is Is -> Op.Equals to initial.value
            is IsNot -> Op.Equals to initial.value
            null -> Op.Equals to null
        }
        this.operator = MutableStateFlow(op)
        this.value = MutableStateFlow(value)
    }

    fun setOp(op: Op) {
        this.operator.value = op
    }

    fun setValue(value: T?) {
        this.value.value = value
    }

    fun toSearchOperator(): KomgaSearchOperator.Equality<T>? {
        val value = this.value.value ?: return null
        val op = operator.value
        val res: KomgaSearchOperator.Equality<T> = when (op) {
            Op.Equals -> Is(value)
            Op.NotEquals -> IsNot(value)
        }
        return res
    }

    enum class Op {
        Equals, NotEquals
    }
}

open class EqualityNullableOpState<T>(
    initial: KomgaSearchOperator.EqualityNullable<T>? = null
) : OperatorState<T> {
    val operator: MutableStateFlow<Op>
    val value: MutableStateFlow<T?>

    init {
        val (op, value) = when (initial) {
            is Is -> Op.Equals to initial.value
            is IsNot -> Op.Equals to initial.value
            is IsNotNullT -> Op.IsNotNull to null
            is IsNullT -> Op.IsNull to null
            null -> Op.Equals to null
        }
        operator = MutableStateFlow(op)
        this.value = MutableStateFlow(value)
    }

    fun setOp(op: Op) {
        this.operator.value = op
    }

    fun setValue(value: T?) {
        this.value.value = value
    }

    fun toSearchOperator(): KomgaSearchOperator.EqualityNullable<T>? {
        val value = this.value.value
        val op = operator.value
        return when (op) {
            Op.Equals -> value?.let { Is(value) }
            Op.NotEquals -> value?.let { IsNot(value) }
            Op.IsNull -> IsNullT()
            Op.IsNotNull -> IsNotNullT()
        }
    }

    enum class Op {
        Equals, NotEquals, IsNull, IsNotNull
    }
}

open class BooleanOpState(
    initial: KomgaSearchOperator.Boolean? = null
) : OperatorState<Boolean> {
    val operator: MutableStateFlow<Op>

    init {
        val op = when (initial) {
            KomgaSearchOperator.IsFalse -> Op.False
            KomgaSearchOperator.IsTrue, null -> Op.True
        }
        operator = MutableStateFlow(op)
    }

    fun setOp(op: Op) {
        this.operator.value = op
    }

    fun toSearchOperator(): KomgaSearchOperator.Boolean {
        return when (this.operator.value) {
            Op.True -> KomgaSearchOperator.IsTrue
            Op.False -> KomgaSearchOperator.IsFalse
        }
    }

    enum class Op {
        True, False
    }
}


open class StringOpState(
    initial: KomgaSearchOperator.StringOp? = null
) : OperatorState<String> {
    val operator: MutableStateFlow<Op>
    val value: MutableStateFlow<String?>

    init {
        val (op, value) = when (initial) {
            is KomgaSearchOperator.BeginsWith -> Op.BeginsWith to initial.value
            is KomgaSearchOperator.Contains -> Op.BeginsWith to initial.value
            is KomgaSearchOperator.DoesNotBeginWith -> Op.BeginsWith to initial.value
            is KomgaSearchOperator.DoesNotContain -> Op.BeginsWith to initial.value
            is KomgaSearchOperator.DoesNotEndWith -> Op.BeginsWith to initial.value
            is KomgaSearchOperator.EndsWith -> Op.BeginsWith to initial.value
            is Is<*> -> Op.Equals to initial.value as String
            is IsNot<*> -> Op.NotEquals to initial.value as String
            null -> Op.BeginsWith to null
        }
        this.operator = MutableStateFlow(op)
        this.value = MutableStateFlow(value)
    }

    fun setOp(op: Op) {
        this.operator.value = op
    }

    fun setValue(value: String) {
        this.value.value = value
    }

    fun toSearchOperator(): KomgaSearchOperator.StringOp? {
        val value = this.value.value ?: return null
        val op = operator.value
        return when (op) {
            Op.Equals -> Is(value)
            Op.NotEquals -> IsNot(value)
            Op.Contains -> KomgaSearchOperator.Contains(value)
            Op.DoesNotContain -> KomgaSearchOperator.DoesNotContain(value)
            Op.BeginsWith -> KomgaSearchOperator.BeginsWith(value)
            Op.DoesNotBeginWith -> KomgaSearchOperator.DoesNotBeginWith(value)
            Op.EndsWith -> KomgaSearchOperator.EndsWith(value)
            Op.DoesNotEndWith -> KomgaSearchOperator.DoesNotEndWith(value)
        }
    }

    enum class Op {
        Equals,
        NotEquals,
        Contains,
        DoesNotContain,
        BeginsWith,
        DoesNotBeginWith,
        EndsWith,
        DoesNotEndWith
    }
}

open class DateOpState(initial: KomgaSearchOperator.Date? = null) : OperatorState<Instant> {
    val operator: MutableStateFlow<Op>
    val date: MutableStateFlow<Instant?>
    val period: MutableStateFlow<Duration?>

    init {
        val (operator, date, duration) = when (initial) {
            is KomgaSearchOperator.After -> Triple(Op.IsAfter, initial.dateTime, null)
            is KomgaSearchOperator.Before -> Triple(Op.IsBefore, initial.dateTime, null)
            is KomgaSearchOperator.IsInTheLast -> Triple(Op.IsInLast, null, initial.duration)
            is KomgaSearchOperator.IsNotInTheLast -> Triple(Op.IsNotInLast, null, initial.duration)
            KomgaSearchOperator.IsNotNull -> Triple(Op.IsNotNull, null, null)
            KomgaSearchOperator.IsNull, null -> Triple(Op.IsNull, null, null)
        }

        this.operator = MutableStateFlow(operator)
        this.date = MutableStateFlow(date)
        this.period = MutableStateFlow(duration)
    }

    fun setOp(op: Op) {
        this.operator.value = op
    }

    fun setDate(date: Instant) {
        this.date.value = date
    }

    fun setPeriod(period: Duration?) {
        this.period.value = period
    }

    fun toSearchOperator(): KomgaSearchOperator.Date? {
        return when (operator.value) {
            Op.IsBefore -> date.value?.let { KomgaSearchOperator.Before(it) }
            Op.IsAfter -> date.value?.let { KomgaSearchOperator.After(it) }
            Op.IsInLast -> period.value?.let { KomgaSearchOperator.IsInTheLast(it) }

            Op.IsNotInLast -> period.value?.let { KomgaSearchOperator.IsNotInTheLast(it) }
            Op.IsNull -> KomgaSearchOperator.IsNull
            Op.IsNotNull -> KomgaSearchOperator.IsNotNull
        }
    }

    enum class Op {
        IsBefore,
        IsAfter,
        IsInLast,
        IsNotInLast,
        IsNull,
        IsNotNull,
    }
}


open class NumericOpState<T>(
    initial: KomgaSearchOperator.Numeric<T>?
) : OperatorState<T> {
    val operator: MutableStateFlow<Op>
    val value: MutableStateFlow<T?>

    init {
        val (operator, value) = when (initial) {
            is Is -> Op.EqualTo to initial.value
            is IsNot -> Op.NotEqualTo to initial.value
            is GreaterThan -> Op.GreaterThan to initial.value
            is LessThan -> Op.LessThan to initial.value
            null -> Op.EqualTo to null
        }

        this.operator = MutableStateFlow(operator)
        this.value = MutableStateFlow(value)
    }

    fun setOp(op: Op) {
        this.operator.value = op
    }

    fun setValue(value: T?) {
        this.value.value = value
    }

    fun toSearchOperator(): KomgaSearchOperator.Numeric<T>? {
        val currentValue = value.value ?: return null
        val op = operator.value

        return when (op) {
            Op.EqualTo -> Is(currentValue)
            Op.NotEqualTo -> IsNot(currentValue)
            Op.GreaterThan -> GreaterThan(currentValue)
            Op.LessThan -> LessThan(currentValue)
        }
    }

    enum class Op {
        EqualTo,
        NotEqualTo,
        GreaterThan,
        LessThan,
    }
}

open class NumericNullableOpState<T>(
    initial: NumericNullable<T>?
) : OperatorState<T> {
    val operator: MutableStateFlow<Op>
    val value: MutableStateFlow<T?>

    init {
        val (operator, value) = when (initial) {
            is Is -> Op.EqualTo to initial.value
            is IsNot -> Op.NotEqualTo to initial.value
            is GreaterThan -> Op.GreaterThan to initial.value
            is LessThan -> Op.LessThan to initial.value
            is IsNotNullT<*> -> Op.IsNotNull to null
            is IsNullT<*> -> Op.IsNull to null
            null -> Op.EqualTo to null
        }

        this.operator = MutableStateFlow(operator)
        this.value = MutableStateFlow(value)
    }

    fun setOp(op: Op) {
        this.operator.value = op
    }

    fun setValue(value: T?) {
        this.value.value = value
    }

    fun toSearchOperator(): NumericNullable<T>? {
        val currentValue = value.value
        val op = operator.value

        return when (op) {
            Op.EqualTo -> currentValue?.let { Is(currentValue) }
            Op.NotEqualTo -> currentValue?.let { IsNot(currentValue) }
            Op.GreaterThan -> currentValue?.let { GreaterThan(currentValue) }
            Op.LessThan -> currentValue?.let { LessThan(currentValue) }
            Op.IsNull -> IsNullT()
            Op.IsNotNull -> IsNotNullT()
        }
    }

    enum class Op {
        EqualTo,
        NotEqualTo,
        GreaterThan,
        LessThan,
        IsNull,
        IsNotNull,
    }
}
