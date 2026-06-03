<?php
// get_products.php - Catalog streaming endpoint

require_once 'db_config.php';

try {
    $stmt = $conn->prepare("SELECT id, name, description, image_url, price, rating, delivery_time FROM products ORDER BY id ASC");
    $stmt->execute();
    
    $products = array();
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        // Strip non-numeric price characters (like currency symbols) to parse as double
        $priceNum = doubleval(preg_replace('/[^\d.]/', '', $row['price']));
        if ($priceNum == 0) {
            $priceNum = 15.0; // safety fallback
        } else {
            $priceNum = $priceNum / 15.0; // scale back to match DTO mapper price*15 scaling on Android client
        }
        
        $products[] = array(
            "id" => intval($row['id']),
            "title" => $row['name'],
            "description" => $row['description'],
            "price" => $priceNum,
            "rating" => doubleval($row['rating']),
            "thumbnail" => $row['image_url'],
            "category" => "Food"
        );
    }
    
    // Return compatible ProductResponse structure
    echo json_encode(array("products" => $products));
} catch(PDOException $e) {
    echo json_encode(array("status" => "error", "message" => "Database Error: " . $e->getMessage()));
}
?>
