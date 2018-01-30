package miniserveurhttp.client;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;

// utilisation: java Client url
// exemple: java Client http://www.info.univ-tours.fr/
class Client {

	// Petit client de test utile pour debugger
	public static void main(String[] args) throws Exception {
		// On va demander la page par défaut du serveur tournant sur
		// localhost, port 8080
		//URL url = new URL("http://localhost:8080");
		URL url = new URL("http://www.info.univ-tours.fr/");

		// Classe très pratique pour "parler HTTP"
		HttpURLConnection connexion;
		connexion = (HttpURLConnection) url.openConnection();
		// On dit qu'on veut faire un GET (ici sur "/", cf URL en 1ère ligne)
		connexion.setRequestMethod("GET");

		// On lit ici l'entête de la réponse su serveur
		String ligne;
		int i = 0;
		while (true) {
			// cette méthode lit sur le flux d'entrée de la connexion HTTP
			// les lignes de l'en-tête
			ligne = connexion.getHeaderField(i);
			if (ligne == null) {
				break;
			}
			System.out.println("toto: " + ligne);
			i++;
		}

		System.out.println();

		// On lit maintenant le body de la réponse !
		InputStream is = connexion.getInputStream();
		int b;
		while (true) {
			b = is.read();
			if (b == -1) {
				break;
			}
			System.out.print((char) b);
		}
		System.out.println();
	}
}
