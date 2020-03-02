package multiplicator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import petrinet.PetriNet;
import petrinet.Transition;

public class Main {

	private static final int TRANSITIONS = 3;
	private static final int THREADS = 4;

	private static final int A = 0;
	private static final int B = 1;
	private static final int C = 2;

	private static enum Place {
		A, B, C
	}

	private static void putPositive(Map<Place, Integer> map, Place key, Integer value) {
		if (value > 0) {
			map.put(key, value);
		}
	}

	private static class Counting implements Runnable {

		private PetriNet<Place> petriNet;
		private Collection<Transition<Place>> transitions;
		private int counter;

		public Counting(PetriNet<Place> petriNet, Collection<Transition<Place>> transitions) {
			this.petriNet = petriNet;
			this.transitions = transitions;
			this.counter = 0;
		}

		@Override
		public void run() {
			try {
				while (true) {
					petriNet.fire(transitions);
					counter++;
				}
			} catch (InterruptedException e) {
				Thread t = Thread.currentThread();
				t.interrupt();
				System.out.println("Wątek " + t.getName() + " odpalił " + counter + " przejść.");
			}
		}
	}

	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);

		int a = in.nextInt();
		int b = in.nextInt();

		in.close();

		if (a > b) {
			int c = a;
			a = b;
			b = c;
		}

		Map<Place, Integer> initial = new HashMap<Place, Integer>();

		putPositive(initial, Place.A, a);
		putPositive(initial, Place.B, b);
		putPositive(initial, Place.C, b);

		PetriNet<Place> petriNet = new PetriNet<Main.Place>(initial, true);

		List<Map<Place, Integer>> inputArcs = new ArrayList<Map<Place, Integer>>();
		List<Collection<Place>> resetArcs = new ArrayList<Collection<Place>>();
		List<Collection<Place>> inhibitorArcs = new ArrayList<Collection<Place>>();
		List<Map<Place, Integer>> outputArcs = new ArrayList<Map<Place, Integer>>();

		for (int i = 0; i < TRANSITIONS; i++) {
			inputArcs.add(new HashMap<Place, Integer>());
			resetArcs.add(new ArrayList<Place>());
			inhibitorArcs.add(new ArrayList<Place>());
			outputArcs.add(new HashMap<Place, Integer>());
		}

		inputArcs.get(A).put(Place.A, 1);

		inputArcs.get(B).put(Place.B, b);
		inputArcs.get(B).put(Place.C, b);

		inhibitorArcs.get(C).add(Place.A);
		inhibitorArcs.get(C).add(Place.B);

		outputArcs.get(A).put(Place.C, b);

		List<Transition<Place>> transitions = new ArrayList<Transition<Place>>();

		for (int i = 0; i < TRANSITIONS; i++) {
			transitions.add(new Transition<Main.Place>(inputArcs.get(i), resetArcs.get(i),
					inhibitorArcs.get(i), outputArcs.get(i)));
		}

		List<Transition<Place>> firstTransitions = new ArrayList<Transition<Place>>();

		firstTransitions.add(transitions.get(A));
		firstTransitions.add(transitions.get(B));

		List<Thread> threads = new ArrayList<Thread>();

		for (int i = 0; i < THREADS; i++) {
			threads.add(new Thread(new Counting(petriNet, firstTransitions), Integer.toString(i)));
		}

		for (Thread t : threads) {
			t.start();
		}

		Collection<Transition<Place>> finalTransition = Collections.singleton(transitions.get(C));

		try {
			petriNet.fire(finalTransition);

			if (!petriNet.currentMarking().containsKey(Place.C)) {
				System.out.println("0");
			} else {
				System.out.println(petriNet.currentMarking().get(Place.C));
			}

			for (Thread t : threads) {
				t.interrupt();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
