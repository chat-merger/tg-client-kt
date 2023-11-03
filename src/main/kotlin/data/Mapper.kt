package data

import domain.Distributor
import mergerapi.Response

class Mapper {
    fun Response.toEvent(): Distributor.Event? {
        this.eventCase
        when (this) {
            is th -> {}
            else -> {}
        }
    }
}
