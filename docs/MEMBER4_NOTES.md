# Member 4 Notes

## Current Scope

This branch owns the customer/database foundation work for the terminal-based Manufacturing and Supply Chain Management project.

Completed in the first task:

- Created the database script at `database/manufacturing_supply_chain.sql`.
- Created the isolated customer package folders only.
- Created shared package folders for future integration points.
- Added one shared JDBC connection utility at `src/util/DBConnection.java`.
- Added central database configuration at `config/database.properties`.

No customer module implementation has been added yet.

Completed in Phase 2:

- Added `Customer` model with fields matching the `customers` table.
- Added `CustomerDAO` with basic CRUD and customer login lookup support.
- Added customer-focused exceptions for missing customers and invalid login.
- Added `DatabaseConnectionTest` for a simple manual JDBC runtime check.
- Updated the database script so customer phone/PIN authentication data belongs to `users`.

Completed in Phase 3:

- Added terminal-based `CustomerController`.
- Added `CustomerService` for customer login, product browsing validation, and profile loading.
- Added read-only `CustomerProductDAO` for customer-facing product queries.
- Added customer-owned view models:
  - `CustomerProductView`
  - `CustomerProfileView`
- Added shared `InputUtil` to avoid creating multiple `Scanner` objects over `System.in`.
- Added customer menu placeholders for future order/shipment options without implementing order logic.

Completed in Phase 4:

- Added customer order and order item models.
- Added customer order placement from the terminal menu.
- Added customer order history.
- Added customer order details.
- Added customer order cancellation for `PLACED` orders only.
- Added supplier inventory validation before order creation.
- Added service-level JDBC transaction handling for order creation.
- Added `InsufficientStockException`.
- Updated the `customer_orders.status` enum so new customer orders can use `PLACED`.

Completed in Phase 5:

- Added read-only customer shipment tracking.
- Added `CustomerShipmentView` as a customer-facing shipment view model.
- Added read-only `CustomerShipmentDAO`.
- Added `CustomerShipmentService`.
- Updated the customer menu to include `Track Shipment`.
- Kept shipment creation, shipment updates, sales, and inventory deduction out of the Customer module.

## Ownership Boundaries

Member 4 should avoid implementing or editing:

- Admin controller/business logic
- Manufacturer controller/business logic
- Supplier controller/business logic
- Complete `Main.java` role menu integration

The shared database schema and JDBC connection utility are intentionally placed in shared locations because all modules will need them.

## Database Notes

Database name: `manufacturing_supply_chain`

The SQL script defines the required 21 tables:

1. users
2. manufacturers
3. suppliers
4. customers
5. categories
6. products
7. raw_materials
8. product_materials
9. workers
10. machines
11. production_requests
12. production_orders
13. production_workers
14. production_machines
15. quality_checks
16. inventory
17. inventory_transactions
18. customer_orders
19. order_items
20. sales
21. shipments

The script also includes:

- Initial admin user: username `admin`, password `admin123`
- Sample categories
- Stored procedures:
  - `deduct_production_materials`
  - `add_supplier_product_inventory`
- Triggers for shipment delivery, customer order totals, and sale inventory deduction

Customer authentication uses `users.phone_no` and `users.pin`.
The `customers` table stores only customer profile data such as `customer_name` and `address`.

## Customer Menu Responsibilities

`CustomerController` currently supports:

- Customer login using phone number and PIN
- View active products
- Search active products by name
- View active product details by product ID
- View customer profile
- Logout

The following menu options are displayed but intentionally left as placeholders for later phases:

- Track Shipment

No sale creation, inventory deduction, or shipment creation is implemented in Phase 4.

## Customer Integration Point

When the final shared `Main.java` is ready, another member can call the customer module with:

```java
CustomerController customerController = new CustomerController();
customerController.runCustomerLogin();
```

If a customer has already been authenticated elsewhere, the menu can be opened with:

```java
customerController.runCustomerMenu(customer);
```

This branch does not modify `Main.java` to avoid merge conflicts with other role menus.

## Product Browsing Assumptions

Member 2 owns the Product, Category, and Manufacturer classes/modules.
Because those classes do not exist in this branch yet, Phase 3 uses a customer-owned read-only DAO and view model:

- `CustomerProductDAO`
- `CustomerProductView`

These are not replacements for Member 2's Product implementation.
They only provide customer-facing SELECT queries that can later be integrated with the final Product module.

Product browsing reads from:

- `products`
- `categories`
- `manufacturers`

SQL joins used:

```sql
FROM products p
INNER JOIN categories c ON p.category_id = c.category_id
INNER JOIN manufacturers m ON p.manufacturer_id = m.manufacturer_id
```

Only active products are shown with:

```sql
WHERE p.status = 'ACTIVE'
```

Search uses a parameterized `LIKE` condition:

```sql
AND LOWER(p.product_name) LIKE LOWER(?)
```

Customer profile reads phone number through:

```sql
FROM customers c
INNER JOIN users u ON c.user_id = u.user_id
```

## Customer Order Flow

The Phase 4 order flow is:

1. Customer opens `Place Order`.
2. Active products are displayed.
3. Customer selects a product ID.
4. Customer selects a supplier that has the product in supplier inventory.
5. Customer enters quantity.
6. Quantity, product, supplier, and stock are validated.
7. Current product price is read from `products.unit_price`.
8. A `customer_orders` record is created with status `PLACED`.
9. One or more `order_items` records are created.
10. Database triggers update `customer_orders.total_amount`.
11. The transaction is committed.

If any validation or SQL operation fails after the transaction starts, the service rolls the transaction back.

Important rule:

Customer order placement does not deduct supplier inventory.
Supplier inventory is deducted later when the Supplier module creates a `sales` record and the database sale trigger runs.

## Order Status Rules

Customer-created orders use:

`PLACED`

Customer cancellation is allowed only for:

`PLACED`

Customer cancellation is not allowed for:

- `CONFIRMED`
- `PROCESSING`
- `SHIPPED`
- `DELIVERED`
- `REJECTED`
- `CANCELLED`

Cancellation updates the order status to `CANCELLED`.
Orders are not deleted because order history must remain available.

## Order DAO And Service Responsibilities

`CustomerOrderDAO` handles:

- Creating customer orders using an existing transaction connection
- Reading customer-scoped order summaries
- Reading customer-scoped order details
- Customer-scoped status updates/cancellation
- Reading active product price
- Reading supplier stock for a selected product
- Listing suppliers that currently have stock for a product

`OrderItemDAO` handles:

- Creating order items using the same transaction connection as the order
- Reading order items
- Reading customer-scoped order item details through `customer_orders`
- Deleting order items when appropriate for future maintenance use

`CustomerOrderService` coordinates:

- Customer validation
- Product ID validation
- Supplier ID validation
- Quantity validation
- Stock validation
- Transaction begin/commit/rollback
- Order history, details, and cancellation rules

## Inventory Validation Design

Supplier stock is checked with:

```sql
FROM inventory inv
INNER JOIN suppliers s ON inv.owner_type = 'SUPPLIER'
    AND inv.owner_id = s.supplier_id
INNER JOIN products p ON inv.product_id = p.product_id
WHERE inv.product_id = ?
AND s.supplier_id = ?
AND p.status = 'ACTIVE'
```

If the selected supplier has no matching inventory row, the order is rejected.
If requested quantity is greater than available inventory quantity, `InsufficientStockException` is thrown.

## Supplier Integration Assumptions

Member 3 owns the Supplier and Inventory modules.
Phase 4 does not create `Supplier.java`, `SupplierDAO.java`, or `Inventory.java`.

The customer module uses customer-owned read-only view models for supplier choices:

- `SupplierProductStockView`

This should integrate cleanly with Member 3's final supplier inventory code because it reads the shared `suppliers` and `inventory` tables without implementing supplier behavior.

## Order SQL Joins

Order history:

```sql
FROM customer_orders co
INNER JOIN suppliers s ON co.supplier_id = s.supplier_id
WHERE co.customer_id = ?
```

Order details:

```sql
FROM customer_orders co
INNER JOIN suppliers s ON co.supplier_id = s.supplier_id
WHERE co.order_id = ?
AND co.customer_id = ?
```

Order item details:

```sql
FROM customer_orders co
INNER JOIN order_items oi ON co.order_id = oi.order_id
INNER JOIN products p ON oi.product_id = p.product_id
WHERE co.order_id = ?
AND co.customer_id = ?
```

All customer-entered values use `PreparedStatement`.

## Customer Shipment Tracking

Shipment tracking is read-only in the Customer module.

The customer menu now uses:

1. View Products
2. Search Products
3. View Product Details
4. Place Order
5. View My Orders
6. View Order Details
7. Cancel Order
8. Track Shipment
9. View Profile
10. Logout

`CustomerShipmentView` uses fields available from the existing schema:

- `shipments.shipment_id`
- `shipments.customer_order_id`
- `shipments.tracking_number`
- `shipments.shipment_type`
- `shipments.status`
- `shipments.shipped_date`
- `shipments.delivered_date`
- `suppliers.company_name`
- `customers.customer_name`
- `customers.address`

Shipment statuses come from the existing database enum:

- `PENDING`
- `IN_TRANSIT`
- `DELIVERED`
- `CANCELLED`

The customer shipment DAO only performs `SELECT` queries.
It must not insert, update, or delete shipment rows.

## Shipment Ownership Security

Every shipment lookup is scoped to the logged-in customer.

Base query:

```sql
FROM shipments s
INNER JOIN customer_orders co ON s.customer_order_id = co.order_id
INNER JOIN customers c ON co.customer_id = c.customer_id
LEFT JOIN suppliers sup ON s.supplier_id = sup.supplier_id
WHERE s.shipment_type = 'SUPPLIER_TO_CUSTOMER'
```

List shipments:

```sql
AND co.customer_id = ?
```

Track by shipment:

```sql
AND co.customer_id = ?
AND s.shipment_id = ?
```

Track by order:

```sql
AND co.customer_id = ?
AND co.order_id = ?
```

The Customer module never queries shipment details by `shipment_id` alone.
If a shipment does not belong to the logged-in customer, the result is treated as unavailable.

## Shipment Integration Assumptions

Member 3 owns Supplier shipment creation and updates.
Phase 5 does not create:

- `Shipment.java`
- `SupplierShipmentDAO.java`
- `SupplierService.java`
- `SupplierController.java`

The Customer module expects Supplier/Manufacturer workflows to create `SUPPLIER_TO_CUSTOMER` shipment rows linked through `shipments.customer_order_id`.

## JDBC Notes

`DBConnection` reads database settings from:

`config/database.properties`

Default XAMPP settings are:

- Host: `localhost`
- Port: `3306`
- Database: `manufacturing_supply_chain`
- Username: `root`
- Password: empty

Before running Java code that uses JDBC, add the MySQL Connector/J jar to the project classpath.

Manual database test entry point:

`src/util/DatabaseConnectionTest.java`

Run it only after XAMPP MySQL/MariaDB is started and the Connector/J jar is available.

## Next Suggested Phase

The next phase can add customer-facing file export or integration testing if required by the project plan.
