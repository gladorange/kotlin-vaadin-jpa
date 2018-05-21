package ru.evilorange.redbutton

import org.springframework.stereotype.Service
import ru.evilorange.redbutton.model.Team
import java.util.concurrent.ConcurrentHashMap
import java.util.LinkedList
import java.util.concurrent.Executors
import java.io.Serializable



@Service
class ButtonService {

    data class ButtonNotification(val time: Long, val teams: Map<Team, Long>, val reset:Boolean = false)

    val existingTeams = ConcurrentHashMap<Team, Long>()
    var startTime = System.currentTimeMillis()

    fun reset() {
        existingTeams.clear()
        startTime = System.currentTimeMillis()
        Broadcaster.broadcast(ButtonNotification(startTime,existingTeams,true))
    }

    fun pressButton(team: Team) {
        val currentTimeMillis = System.currentTimeMillis()
        existingTeams[team] = currentTimeMillis - startTime
        val teams = getSortedTeamsMap()
        Broadcaster.broadcast(ButtonNotification(currentTimeMillis, teams
        ))
    }

    fun getSortedTeamsMap(): Map<Team, Long> {
        return existingTeams.toList().sortedBy { (_, value) -> value }.toMap()
    }

}

class Broadcaster : Serializable {

    interface BroadcastListener {
        fun receiveBroadcast(message: ButtonService.ButtonNotification)
    }

    companion object {
        internal var executorService = Executors.newFixedThreadPool(50)

        private val listeners = LinkedList<BroadcastListener>()

        @Synchronized
        fun register(
                listener: BroadcastListener) {
            listeners.add(listener)
        }

        @Synchronized
        fun unregister(
                listener: BroadcastListener) {
            listeners.remove(listener)
        }

        @Synchronized
        fun broadcast(
                message: ButtonService.ButtonNotification) {
            for (listener in listeners)
                executorService.execute { listener.receiveBroadcast(message) }
        }
    }
}