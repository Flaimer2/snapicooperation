package ru.snapix.snapicooperation.api.events.party

import com.velocitypowered.api.proxy.Player
import ru.snapix.snapicooperation.api.Party

data class PartyDisbandEvent(val player: Player, val party: Party, val reason: DisbandReason)

enum class DisbandReason {
    DISABLE_PLUGIN,
    USER_DISBAND
}
