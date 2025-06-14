package residential3;



public class Resident {
	int ID;
	String name;
	static int count = 0;
	
	public Resident(int ID_in, String name_in) {
		ID = ID_in;
		name = name_in;
	}
	
}


class Dependent extends Resident {
	Resident parent;
	
	public Dependent(int ID_in, String name_in, Resident res_in) {
		super(ID_in, name_in);
		parent = res_in;
	}
	
}

