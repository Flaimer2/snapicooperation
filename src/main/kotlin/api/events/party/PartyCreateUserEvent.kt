package ru.snapix.snapicooperation.api.events.party

import com.velocitypowered.api.proxy.Player
import ru.snapix.snapicooperation.api.Party

data class PartyCreateUserEvent(val player: Player, val party: Party)
