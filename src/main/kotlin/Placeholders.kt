package ru.snapix.snapicooperation

import ru.snapix.snapicooperation.api.Party

fun Party?.placeholders(): Array<Pair<String, Any>> {
    return if (this != null) arrayOf(
        "party_leader" to leader,
        "party_size" to size,
        "party_max_size" to maxSize
    ) else emptyArray<Pair<String, Any>>()
}