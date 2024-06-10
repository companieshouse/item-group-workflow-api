# item-group-workflow-api

Rest API to handle item group workflow requests.

## Build Requirements

In order to build `item-group-workflow-api` locally you will need the following:

- Java 21
- Maven
- Git

## Environment Variables

| Name                              | Description                                                                          | Mandatory | Default | Example                                    |
|-----------------------------------|--------------------------------------------------------------------------------------|-----------|---------|--------------------------------------------|
| BOOTSTRAP_SERVER_URL              | The URLs of the Kafka brokers that this app's producer will connect to.              | √         | N/A     | `kafka:9092`                               |
| CHS_KAFKA_API_URL                 | The URL used by this app to send item status update requests to the `chs-kafka-api`. | √         | N/A     | `http://chs-kafka-api:4081`                |
| ITEM_GROUP_PROCESSED_TOPIC        | The name of the topic this app produces `item-group-processed` messages to.          | √         | N/A     | `item-group-processed`                     |
| ITEM_ORDERED_CERTIFIED_COPY_TOPIC | The name of the topic this app produces `item-order-certified-copy` messages to.     | √         | N/A     | `item-ordered-certified-copy`              |
| MONGODB_URL                       | The URL used by this app to connect to its database.                                 | √         | N/A     | `mongodb://mongo:27017/orders_item_groups` |

## Endpoints

| Path                                             | Method | Description                                                         |
|--------------------------------------------------|--------|---------------------------------------------------------------------|
| *`/item-groups`*                                 | POST   | Creates an item group.                                              |
| *`/item-groups/{item group ID}/items/{item ID}`* | PATCH  | Updates the status of the item within the group.                    |
| *`/item-group-workflow-api/healthcheck`*         | GET    | Returns HTTP OK (`200`) to indicate a healthy application instance. |

