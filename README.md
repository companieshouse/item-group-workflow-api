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

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.


Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        |order-service                                     | ECS cluster (stack) the service belongs to
**Load balancer**      |{env}-chs-internalapi                                            | The load balancer that sits in front of the service
**Concourse pipeline**     |[Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/item-group-workflow-api) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/item-group-workflow-api)                                  | Concourse pipeline link in shared services


### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
