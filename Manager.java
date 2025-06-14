package residential3;

import java.util.*;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.lang.reflect.*;


public class Manager {
	Connection con;
	
	HashMap <Integer, Facility> facilities;
	HashMap <Integer, Resident> residents;
	HashMap <Integer, Event> events;
	PriorityQueue <Employee> employees;
	
	public Manager(Connection con_in) throws SQLException, Exception {
		con = con_in;
		update();
		
		facilities = new HashMap<Integer, Facility>();
		residents = new HashMap<Integer, Resident>();
		events = new HashMap <Integer, Event>();
		employees = new PriorityQueue<Employee>();

		
		Statement stmt = con.createStatement();

		ResultSet RS1 = stmt.executeQuery("SELECT ID, NAME, WEEKLY_HOURS FROM Employees");
		while (RS1.next()) {
			Employee f = new Employee(RS1.getInt("ID"), RS1.getString("NAME"), RS1.getString("WEEKLY_HOURS"));
			Employee.count++;
			employees.add(f);
		}
		
		ResultSet RS2 = stmt.executeQuery("SELECT ID, NAME FROM Residents");
		while (RS2.next()) {
			Resident m = new Resident(RS2.getInt("ID"), RS2.getString("NAME"));
			residents.put(m.ID, m);
			Resident.count++;
		}
		
		
		ResultSet RS3 = stmt.executeQuery("SELECT ID, IDENTIFIER, CLASS FROM Facilities");
		Object o;
		Class<?> clazz;
		int ID;
		String IDENTIFIER, CLASS;
		while (RS3.next()) {
			ID = RS3.getInt("ID");
			IDENTIFIER = RS3.getString("IDENTIFIER");
			CLASS = RS3.getString("CLASS");
			
			try {
				clazz = Class.forName("residential3." + CLASS);
				Constructor<?> cstructor = clazz.getConstructor(int.class, String.class);
				o = cstructor.newInstance(ID, IDENTIFIER);
				facilities.put(ID,(Facility) o);
				Facility.count++;
				Facility f = facilities.get(ID);
				f.addDay(LocalDate.now(), f.antecipation);
			} catch (
					ClassNotFoundException |
					IllegalAccessException |
					InstantiationException|
					NoSuchMethodException|
					InvocationTargetException e) 
			{
				System.out.println(e);
			}		
		}
		
		
		if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
			reset();
			make_supervisions();
		}
		
		ResultSet RS4 = stmt.executeQuery("SELECT * FROM Reserves");
		while (RS4.next()) {
			Reserve rsv = new Reserve(RS4.getInt("ID"), 
					residents.get(RS4.getInt("RESIDENT_ID")), 
					(Reservable)facilities.get(RS4.getInt("FACILITY_ID")),
					RS4.getDate("DATE").toLocalDate(), 
					RS4.getTime("START").toLocalTime());
			events.put(rsv.key, rsv);
			
			
			Statement stmt2 = con.createStatement();
			ResultSet RS5 = stmt2.executeQuery(String.format("SELECT * FROM Facilities WHERE ID = %d", RS4.getInt("FACILITY_ID")));
			while (RS5.next()) {
				Reservable r = (Reservable)facilities.get(RS5.getInt("ID"));
				r.addReserve(rsv);
			}
		}
		
		ResultSet RS6 = stmt.executeQuery("SELECT * FROM Supervisions");
		while (RS6.next()) {
			Supervision spv = new Supervision(RS6.getInt("ID"), 
					(Supervised)facilities.get(RS6.getInt("FACILITY_ID")),
					RS6.getDate("DATE").toLocalDate(), 
					RS6.getTime("START").toLocalTime());
			events.put(spv.key, spv);
			
			
			Statement stmt2 = con.createStatement();
			ResultSet RS7 = stmt2.executeQuery(String.format("SELECT * FROM Facilities WHERE ID = %d", RS6.getInt("FACILITY_ID")));
			while (RS7.next()) {
				Supervised spvd = (Supervised)facilities.get(RS7.getInt("ID"));
				spvd.addEvent(spv);
			}
		}
	}
	
	
	
	public void make_random_events() throws SQLException, Exception {
		Random r = new Random();
		
		int year, month, day, hour, minute, second;
		int fclt;
		Reservable rsvable;
		Supervised spvd;
		Employee emp;
		Resident rsdt;
		
		outerloop: for (int i = 1; i < 100; i++) {
			year = 2025;
			month = 6;
			day = LocalDate.now().getDayOfMonth() + r.nextInt(14);
			hour = 6 + r.nextInt(16);
			minute = 10 * r.nextInt(6);
			second = 0;
			fclt = 1 + r.nextInt(26);
			rsdt = residents.get(1 + r.nextInt(20));
			
			try {
				if (fclt <= 11) {
					rsvable = (Reservable) facilities.get(fclt);
					Reserve rsv = new Reserve(i, rsdt, rsvable, LocalDateTime.of(year, month, day, hour, minute, second));
					//rsvable.addReserve(rsv);
					add_Reserve(rsv);
					
					System.out.printf("%d %s %s %02d/%02d/%02d %02d:%02d:%02d\n",
							i, rsdt.name, rsvable.identifier, year, month, day, hour, minute, second);
					
				} else if (fclt <= 21) {
					rsvable = (Reservable) facilities.get(fclt);
					Reserve rsv = new Reserve(i, rsdt, rsvable, LocalDate.of(year, month, day));
					add_Reserve(rsv);
					
					System.out.printf("%d %s %s %02d/%02d/%02d\n",
							i, rsdt.name, rsvable.identifier, year, month, day);
				} else {
					i--;
				}
				
			} catch (ScheduleException |
					ReservableException |
					SupervisedException |
					NullPointerException|
					EmployeeException e) {
				System.out.printf("%d %s %02d/%02d/%02d %02d:%02d:%02d\n", i, facilities.get(fclt).identifier, 
						day, month, year, hour, minute, second);
				System.out.println(e.getMessage());
				i--;
				continue outerloop;
			}
			
		}
		
		
		

	}
	
	public void drop_events() throws SQLException {
		Statement stmt = con.createStatement();
		stmt.execute("DELETE FROM Reserves");
		stmt.execute("DELETE FROM Supervisions");
	}
	
	
	public void add_Reserve(Reserve rsv) throws SQLException, Exception {
		Statement stmt = con.createStatement();
		String QUERY = String.format("SELECT * FROM Reserves WHERE RESIDENT_ID = %d AND FACILITY_ID = %d", rsv.rsdt.ID, rsv.rsvable.ID);
		ResultSet RS = stmt.executeQuery(QUERY);		
		Reservable rsvable = (Reservable) facilities.get(rsv.rsvable.ID);

		int weekly_count = 0, daily_count = 0; 
		while(RS.next()) {
			if (RS.getDate("DATE").toLocalDate().equals(rsv.start.toLocalDate())) {
				daily_count++;
			}
			weekly_count++;
		}
		
		if (weekly_count < rsvable.weekly_RPP) {
			if (daily_count < rsvable.daily_RPP) {
				events.put(rsv.key, rsv);
				rsvable.addReserve(rsv);
				
				stmt.execute(String.format(
						"INSERT INTO Reserves VALUES (%d, '%d-%d-%d', '%d:%d:%d', '%d:%d:%d', %d, %d)",
						rsv.key, rsv.start.getYear(), rsv.start.getMonthValue(), rsv.start.getDayOfMonth(),
						rsv.start.getHour(), rsv.start.getMinute(), rsv.start.getSecond(),
						rsv.end.getHour(), rsv.end.getMinute(), rsv.end.getSecond(),
						rsv.rsdt.ID, rsvable.ID));
						
			} else {
				throw new ReservableException("Event not added: too much daily reserves");
			}
		} else {
			throw new ReservableException("Event not added: too much weekly reserves");
		}
		
	}
	
	
	public Reserve searchReserve(int reserve_ID) {
		return (Reserve) events.get(reserve_ID);
	}
	
	public void remove_Reserve(Reserve rsv) throws SQLException {
		
		PreparedStatement pstmt = con.prepareStatement("DELETE FROM Reserves WHERE ID = ?");
		pstmt.setInt(1, rsv.key);
		pstmt.executeUpdate();
				
		events.remove(rsv.key);
	}
		
	public LinkedHashMap<Integer, Reserve> getReserves(String residentName) throws SQLException {
		PreparedStatement pstmt = con.prepareStatement("SELECT ID FROM Residents WHERE NAME = ?");
		pstmt.setString(2, residentName);
		LinkedHashMap result = new LinkedHashMap<Integer, Event>();
		
		try (ResultSet RS1 = pstmt.executeQuery()) {
			if (RS1.next()) {
				int resident_ID = RS1.getInt("ID");
				pstmt = con.prepareStatement("SELECT * FROM Reserves WHERE RESIDENT_ID = ?");
				pstmt.setInt(1, resident_ID);
				
				try (ResultSet RS2 = pstmt.executeQuery()){
					Event e;
					while (RS2.next()) {
						e = events.get(RS2.getInt("ID"));
						result.put(e.key, e);
					}		
				}
			}
		}
		return result;	
	}
	
	
	
	public HashMap<Integer, Facility> getFacilities(){
		return facilities;
	}
	
	public void addFacility(String identifier_in, LocalTime start, LocalTime end, 
			int antecipation, int drpp, int wrpp, String CLASS) throws SQLException {
		int key = Facility.count + 1;
		Class<?> clazz;
		try {
			clazz = Class.forName("residential3." + CLASS);
			Constructor<?> cstructor = clazz.getConstructor(int.class, String.class);
			Object o = cstructor.newInstance(key, identifier_in);
			facilities.put(key,(Facility) o);		
			
			Facility f = new Facility(key, identifier_in);
			facilities.put(key, f);
			Facility.count++;
			
			Statement stmt = con.createStatement();
			stmt.execute(String.format(
					"INSERTO INTO Facilities VALUES (%d, %s, '%02d:%02d:%02d', '%02d:%02d:%02d', %d, %d, %d, %s)",
					key, identifier_in, start.getHour(), start.getMinute(), start.getSecond(),
					end.getHour(), end.getMinute(), end.getSecond(), antecipation, drpp, wrpp, CLASS));
		} catch (
				ClassNotFoundException |
				IllegalAccessException |
				InstantiationException|
				NoSuchMethodException|
				InvocationTargetException e) 
		{
			System.out.println(e);
		}		
	}
	
	public Facility getFacility(int facility_ID) {
		return facilities.get(facility_ID);
	}
	
	public void removeFacility(Facility f) throws SQLException {
		
		PreparedStatement pstmt = con.prepareStatement("DELETE FROM Facilities WHERE ID = ?");
		pstmt.setInt(1, f.ID);
		pstmt.executeUpdate();
	
		facilities.remove(f.ID);
	}
	
	
	public HashMap<Integer, Resident> getResidents(){
		return residents;
	}
	
	public Resident getResident(int resident_ID) {
		return residents.get(resident_ID);
	}
	
	
	public void addResident(String name, String residence, String CPF, int age) throws SQLException {
		int key = Resident.count;
		Resident rstd = new Resident(key, name);
		residents.put(key, rstd);
		
		Statement stmt = con.createStatement();
		stmt.execute(String.format("INSERT INTO Residents VALUES (%s, %s, %s, %d)",
				name, residence, CPF, age));
	}
	
	public void removeResident(Resident rsdt) throws SQLException {
		
		PreparedStatement pstmt = con.prepareStatement("DELETE FROM Residents WHERE ID = ?");
		pstmt.setInt(1, rsdt.ID);
		pstmt.executeUpdate();
	
		residents.remove(rsdt.ID);
	}
	
	
	public PriorityQueue<Employee> getEmployees(){
		return employees;
	}
	
	public Employee getEmployee(int employee_ID) {
		Iterator ite = employees.iterator();
		Employee emp;
		while (ite.hasNext()) {
			emp = (Employee)ite.next();
			if (emp.ID == employee_ID) {
				return emp;
			}
		}
		return null;
	}
	
	
	public void addEmployee(String name, String CPF) throws SQLException {
		int key = Employee.count;
		Employee emp = new Employee(key, name);
		employees.add(emp);
		
		Statement stmt = con.createStatement();
		stmt.execute(String.format("INSERT INTO Employees VALUES (%d, %s, %s)",
				key, name, CPF));
	}
	
	
	
	public void removeEmployee(Employee emp) throws SQLException {
		
		
		PreparedStatement pstmt = con.prepareStatement("DELETE FROM Employees WHERE ID = ?");
		pstmt.setInt(1, emp.ID);
		pstmt.executeUpdate();
	
		employees.remove(emp);
	}
		
	
	public void make_supervisions() throws SQLException, Exception {
		
		int i = 1;
		int id = events.size()+1;
		int fclt = 22;
		
		while (true) {
			try {
				Supervised spvd = (Supervised) facilities.get(fclt);
				Supervision spv = new Supervision(id, spvd, LocalDate.now().plusDays(i));			
				try {
					assign(spv);
					i++;
					id++;
				} catch (ScheduleException e1) {
					i = 1;
					fclt++;
				}
			} catch (NullPointerException | EmployeeException e2) {
				return;
			}			
		}
	}
	
	
	public void assign (Supervision spv) throws SQLException, Exception {
		Supervised spvd = (Supervised) facilities.get(spv.spvd.ID);
		Employee emp = employees.poll();
		Statement stmt = con.createStatement();
		
		try {
			if (emp.time_worked.compareTo(Duration.ofHours(40)) < 0) {
				employees.add(emp);
				spv.emp = emp;
				spvd.addEvent(spv);
				emp.time_worked = emp.time_worked.plus(spvd.supervisionDuration);
				events.put(spv.key, spv);
				
				stmt.execute(String.format("UPDATE Employees SET WEEKLY_HOURS = '%d:%d:%d' WHERE ID = %d", 
						(emp.time_worked.getSeconds() / 3600),
			    		(emp.time_worked.getSeconds() % 3600) / 60,
			    		emp.time_worked.getSeconds() % 60,		
			    		emp.ID));
				stmt.execute(String.format("INSERT INTO Supervisions VALUES (%d, %d, %d, '%d-%d-%d', '%d:%d:%d')", spv.key, emp.ID, spvd.ID,
						spv.start.getYear(), spv.start.getMonthValue(), spv.start.getDayOfMonth(),
						spv.start.getHour(), spv.start.getMinute(), spv.start.getSecond()));
			} else {
				employees.add(emp);
				throw new EmployeeException("Event not added: couldn't assign employee.");
			}
		} catch (NullPointerException e) {
			throw new EmployeeException("Event not added: couldn't assign employee.");
		}
			
	}
	
	public void reset() throws SQLException {
		Statement stmt = con.createStatement();
		stmt.execute(String.format("UPDATE Employees SET WEEKLY_HOURS = '%d:%d:%d'", 0, 0, 0));
		stmt.execute("DELETE FROM Supervisions");
	}
	
	
	public void update() throws SQLException {
		Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet RS1 = stmt.executeQuery("SELECT * FROM Reserves WHERE DATE < CURRENT_DATE");
		
		while(RS1.next()) {
			RS1.deleteRow();
		}
		
		ResultSet RS2 = stmt.executeQuery("SELECT * FROM Supervisions WHERE DATE < CURRENT_DATE");
		while(RS2.next()) {
			RS2.deleteRow();
		}
		
	}
	
	
	
	
}
