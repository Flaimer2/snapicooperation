package ru.snapix.snapicooperation.api.events.party

import com.velocitypowered.api.proxy.Player
import ru.snapix.snapicooperation.api.Party

data class PartyRemoveUserEvent(val player: Player, val party: Party, val reason: RemoveUserReason)

enum class RemoveUserReason {
    PARTY_DISBAND,
    USER_REMOVE
}
