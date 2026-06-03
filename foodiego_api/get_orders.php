<?php
// get_orders.php - User order history stream endpoint

require_once 'db_config.php';

$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

if ($user_id <= 0) {
    echo json_encode(array("status" => "error", "message" => "Invalid User Session."));
    exit();
}

try {
    // Select orders converting timestamp to Java millisecond format
    $stmt = $conn->prepare("SELECT id, total_price, status, UNIX_TIMESTAMP(created_at) * 1000 AS timestamp FROM orders WHERE user_id = :user_id ORDER BY id DESC");
    $stmt->bindParam(':user_id', $user_id);
    $stmt->execute();
    
    $orders = array();
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $order_id = $row['id'];
        
        // Retrieve nested row items
        $item_stmt = $conn->prepare("SELECT oi.quantity, p.id, p.name, p.description, p.image_url, p.price, p.rating, p.delivery_time 
                                     FROM order_items oi 
                                     JOIN products p ON oi.product_id = p.id 
                                     WHERE oi.order_id = :order_id");
        $item_stmt->bindParam(':order_id', $order_id);
        $item_stmt->execute();
        
        $items = array();
        while ($item_row = $item_stmt->fetch(PDO::FETCH_ASSOC)) {
            $items[] = array(
                "food" => array(
                    "id" => strval($item_row['id']),
                    "name" => $item_row['name'],
                    "description" => $item_row['description'],
                    "imageUrl" => $item_row['image_url'],
                    "price" => $item_row['price'],
                    "rating" => $item_row['rating'],
                    "deliveryTime" => $item_row['delivery_time']
                ),
                "quantity" => intval($item_row['quantity'])
            );
        }
        
        $orders[] = array(
            "orderId" => strval($order_id),
            "userId" => strval($user_id),
            "items" => $items,
            "totalPrice" => $row['total_price'],
            "timestamp" => intval($row['timestamp']),
            "status" => $row['status']
        );
    }
    
    echo json_encode(array("status" => "success", "orders" => $orders));
} catch(PDOException $e) {
    echo json_encode(array("status" => "error", "message" => "Database Error: " . $e->getMessage()));
}
?>
