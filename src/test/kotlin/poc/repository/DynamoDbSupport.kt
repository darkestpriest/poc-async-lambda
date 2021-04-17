package poc.repository

import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.*

object DynamoDbSupport {

    const val table = "table"

    private lateinit var cachedDynamoClient: DynamoDbAsyncClient
    private lateinit var cachedContainer: LocalStackContainer

    fun dynamoDbClient(): DynamoDbAsyncClient {
        return if (!::cachedDynamoClient.isInitialized) {
            val container = initContainer()
            DynamoDbAsyncClient
                    .builder()
                    .endpointOverride(container.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(
                                            container.accessKey,
                                            container.secretKey
                                    )
                            )
                    )
                    .region(Region.of(container.region))
                    .build()
        } else {
            cachedDynamoClient
        }.also {
            createPriceTable(it)
        }
    }

    private fun initContainer(): LocalStackContainer =
            if(!::cachedContainer.isInitialized) {
                LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.6"))
                        .withServices(LocalStackContainer.Service.DYNAMODB)
                        .apply { start() }
                        .also { cachedContainer = it }
            } else {
                cachedContainer
            }

    private fun createPriceTable(client: DynamoDbAsyncClient) {
        val createTableRequest = CreateTableRequest
                .builder()
                .tableName(table)
                .attributeDefinitions(
                        AttributeDefinition
                                .builder()
                                .attributeName(DynamoDbField.ID.param)
                                .attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition
                                .builder()
                                .attributeName(DynamoDbField.VERSION.param)
                                .attributeType(ScalarAttributeType.N).build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .provisionedThroughput(
                        ProvisionedThroughput.builder().readCapacityUnits(100).writeCapacityUnits(100).build()
                )
                .keySchema(
                        KeySchemaElement.builder().attributeName(DynamoDbField.ID.param).keyType(KeyType.HASH).build(),
                        KeySchemaElement.builder().attributeName(DynamoDbField.VERSION.param).keyType(KeyType.RANGE).build()
                )
                .build()
        client.createTable(createTableRequest).get()
        waitForTableToBeCreated(client)
    }

    private fun waitForTableToBeCreated(client: DynamoDbAsyncClient) {
        (1..3).find {
            val status = client.describeTable(DescribeTableRequest.builder().tableName(table).build()).get().table()
                    .tableStatus()
            val isReady = status == TableStatus.ACTIVE
            if (!isReady) Thread.sleep(500)
            isReady
        } ?: throw IllegalStateException("table $table is not ready yet")
    }
}