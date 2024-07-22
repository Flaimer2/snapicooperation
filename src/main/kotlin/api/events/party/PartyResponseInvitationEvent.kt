package ru.snapix.snapicooperation.api.events.party

import com.velocitypowered.api.proxy.Player
import ru.snapix.snapicooperation.api.Party

data class PartyResponseInvitationEvent(val player: Player, val party: Party, val status: InvitationStatus)

enum class InvitationStatus {
    ACCEPT,
    DECLINE,
    IGNORE,
    REMOVE_LEADER
}