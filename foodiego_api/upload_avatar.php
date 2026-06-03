<?php
// upload_avatar.php - User profile picture uploader

require_once 'db_config.php';

$user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;

if ($user_id <= 0) {
    echo json_encode(array("status" => "error", "message" => "Invalid User Session."));
    exit();
}

if (!isset($_FILES['image'])) {
    echo json_encode(array("status" => "error", "message" => "No image file provided."));
    exit();
}

try {
    $target_dir = "uploads/";
    if (!file_exists($target_dir)) {
        mkdir($target_dir, 0777, true);
    }
    
    $extension = strtolower(pathinfo($_FILES["image"]["name"], PATHINFO_EXTENSION));
    if (empty($extension)) {
        $extension = "jpg";
    }
    
    // Create unique filename to prevent browser caching issues
    $filename = "avatar_" . $user_id . "_" . time() . "." . $extension;
    $target_file = $target_dir . $filename;
    
    if (move_uploaded_file($_FILES["image"]["tmp_name"], $target_file)) {
        // Construct dynamic absolute server path
        $protocol = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http");
        $host = $_SERVER['HTTP_HOST'];
        $dir = dirname($_SERVER['REQUEST_URI']);
        $dir_clean = ($dir === "\\" || $dir === "/") ? "" : $dir;
        $absolute_url = $protocol . "://" . $host . $dir_clean . "/" . $target_file;
        
        // Save to users table
        $stmt = $conn->prepare("UPDATE users SET profile_image = :profile_image WHERE id = :id");
        $stmt->bindParam(':profile_image', $absolute_url);
        $stmt->bindParam(':id', $user_id);
        
        if ($stmt->execute()) {
            echo json_encode(array(
                "status" => "success",
                "message" => "Profile image updated successfully!",
                "profileImage" => $absolute_url
            ));
        } else {
            echo json_encode(array("status" => "error", "message" => "Failed to update profile record in database."));
        }
    } else {
        echo json_encode(array("status" => "error", "message" => "Unable to save file to uploads folder."));
    }
} catch(Exception $e) {
    echo json_encode(array("status" => "error", "message" => "Server Error: " . $e->getMessage()));
}
?>
