# Payment Service LLD

## Service Summary
- Service: `payment-service`
- Port: `8085`
- Database configured: MySQL `payment_db`
- Responsibilities:
  - Create Stripe checkout sessions
  - Receive and validate Stripe webhooks
  - Publish payment result events to Kafka

## Internal Structure
```mermaid
flowchart TD
    Req["HTTP Request"] --> PC["PaymentController"]
    PC --> PS["PaymentService"]
    PS --> PG["IPaymentGateway / StripePaymentGateway"]
    PS --> WP["WebhookProcessor"]
    PG --> ST["Stripe Checkout API"]
    ST --> WH["Stripe Webhook"]
    WH --> WP
    WP --> H1["CheckoutCompletedHandler"]
    WP --> H2["PaymentFailedHandler"]
    H1 --> PUB["IPaymentEventPublisher / KafkaPaymentEventPublisher"]
    H2 --> PUB
    PUB --> K["Kafka topic: payment-events"]
```

## Main Components
### `PaymentController`
Endpoints:
- `POST /api/payments/checkout/{orderId}`
- `POST /api/payments/webhook`

Behavior:
- Starts checkout for a given order id, email, and amount
- Accepts Stripe webhook payload and signature header

### `PaymentService`
- Thin application service delegating to:
  - `IPaymentGateway`
  - `WebhookProcessor`

### `StripePaymentGateway`
- Creates Stripe hosted checkout session
- Sets:
  - payment mode
  - customer email
  - success URL
  - cancel URL
  - metadata `order_id`
- Returns Stripe checkout URL

### `WebhookProcessor`
- Validates Stripe webhook signature using configured secret
- Selects matching webhook handler by event type

### Webhook Handlers
- `CheckoutCompletedHandler`
  - Supports `checkout.session.completed`
  - Extracts `order_id` and amount
  - Publishes `PAID` or `FAILED`
- `PaymentFailedHandler`
  - Supports `payment_intent.payment_failed`
  - Publishes failure event

### Event Publisher
- `KafkaPaymentEventPublisher`
- Writes `PaymentEventDTO` messages to topic `payment-events`

## Checkout Flow
```mermaid
sequenceDiagram
    participant C as Client
    participant PC as PaymentController
    participant PS as PaymentService
    participant SP as StripePaymentGateway
    participant ST as Stripe

    C->>PC: POST /api/payments/checkout/{orderId}
    PC->>PS: createCheckout(orderId, email, amount)
    PS->>SP: createCheckoutSession(...)
    SP->>ST: Create Stripe checkout session
    ST-->>SP: Checkout URL
    SP-->>PS: Checkout URL
    PS-->>PC: Checkout URL
    PC-->>C: HTTP 200 + URL
```

## Webhook Processing Flow
```mermaid
sequenceDiagram
    participant ST as Stripe
    participant PC as PaymentController
    participant PS as PaymentService
    participant WP as WebhookProcessor
    participant H as Matching Handler
    participant K as Kafka

    ST->>PC: POST /api/payments/webhook
    PC->>PS: handleWebhook(payload, signature)
    PS->>WP: process(payload, signature)
    WP->>WP: Verify signature and parse event
    WP->>H: Dispatch by event type
    H->>K: Publish PAID or FAILED event
    PC-->>ST: Webhook processed
```

## Dependencies
- Stripe
- Kafka
- MySQL configuration present
- Eureka

## Notable Design Notes
- Current code is integration-heavy and persistence-light.
- `IOrderServiceClient` and `IAuthServiceClient` exist as placeholders but are not yet implemented in the observed flow.
- Payment result propagation is event-driven instead of direct order callback.

## Result
`payment-service` is the external payment integration boundary for the platform and converts Stripe events into internal Kafka messages consumed by `order-service`.
