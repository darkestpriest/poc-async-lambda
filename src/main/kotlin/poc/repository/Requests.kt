package poc.repository

import kotlinx.serialization.Serializable

@Serializable
data class FindByRequest(
        val id: String,
        val version: Long
)