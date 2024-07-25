package ru.snapix.snapicooperation.caches

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.snapix.library.cache.DatabaseCache
import ru.snapix.library.utils.json
import ru.snapix.snapicooperation.api.User
import ru.snapix.snapicooperation.database.FriendDatabase

object Friends : DatabaseCache<User>() {
    override val KEY_REDIS: String = "friends"
    override fun key(value: User) = value.name
    override fun valueFromDatabase(key: String) = FriendDatabase[key]
    override fun decode(value: String) = json.decodeFromString<User>(value)
    override fun encode(value: User) = json.encodeToString(value)
}