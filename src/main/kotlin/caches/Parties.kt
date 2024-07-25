package ru.snapix.snapicooperation.caches

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.snapix.library.cache.RedisCache
import ru.snapix.library.utils.json
import ru.snapix.snapicooperation.api.Party

object Parties : RedisCache<Party>() {
    override val KEY_REDIS: String = "parties"
    override fun key(value: Party) = value.leader.getName()
    override fun decode(value: String) = json.decodeFromString<Party>(value)
    override fun encode(value: Party) = json.encodeToString(value)
}