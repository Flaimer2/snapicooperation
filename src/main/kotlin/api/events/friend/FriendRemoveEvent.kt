package ru.snapix.snapicooperation.api.events.friend

import ru.snapix.snapicooperation.api.User

data class FriendRemoveEvent(val user: User, val friend: User)