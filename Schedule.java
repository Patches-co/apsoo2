package residential3;

import java.util.*;
import java.time.*;
import java.io.*;

class Schedule {
    LinkedHashMap <LocalDate, Day> calendar;
    LocalDateTime today;
    LocalTime[] start = new LocalTime[7];
    LocalTime[] end = new LocalTime[7];
    int EventCount;

    public Schedule () 
    {
    	calendar = new LinkedHashMap<>();
    	today = LocalDateTime.now();
    	EventCount = 0;
    	
		Arrays.fill(start, LocalTime.MIN);
		
		Arrays.fill(end, LocalTime.MAX);
    }

    public void set(LocalTime start_in, LocalTime end_in)
    {
		Arrays.fill(start, start_in);
		Arrays.fill(end, end_in);
    }
    
    public void set(DayOfWeek specific_day, LocalTime another_start, LocalTime another_end)
    {
    	int index = specific_day.getValue() - 1;
    	start[index] = another_start;
    	end[index] = another_end;
    }
    
    
    public void set(LocalDate specialDay, LocalTime specialStart, LocalTime specialEnd) {
    	Day d = calendar.get(specialDay);
    	
    	if (d == null) {
    		calendar.put(specialDay, new Day(specialDay, specialStart, specialEnd));
    	} else {
    		d.Events.clear();
    		d.start = LocalDateTime.of(specialDay, specialStart);
    		d.end = LocalDateTime.of(specialDay, specialEnd);
    	}
    }
    
    
    public void addDay(LocalDate date)
    {	
    	if (!calendar.containsKey(date)) {
    		int day = date.getDayOfWeek().getValue() - 1;
    		calendar.put(date, new Day(date, start[day], end[day]));
    	}
    }
    
    public void addDay(LocalDate date, int n)
    {	
    	for (int i = 0; i < n; i++) {
    		if (!calendar.containsKey(date)) {
    			int day = date.getDayOfWeek().getValue() - 1;
    			calendar.put(date, new Day(date, start[day], end[day]));
    		}
			date = date.plusDays(1);
    	}
    }
    
    public void removeDay(LocalDate date)
    {	
    	if (calendar.containsKey(date)) {
    		calendar.remove(date);
    	}
    }
     

    public void addEvent(Event e) throws Exception {
    	
    	
    	if (e.start.toLocalDate().isBefore(LocalDate.now())) {
    		throw new ScheduleException("Event not added: date already passed.");
    	}
    	
    	Day d = calendar.get(e.start.toLocalDate());
    	try {
    		if (e.end.isBefore(d.end)) {
    			d.addEvent(e);
    		} else {
    			List <Event> split = new ArrayList<>();
    			LocalDateTime p = e.start;
    			Duration r = e.duration;
    			while (!r.isZero()) {
    				LocalDateTime q = p.plus(r);
    				LocalDate p_day = p.toLocalDate();
    				LocalDateTime end;
    				Duration segm;
    				if (q.toLocalDate().isAfter(p_day)) {
    					end = p.with(LocalTime.MAX);
    					segm = Duration.between(p, end);
    				} else {
    					end = q;
    					segm = r;
    				}
    				
    				split.add(new Event(e.key, p, segm));
    				
    				r = r.minus(segm);
    				p = end.plusSeconds(1);
    			}
    			
    			Iterator <Event> ite = split.iterator();
    			Day day;
    			Event evt;
    			while (ite.hasNext()) {
    				evt = ite.next();
    				day = calendar.get(evt.start.toLocalDate());
    				day.addEvent(evt);
    			}	
    		}
    	} catch (NullPointerException NullDate) {
    		throw new ScheduleException("Event not added: can't find date.");
    	}
    }
    
    public void removeEvent(Event e){
    	Day d = calendar.get(e.start.toLocalDate());
	
    	try {
    		d.Events.remove(e.key);
    		EventCount--;
    		System.out.println("Event successfully removed.\n");   
    	} catch (NullPointerException Nulldate) {
    		System.out.println("Not removed: can't find date " + e.start.toLocalDate() + "\n");
    	}
    }
    
    

    public Event get(int key)
    {
    	Event r;
    	for (Day d : calendar.values()){
    		r = d.Events.get(key);
    		if (r != null){
    			return r;
    		}
    	}
    	return null;
    }
    
    public void print_file(String s){
    	try {
    		FileWriter fout = new FileWriter(s + ".txt");
    		for (Day d : calendar.values()){
    			String interval = String.format("[%02d:%02d ~ %02d:%02d]",
    					d.start.getHour(), d.start.getMinute(),
    					d.end.getHour(), d.end.getMinute());
    			fout.write("///////////////   " + d.toString() + interval + "   ///////////////" + "\n");
    			for (Event e : d.Events.values()){
    				fout.write(e.toString() + "\n");
    			}
    			fout.write("\n");
    			}
    		fout.write("\n\n");
    		fout.close();
    	} catch (IOException e){}
    }
    
    
    public void update()
    {
    	LocalDate now = LocalDate.now();
    	if (today.toLocalDate().isBefore(now)) {
    		Iterator <Day> iterator = calendar.values().iterator();
    		Day d = iterator.next();
    		while (!d.date.isEqual(now)) {
    			
    			iterator.remove();
    			d = iterator.next();
    		}
    		LocalDate last = null;
    		int count = 0;
    		while (iterator.hasNext()) {
    			last = iterator.next().date;
    			count++;
    		}
    		for (int i = 1; i < count-1; i++) {
    			addDay(last.plusDays(i));
    		}
    	}
    } 
}

class Day {
    LocalDate date;
    LocalDateTime start;
    LocalDateTime end;
    LinkedHashMap <Integer, Event> Events;
  
    public Day (LocalDate d_in) {
    	date = d_in;
    	Events = new LinkedHashMap<>();
    }
    
    public Day (LocalDate d_in, LocalTime start_in, LocalTime end_in) {
    	date = d_in;
		start = LocalDateTime.of(d_in, start_in);
		end = LocalDateTime.of(d_in, end_in);
    	Events = new LinkedHashMap<>();
    }
    
    
    public void addEvent(Event e) throws ScheduleException {
    	
    	/*
		System.out.println();
		System.out.println(start + " " + end);
		System.out.println(e.start + " " + e.end);
		System.out.println();
    	 */
        
	    if (e.start.isBefore(start) ||
	    	e.end.isAfter(end))
	    {
	    	throw new ScheduleException("Event not added: out of bounds.");
	    }

    	Events.put(e.key, e);
    }

    public String toString(){
    	return String.format("%02d/%02d/%02d",
			     date.getDayOfMonth(),
			     date.getMonthValue(),
			     date.getYear());
    }
}


class Event {
    int key;
    LocalDateTime start;
    LocalDateTime end;
    Duration duration;
  
    public Event(int key_in, LocalDateTime start_in, Duration du_in){
    	key = key_in;
    	
    	start = start_in;
    	end = start_in.plus(du_in);
    	duration = du_in;
    }
    
    public Event(int key_in, LocalDateTime start_in, LocalDateTime end_in){
    	key = key_in;
    	
    	start = start_in;
    	end = end_in;
    	duration = Duration.between(start_in, end_in);
    }
    
    public Event(int key_in, LocalDateTime start_in){
    	key = key_in;
    	start = start_in;
    }

    public String toString(){
    	return String.format("e%03d [%02d:%02d ~ %02d:%02d]", key,
    			start.toLocalTime().getHour(),
    			start.toLocalTime().getMinute(),
    			end.toLocalTime().getHour(),
    			end.toLocalTime().getMinute());
    }
}

class ScheduleException extends Exception {
	public ScheduleException (String message) {
		super(message);
	}
}

