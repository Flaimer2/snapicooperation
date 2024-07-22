package ru.snapix.snapicooperation.caches

import kotlinx.serialization.encodeToString
import ru.snapix.library.caches.Cache
import ru.snapix.library.json
import ru.snapix.snapicooperation.api.User
import ru.snapix.snapicooperation.database.FriendDatabase

object Friends : Cache<User>() {
    override val KEY_REDIS: String = "friends"
    override fun key(value: User) = value.name
    override fun decode(value: String) = json.decodeFromString<User>(value)
    override fun valueFromDatabase(key: String) = FriendDatabase[key]
    override fun encode(value: User) = json.encodeToString(value)
}