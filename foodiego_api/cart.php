<?php
// cart.php - Shopping cart REST endpoint

require_once 'db_config.php';

$method = $_SERVER['REQUEST_METHOD'];
$data = json_decode(file_get_contents("php://input"));

// Extract user_id parameter from URL query, JSON body, or form POST parameters
$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : (isset($data->user_id) ? intval($data->user_id) : (isset($_POST['user_id']) ? intval($_POST['user_id']) : 0));

if ($user_id <= 0) {
    echo json_encode(array("status" => "error", "message" => "Invalid User Session."));
    exit();
}

if ($method === 'GET') {
    // Read cart contents for user, joining with products to get complete details
    try {
        $stmt = $conn->prepare("SELECT c.quantity, p.id, p.name, p.description, p.image_url, p.price, p.rating, p.delivery_time 
                                FROM cart c 
                                JOIN products p ON c.product_id = p.id 
                                WHERE c.user_id = :user_id");
        $stmt->bindParam(':user_id', $user_id);
        $stmt->execute();
        
        $items = array();
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $items[] = array(
                "food" => array(
                    "id" => strval($row['id']),
                    "name" => $row['name'],
                    "description" => $row['description'],
                    "imageUrl" => $row['image_url'],
                    "price" => $row['price'],
                    "rating" => $row['rating'],
                    "deliveryTime" => $row['delivery_time']
                ),
                "quantity" => intval($row['quantity'])
            );
        }
        echo json_encode(array("status" => "success", "items" => $items));
    } catch(PDOException $e) {
        echo json_encode(array("status" => "error", "message" => "Database Error: " . $e->getMessage()));
    }
} elseif ($method === 'POST') {
    // Synchronize full list array or execute individual updates
    $items = isset($data->items) ? $data->items : null;
    
    if ($items !== null) {
        try {
            $conn->beginTransaction();
            
            // Delete existing cart contents
            $del = $conn->prepare("DELETE FROM cart WHERE user_id = :user_id");
            $del->bindParam(':user_id', $user_id);
            $del->execute();
            
            // Re-insert synchronized list rows
            $ins = $conn->prepare("INSERT INTO cart (user_id, product_id, quantity) VALUES (:user_id, :product_id, :quantity)");
            
            foreach ($items as $item) {
                $product_id = intval($item->food->id);
                $quantity = intval($item->quantity);
                
                if ($product_id > 0 && $quantity > 0) {
                    $ins->bindParam(':user_id', $user_id);
                    $ins->bindParam(':product_id', $product_id);
                    $ins->bindParam(':quantity', $quantity);
                    $ins->execute();
                }
            }
            
            $conn->commit();
            echo json_encode(array("status" => "success", "message" => "Cart synchronized successfully."));
        } catch(PDOException $e) {
            $conn->rollBack();
            echo json_encode(array("status" => "error", "message" => "Database Error: " . $e->getMessage()));
        }
    } else {
        // Individual item updates fallback
        $product_id = isset($data->product_id) ? intval($data->product_id) : (isset($_POST['product_id']) ? intval($_POST['product_id']) : 0);
        $quantity = isset($data->quantity) ? intval($data->quantity) : (isset($_POST['quantity']) ? intval($_POST['quantity']) : 0);
        $action = isset($data->action) ? $data->action : (isset($_POST['action']) ? $_POST['action'] : "add");
        
        if ($product_id <= 0) {
            echo json_encode(array("status" => "error", "message" => "Product ID required."));
            exit();
        }
        
        try {
            if ($action === "remove" || $quantity <= 0) {
                $stmt = $conn->prepare("DELETE FROM cart WHERE user_id = :user_id AND product_id = :product_id");
                $stmt->bindParam(':user_id', $user_id);
                $stmt->bindParam(':product_id', $product_id);
                $stmt->execute();
                echo json_encode(array("status" => "success", "message" => "Item removed."));
            } else {
                $chk = $conn->prepare("SELECT id, quantity FROM cart WHERE user_id = :user_id AND product_id = :product_id");
                $chk->bindParam(':user_id', $user_id);
                $chk->bindParam(':product_id', $product_id);
                $chk->execute();
                
                if ($chk->rowCount() > 0) {
                    $row = $chk->fetch(PDO::FETCH_ASSOC);
                    $new_qty = ($action === "set") ? $quantity : ($row['quantity'] + $quantity);
                    
                    $upd = $conn->prepare("UPDATE cart SET quantity = :quantity WHERE id = :id");
                    $upd->bindParam(':quantity', $new_qty);
                    $upd->bindParam(':id', $row['id']);
                    $upd->execute();
                } else {
                    $ins = $conn->prepare("INSERT INTO cart (user_id, product_id, quantity) VALUES (:user_id, :product_id, :quantity)");
                    $ins->bindParam(':user_id', $user_id);
                    $ins->bindParam(':product_id', $product_id);
                    $ins->bindParam(':quantity', $quantity);
                    $ins->execute();
                }
                echo json_encode(array("status" => "success", "message" => "Cart updated."));
            }
        } catch(PDOException $e) {
            echo json_encode(array("status" => "error", "message" => "Database Error: " . $e->getMessage()));
        }
    }
} elseif ($method === 'DELETE') {
    // Clear cart completely
    try {
        $stmt = $conn->prepare("DELETE FROM cart WHERE user_id = :user_id");
        $stmt->bindParam(':user_id', $user_id);
        $stmt->execute();
        echo json_encode(array("status" => "success", "message" => "Cart cleared."));
    } catch(PDOException $e) {
        echo json_encode(array("status" => "error", "message" => "Database Error: " . $e->getMessage()));
    }
}
?>
