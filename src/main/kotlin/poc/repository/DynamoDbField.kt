package poc.repository

enum class DynamoDbField{
    ID,
    VERSION,
    PROPERTIES
    ;

    val param: String = this.name.toLowerCase()
}