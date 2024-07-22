package ru.snapix.snapicooperation.api.events.party

import com.velocitypowered.api.proxy.Player
import ru.snapix.snapicooperation.api.Party

data class PartyChangeLeaderEvent(val oldLeader: Player, val newLeader: Player, val party: Party)