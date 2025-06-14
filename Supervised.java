package residential3;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;



class Supervision extends Event {
	Employee emp;
	Supervised spvd;
	
    public Supervision (int ID_in, Supervised spvd_in, LocalDate date_in, LocalTime time_in){
    	super(ID_in, LocalDateTime.of(date_in, time_in), spvd_in.supervisionDuration);
    	spvd = spvd_in;
    }
    
    public Supervision (int ID_in, Supervised spvd_in, LocalDate date_in){
    	super(ID_in, LocalDateTime.of(date_in, spvd_in.start[date_in.getDayOfWeek().getValue()-1]), spvd_in.supervisionDuration);
    	spvd = spvd_in;
    }

	
    @Override
    public String toString(){
    	return String.format("supervision%03d [%02d:%02d ~ %02d:%02d]", key,
    			start.toLocalTime().getHour(),
    			start.toLocalTime().getMinute(),
    			end.toLocalTime().getHour() + 1,
    			end.toLocalTime().getMinute());
    }
}


class Supervised extends Facility {
	Duration supervisionDuration;
	
	public Supervised(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
	}
	
	public void addSupervision(Supervision spv) throws Exception {
		if (spv.emp != null) {
			addEvent(spv);
		} else {
			//System.out.printf("%d not added: couldn't find employee to supervise", spv.key);
			throw new SupervisedException("Supervision not added: couldn't assign employee.");
		}
	}
	
}

class SupervisedException extends Exception {
	public SupervisedException(String message) {
		super(message);
	}
}

class PiscoOlimp extends Supervised {
	
	public PiscoOlimp(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.of(9, 0, 0), LocalTime.of(18, 0, 0));
		antecipation = 8;
		supervisionDuration = Duration.ofHours(8);
	}

}

class ComplexAqua extends Supervised {
	
	public ComplexAqua(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.of(9, 0, 0), LocalTime.of(18, 0, 0));
		antecipation = 8;
		supervisionDuration = Duration.ofHours(8);
	}
	
}


class PistaSkate extends Supervised {
	
	public PistaSkate(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.of(9, 0, 0), LocalTime.of(18, 0, 0));
		antecipation = 8;
		supervisionDuration = Duration.ofHours(8);
	}
	
}


class PistaCorrida extends Supervised {
	
	public PistaCorrida(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.of(9, 0, 0), LocalTime.of(18, 0, 0));
		antecipation = 8;
		supervisionDuration = Duration.ofHours(8);
	}
	
}

class ParedEscalada extends Supervised {
	
	public ParedEscalada(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.of(9, 0, 0), LocalTime.of(18, 0, 0));
		supervisionDuration = Duration.ofHours(8);
		antecipation = 8;
		//set(DayOfWeek.TUESDAY, LocalTime.of(9, 0, 0), LocalTime.of(22, 0, 0));
	}
	
}

