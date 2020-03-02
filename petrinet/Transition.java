package petrinet;

import java.util.Collection;
import java.util.Map;

public class Transition<T> {

	private Map<T, Integer> input;
	private Collection<T> reset;
	private Collection<T> inhibitor;
	private Map<T, Integer> output;

	/**
	 * Tworzy przejście między miejcami typu T.
	 */
	public Transition(Map<T, Integer> input, Collection<T> reset, Collection<T> inhibitor,
			Map<T, Integer> output) {
		this.input = input;
		this.reset = reset;
		this.inhibitor = inhibitor;
		this.output = output;
	}

	/**
	 * Sprawdza czy przejście jest dozwolone.
	 */
	public boolean isEnabled(Map<T, Integer> places) {
		for (Map.Entry<T, Integer> entry : input.entrySet()) {
			if (!places.containsKey(entry.getKey())
					|| (places.get(entry.getKey()) < entry.getValue())) {
				return false;
			}
		}

		for (T place : inhibitor) {
			if (places.containsKey(place)) {
				return false;
			}
		}

		return true;
	}

	public Map<T, Integer> input() {
		return input;
	}

	public Collection<T> reset() {
		return reset;
	}

	public Collection<T> inhibitor() {
		return inhibitor;
	}

	public Map<T, Integer> output() {
		return output;
	}

}
