package ru.snapix.snapicooperation.api.events.party

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import ru.snapix.library.network.player.NetworkPlayer
import ru.snapix.snapicooperation.api.Party

class PartyResponseInvitationEvent(val player: NetworkPlayer, val party: Party, val status: InvitationStatus) :
    Event() {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}

enum class InvitationStatus {
    ACCEPT,
    DECLINE,
    IGNORE,
    REMOVE_LEADER
}
