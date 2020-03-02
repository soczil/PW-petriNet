/**
 * Pakiet zawierający program przykładowy Multiplicator.
 * 
 * Wczytujemy liczby a i b ze standardowego wejścia i ustawiamy je tak, żeby a
 * było mniejsze od b. Tworzymy sieć Petriego z miejscami A, B, C. Początkowo
 * miejsce A ma a żetonów, a miejsca B i C mają po b żetonów.
 * 
 * Tworzymy 3 przejścia.
 * 
 * Pierwsze przejście zawiera krawędź wejściową o wadze jeden do miejsca A oraz
 * krawędź wyjściową do miejsca C o wadze b.
 * 
 * Drugie przejście zawiera krawędzie wejściowe o wagach b do miejsc B i C.
 * 
 * Trzecie przejście (przejście końcowe) zawiera krawędzie wzbraniające do
 * miejsc A i B.
 * 
 * Pierwsze przejście doda nam a razy b do liczby żetonów w miejscu C (czyli w
 * miejscu C będzie (a + 1)*b żetonów). Drugie przejście odejmie b od liczby
 * żetonów w miejscach B i C, dzięki czemu w miejscu C będziemy mieli a*b
 * żetonów. Wtedy przejście końcowe stanie się dozwolone, bo w miejscach A i B
 * będzie po 0 żetonów.
 */
package multiplicator;