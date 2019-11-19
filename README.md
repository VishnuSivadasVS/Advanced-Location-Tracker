# Advanced Location Tracker

Best solution for tracking live location in Background.

This application can manage to get the live location in background on all Android versions. Fixed all the problems that could occur while
creating a live location tracking application.

Implemented in fragments with bottom navigation. Polished UI and is easy to customize and use.

From the API side do this to update data to DataBase

```<?php 
require "connect.php";
if(!empty($_POST['latitude']) && !empty($_POST['longitude'])) {
    date_default_timezone_set('Asia/Kolkata');
    $timestamp = date("Y-m-d H:i:s");    
	$user_la = $_POST["latitude"];
    $user_lo = $_POST["longitude"];
	$sql="update livelocation set latitude = $user_la, longitude = $user_lo, updatetime = '$timestamp' where id = 1";
	$result=mysqli_query($conn,$sql);
}
?>```
