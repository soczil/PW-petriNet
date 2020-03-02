package petrinet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class PetriNet<T> {

	private Map<Thread, Collection<Transition<T>>> threadsWithTransitions;
	private Map<Thread, Semaphore> watingThreads;

	private Map<T, Integer> currentMarking;
	private boolean fair;
	private Semaphore mutex;

	/**
	 * Tworzy sieć petriego z miejscami typu T.
	 */
	public PetriNet(Map<T, Integer> initial, boolean fair) {
		this.currentMarking = initial;
		this.fair = fair;
		this.mutex = new Semaphore(1, this.fair);
		this.threadsWithTransitions = new LinkedHashMap<Thread, Collection<Transition<T>>>();
		this.watingThreads = new HashMap<Thread, Semaphore>();
	}

	/**
	 * Metoda pomocnicza dla metody reachable, zagłębiająca się w rekurencję.
	 */
	private void assistantReachable(Map<T, Integer> marking, Set<Map<T, Integer>> possibleMarkings,
			Collection<Transition<T>> transitions) {
		for (Transition<T> transition : transitions) {
			if (transition.isEnabled(marking)) {
				Map<T, Integer> newMarking = new HashMap<T, Integer>(marking);
				atomicOperations(newMarking, transition);
				if (!possibleMarkings.contains(newMarking)) {
					possibleMarkings.add(newMarking);
					assistantReachable(newMarking, possibleMarkings, transitions);
				}
			}
		}
	}

	/**
	 * Próbuje wyznaczyć zbiór wszystkich znakowań sieci, które są osiągalne z
	 * aktualnego jej stanu w rezultacie odpalenia, zero lub więcej razy, przejść z
	 * danej kolekcji przejść.
	 */
	public Set<Map<T, Integer>> reachable(Collection<Transition<T>> transitions) {
		try {
			mutex.acquire();
			Map<T, Integer> marking = new HashMap<T, Integer>(currentMarking);
			mutex.release();

			Set<Map<T, Integer>> possibleMarkings = new HashSet<Map<T, Integer>>();
			possibleMarkings.add(marking);

			assistantReachable(marking, possibleMarkings, transitions);

			return possibleMarkings;
		} catch (InterruptedException e) {
			Thread t = Thread.currentThread();
			t.interrupt();
			System.err.println(t.getName() + " przerwany");
		} catch (StackOverflowError e) {
			System.err.println("Przepełnienie stosu");
		}

		return null;
	}

	/**
	 * Wykonuje trzy kroki z niepodzielnej operacji fire.
	 */
	private void atomicOperations(Map<T, Integer> places, Transition<T> transition) {
		for (Map.Entry<T, Integer> entry : transition.input().entrySet()) {
			int newTokens = places.get(entry.getKey()) - entry.getValue();
			if (newTokens == 0) {
				places.remove(entry.getKey());
			} else {
				places.put(entry.getKey(), newTokens);
			}
		}

		for (T place : transition.reset()) {
			places.remove(place);
		}

		for (Map.Entry<T, Integer> entry : transition.output().entrySet()) {
			int oldTokens, newTokens;
			if (places.containsKey(entry.getKey())) {
				oldTokens = places.get(entry.getKey());
			} else {
				oldTokens = 0;
			}
			newTokens = oldTokens + entry.getValue();
			places.put(entry.getKey(), newTokens);
		}
	}

	/**
	 * Szuka przejścia dozwolonego w danej kolekcji przejść.
	 */
	private Transition<T> enabledTransition(Collection<Transition<T>> transitions) {
		for (Transition<T> transition : transitions) {
			if (transition.isEnabled(currentMarking)) {
				return transition;
			}
		}

		return null;
	}

	/**
	 * Jeśli w mapie wątków śpiących z ich przejściami napotka jakieś dozwolone
	 * przejście to budzi wątek z tym przejściem.
	 */
	private boolean runAnyThreadWithEnabledTransition() {
		for (Map.Entry<Thread, Collection<Transition<T>>> entry : threadsWithTransitions
				.entrySet()) {
			Transition<T> enabledTransition = enabledTransition(entry.getValue());
			if (enabledTransition != null) {
				watingThreads.get(entry.getKey()).release();
				return true;
			}
		}

		return false;
	}

	/**
	 * Odpala przejście, jeśli jest ono dozwolone, w przeciwnym przypadku wstrzymuje
	 * wątek. Szuka pierwszego dozwolonego przejścia. Jeśli takie znajdzie, odpala
	 * je, podnosi mutexa i wywołuje metodę runAnyThreadWithEnabledTransition, jeśli
	 * ta zwróci false to podnosi mutexa. Jeśli nie znajdzie dozwolonego przejścia,
	 * dodaje obecny wątek do mapy wątków wraz z ich przejściami oraz do mapy wątków
	 * wstrzymanych, podnosi mutexa i wstrzymuje obecny wątek na swoim semaforze.
	 * Jeśli wątek się obudzi, szuka dozwolonego przejścia (bo wie, że takie ma),
	 * odpala je, usuwa się z map wstrzymanych wątków i wywołuje metodę
	 * runAnyThreadWithEnabledTransition, jeśli ta zwróci false to podnosi mutexa.
	 * Jeśli wątek został przerwany to usuwa się z map wstrzymanych wątków.
	 */
	public Transition<T> fire(Collection<Transition<T>> transitions) throws InterruptedException {
		try {
			mutex.acquire();
			Transition<T> transition = enabledTransition(transitions);
			if (transition != null) {
				atomicOperations(currentMarking, transition);
			} else {
				threadsWithTransitions.put(Thread.currentThread(), transitions);
				watingThreads.put(Thread.currentThread(), new Semaphore(0));
				mutex.release();
				watingThreads.get(Thread.currentThread()).acquire();
				transition = enabledTransition(transitions);
				atomicOperations(currentMarking, transition);
				watingThreads.remove(Thread.currentThread());
				threadsWithTransitions.remove(Thread.currentThread());
			}

			return transition;
		} finally {
			if (Thread.currentThread().isInterrupted()) {
				watingThreads.remove(Thread.currentThread());
				threadsWithTransitions.remove(Thread.currentThread());
			}
			if (!runAnyThreadWithEnabledTransition()) {
				mutex.release();
			}
		}
	}

	public Map<T, Integer> currentMarking() {
		return currentMarking;
	}
}
