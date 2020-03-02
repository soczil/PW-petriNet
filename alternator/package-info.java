/**
 * Pakiet zawierający program przykładowy Alternator.
 * 
 * Tworzymy sieć Petriego z trzema miejscami A, B, C. Początkowo każde miejsce
 * ma po jednym żetonie. Tworzymy dwa zbiory przejść: przejścia początkowe oraz
 * końcowe.
 * 
 * Przejścia początkowe łączymy krawędzią wejściową o wadze 1 z odpowiadającymi
 * im miejscami oraz krawędziami zerującymi z pozostałymi dwoma miejscami (np.
 * przejście 0 jest połączone krawędzią wejściową o wadze 1 z miejscem A oraz
 * krawędziami zerującymi z miejscami B i C).
 * 
 * Przejścia końcowe łączymy krawędziami wzbraniającymi ze wszystkimi miejscami
 * oraz krawędziami wyjściowymi o wadze 1 z nieodpowiadającymi im miejscami (np.
 * przejście końcowe 0 jest połączone krawędziami wzbraniającymi z miejscami A,
 * B, C oraz krawędziami wyjściowymi o wadze 1 z miejscami B i C).
 * 
 * Dzięki temu gdy jesteśmy w sekcji krytycznej (czyli w wypisywaniu) wszystkie
 * miejsca mają po 0 żetonów, a gdy wychodzimy z sekcji zwiększamy żetony o 1
 * pozostałym dwóm miejscom, dzięki czemu nie wejdziemy do sekcji krytycznej dwa
 * razy z rzędu.
 * 
 * Możliwe stany sieci: (1, 1, 1), (1, 1, 0), (1, 0, 1), (0, 1, 1), (0, 0, 0).
 * Każdy inny stan oznacza złamanie warunku bezpieczeństwa.
 */
package alternator;