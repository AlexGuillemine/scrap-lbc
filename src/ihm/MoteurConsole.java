package ihm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.output.TeeOutputStream;

import dao.CompteLbcDao;
import exception.HomeException;
import exception.MenuClientException;
import exception.NoAddsOnlineException;
import scraper.CompteLbc;
import service.ObjectManager;
import service.PrintManager;
import util.Console;

public class MoteurConsole {

	ObjectManager manager;
	PrintManager printManager;
	String bilanControlCompte="";
	public static PrintStream ps;

	public static void main(String[] args) {
		File file = new File("C:\\temp\\addPoster.log");
		try{
			ps = new PrintStream(file);
		}catch(Exception excep){
			excep.printStackTrace(ps);
		}

		try{
			MoteurConsole console = new MoteurConsole();
			console.acceuil();
		}catch(Exception ex){
			ex.printStackTrace(ps);
		}
		ps.close();


	}

	public void acceuil(){
		manager = new ObjectManager();
		printManager = new PrintManager(manager);
		boolean continueBoucle = true;
		while (continueBoucle) {
			System.out.println("------------------------------------");
			System.out.println("------- BIENVENUE DANS ADDS MANAGER ------");
			System.out.println("------------------------------------");
			System.out.println();
			System.out.println("---------    COMMANDES    ----------");
			System.out.println("ESC : pour quitter l'appli");
			System.out.println("HOME : pour revenir � ce menu");
			System.out.println();
			System.out.println("----------    MENU GESTION DES CLIENTS    -----------");
			System.out.println();
			System.out.println("1 : Gerer les annonces d'un client");
			System.out.println("2 : Ajouter un nouveau client");
			System.out.println("3 : Devenir invisible");
			System.out.println();
			String saisie="";
			try{
				saisie = readConsoleInput("^[1-3]$",
						"Que voulez vous faire ? ",
						"Votre r�ponse", " �tre un entier entre 1 et 3.");
				// Enregistrement du choix de l'utilisateur dans num�ro

				switch (saisie) {
				// si le num�ro, on va cr�er un doodle
				case "1":

					selectAClient();
					menuGestionCompteClient();

					break;
				case "2":

					addNewClient();

					break;
				case "3":
					manager.createAgentLbc();
					manager.getAgentLbc().becomeInvisible();
					System.out.println("Vous �tes invisible");
					break;
				default:
					System.out.println("Erreur de saisie");
					break;
				}
			}catch(Exception excep){
				if(excep instanceof HomeException)
					System.out.println("Vous �tes d�j� dans le menu d'acceuil");
				else if(excep instanceof MenuClientException)
					System.out.println("Vous ne pouvez pas vous rendre dans le menu client \n"
							+ "Il faut d'abord en avoir choisi un !");
				else
					excep.printStackTrace();
				continueBoucle = true;
			}
		}
	}

	private void addNewClient() throws HomeException, MenuClientException{
		String nom = printManager.entrerNom();
		String prenom = printManager.entrerPrenom();
		manager.addNewClient(nom, prenom);
	}

	private void selectAClient() throws HomeException, MenuClientException{
		manager.setClients();
		printManager.printClients();
		int refClientChoisie = printManager.selectClient();
		manager.setClientInUse(refClientChoisie);
	}

	// Proc�dure qui permet d'afficher le type de sondage choisi par
	// l'utilisateur
	public void menuGestionCompteClient() throws HomeException{
		boolean continueBoucle = true;
		while (continueBoucle) {
			manager.setComptes();
			System.out.println("----------    MENU GESTION D'UN CLIENT    -----------");
			System.out.println();
			System.out.println("1 : Publier des annonces");
			System.out.println("2 : Ajouter un nouveau compte LBC");
			System.out.println("3 : Controler un compte LBC");
			System.out.println("4 : Controler tous les comptes LBC");
			System.out.println("5 : G�rer les comptes LBC");
			System.out.println("6 : G�rer les titres et les textes");
			System.out.println("7 : Afficher r�sum� des annonces en ligne");
			System.out.println("8 : Se connecter � un compte");
			System.out.println("9 : Revenir au menu de gestion des clients");
			System.out.println();
			try{
				String saisie = readConsoleInput("^[1-9]$",
						"Que voulez vous faire ? ",
						"Votre r�ponse", " �tre un entier entre 1 et 9");
				// Enregistrement du choix de l'utilisateur dans num�ro
				switch (saisie) {
				// si le num�ro, on va cr�er un doodle
				case "1":
					publishAdd();
					break;
				case "2":
					addNewCompteLbc();
					break;
				case "3":
					choixDunCompte(false);
					controlCompteLbc();
					break;
				case "4":
					controlAllCompte();
					break;
				case "5":
					gererCompteLbc();
					continueBoucle = true;
					break;
				case "6":
					printManager.menuAddTextesTitre();
					break;
				case "7":
					printManager.menuSummary();
					break;
				case "8":
					choixDunCompte(false);
					connectToCompte();
					break;
				case "9":
					continueBoucle = false;
					break;
				default:
					System.out.println("Erreur de saisie");
					break;
				}
			}catch(MenuClientException exp){
				continueBoucle = true;
			}
		}
	}

	private void bilan() {
		// TODO Auto-generated method stub

	}



	private void gererCompteLbc() throws HomeException, MenuClientException {

		System.out.println("---------- MENU DE GESTION DES COMPTES -----------");
		System.out.println();
		printManager.printComptes();
		choixDunCompte(true);
		printManager.menuGestionDesComptes();


	}

	private void controlAllCompte(){
		bilanControlCompte="";
		Collection<CompteLbc> comptes = manager.getComptes().values();
		for(CompteLbc compte : comptes){
			manager.setCompteInUse(compte);
			bilanControlCompte = bilanControlCompte + "Fin de contr�le du Compte "+compte.getMail();
			System.out.println(bilanControlCompte);
			try{
				controlCompteLbc();
			}catch(HomeException ex){// est g�n�r� si un compte ne comporte pas d'annonces
				System.out.println("Le dernier compte control� n'a aucune annonce");
			}
		}
		System.out.println(bilanControlCompte);

	}
	private void connectToCompte(){
		manager.createAgentLbc();
		manager.connect();
		System.out.println("Entrez qqch pour vous d�connecter");
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		manager.getAgentLbc().getDriver().quit();
		manager.getAgentLbc().becomeInvisible();
	}

	private void controlCompteLbc() throws HomeException{
		manager.createAgentLbc();
		try{
			manager.scanAddsOnLbc();
		}catch(NoAddsOnlineException excep){
			bilanControlCompte = bilanControlCompte+'\n'+" Il n'y a aucune annonces en ligne !! ";
			bilanControlCompte = bilanControlCompte+'\n'+"Le nb d'annonces qui �tait en ligne est de : "+excep.getNbAddsOnline();
			bilanControlCompte = bilanControlCompte+'\n'+"Le nb d'annonces qui �tait en attende mod�ration est de : "+excep.getNbAddsEnAttenteMode();
			bilanControlCompte = bilanControlCompte+'\n'+" Le contr�le des annonces est fini\n\n";
			System.out.println(bilanControlCompte);
			manager.getAgentLbc().getDriver().quit();
			manager.getAgentLbc().becomeInvisible();
			throw  new HomeException();
		}
		manager.checkAndSaveBooster();
		boolean texteAndTitleOnlineReferenced;
		System.out.println("V�rification des correspondances entre les annonces en ligne et la bdd");
		do{
			texteAndTitleOnlineReferenced = manager.isTexteAndTitleOnlineReferenced();
			if(!texteAndTitleOnlineReferenced){
				System.out.println("Il y a des textes et des titres en ligne non r�f�renc�s !");
			}else{
				System.out.println("Tous les textes et les titres sont r�f�renc�s");
			}
			printManager.toLinkTexteAndTitleWhitoutRef();
		}while(!texteAndTitleOnlineReferenced);
		boolean addsOnlineHasMoreThanOneReference;
		System.out.println("V�rification que les annonces en ligne soient r�f�renc�es qu'une seule fois dans la bdd");

		do{
			addsOnlineHasMoreThanOneReference = manager.hasAddsWithMultipleReferenced();// vaudra vrai si chaque annonce a une unique correspondance en bdd
			if(addsOnlineHasMoreThanOneReference){
				System.out.println("Il y a des annonces en ligne avec plusieurs correspondances !");
				printManager.toSolveMultipleAddMatch();
			}else{
				System.out.println("Toutes les annonces en lignes sont r�f�renc�s au plus une fois");
			}

		}while(addsOnlineHasMoreThanOneReference);

		if(manager.isReadyToSave()){
			System.out.println("Toutes les �l�ments des annonces lbc ont une correspondance en bdd et une seule");
			System.out.println("De m�me chaque annonce en ligne a une unique ref en base");
			manager.saveAddsFromScanOfLbc();
			System.out.println("---- Gestion de la correspondance des communes des annonces r�f�renc�s en ligne entre la Bdd et LeBonCoin ----");
			printManager.gererCorrepondanceCommunes(manager.getAddsSaver().getAddsUpdated());
			System.out.println("Les annonces de lbc ont bien �t� mise � jour");
			String resultsToPrint = printManager.printResults();
			bilanControlCompte = bilanControlCompte + resultsToPrint + "\n\n";
		}else{
			System.out.println("Les annonces sont pas pr�tes � �tre sauvegard�s");
		}
		System.out.println(" Le contr�le des annonces est fini");
		manager.getAgentLbc().getDriver().quit();
		manager.getAgentLbc().becomeInvisible();
	}



	private void addNewCompteLbc() throws HomeException, MenuClientException  {
		String mail = readConsoleInput("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", "Entrez le mail du compte LBC � ajouter",
				"Votre r�ponse", "doit �tre une adresse mail");
		String password = readConsoleInput(".{3,}", "Entrez le password du compte LBC � ajouter",
				"Votre r�ponse", "doit �tre faire plus de 3 caract�res");
		CompteLbc compteToAdd = new CompteLbc(mail, password);
		compteToAdd.setRefClient(manager.getClientInUse().getRefClient());
		CompteLbcDao compteLbcDao = new CompteLbcDao();
		compteLbcDao.save(compteToAdd);

	}


	private void publishAdd() throws HomeException, MenuClientException {
		System.out.println("------    MENU DE PUBLICATION DES ANNONCES   ------");
		choixDunCompte(false);
		printManager.doYouWantToSaveAddIndd();
		String nbAnnonces = readConsoleInput("^[1-9]\\d*$", 
				"Entrez le nb d'annonces � publier",
				"Votre r�ponse", 
				"doit �tre un entier positif");


		String numTel="0200000000";

		String afficherNumTel = readConsoleInput("^oui|non$", 
				"Voulez vous afficher le num�ro de t�l�phone dans l'annonce",
				"Votre r�ponse", 
				" �tre oui ou non");
		if(afficherNumTel.equals("oui")){
			numTel = readConsoleInput("^0\\S{9}$", 
					"Entrez le num�ro de t�l�phone � mettre dans les annonces",
					"Votre r�ponse", 
					"doit �tre une cha�ne de 10 caract�res sans espace commencant par 0");
		}

		String category = readConsoleInput("^CoursParticuliers|Prestations|Animaux|AccessoiresBagagerie$", 
				"S�lectionner la cat�gorie � utiliser (Prestations ou CoursParticuliers ou Animaux ou AccessoiresBagagerie)",
				"Votre r�ponse", 
				"�tre Prestations ou CoursParticuliers ou Animaux ou AccessoiresBagagerie");

		String prenom = readConsoleInput("^\\S{3,}$", 
				"Entrez le prenom � mettre dans les annonces",
				"Votre r�ponse", 
				"�tre une chaine de caract�res d'au moins 3 lettres");
		
		String prix = readConsoleInput("^oui|non$", 
				"Voulez vous mettre un prix",
				"Votre r�ponse", 
				" �tre oui ou non");
		
		manager.getCompteInUse().setPrenom(prenom);
		
		manager.createAgentLbc(Integer.parseInt(nbAnnonces), afficherNumTel, numTel, category, prix);
		manager.createAddsGenerator();
		selectionTitres();
		selectionTextes();
		selectionCommunes();
		selectionImages();
		System.out.println("D�marrage de la publication ...");
		manager.genererEtPublier();

		// pour g�rer les correspondances des communes (faire en sorte que la commune soumise soit bien en base)
		printManager.gererCorrepondanceCommunes(manager.getAddsPublieAvtMode());
		// pour afficher le nb d'annones soumises
		printManager.printBilanPublication();
		manager.getAgentLbc().getDriver().quit();
		manager.getAgentLbc().becomeInvisible();
	}




	private void selectionImages() throws HomeException, MenuClientException {
		String path;
		path = selectPath(" les images");
		manager.setPathToAdds(path);
	}


	private void selectionCommunes() throws HomeException, MenuClientException {
		String renouvellez;
		do{
			String strTypeSource;

			strTypeSource = selectSource("communes");

			manager.setCommuneSourceType(strTypeSource);

			switch (manager.getAddsGenerator().getTypeSourceCommunes()) {
			case SQL:
				selectionCommuneSql();
				break;
			case XLSX:
				selectionCommuneXlsx();	
				break;
			}
			manager.setcommunes();
			// on affiche les titres choisies pour v�rification de la part de l'utilisateur 
			System.out.println(printManager.communeToString());
			printManager.afficherNbCommunesForPublication();
			renouvellez = readConsoleInput("^oui|non", "Est ce bien les communes ci dessus que vous voulez utiliser ?",
					"Votre r�ponse", "doit �tre oui ou non");
		}while(renouvellez.equals("non"));

	}


	private void selectionCommuneXlsx() throws HomeException, MenuClientException {
		String path = selectPath("communes");
		manager.setPathToAdds(path);

	}


	private void selectionCommuneSql() throws HomeException, MenuClientException {
		String renouvellez;
		int bornInf;
		int bornSup ;
		do{
			bornInf = Integer.parseInt(readConsoleInput("[0-9]\\d*", "Saisir la borne inf�rieur de population des communes � choisir :",
					"Votre r�ponse", "doit �tre un entier positive"));
			bornSup = Integer.parseInt(readConsoleInput("[0-9]\\d*", "Saisir la borne sup�rieure de population des communes � choisir :",
					"Votre r�ponse", "doit �tre un entier positive"));
			if(bornSup>bornInf){
				renouvellez = readConsoleInput("^oui|non", "Vous confirmez votre choix : "
						+ "born inf : "+ bornInf+
						" born sup : "+ bornSup,
						"Votre r�ponse", "doit �tre oui ou non");
			}else{
				System.out.println("Veuillez renouvellez votre saisie afin que la borne sup soit plus"
						+ "grande que la borne inf");
				renouvellez="non";
			}

		}while(renouvellez.equals("non"));
		manager.setCritSelectVille(bornInf, bornSup);
	}

	// pour poser la question : quelle type de source � utiliser ?
	private String selectSource(String objectRelated)throws HomeException, MenuClientException {
		String renouvellez;
		String strTypeSource;
		do{
			String[] pourAffichageEtSaisieDesSourceType = this.printManager.typeSourcesToString();
			System.out.println();
			System.out.println("Choisir un type de source � utiliser pour les "+objectRelated+" : ");
			System.out.println(pourAffichageEtSaisieDesSourceType[0]);
			System.out.println("Saisir le type de source � utiliser pour les "+objectRelated+" : ");
			strTypeSource = readConsoleInput(pourAffichageEtSaisieDesSourceType[1], "Entrez le type de source choisi : ",
					"Votre r�ponse", "doit �tre un des types sources");
			renouvellez = readConsoleInput("^oui|non", "Est ce bien ce type de source : "+ strTypeSource +""
					+ " que vous voulez utiliser ? ",
					"Votre r�ponse", "doit �tre oui ou non");
		}while(renouvellez.equals("non"));
		return strTypeSource;
	}

	private String selectPath(String elementsAdds)throws HomeException, MenuClientException {
		String renouvellez;
		String path;
		do{
			path = readConsoleInput("^MINE$|CLIENT|MARTIN|ECOM", "Saisir le r�pertoire des annonces � utiliser pour les "+elementsAdds,
					"Votre r�ponse", "doit �tre MINE ou CLIENT ou MARTIN ou ECOM");
			renouvellez = readConsoleInput("^oui|non", "Est ce bien ce r�pertoire : "+ path +""
					+ " que vous voulez utiliser ? ",
					"Votre r�ponse", "doit �tre oui ou non");
		}while(renouvellez.equals("non"));
		return path;
	}


	private void selectionTextes() throws HomeException, MenuClientException  {
		String renouvellez;
		do{
			String strTypeSource="SQL";

			strTypeSource = selectSource("textes");
			manager.setTexteSourceType(strTypeSource);

			switch (manager.getAddsGenerator().getTypeSourceTextes()) {
			case SQL:
				selectionTexteSql();
				break;
			case XLSX:
				selectionTextesXlsx();	
				break;
			}
			manager.setTextes();
			// on affiche les titres choisies pour v�rification de la part de l'utilisateur 
			System.out.println(printManager.texteToString());
			printManager.afficherNbTexteForPublication();
			renouvellez = readConsoleInput("^oui|non", "Est ce bien les textes ci dessus que vous voulez utiliser ?",
					"Votre r�ponse", "doit �tre oui ou non");
		}while(renouvellez.equals("non"));

	}


	private void selectionTextesXlsx() throws HomeException, MenuClientException {
		String path = selectPath("les textes");
		manager.setPathToAdds(path);	
	}


	private void selectionTexteSql() throws HomeException, MenuClientException {
		String typeTexteChoisie;

		typeTexteChoisie = printManager.chooseTypeTexte();
		manager.setCritSelectTexte(typeTexteChoisie);

	}


	private void selectionTitres() throws HomeException, MenuClientException {
		String renouvellez;
		do{
			String strTypeSource;

			strTypeSource = selectSource("titres");
			manager.setTitleSourceType(strTypeSource);

			switch (manager.getAddsGenerator().getTypeSourceTitles()) {
			case SQL:
				selectionTitresSql();
				break;
			case XLSX:
				selectionTitresXlsx();	
				break;
			}
			manager.setTitres();
			// on affiche les titres choisies pour v�rification de la part de l'utilisateur 
			System.out.println(printManager.titreToString());
			printManager.afficherNbTitleForPublication();
			renouvellez = readConsoleInput("^oui|non", "Est ce bien les titres ci dessus que vous voulez utiliser ?",
					"Votre r�ponse", "doit �tre oui ou non");
		}while(renouvellez.equals("non"));


	}




	private void selectionTitresXlsx() throws HomeException, MenuClientException {
		String path = selectPath("titres");
		manager.setPathToAdds(path);
	}


	private void selectionTitresSql() throws HomeException, MenuClientException {
		String typeTitleChoisie;

		typeTitleChoisie = printManager.chooseTypeTitle();
		manager.setCritSelectTitre(typeTitleChoisie);

	}


	private void choixDunCompte(boolean printDisabled) throws HomeException, MenuClientException {
		String[] pourAffichageEtSaisieDesComptes = this.printManager.comptestoString(printDisabled);
		String renouvellez;
		String idCompte ;
		System.out.println();
		do{


			System.out.println("Choisir un compte � utiliser : ");
			System.out.println(pourAffichageEtSaisieDesComptes[0]);
			System.out.println("Saisir le compte � utiliser : ");
			idCompte = readConsoleInput(pourAffichageEtSaisieDesComptes[1], "Entrez l'identifiant du compte choisi : ",
					"Votre r�ponse", "doit �tre un des identifiants");
			manager.setCompte(Integer.parseInt(idCompte));
			//		System.out.println("Vous avez choisi le compte : "+manager.getCompteInUse().getMail());
			renouvellez = readConsoleInput("^oui|non", "Est ce bien ce compte : "+ manager.getCompteInUse().getMail() +""
					+ " que vous voulez utiliser ? ",
					"Votre r�ponse", "doit �tre oui ou non");
		}while(renouvellez.equals("non"));
		manager.setCompte(Integer.parseInt(idCompte));
		
	}


	public String readConsoleInput(String regex, String message, String variableASaisir, String format)
			throws HomeException, MenuClientException {
		Pattern p = Pattern.compile(regex);
		String phraseCloseApplication = "Voulez fermez l'application ? (si il y a un travail, il ne sera pas enregistr�)";
		boolean continueBoucle = true;
		String input = "";
		while (continueBoucle) {
			input = Console.readString(message);
			if (input.equals(phraseCloseApplication))
				input = "autreChoseQueESCQueHomeQueOUIQueNON";
			switch (input) {
			case "ESC":
				String closeAppli = readConsoleInput("OUI|NON", phraseCloseApplication, "La r�ponse ",
						"�tre OUI ou NON.");
				if (closeAppli.equals("OUI")) {
					System.out.println("Vous venez de fermer l'application ! ");
					System.exit(0);
				} else {
					continueBoucle = true;
				}
				break;
			case "HOME":
				throw new HomeException();
			case "MENU CLIENT":
				throw new MenuClientException();
			default:
				Matcher m = p.matcher(input);
				boolean b = m.matches();
				if (b) {
					continueBoucle = false;
				} else {
					System.out.println(variableASaisir + " doit " + format);
				}
				break;
			}
		}
		return (input);
	}


}
