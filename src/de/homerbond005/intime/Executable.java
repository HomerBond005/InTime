package de.homerbond005.intime;

import java.util.Set;

public abstract class Executable {
	protected Set<String> times;
	protected Weekday weekday;

	public Executable(Set<String> times, Weekday weekday) {
		this.times = times;
	}

	public void addTime(String time) {
		times.add(time);
	}

	public abstract void execute(String args);

	public abstract boolean matchesTime(int hours, int minutes, Weekday weekday);

	public boolean executeIfMatches(int hours, int minutes, Weekday weekday,
			String args) {
		if (matchesTime(hours, minutes, weekday)) {
			execute(args);
			return true;
		}
		return false;
	}

	protected String t(int t) {
		if (t < 10) {
			return "0" + t;
		} else {
			return "" + t;
		}
	}

	public enum Weekday {
		MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY, ALL;

		public static Weekday getByName(String str) {
			if (str.trim().equalsIgnoreCase("monday")) {
				return MONDAY;
			}
			if (str.trim().equalsIgnoreCase("tuesday")) {
				return TUESDAY;
			}
			if (str.trim().equalsIgnoreCase("wednesday")) {
				return WEDNESDAY;
			}
			if (str.trim().equalsIgnoreCase("thursday")) {
				return THURSDAY;
			}
			if (str.trim().equalsIgnoreCase("friday")) {
				return FRIDAY;
			}
			if (str.trim().equalsIgnoreCase("saturday")) {
				return SATURDAY;
			}
			if (str.trim().equalsIgnoreCase("sunday")) {
				return SUNDAY;
			}
			return ALL;
		}

		public static Weekday getByInt(int calval) {
			switch (calval) {
			case 1:
				return SUNDAY;
			case 2:
				return MONDAY;
			case 3:
				return TUESDAY;
			case 4:
				return WEDNESDAY;
			case 5:
				return THURSDAY;
			case 6:
				return FRIDAY;
			case 7:
				return SATURDAY;
			default:
				return ALL;
			}
		}
	}
}
