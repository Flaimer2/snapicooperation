package ru.snapix.snapicooperation.api.events.friend

import com.velocitypowered.api.proxy.Player
import ru.snapix.snapicooperation.api.User

data class FriendSendInvitationEvent(val player: Player, val user: User)
