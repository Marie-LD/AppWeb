
package miniserveurhttp.server;

import java.net.*;
import java.io.*;
import java.util.*;

class Connection implements Runnable {

	private Thread thread;
	private Socket socket;
	private boolean requeteEstUnPost = false;
	private int bodyLength;
	private String codeOk="200 : OK";
	private String codeErr="404 : not found";
	private 	String nomPage=System.getProperty("user.dir");
	private boolean fichierExiste;
	private String navigateur;
	private String page;



	public Connection(Socket socket) {
		this.socket = socket;
		thread = new Thread(this);
		thread.start();
		System.out.print(nomPage);

	}

	void sendResponseHeader(PrintStream ps, int len, String code) {
		ps.println("HTTP/1.0 "+code);
		ps.println("Date: " + new Date());
		ps.println("Server: nfa016Server/1.0");
		ps.println("Content-type: text/html");
		ps.println("Content-length: " + len);
		ps.println("Set-Cookie: cookie-name = toto;");
		System.out.println("Navigateur : " + navigateur);
		ps.println("");
	}

	String litHeaderDeLaRequete() {
		String header = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line = br.readLine();
			header = line;
			StringTokenizer st = new StringTokenizer(line);

			requeteEstUnPost = st.nextToken().equals("POST");

			//Verification du fichier dans notre dossier
			System.out.println(line);
			String[] chemin=line.split(" ");
			String fichier = nomPage+chemin[1];
			File f = new File(fichier);
			fichierExiste=f.exists();
			
			String pageAffiche = null; // La page à afficher
			page = st.nextToken();
			System.out.println("Nom de la page avant : "+page);
			page = page.substring(1); // On enlève le "/"
			System.out.println("Nom de la page apres : "+page);
			pageAffiche = lireBodyPage(page); // On met le body de la page a afficher dans pageAffiche

			while (!line.equals("")) {
				line = br.readLine();
				header = header + '\n' + line;
				System.out.println(line);
				st = new StringTokenizer(line);
				String ung;

				if(line.contains("User-Agent")) {
					if(line.contains("Safari"))
						navigateur="Safari";
						header=pageAffiche;
				}

				while (st.hasMoreTokens()) {
					ung = st.nextToken();
					System.out.print(ung + '#');
					if (ung.equals("Content-Length:")) {
						requeteEstUnPost = true;
						System.out.println("on est dedans");
						bodyLength = Integer.parseInt(st.nextToken());
					}
				}

			}
		} catch (IOException e) {
			System.err.println("I/O error " + e);
		}
		return header;
	}

	public String lireBodyPage(String page) {
		boolean read = false;
		String toto = "";
		try {
			FileReader fr = new FileReader("./" + page); // On ouvre toto.html
			BufferedReader reader = new BufferedReader(fr);
			String lecture = reader.readLine();

			while (!lecture.equals("</html>")) { // Tant qu'il y a des choses à lire
				if (lecture.equals("<body>"))
					read = true;
				if (lecture.equals("</body>"))
					read = false;

				lecture = reader.readLine();

				if (read && !lecture.equals("</body>")) {// Si on lit le body
					toto = toto + lecture;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return toto;
	}

	String litBodyDeLaRequete() {
		// Dans le cas où la requête est un post, il faut lire la suite
		// du header pour avoir par ex les paramètres "postés" par un
		// formulaire !
		byte[] line = new byte[bodyLength];
		try {
			for (int i = 0; i < bodyLength; i++) {
				line[i] = (byte) socket.getInputStream().read();
			}
		} catch (IOException e) {
			System.err.println("I/O error " + e);
		}
		return new String(line);
	}

	public void run() {
		try {
			System.out.println("connection reçue du client");
			// out est le flux sur lequel on va écrire : un socket vers le client !
			PrintStream out = new PrintStream(socket.getOutputStream());

			// Préparation de la réponse
			String reponse =
					"<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"fr\">\n<head>\n<title>Requete</title>\n</head>\n<body><p><pre>\n";
			reponse = reponse + litHeaderDeLaRequete();
			//if fichier existe, le mettre dans une chaine de caractere et le renvoyer a la place de reponse
			System.out.println("requête est un POST ? : " + requeteEstUnPost);

			if (requeteEstUnPost) {
				reponse = reponse + '\n' + litBodyDeLaRequete();
			}
			reponse = reponse + "\n</pre>\n</p>\n</body>\n</html>\n";

			if(navigateur.equals("Safari")) {

				// L'en-tête de la réponse part sur le socket, direction le client !

				//Renvoie le code ok ou erreur selon le boolean
				sendResponseHeader(out, reponse.length(),fichierExiste ? codeOk : codeErr);

				// et le body !
				out.print(reponse);
			}
			socket.close();
		} catch (IOException e) {
			System.err.println("I/O error " + e);
		}
	}
}
