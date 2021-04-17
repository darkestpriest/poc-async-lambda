package poc.repository

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import poc.exceptions.NotFound
import poc.model.domain.Item
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse

class ItemRepository(
        private val table: String,
        private val dynamoDb: DynamoDbAsyncClient,
        private val mapper: Json
) {

    companion object {
        private val log = LoggerFactory.getLogger(ItemRepository::class.java)
    }

    fun save(item: Item): Mono<SaveResponse> {
        return Mono.just(item).flatMap {
            log.info("about to save {}", item)
            saveItem(it)
        }
    }

    fun findBy(request: FindByRequest): Mono<Item> {
        return Mono.just(request).flatMap {
            log.info("about to find item for {}", request)
            findByRequest(it)
        }
    }

    fun findAllBy(id: String): Flux<Item> {
        return Mono.just(id).flatMapMany {
            log.debug("about to find items for {}", id)
            findAll(it)
        }
    }

    private fun saveItem(item: Item): Mono<SaveResponse> {
        return Mono.fromFuture {
            dynamoDb.putItem { it
                    .tableName(table)
                    .item(item.toAttributeMap())
            }
        }.map {
            SaveResponse(item, it.toJsonString()).also { response ->
                log.trace("response for save {} is {}", item, response)
            }
        }
    }

    private fun findByRequest(request: FindByRequest): Mono<Item> {
        return Mono.fromFuture {
            dynamoDb.getItem { it
                    .tableName(table)
                    .key(request.toAttributeMap())
            }
        }.map {
            it.takeIf { it.hasItem() }?.item()?.toItem() ?: throw NotFound(mapper.encodeToString(request))
        }
    }

    private fun findAll(id: String): Flux<Item> {
        return Flux.from(
                dynamoDb.queryPaginator { it
                        .tableName(table)
                        .keyConditionExpression("#id = :value")
                        .expressionAttributeNames(mapOf(
                                "#id" to DynamoDbField.ID.param
                        ))
                        .expressionAttributeValues(
                                mapOf(":value" to id.toS())
                        )
                }
        ).map {
            it.takeIf { it.hasItems() }?.items()?.toItems()
        }.flatMapIterable { it }
    }

    private fun PutItemResponse.toJsonString(): String = mapper.encodeToString(PutItemResponseSerializer,this)
}

internal object PutItemResponseSerializer : KSerializer<PutItemResponse> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PutItemResponse", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: PutItemResponse) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): PutItemResponse = PutItemResponse.builder().build()
}