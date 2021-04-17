package poc.repository

import JsonMapper.mapper
import anyItem
import org.junit.Before
import org.junit.Test
import poc.exceptions.NotFound
import poc.model.domain.Item
import poc.repository.DynamoDbSupport.dynamoDbClient
import poc.repository.DynamoDbSupport.table
import randomLong
import randomString
import reactor.test.StepVerifier
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ItemRepositoryTest {

    companion object {
        private val dynamo = dynamoDbClient()
    }

    private lateinit var sut: ItemRepository

    @Before
    fun setup() {
        sut = ItemRepository(
                table = table,
                dynamoDb = dynamo,
                mapper = mapper
        )
    }

    @Test
    fun `can find an item by request`() {
        val saved = saveAnyItem()

        StepVerifier.create(
                sut.findBy(saved.toRequest())
        ).assertNext {
            assertEquals(saved, it)
        }.verifyComplete()
    }

    @Test
    fun `fails on not found item`() {
        /*any saved*/saveAnyItem()

        StepVerifier.create(
                sut.findBy(FindByRequest(randomString(), randomLong()))
        ).expectError(NotFound::class.java).verify()
    }

    @Test
    fun `can find several items`() {
        val id = randomString()

        val items = 0.rangeTo(100).map {
            saveAnyItem(id)
        }

        StepVerifier.create(
                sut.findAllBy(id)
        )
                .expectSubscription()
                .recordWith {
                    ArrayList()
                }.expectNextCount(items.size.toLong())
                .consumeRecordedWith {
                    assertTrue(it.containsAll(items))
                }.verifyComplete()
    }

    private fun saveAnyItem(id: String = randomString()): Item {
        return sut.save(anyItem(id))
                .blockOptional()
                .orElseThrow { AssertionError("cannot instantiate item from db response") }.item
    }

    private fun Item.toRequest() = FindByRequest(
            id, version
    )
}