<?php
// db_config.php - MySQL PDO Connection configuration

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

$host = "localhost";
$db_name = "foodiego_db";
$username = "root";
$password = ""; // Default Apache/MySQL password

try {
    $conn = new PDO("mysql:host=" . $host . ";dbname=" . $db_name, $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $conn->exec("set names utf8");
} catch(PDOException $exception) {
    // If the database does not exist (code 1049), auto-create and seed it dynamically!
    if ($exception->getCode() == 1049 || strpos($exception->getMessage(), "Unknown database") !== false) {
        try {
            $temp_conn = new PDO("mysql:host=" . $host, $username, $password);
            $temp_conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            $temp_conn->exec("CREATE DATABASE IF NOT EXISTS `$db_name` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;");
            
            // Connect to newly created database
            $conn = new PDO("mysql:host=" . $host . ";dbname=" . $db_name, $username, $password);
            $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            $conn->exec("set names utf8");
            
            // Read and run sql seed queries
            $sql_file = __DIR__ . "/foodiego_db.sql";
            if (file_exists($sql_file)) {
                $sql_content = file_get_contents($sql_file);
                $conn->exec($sql_content);
            }
        } catch (PDOException $inner_exception) {
            echo json_encode(array("status" => "error", "message" => "Auto-creation failed: " . $inner_exception->getMessage()));
            exit();
        }
    } else {
        echo json_encode(array("status" => "error", "message" => "Connection failed: " . $exception->getMessage()));
        exit();
    }
}
?>
