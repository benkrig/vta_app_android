package johankrig.hotmail.com;

public class Bus {
	
	String vehicleID;
	String lat;
	String lng;
//	String timestamp;
	
	


	public Bus(String vehicleID, String lat, String lng) {
		// TODO Auto-generated constructor stub
		this.vehicleID = vehicleID;
		this.lat = lat;
		this.lng = lng;
		//this.timestamp = timestamp;
	}


	public String getVehicleID() {
		return vehicleID;
	}


	public void setVehicleID(String vehicleID) {
		this.vehicleID = vehicleID;
	}


	public String getLat() {
		return lat;
	}


	public void setLat(String lat) {
		this.lat = lat;
	}


	public String getLng() {
		return lng;
	}


	public void setLng(String lng) {
		this.lng = lng;
	}

/*
	public String getHeading() {
		return heading;
	}


	public void setHeading(String heading) {
		this.heading = heading;
	}


	public String getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}*/
}
