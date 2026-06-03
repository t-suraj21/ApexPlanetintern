<?php
// place_order.php - Order placement transaction endpoint

require_once 'db_config.php';

$data = json_decode(file_get_contents("php://input"));

$user_id = isset($data->user_id) ? intval($data->user_id) : (isset($_POST['user_id']) ? intval($_POST['user_id']) : 0);
$total_price = isset($data->total_price) ? trim($data->total_price) : (isset($_POST['total_price']) ? trim($_POST['total_price']) : "");

if ($user_id <= 0 || empty($total_price)) {
    echo json_encode(array("status" => "error", "message" => "Invalid Order details."));
    exit();
}

try {
    $conn->beginTransaction();
    
    // 1. Fetch current cart items to place order
    $cart_stmt = $conn->prepare("SELECT product_id, quantity FROM cart WHERE user_id = :user_id");
    $cart_stmt->bindParam(':user_id', $user_id);
    $cart_stmt->execute();
    
    if ($cart_stmt->rowCount() == 0) {
        echo json_encode(array("status" => "error", "message" => "Your cart is empty."));
        $conn->rollBack();
        exit();
    }
    
    $cart_items = $cart_stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // 2. Insert main order row
    $order_stmt = $conn->prepare("INSERT INTO orders (user_id, total_price, status) VALUES (:user_id, :total_price, 'Pending')");
    $order_stmt->bindParam(':user_id', $user_id);
    $order_stmt->bindParam(':total_price', $total_price);
    $order_stmt->execute();
    
    $order_id = $conn->lastInsertId();
    
    // 3. Clone item records into order_items
    $item_ins = $conn->prepare("INSERT INTO order_items (order_id, product_id, quantity, price) 
                                VALUES (:order_id, :product_id, :quantity, :price)");
    
    foreach ($cart_items as $item) {
        $product_id = intval($item['product_id']);
        $quantity = intval($item['quantity']);
        
        // Lookup individual product pricing
        $price_stmt = $conn->prepare("SELECT price FROM products WHERE id = :id LIMIT 1");
        $price_stmt->bindParam(':id', $product_id);
        $price_stmt->execute();
        $prod = $price_stmt->fetch(PDO::FETCH_ASSOC);
        $price = $prod ? $prod['price'] : "₹0";
        
        $item_ins->bindParam(':order_id', $order_id);
        $item_ins->bindParam(':product_id', $product_id);
        $item_ins->bindParam(':quantity', $quantity);
        $item_ins->bindParam(':price', $price);
        $item_ins->execute();
    }
    
    // 4. Clear shopping cart
    $del_cart = $conn->prepare("DELETE FROM cart WHERE user_id = :user_id");
    $del_cart->bindParam(':user_id', $user_id);
    $del_cart->execute();
    
    $conn->commit();
    echo json_encode(array(
        "status" => "success",
        "message" => "Order placed successfully! Thank you for ordering with FoodieGo.",
        "orderId" => strval($order_id)
    ));
} catch (PDOException $e) {
    $conn->rollBack();
    echo json_encode(array("status" => "error", "message" => "Checkout Error: " . $e->getMessage()));
}
?>
