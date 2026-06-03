<?php
// login.php - User authentication endpoint

require_once 'db_config.php';

$data = json_decode(file_get_contents("php://input"));

$email = isset($data->email) ? trim($data->email) : (isset($_POST['email']) ? trim($_POST['email']) : "");
$password = isset($data->password) ? trim($data->password) : (isset($_POST['password']) ? trim($_POST['password']) : "");

if (empty($email) || empty($password)) {
    echo json_encode(array("status" => "error", "message" => "Please enter your email and password."));
    exit();
}

try {
    $stmt = $conn->prepare("SELECT id, name, email, password, profile_image FROM users WHERE email = :email LIMIT 1");
    $stmt->bindParam(':email', $email);
    $stmt->execute();

    if ($stmt->rowCount() > 0) {
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        
        // Verify hashed password
        if (password_verify($password, $row['password'])) {
            $avatar = "";
            if (!empty($row['profile_image'])) {
                // Return dynamic absolute profile path if uploaded
                $avatar = $row['profile_image'];
            }
            
            echo json_encode(array(
                "status" => "success",
                "message" => "Login Successful!",
                "user" => array(
                    "userId" => strval($row['id']),
                    "name" => $row['name'],
                    "email" => $row['email'],
                    "profileImage" => $avatar
                )
            ));
        } else {
            echo json_encode(array("status" => "error", "message" => "Invalid Email Or Password"));
        }
    } else {
        echo json_encode(array("status" => "error", "message" => "Invalid Email Or Password"));
    }
} catch(PDOException $e) {
    echo json_encode(array("status" => "error", "message" => "Database Error: " . $e->getMessage()));
}
?>
