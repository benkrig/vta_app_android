<?php
//metafora webserivce
//development started 4/7/2014

//initialize
$response = null;
//database stuff----------------------
$host = 'localhost';
$username = 'root';
$password = '';
$database = 'trimet';
//------------------------------------

//get request 
//valid requests----------------------
$createroutes = 'rt';
$getlocations = 'loc';
//------------------------------------
$request = $_GET['type'];

//process request
if($request == $createroutes)
{
	///createroutes/
	$sloc = explode(",", $_GET['sloc']);
	
	$eloc = explode(",", $_GET['eloc']);
	//ROUTE ALGORITM
	echo $response;
}
elseif($request == $getlocations)
{
	//unencoded response from db
	$raw_response = null;

	//get request parameters and cast them to float,------
	//if a string is inputted coords will default to 0,0
	//and no vehicles will be returned
	$loc = explode(",", $_GET['loc']);
	$loc[0] = floatval($loc[0]);
	$loc[1] = floatval($loc[1]);
	//----------------------------------------------------
	
	//create connection to db with $connect---------------
	$connect = mysql_connect($host, $username, $password);
	mysql_select_db($database, $connect);
	//----------------------------------------------------
	
	//uses haversine to test whether vehicles are within distance of loc
	$sql = "SELECT vehicleID, latitude, longitude, type,
	(3959 * acos( cos( radians({$loc[0]}))
	* cos( radians(latitude)) 
	* cos( radians(longitude) 
	- radians({$loc[1]})) 
	+ sin( radians({$loc[0]})) 
	* sin( radians( latitude)))) 
	AS distance FROM data HAVING distance < 25 
	ORDER BY distance;";
	//---------------------------------------------------
	
	//get data-------------------------------------------
	$db_response = mysql_query($sql, $connect);
	while ($row = mysql_fetch_assoc($db_response))
	{
		$row['vehicleID'] = (int)$row['vehicleID'];
		$row['latitude'] = (float)$row['latitude'];
		$row['longitude'] = (float)$row['longitude'];
		$row['distance'] = (float)$row['distance'];
    	$raw_response[] = $row;
	}
	//---------------------------------------------------
	
	//close db_connection
	mysql_close($connect);
	
	//json format
	$response = json_encode($raw_response);
	
	//print response
	echo $response;
}
else 
{
	//invalid request
	echo $response;
	
}
?>
