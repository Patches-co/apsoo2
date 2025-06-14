package residential3;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;



class Reserve extends Event {
	Resident rsdt;
	Reservable rsvable;
	
    public Reserve (int ID_in, Resident rsdt_in, Reservable rsvable_in, LocalDate date_in, LocalTime time_in){
    	super(ID_in, LocalDateTime.of(date_in, time_in));
    	rsdt = rsdt_in;
    	rsvable = rsvable_in;
    }
	
    public Reserve (int ID_in, Resident rsdt_in, Reservable rsvable_in, LocalDateTime datetime_in){
    	super(ID_in, datetime_in);
    	rsdt = rsdt_in;
    	rsvable = rsvable_in;
    }
    
    public Reserve (int ID_in, Resident rsdt_in, Reservable rsvable_in, LocalDate date_in){
    	super(ID_in, LocalDateTime.of(date_in, LocalTime.MIN), LocalDateTime.of(date_in, LocalTime.MAX));
    	rsdt = rsdt_in;
    	rsvable = rsvable_in;
    }
    
    @Override
    public String toString(){
    	return String.format("Reserve %03d %s %s %02d/%02d [%02d:%02d ~ %02d:%02d]", key,
    			rsdt.name, rsvable.identifier,
    			start.toLocalDate().getDayOfMonth(), start.toLocalDate().getMonthValue(),
    			start.toLocalTime().getHour(),
    			start.toLocalTime().getMinute(),
    			end.toLocalTime().getHour(),
    			end.toLocalTime().getMinute());
    }
}

public class Reservable extends Facility {
	Duration reserveDuration;
	int daily_RPP;	//reserves per person
	int weekly_RPP;

	public Reservable(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
	}
	
	//Essa função verifica se a reserva a ser adicionada tem overlap com outra já existente
	public void addReserve(Reserve r) throws Exception {
		r.duration = reserveDuration;
		r.end = r.start.plus(r.duration);
		Day d = calendar.get(r.start.toLocalDate());
		if (d != null) {
		Iterator <Event> ite = d.Events.values().iterator();
			Event p;
			while (ite.hasNext()) {
				p = ite.next();
				if ((p.start.isAfter(r.start) && p.start.isBefore(r.end)) || 
					(r.start.isAfter(p.start) && r.start.isBefore(p.end)) ||
					(p.start.equals(r.start) && p.end.equals(r.end)))
				{
					//System.out.println(r.key + " not added: already reserved");
					throw new ReservableException("Reserve not added: already reserved.");
				}
				
				
			}
		}
		
		addEvent(r);
	}
}


class ReservableException extends Exception {
	public ReservableException(String message) {
		super(message);
	}
	
}

class Quadra extends Reservable {

	public Quadra(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.of(6, 0, 0), LocalTime.of(22, 0, 0));
		reserveDuration = Duration.ofMinutes(50);
		antecipation = 7;
		daily_RPP = 2;
		weekly_RPP = 4;
		set(DayOfWeek.MONDAY, LocalTime.of(13, 0, 0), LocalTime.of(22, 0, 0));
	}
	
}

class QuadraTenis extends Reservable {

	public QuadraTenis(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.of(6, 0, 0), LocalTime.of(22, 0, 0));
		reserveDuration = Duration.ofMinutes(50);
		antecipation = 7;
		daily_RPP = 2;
		weekly_RPP = 4;
		set(DayOfWeek.THURSDAY, LocalTime.of(13, 0, 0), LocalTime.of(22, 0, 0));	
	}
	
}

class CampoFutebol extends Reservable {

	public CampoFutebol(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.of(6, 0, 0), LocalTime.of(22, 0, 0));
		reserveDuration = Duration.ofHours(2);
		antecipation = 14;
		daily_RPP = 1;
		weekly_RPP = 4;
		set(DayOfWeek.THURSDAY, LocalTime.of(13, 0, 0), LocalTime.of(22, 0, 0));	
	}
	
}


class PistaBoliche extends Reservable {

	public PistaBoliche(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.of(15, 0, 0), LocalTime.of(23, 0, 0));
		reserveDuration = Duration.ofMinutes(90);
		daily_RPP = 1;
		weekly_RPP = 2;
		antecipation = 7;
	}
	
}


class Quiosque extends Reservable {

	public Quiosque(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.MIN, LocalTime.MAX);
		reserveDuration = Duration.ofMinutes(1439);
		daily_RPP = 1;
		weekly_RPP = 1;
		antecipation = 30;
	}
	
}


class SalaoFesta extends Reservable {

	public SalaoFesta(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.MIN, LocalTime.MAX);
		reserveDuration = Duration.ofMinutes(1439);
		daily_RPP = 1;
		weekly_RPP = 1;
		antecipation = 30;
	}
	
}


class EstudioMusica extends Reservable {

	public EstudioMusica(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.of(6, 0, 0), LocalTime.of(22, 0, 0));
		set(DayOfWeek.THURSDAY, LocalTime.of(13, 0, 0), LocalTime.of(22, 0, 0));
		reserveDuration = Duration.ofMinutes(90);
		daily_RPP = 2;
		weekly_RPP = 4;
		antecipation = 7;
	}	
}


class OficinArtesanato extends Reservable {

	public OficinArtesanato(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.of(6, 0, 0), LocalTime.of(22, 0, 0));
		set(DayOfWeek.TUESDAY, LocalTime.of(13, 0, 0), LocalTime.of(22, 0, 0));
		reserveDuration = Duration.ofMinutes(90);
		daily_RPP = 2;
		weekly_RPP = 4;
		antecipation = 7;
	}
	
}



class QuadrAreia extends Reservable {

	public QuadrAreia(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.of(6, 0, 0), LocalTime.of(22, 0, 0));
		set(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0, 0), LocalTime.of(22, 0, 0));
		reserveDuration = Duration.ofMinutes(50);
		antecipation = 7;
		daily_RPP = 2;
		weekly_RPP = 4;
	}
	
}



class MiniGolf extends Reservable {

	public MiniGolf(int ID_in, String identifier_in) {
		super(ID_in, identifier_in);
		set(LocalTime.of(6, 0, 0), LocalTime.of(22, 0, 0));
		set(DayOfWeek.MONDAY, LocalTime.of(9, 0, 0), LocalTime.of(22, 0, 0));
		reserveDuration = Duration.ofHours(2);
		antecipation = 14;
		daily_RPP = 1;
		weekly_RPP = 1;
	}
	
}

