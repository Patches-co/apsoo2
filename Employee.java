package residential3;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;



class Maintenance extends Event {
	Employee emp;
	Duration MaintenanceDuration;
	
    public Maintenance(Employee emp_in, int key_in, LocalDateTime start_in, Duration d){
    	super(key_in, start_in);
    	emp = emp_in;
    	MaintenanceDuration = d;
    }
    
    @Override
    public String toString(){
    	return String.format("maintenance%03d [%02d:%02d ~ %02d:%02d]", key,
    			start.toLocalTime().getHour(),
    			start.toLocalTime().getMinute(),
    			end.toLocalTime().getHour(),
    			end.toLocalTime().getMinute());
    }
	
}






public class Employee 
implements Comparable <Employee>
{
	int ID;
	String name;
	Duration time_worked;
	static int count = 0;
	
	public Employee(int ID_in, String name_in) {
		ID = ID_in;
		name = name_in;
		time_worked = Duration.ZERO;
	}
	
	public Employee(int ID_in, String name_in, String weekly_hours) {
		ID = ID_in;
		name = name_in;
		
		String[] parts = weekly_hours.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

       time_worked = Duration.ofHours(hours)
                                  .plusMinutes(minutes)
                                  .plusSeconds(seconds);	
	}
	
	@Override
	public int compareTo (Employee other){
		return Long.compare(time_worked.getSeconds(), 
					other.time_worked.getSeconds());
	}
	
	public String getTimeWorked(){
	    return String.format("%02d:%02d:%02d",
	    		time_worked.getSeconds() / 3600,
	    		(time_worked.getSeconds() % 3600) / 60,
	    		time_worked.getSeconds() % 60);
	}
}


class EmployeeException extends Exception {
	public EmployeeException(String message) {
		super(message);
	}
}

