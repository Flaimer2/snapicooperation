package ru.snapix.snapicooperation.caches

import kotlinx.serialization.encodeToString
import ru.snapix.library.caches.RedisCache
import ru.snapix.library.json
import ru.snapix.snapicooperation.api.Party

object Parties : RedisCache<Party>() {
    override val KEY_REDIS: String = "parties"
    override fun key(value: Party) = value.leader
    override fun decode(value: String) = json.decodeFromString<Party>(value)
    override fun encode(value: Party) = json.encodeToString(value)
}