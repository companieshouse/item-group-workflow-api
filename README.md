# item-group-workflow-api

Rest API to handle item group workflow requests.

## Build Requirements

In order to build `item-group-workflow-api` locally you will need the following:

- Java 11
- Maven
- Git

## Environment Variables

| Name                 | Description                                                              | Mandatory | Default | Example                                    |
|----------------------|--------------------------------------------------------------------------|-----------|---------|--------------------------------------------|
| BOOTSTRAP_SERVER_URL | The URLs of the Kafka brokers that this app's  producer will connect to. | √         | N/A     | `kafka:9092`                               |
| MONGODB_URL          | The URL used by this app to connect to its database.                     | √         | ""      | `mongodb://mongo:27017/orders_item_groups` |

## Endpoints

| Path                                             | Method | Description                                                         |
|--------------------------------------------------|--------|---------------------------------------------------------------------|
| *`/item-groups`*                                 | POST   | Creates an item group.                                              |
| *`/item-groups/{item group ID}/items/{item ID}`* | PATCH  | Updates the status of the item within the group.                    |
| *`/item-group-workflow-api/healthcheck`*         | GET    | Returns HTTP OK (`200`) to indicate a healthy application instance. |

