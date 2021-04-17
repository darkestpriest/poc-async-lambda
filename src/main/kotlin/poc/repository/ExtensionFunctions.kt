package poc.repository

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import poc.configuration.Configuration.mapper
import poc.model.domain.Item
import poc.model.domain.Property
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun Item.toAttributeMap(): Map<String, AttributeValue> {
    return mapOf(
            *id.toS(DynamoDbField.ID),
            *version.toN(DynamoDbField.VERSION),
            *properties.toM(DynamoDbField.PROPERTIES)
    )
}

fun FindByRequest.toAttributeMap(): Map<String, AttributeValue> {
    return mapOf(
            *id.toS(DynamoDbField.ID),
            *version.toN(DynamoDbField.VERSION)
    )
}

fun List<Map<String, AttributeValue>>.toItems() =
        this.map {
            it.toItem()
        }

fun Map<String, AttributeValue>.toItem(): Item {
    return Item(
            id = this[DynamoDbField.ID].s(),
            version = this[DynamoDbField.VERSION].n().toLong(),
            properties = this[DynamoDbField.PROPERTIES].m().toProperties()
    )
}

fun String.toS(): AttributeValue = AttributeValue.builder().s(this).build()

private fun Long.toN(field: DynamoDbField) = this.toN(field.param)
private fun Long.toN(field: String) = arrayOf(field to AttributeValue.builder().n(this.toString()).build())

private fun Map<String, AttributeValue>.toProperties() = this.mapValues { (_, value) ->
    mapper().decodeFromString<Property>(value.s())
}

private fun String.toS(field: DynamoDbField) = this.toS(field.param)
private fun String.toS(field: String) = arrayOf(field to this.toS())

private fun Map<String, Property>.toM(field: DynamoDbField) = arrayOf(field.param to AttributeValue.builder().m(this.toM()).build())

private fun Map<String, Property>.toM() = this.mapValues { (_, value) ->
    mapper().encodeToString(value).let { AttributeValue.builder().s(it).build() }
}

private operator fun Map<String, AttributeValue>.get(field: DynamoDbField): AttributeValue = this[field.param]!!