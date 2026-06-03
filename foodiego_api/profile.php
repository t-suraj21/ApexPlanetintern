<?php
// profile.php - User profile details controller

require_once 'db_config.php';

$method = $_SERVER['REQUEST_METHOD'];
$data = json_decode(file_get_contents("php://input"));

$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : (isset($data->user_id) ? intval($data->user_id) : (isset($_POST['user_id']) ? intval($_POST['user_id']) : 0));

if ($user_id <= 0) {
    echo json_encode(array("status" => "error", "message" => "Invalid User Session."));
    exit();
}

if ($method === 'GET') {
    try {
        $stmt = $conn->prepare("SELECT id, name, email, profile_image FROM users WHERE id = :id LIMIT 1");
        $stmt->bindParam(':id', $user_id);
        $stmt->execute();
        
        if ($stmt->rowCount() > 0) {
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            echo json_encode(array(
                "status" => "success",
                "user" => array(
                    "userId" => strval($row['id']),
                    "name" => $row['name'],
                    "email" => $row['email'],
                    "profileImage" => $row['profile_image'] ? $row['profile_image'] : ""
                )
            ));
        } else {
            echo json_encode(array("status" => "error", "message" => "User record not found."));
        }
    } catch(PDOException $e) {
        echo json_encode(array("status" => "error", "message" => "Database Error: " . $e->getMessage()));
    }
} elseif ($method === 'POST') {
    $name = isset($data->name) ? trim($data->name) : (isset($_POST['name']) ? trim($_POST['name']) : "");
    
    if (empty($name)) {
        echo json_encode(array("status" => "error", "message" => "Please enter your name."));
        exit();
    }
    
    try {
        $stmt = $conn->prepare("UPDATE users SET name = :name WHERE id = :id");
        $stmt->bindParam(':name', $name);
        $stmt->bindParam(':id', $user_id);
        
        if ($stmt->execute()) {
            echo json_encode(array("status" => "success", "message" => "Profile details updated successfully!"));
        } else {
            echo json_encode(array("status" => "error", "message" => "Unable to update profile."));
        }
    } catch(PDOException $e) {
        echo json_encode(array("status" => "error", "message" => "Database Error: " . $e->getMessage()));
    }
}
?>
