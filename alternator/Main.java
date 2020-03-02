package alternator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import petrinet.PetriNet;
import petrinet.Transition;

public class Main {

	private static final int TRANSITIONS = 3;

	private static final int A = 0;
	private static final int B = 1;
	private static final int C = 2;

	private static enum Place {
		A, B, C
	}

	private static class Writing implements Runnable {

		private PetriNet<Place> petriNet;
		private Collection<Transition<Place>> startingTransition;
		private Collection<Transition<Place>> endingTransition;

		public Writing(PetriNet<Place> petriNet, Transition<Place> startingTransition,
				Transition<Place> endingTransition) {
			this.petriNet = petriNet;
			this.startingTransition = Collections.singleton(startingTransition);
			this.endingTransition = Collections.singleton(endingTransition);
		}

		@Override
		public void run() {
			try {
				while (true) {
					petriNet.fire(startingTransition);
					System.out.print(Thread.currentThread().getName());
					System.out.print(".");
					petriNet.fire(endingTransition);
				}
			} catch (InterruptedException e) {
				Thread t = Thread.currentThread();
				t.interrupt();
				System.err.println(t.getName() + " przerwany");
			}
		}
	}

	public static void main(String[] args) {
		Map<Place, Integer> initial = new HashMap<Place, Integer>();

		initial.put(Place.A, 1);
		initial.put(Place.B, 1);
		initial.put(Place.C, 1);

		PetriNet<Place> petriNet = new PetriNet<Place>(initial, true);

		List<Map<Place, Integer>> startingInputArcs = new ArrayList<Map<Place, Integer>>(
				TRANSITIONS);
		List<Collection<Place>> startingResetArcs = new ArrayList<Collection<Place>>(TRANSITIONS);
		List<Collection<Place>> startingInhibitorArcs = new ArrayList<Collection<Place>>(
				TRANSITIONS);
		List<Map<Place, Integer>> startingOutputArcs = new ArrayList<Map<Place, Integer>>(
				TRANSITIONS);

		List<Map<Place, Integer>> endingInputArcs = new ArrayList<Map<Place, Integer>>(TRANSITIONS);
		List<Collection<Place>> endingResetArcs = new ArrayList<Collection<Place>>(TRANSITIONS);
		List<Collection<Place>> endingInhibitorArcs = new ArrayList<Collection<Place>>(TRANSITIONS);
		List<Map<Place, Integer>> endingOutputArcs = new ArrayList<Map<Place, Integer>>(
				TRANSITIONS);

		for (int i = 0; i < TRANSITIONS; i++) {
			startingInputArcs.add(new HashMap<Place, Integer>());
			startingResetArcs.add(new ArrayList<Place>());
			startingInhibitorArcs.add(new ArrayList<Place>());
			startingOutputArcs.add(new HashMap<Place, Integer>());

			endingInputArcs.add(new HashMap<Place, Integer>());
			endingResetArcs.add(new ArrayList<Place>());
			endingInhibitorArcs.add(new ArrayList<Place>());
			endingOutputArcs.add(new HashMap<Place, Integer>());
		}

		startingInputArcs.get(A).put(Place.A, 1);

		startingInputArcs.get(B).put(Place.B, 1);

		startingInputArcs.get(C).put(Place.C, 1);

		startingResetArcs.get(A).add(Place.B);
		startingResetArcs.get(A).add(Place.C);

		startingResetArcs.get(B).add(Place.A);
		startingResetArcs.get(B).add(Place.C);

		startingResetArcs.get(C).add(Place.A);
		startingResetArcs.get(C).add(Place.B);

		for (int i = 0; i < TRANSITIONS; i++) {
			endingInhibitorArcs.get(i).add(Place.A);
			endingInhibitorArcs.get(i).add(Place.B);
			endingInhibitorArcs.get(i).add(Place.C);
		}

		endingOutputArcs.get(A).put(Place.B, 1);
		endingOutputArcs.get(A).put(Place.C, 1);

		endingOutputArcs.get(B).put(Place.A, 1);
		endingOutputArcs.get(B).put(Place.C, 1);

		endingOutputArcs.get(C).put(Place.A, 1);
		endingOutputArcs.get(C).put(Place.B, 1);

		List<Transition<Place>> startingTransitions = new ArrayList<Transition<Place>>();
		List<Transition<Place>> endingTransitions = new ArrayList<Transition<Place>>();

		for (int i = 0; i < TRANSITIONS; i++) {
			startingTransitions.add(
					new Transition<Main.Place>(startingInputArcs.get(i), startingResetArcs.get(i),
							startingInhibitorArcs.get(i), startingOutputArcs.get(i)));

			endingTransitions.add(new Transition<Main.Place>(endingInputArcs.get(i),
					endingResetArcs.get(i), endingInhibitorArcs.get(i), endingOutputArcs.get(i)));
		}

		List<Thread> threads = new ArrayList<Thread>();

		threads.add(new Thread(
				new Writing(petriNet, startingTransitions.get(A), endingTransitions.get(A)), "A"));
		threads.add(new Thread(
				new Writing(petriNet, startingTransitions.get(B), endingTransitions.get(B)), "B"));
		threads.add(new Thread(
				new Writing(petriNet, startingTransitions.get(C), endingTransitions.get(C)), "C"));

		Collection<Transition<Place>> allTransitions = new ArrayList<Transition<Place>>();

		allTransitions.addAll(startingTransitions);
		allTransitions.addAll(endingTransitions);

		Set<Map<Place, Integer>> markings = petriNet.reachable(allTransitions);

		System.out.println(markings.size());

		for (Map<Place, Integer> map : markings) {
			if (map.size() == 1) {
				// dokładnie dwa miejsca mają zerową liczbę żetonów
				System.err.println("Warunek bezpieczeństwa niespełniony");
			}
			for (Map.Entry<Place, Integer> entry : map.entrySet()) {
				if (entry.getValue() > 1) {
					// jakieś miejsce ma liczbę żetonów większą niż 1
					System.err.println("Warunek bezpieczeństwa niespełniony");
				}
			}
		}

		for (Thread t : threads) {
			t.start();
		}

		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("Główny przerwany");
		}

		for (Thread t : threads) {
			t.interrupt();
		}

	}

}
