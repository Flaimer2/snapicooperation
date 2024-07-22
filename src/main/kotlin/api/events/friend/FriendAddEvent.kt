package ru.snapix.snapicooperation.api.events.friend

import ru.snapix.snapicooperation.api.User

data class FriendAddEvent(val user: User, val friend: User)