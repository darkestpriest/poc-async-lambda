package poc.model.domain

import kotlinx.serialization.Serializable

data class Item(
        val id: String,
        val version: Long,
        val properties: Map<String, Property>
)

@Serializable
data class Property(
        val value: String,
        val version: Long
)