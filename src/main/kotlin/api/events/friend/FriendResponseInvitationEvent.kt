package ru.snapix.snapicooperation.api.events.friend

import com.velocitypowered.api.proxy.Player
import ru.snapix.snapicooperation.api.User

data class FriendResponseInvitationEvent(val invited: Player, val sender: User, val status: InvitationStatus)

enum class InvitationStatus {
    ACCEPT,
    DECLINE,
    IGNORE,
    REMOVE
}