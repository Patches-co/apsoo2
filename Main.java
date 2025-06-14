package residential3;

import java.util.*;
import java.time.*;
import javax.swing.*;
import java.sql.*;
import java.lang.reflect.*;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException, SQLException, Exception {
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/RESIDENTIAL", "root", "feb1221a");
		
		Manager manager = new Manager(con);
		
		
		manager.make_random_events();
		manager.make_supervisions();
		
		
		
		Iterator <Facility> ite = manager.facilities.values().iterator();
		Facility fac;
		while (ite.hasNext()) {
			fac = ite.next();
			fac.print_file(fac.identifier);
		}
				
		
		
	}

}
