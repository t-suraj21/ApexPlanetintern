<?php
// register.php - User account registrar endpoint

require_once 'db_config.php';

$data = json_decode(file_get_contents("php://input"));

// Extract fields from body or form-data fallback
$name = isset($data->name) ? trim($data->name) : (isset($_POST['name']) ? trim($_POST['name']) : "");
$email = isset($data->email) ? trim($data->email) : (isset($_POST['email']) ? trim($_POST['email']) : "");
$password = isset($data->password) ? trim($data->password) : (isset($_POST['password']) ? trim($_POST['password']) : "");

if (empty($name) || empty($email) || empty($password)) {
    echo json_encode(array("status" => "error", "message" => "Please enter your name, email and password."));
    exit();
}

try {
    // Verify duplicate email
    $stmt = $conn->prepare("SELECT id FROM users WHERE email = :email LIMIT 1");
    $stmt->bindParam(':email', $email);
    $stmt->execute();

    if ($stmt->rowCount() > 0) {
        echo json_encode(array("status" => "error", "message" => "Email address already registered."));
        exit();
    }

    // Encrypt password securely
    $hashed = password_hash($password, PASSWORD_DEFAULT);

    // Save record
    $insert = $conn->prepare("INSERT INTO users (name, email, password) VALUES (:name, :email, :password)");
    $insert->bindParam(':name', $name);
    $insert->bindParam(':email', $email);
    $insert->bindParam(':password', $hashed);

    if ($insert->execute()) {
        $last_id = $conn->lastInsertId();
        echo json_encode(array(
            "status" => "success",
            "message" => "Registration Successful!",
            "user" => array(
                "userId" => strval($last_id),
                "name" => $name,
                "email" => $email,
                "profileImage" => ""
            )
        ));
    } else {
        echo json_encode(array("status" => "error", "message" => "Unable to create account."));
    }
} catch(PDOException $e) {
    echo json_encode(array("status" => "error", "message" => "Database Error: " . $e->getMessage()));
}
?>
