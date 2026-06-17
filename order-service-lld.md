# Order Service LLD

## Service Summary
- Service: `order-service`
- Port: `8084`
- Database: MySQL `order_db`
- Responsibilities:
  - Create orders from cart contents
  - Query user orders
  - Update order status
  - React to Kafka payment events

## Internal Structure
```mermaid
flowchart TD
    Req["HTTP Request"] --> F["JwtAuthFilter"]
    F --> OC["OrderController"]
    OC --> JU["JwtUtils"]
    OC --> OS["OrderService"]
    OS --> OR["OrderRepository"]
    OS --> OIR["OrderItemRepository"]
    OS --> RT["RestTemplate"]
    RT --> CS["CART-SERVICE"]
    RT --> PS["PRODUCT-SERVICE"]
    OR --> DB["MySQL order_db"]
    OIR --> DB
    K["Kafka payment-events"] --> OEL["OrderEventListener"]
    OEL --> OS
```

## Main Components
### `OrderController`
Endpoints:
- `POST /api/orders/place`
- `GET /api/orders`
- `PUT /api/orders/update/{orderId}?status=...`

Behavior:
- Extracts `userId` from JWT for user-facing endpoints.

### `OrderService`
Key methods:
- `placeOrder(userId, token)`
- `getOrdersByUser(userId)`
- `updateOrderStatus(orderId, status)`

Responsibilities:
- Fetch current cart from `cart-service`
- Fetch product prices from `product-service`
- Compute total order amount
- Persist `Order` and `OrderItem`
- Clear cart after successful creation

### `OrderEventListener`
- Kafka consumer on topic `payment-events`
- Parses JSON payload
- Updates order status to `PAID` or `FAILED`

## Order Placement Flow
```mermaid
sequenceDiagram
    participant C as Client
    participant OC as OrderController
    participant OS as OrderService
    participant CART as Cart Service
    participant PROD as Product Service
    participant DB as MySQL

    C->>OC: POST /api/orders/place
    OC->>OS: placeOrder(userId, token)
    OS->>CART: GET /api/cart
    CART-->>OS: Cart items
    loop for each cart item
        OS->>PROD: GET product details
        PROD-->>OS: Product price
    end
    OS->>DB: Save order and order items
    OS->>CART: DELETE /api/cart/clear
    OS-->>OC: OrderResponse
    OC-->>C: Created order
```

## Payment Update Flow
```mermaid
sequenceDiagram
    participant PAY as Payment Service
    participant K as Kafka
    participant L as OrderEventListener
    participant OS as OrderService
    participant DB as MySQL

    PAY->>K: Publish payment-events
    K->>L: Consume event
    L->>OS: updateOrderStatus(orderId, status)
    OS->>DB: Persist new status
```

## Data Model
- `Order`
  - id
  - userId
  - totalAmount
  - status
  - createdAt
  - items
- `OrderItem`
  - id
  - productId
  - quantity
  - price

## Dependencies
- MySQL
- Eureka
- `cart-service`
- `product-service`
- Kafka

## Result
`order-service` is the orchestration core for checkout preparation: it converts cart state into persisted orders and then tracks final payment outcome asynchronously.
