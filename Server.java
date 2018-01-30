package miniserveurhttp.server;

import java.net.*;
import java.io.*;

public class Server {

	public Server() throws IOException {
		ServerSocket ss = new ServerSocket(8080);
		System.out.println("Server HTTP fait maison attend des connexions sur le port 8080");
		while (true) {
			new Connection(ss.accept());
		}
	}

	public static void main(String[] args) throws IOException {
		new Server();
	}

	void sendBinaryFileStream(File f, PrintStream out) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fis);
			int s;

			while ((s = bis.read()) != -1) {
				// Là, ça streame la réponse ! Si le fichier est audio
				// ou vidéo ça va streamer vraiment !
				out.write((char) s);
			}
		} catch (FileNotFoundException e) {
			// envoyer code erreur not found
		} catch (IOException e) {
			// envoyer code erreur serveur
		}
	}

}
