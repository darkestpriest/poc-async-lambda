import kotlinx.serialization.json.Json
import poc.model.domain.Item
import poc.model.domain.Property
import java.util.*
import kotlin.random.Random

object JsonMapper {
    val mapper = Json
}

fun anyItem(id: String = randomString()): Item = Item(
        id = id,
        version = randomLong(),
        properties = anyProperties()
)

fun anyProperties(values: Int = 10): Map<String, Property> = 0.rangeTo(values).associate {
    randomString() to anyProperty()
}

fun anyProperty() = Property(
        randomString(), randomLong()
)

fun randomLong(): Long = Random.nextLong()
fun randomString(): String = UUID.randomUUID().toString()
