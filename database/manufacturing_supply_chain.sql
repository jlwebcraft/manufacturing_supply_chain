CREATE DATABASE IF NOT EXISTS manufacturing_supply_chain;
USE manufacturing_supply_chain;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(100),
    phone_no VARCHAR(20) UNIQUE,
    pin VARCHAR(20),
    role ENUM('ADMIN', 'MANUFACTURER', 'SUPPLIER', 'CUSTOMER') NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS manufacturers (
    manufacturer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    company_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255),
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_manufacturers_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    company_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255),
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_suppliers_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    customer_name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customers_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    manufacturer_id INT NOT NULL,
    category_id INT NOT NULL,
    product_name VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_manufacturer
        FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(manufacturer_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_products_name (product_name),
    INDEX idx_products_category (category_id)
);

CREATE TABLE IF NOT EXISTS raw_materials (
    material_id INT AUTO_INCREMENT PRIMARY KEY,
    manufacturer_id INT NOT NULL,
    material_name VARCHAR(100) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    unit_cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_raw_materials_manufacturer
        FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(manufacturer_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uq_raw_materials_manufacturer_name UNIQUE (manufacturer_id, material_name)
);

CREATE TABLE IF NOT EXISTS product_materials (
    product_id INT NOT NULL,
    material_id INT NOT NULL,
    quantity_required DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (product_id, material_id),
    CONSTRAINT fk_product_materials_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_product_materials_material
        FOREIGN KEY (material_id) REFERENCES raw_materials(material_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS workers (
    worker_id INT AUTO_INCREMENT PRIMARY KEY,
    manufacturer_id INT NOT NULL,
    worker_name VARCHAR(100) NOT NULL,
    skill VARCHAR(100),
    status ENUM('AVAILABLE', 'ASSIGNED', 'INACTIVE') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_workers_manufacturer
        FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(manufacturer_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS machines (
    machine_id INT AUTO_INCREMENT PRIMARY KEY,
    manufacturer_id INT NOT NULL,
    machine_name VARCHAR(100) NOT NULL,
    machine_type VARCHAR(100),
    status ENUM('AVAILABLE', 'IN_USE', 'MAINTENANCE', 'INACTIVE') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_machines_manufacturer
        FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(manufacturer_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS production_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_production_requests_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_production_requests_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_production_requests_status (status)
);

CREATE TABLE IF NOT EXISTS production_orders (
    production_order_id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL UNIQUE,
    manufacturer_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    status ENUM('CREATED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_production_orders_request
        FOREIGN KEY (request_id) REFERENCES production_requests(request_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_production_orders_manufacturer
        FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(manufacturer_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_production_orders_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_production_orders_status (status)
);

CREATE TABLE IF NOT EXISTS production_workers (
    production_order_id INT NOT NULL,
    worker_id INT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (production_order_id, worker_id),
    CONSTRAINT fk_production_workers_order
        FOREIGN KEY (production_order_id) REFERENCES production_orders(production_order_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_production_workers_worker
        FOREIGN KEY (worker_id) REFERENCES workers(worker_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS production_machines (
    production_order_id INT NOT NULL,
    machine_id INT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (production_order_id, machine_id),
    CONSTRAINT fk_production_machines_order
        FOREIGN KEY (production_order_id) REFERENCES production_orders(production_order_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_production_machines_machine
        FOREIGN KEY (machine_id) REFERENCES machines(machine_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS quality_checks (
    quality_check_id INT AUTO_INCREMENT PRIMARY KEY,
    production_order_id INT NOT NULL,
    checked_by VARCHAR(100),
    status ENUM('PENDING', 'PASSED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    remarks VARCHAR(255),
    checked_at TIMESTAMP NULL,
    CONSTRAINT fk_quality_checks_order
        FOREIGN KEY (production_order_id) REFERENCES production_orders(production_order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS inventory (
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    owner_type ENUM('MANUFACTURER', 'SUPPLIER') NOT NULL,
    owner_id INT NOT NULL,
    product_id INT NULL,
    material_id INT NULL,
    quantity DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    reorder_level DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_inventory_material
        FOREIGN KEY (material_id) REFERENCES raw_materials(material_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_inventory_item CHECK (
        (owner_type = 'MANUFACTURER' AND material_id IS NOT NULL AND product_id IS NULL)
        OR
        (owner_type = 'SUPPLIER' AND product_id IS NOT NULL AND material_id IS NULL)
    ),
    INDEX idx_inventory_owner (owner_type, owner_id),
    INDEX idx_inventory_product_owner (owner_type, owner_id, product_id),
    INDEX idx_inventory_material_owner (owner_type, owner_id, material_id)
);

CREATE TABLE IF NOT EXISTS inventory_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    inventory_id INT NOT NULL,
    transaction_type ENUM('ADD', 'DEDUCT') NOT NULL,
    quantity DECIMAL(12, 2) NOT NULL,
    reference_type ENUM('PRODUCTION', 'SHIPMENT', 'SALE', 'MANUAL_ADD') NOT NULL,
    reference_id INT,
    remarks VARCHAR(255),
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_transactions_inventory
        FOREIGN KEY (inventory_id) REFERENCES inventory(inventory_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_inventory_transactions_reference (reference_type, reference_id)
);

CREATE TABLE IF NOT EXISTS customer_orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    supplier_id INT NOT NULL,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PLACED', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REJECTED') NOT NULL DEFAULT 'PLACED',
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_customer_orders_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_customer_orders_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_customer_orders_customer (customer_id),
    INDEX idx_customer_orders_supplier (supplier_id),
    INDEX idx_customer_orders_status (status)
);

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES customer_orders(order_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uq_order_items_order_product UNIQUE (order_id, product_id)
);

CREATE TABLE IF NOT EXISTS sales (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL UNIQUE,
    supplier_id INT NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    sale_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sales_order
        FOREIGN KEY (order_id) REFERENCES customer_orders(order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_sales_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS shipments (
    shipment_id INT AUTO_INCREMENT PRIMARY KEY,
    shipment_type ENUM('MANUFACTURER_TO_SUPPLIER', 'SUPPLIER_TO_CUSTOMER') NOT NULL,
    production_order_id INT NULL,
    customer_order_id INT NULL,
    supplier_id INT NULL,
    customer_id INT NULL,
    tracking_number VARCHAR(60) NOT NULL UNIQUE,
    status ENUM('PENDING', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    shipped_date TIMESTAMP NULL,
    delivered_date TIMESTAMP NULL,
    inventory_processed TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_shipments_production_order
        FOREIGN KEY (production_order_id) REFERENCES production_orders(production_order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_shipments_customer_order
        FOREIGN KEY (customer_order_id) REFERENCES customer_orders(order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_shipments_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_shipments_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_shipments_reference CHECK (
        (shipment_type = 'MANUFACTURER_TO_SUPPLIER' AND production_order_id IS NOT NULL AND customer_order_id IS NULL)
        OR
        (shipment_type = 'SUPPLIER_TO_CUSTOMER' AND customer_order_id IS NOT NULL AND production_order_id IS NULL)
    ),
    INDEX idx_shipments_tracking (tracking_number),
    INDEX idx_shipments_status (status)
);

INSERT INTO users (username, password, role, status)
SELECT 'admin', 'admin123', 'ADMIN', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);

INSERT INTO categories (category_name, description)
SELECT 'Electronics', 'Electronic devices and components'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE category_name = 'Electronics');

INSERT INTO categories (category_name, description)
SELECT 'Mechanical Parts', 'Machine and manufacturing parts'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE category_name = 'Mechanical Parts');

INSERT INTO categories (category_name, description)
SELECT 'Packaging', 'Packaging and shipping materials'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE category_name = 'Packaging');

DROP TRIGGER IF EXISTS trg_sales_after_insert;
DROP TRIGGER IF EXISTS trg_order_items_after_delete;
DROP TRIGGER IF EXISTS trg_order_items_after_update;
DROP TRIGGER IF EXISTS trg_order_items_after_insert;
DROP TRIGGER IF EXISTS trg_order_items_before_update;
DROP TRIGGER IF EXISTS trg_order_items_before_insert;
DROP TRIGGER IF EXISTS trg_shipments_before_update;
DROP PROCEDURE IF EXISTS add_supplier_product_inventory;
DROP PROCEDURE IF EXISTS deduct_production_materials;

DELIMITER //

CREATE PROCEDURE deduct_production_materials(IN p_production_order_id INT)
BEGIN
    DECLARE v_manufacturer_id INT;
    DECLARE v_product_id INT;
    DECLARE v_order_quantity INT;
    DECLARE v_invalid_count INT DEFAULT 0;

    SELECT manufacturer_id, product_id, quantity
    INTO v_manufacturer_id, v_product_id, v_order_quantity
    FROM production_orders
    WHERE production_order_id = p_production_order_id;

    IF v_product_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Production order not found.';
    END IF;

    SELECT COUNT(*)
    INTO v_invalid_count
    FROM product_materials pm
    LEFT JOIN inventory inv
        ON inv.owner_type = 'MANUFACTURER'
        AND inv.owner_id = v_manufacturer_id
        AND inv.material_id = pm.material_id
    WHERE pm.product_id = v_product_id
        AND (inv.inventory_id IS NULL OR inv.quantity < (pm.quantity_required * v_order_quantity));

    IF v_invalid_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Insufficient manufacturer raw material inventory.';
    END IF;

    INSERT INTO inventory_transactions (
        inventory_id,
        transaction_type,
        quantity,
        reference_type,
        reference_id,
        remarks
    )
    SELECT
        inv.inventory_id,
        'DEDUCT',
        pm.quantity_required * v_order_quantity,
        'PRODUCTION',
        p_production_order_id,
        'Raw materials deducted after manufacturer shipment delivery'
    FROM product_materials pm
    INNER JOIN inventory inv
        ON inv.owner_type = 'MANUFACTURER'
        AND inv.owner_id = v_manufacturer_id
        AND inv.material_id = pm.material_id
    WHERE pm.product_id = v_product_id;

    UPDATE inventory inv
    INNER JOIN product_materials pm
        ON inv.owner_type = 'MANUFACTURER'
        AND inv.owner_id = v_manufacturer_id
        AND inv.material_id = pm.material_id
    SET inv.quantity = inv.quantity - (pm.quantity_required * v_order_quantity)
    WHERE pm.product_id = v_product_id;
END//

CREATE PROCEDURE add_supplier_product_inventory(
    IN p_production_order_id INT,
    IN p_supplier_id INT
)
BEGIN
    DECLARE v_product_id INT;
    DECLARE v_order_quantity INT;
    DECLARE v_inventory_id INT;

    SELECT product_id, quantity
    INTO v_product_id, v_order_quantity
    FROM production_orders
    WHERE production_order_id = p_production_order_id;

    IF v_product_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Production order not found.';
    END IF;

    SELECT inventory_id
    INTO v_inventory_id
    FROM inventory
    WHERE owner_type = 'SUPPLIER'
        AND owner_id = p_supplier_id
        AND product_id = v_product_id
    LIMIT 1;

    IF v_inventory_id IS NULL THEN
        INSERT INTO inventory (
            owner_type,
            owner_id,
            product_id,
            quantity
        )
        VALUES (
            'SUPPLIER',
            p_supplier_id,
            v_product_id,
            v_order_quantity
        );

        SET v_inventory_id = LAST_INSERT_ID();
    ELSE
        UPDATE inventory
        SET quantity = quantity + v_order_quantity
        WHERE inventory_id = v_inventory_id;
    END IF;

    INSERT INTO inventory_transactions (
        inventory_id,
        transaction_type,
        quantity,
        reference_type,
        reference_id,
        remarks
    )
    VALUES (
        v_inventory_id,
        'ADD',
        v_order_quantity,
        'SHIPMENT',
        p_production_order_id,
        'Finished products added to supplier inventory after delivery'
    );
END//

CREATE TRIGGER trg_shipments_before_update
BEFORE UPDATE ON shipments
FOR EACH ROW
BEGIN
    IF NEW.status = 'DELIVERED'
        AND OLD.status <> 'DELIVERED'
        AND NEW.shipment_type = 'MANUFACTURER_TO_SUPPLIER'
        AND OLD.inventory_processed = 0 THEN

        IF NEW.production_order_id IS NULL OR NEW.supplier_id IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Manufacturer shipment requires production order and supplier.';
        END IF;

        CALL deduct_production_materials(NEW.production_order_id);
        CALL add_supplier_product_inventory(NEW.production_order_id, NEW.supplier_id);
        SET NEW.inventory_processed = 1;
    END IF;

    IF NEW.status = 'DELIVERED'
        AND OLD.status <> 'DELIVERED'
        AND NEW.delivered_date IS NULL THEN
        SET NEW.delivered_date = CURRENT_TIMESTAMP;
    END IF;
END//

CREATE TRIGGER trg_order_items_before_insert
BEFORE INSERT ON order_items
FOR EACH ROW
BEGIN
    SET NEW.subtotal = NEW.quantity * NEW.unit_price;
END//

CREATE TRIGGER trg_order_items_before_update
BEFORE UPDATE ON order_items
FOR EACH ROW
BEGIN
    SET NEW.subtotal = NEW.quantity * NEW.unit_price;
END//

CREATE TRIGGER trg_order_items_after_insert
AFTER INSERT ON order_items
FOR EACH ROW
BEGIN
    UPDATE customer_orders
    SET total_amount = (
        SELECT COALESCE(SUM(subtotal), 0.00)
        FROM order_items
        WHERE order_id = NEW.order_id
    )
    WHERE order_id = NEW.order_id;
END//

CREATE TRIGGER trg_order_items_after_update
AFTER UPDATE ON order_items
FOR EACH ROW
BEGIN
    UPDATE customer_orders
    SET total_amount = (
        SELECT COALESCE(SUM(subtotal), 0.00)
        FROM order_items
        WHERE order_id = NEW.order_id
    )
    WHERE order_id = NEW.order_id;

    IF OLD.order_id <> NEW.order_id THEN
        UPDATE customer_orders
        SET total_amount = (
            SELECT COALESCE(SUM(subtotal), 0.00)
            FROM order_items
            WHERE order_id = OLD.order_id
        )
        WHERE order_id = OLD.order_id;
    END IF;
END//

CREATE TRIGGER trg_order_items_after_delete
AFTER DELETE ON order_items
FOR EACH ROW
BEGIN
    UPDATE customer_orders
    SET total_amount = (
        SELECT COALESCE(SUM(subtotal), 0.00)
        FROM order_items
        WHERE order_id = OLD.order_id
    )
    WHERE order_id = OLD.order_id;
END//

CREATE TRIGGER trg_sales_after_insert
AFTER INSERT ON sales
FOR EACH ROW
BEGIN
    DECLARE v_invalid_count INT DEFAULT 0;

    SELECT COUNT(*)
    INTO v_invalid_count
    FROM (
        SELECT product_id, SUM(quantity) AS sold_quantity
        FROM order_items
        WHERE order_id = NEW.order_id
        GROUP BY product_id
    ) sold
    LEFT JOIN inventory inv
        ON inv.owner_type = 'SUPPLIER'
        AND inv.owner_id = NEW.supplier_id
        AND inv.product_id = sold.product_id
    WHERE inv.inventory_id IS NULL
        OR inv.quantity < sold.sold_quantity;

    IF v_invalid_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Insufficient supplier product inventory.';
    END IF;

    INSERT INTO inventory_transactions (
        inventory_id,
        transaction_type,
        quantity,
        reference_type,
        reference_id,
        remarks
    )
    SELECT
        inv.inventory_id,
        'DEDUCT',
        sold.sold_quantity,
        'SALE',
        NEW.sale_id,
        'Supplier inventory deducted after sale'
    FROM (
        SELECT product_id, SUM(quantity) AS sold_quantity
        FROM order_items
        WHERE order_id = NEW.order_id
        GROUP BY product_id
    ) sold
    INNER JOIN inventory inv
        ON inv.owner_type = 'SUPPLIER'
        AND inv.owner_id = NEW.supplier_id
        AND inv.product_id = sold.product_id;

    UPDATE inventory inv
    INNER JOIN (
        SELECT product_id, SUM(quantity) AS sold_quantity
        FROM order_items
        WHERE order_id = NEW.order_id
        GROUP BY product_id
    ) sold
        ON inv.owner_type = 'SUPPLIER'
        AND inv.owner_id = NEW.supplier_id
        AND inv.product_id = sold.product_id
    SET inv.quantity = inv.quantity - sold.sold_quantity;

    UPDATE customer_orders
    SET status = 'PROCESSING'
    WHERE order_id = NEW.order_id;
END//

DELIMITER ;
