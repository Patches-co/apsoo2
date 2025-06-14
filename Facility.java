package residential3;


public class Facility extends Schedule {
	int ID;
	String identifier;
	int antecipation;
	static int count = 0;
	
	public Facility(int ID_in, String identifier_in) {
		ID = ID_in;
		identifier = identifier_in;
	}

	
	public Facility(int ID_in, String identifier_in, int antecipation_in) {
		ID = ID_in;
		identifier = identifier_in;
		antecipation = antecipation_in;
	}
	
    public void setID(int id_in) {
        ID = id_in;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier_in) {
        identifier = identifier;
    }

    public int getAntecipation() {
        return antecipation;
    }

    public void setAntecipation(int antecipation) {
        this.antecipation = antecipation;
    }
}

