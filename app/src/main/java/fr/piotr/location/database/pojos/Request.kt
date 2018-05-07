package fr.piotr.location.database.pojos

import fr.piotr.location.Coordinates
import java.io.Serializable
import java.util.*

enum class Status {
    PENDING,
    REJECTED,
    ACCEPTED,
    OPENED
}

data class Request(val uuid: String = "", val date: Date = Date(), val from:String="any", val target:String="target", var status:Status=Status.PENDING, var coordinates: Coordinates=Coordinates()): Serializable, Comparable<Request> {
    override fun compareTo(other: Request): Int {
        return date.compareTo(other.date)
    }
}