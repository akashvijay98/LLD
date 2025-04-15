// Parking Lot LLD
/*
    
    The parking lot should have multiple levels, each level with a certain number of parking spots.
    The parking lot should support different types of vehicles, such as cars, motorcycles, and trucks.
    Each parking spot should be able to accommodate a specific type of vehicle.
    The system should assign a parking spot to a vehicle upon entry and release it when the vehicle exits.
    The system should track the availability of parking spots and provide real-time information to customers.
    The system should handle multiple entry and exit points and support concurrent access=

    ![LLD](./static/Vehicle (abstract) - visual selection.png)

*/

import java.util.List;
import java.util.ArrayList;


abstract class Vehicle{
    int id;
    VehicleType type;
    
    public Vehicle(int id, VehicleType type){
        this.id = id;
        this.type = type;
    }
    
    public VehicleType getType(){
        return type;
    }
}

class Car extends Vehicle{
    public Car(int id){
        super(id, VehicleType.CAR);
    }
    
}

class Bike extends Vehicle{
    public Bike(int id){
        super(id, VehicleType.BIKE);
    }
    
}


class ParkingSpot{
    int id;
    boolean isAvailable;
    VehicleType spotType;
    Vehicle currentVehicle;
    
    public ParkingSpot(int id, VehicleType vehicleType){
        this.id =id;
        this.spotType = vehicleType;
        this.isAvailable = true; 
    }
    
    public boolean assignVehicle(Vehicle vehicle){
        if(isAvailable == true && spotType == vehicle.type){
            this.currentVehicle = vehicle;
            isAvailable = false;
            return true;
        }
        return false;
    }
    public boolean isSpotAvailable(){
        return isAvailable;
    }
    public void release(){
        currentVehicle = null;
        isAvailable = true;
    }
    
  
}

class Level{
    int levelNo;
    private List<ParkingSpot> parkingSpots = new ArrayList<>();
    
    public Level(int levelNo, List<ParkingSpot> spots){
        this.levelNo = levelNo;
        parkingSpots = spots;
    }
    
    
    public String findAvailableSpot(Vehicle vehicle){
        for(ParkingSpot parkingSpot:parkingSpots){
            if(parkingSpot.isAvailable && parkingSpot.spotType == vehicle.type){
                String msg = "parking spot" + parkingSpot.spotType + "no"+parkingSpot.id +"At level"+levelNo+"is available";
                System.out.println(msg);
                
                return msg;
                
            }
        }
        return "no available spots";
        
    }
    
    public boolean parkVehicle(Vehicle vehicle){
        for(ParkingSpot spot : parkingSpots){
            if(spot.isAvailable && spot.spotType == vehicle.type){
                spot.assignVehicle(vehicle);
                  String msg = "Car " + vehicle.id + " is parked at " + "parking spot "+ "no " +spot.id + " At level " +levelNo;
                  System.out.println(msg);
                return true;
            }
        }
        return false;
    }
    
    public boolean releaseVehicle(Vehicle vehicle){
        for(ParkingSpot spot : parkingSpots){
            if(spot.currentVehicle == vehicle && ! spot.isAvailable){
                spot.release();
                return true;
            }
        }
        return false;
    }
    
    
    
}

enum VehicleType{
    CAR, BIKE;
}

 class ParkingManager{
    List<Level> levels = new ArrayList<>();
    int numLevels;
    
    public ParkingManager(int numLevels){
        this.numLevels = numLevels;
        
        for(int i=0;i<numLevels;i++){
            List<ParkingSpot> parkingSpots = new ArrayList<>();
            for(int j=0;j<10;j++){
                parkingSpots.add(new ParkingSpot(i,VehicleType.CAR));
            }
            
            
            for(int j=0;j<5;j++){
                parkingSpots.add(new ParkingSpot(i,VehicleType.BIKE));
            }
            
            levels.add(new Level(i,parkingSpots));
        }
        
    }
    
    public boolean park(Vehicle vehicle){
        for(Level level: levels){
            if(level.parkVehicle(vehicle)){
                return true;
            }
        }
        return false;
    }
}

class ParkingLot {
    public static void main(String[] args) {
        System.out.println("simulator started");
        ParkingManager obj = new ParkingManager(2);
        Vehicle car = new Car(100);
        obj.park(car);
        
    }
}
