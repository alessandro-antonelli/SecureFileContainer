/*
 * Università di Pisa - Corso di Programmazione II (273AA)
 * Progetto Java - Sessione estiva-autunnale A.A. 2018/2019 (appello settembre 2019)
 * Candidato Alessandro Antonelli (matricola 507264, corso A)
 */

package pr2.impl2;

import java.io.Serializable;
import java.util.HashSet;

import pr2.Clonatore;

/*
 * =========
 * OVERVIEW:
 * =========
 * DescrittoreFile rappresenta un file della collezione,
 * di cui memorizza il contenuto e i riferimenti agli utenti che ne hanno accesso a vario titolo.
 * 
 * ===============================
 * INVARIANTE DI RAPPRESENTAZIONE:
 * ===============================
 * contenuto != null && utentiAccessoLettura != null && utentiAccessoScrittura != null &&
 * utenteCreatore != "" && utentiAccessoLettura e utentiAccessoScrittura non contengono null né stringhe vuote &&
 * gli insiemi {utenteCreatore}, utentiAccessoLettura e utentiAccessoScrittura non contengono duplicati e sono disgiunti due a due
 * 
 * =======================
 * FUNZIONE DI ASTRAZIONE:
 * =======================
 * FA = this rappresenta il file
 * <contenuto, creatore, autorizzatiR, autorizzatiRW>
 * appartenente a F, con:
 * 
 * contenuto = this.contenuto
 * creatore = this.utenteCreatore
 * autorizzatiR = this.utentiAccessoLettura
 * autorizzatiRW = this.utentiAccessoScrittura
 */
class DescrittoreFile<E extends Serializable>
// Nessun modificatore per rendere la classe "package private" (visibile solo all'interno del proprio package)
{
	// Riferimento al contenuto del file (oggetto generico E)
	private E contenuto;
	
	// Username dell'utente creatore del file (unico). Non è "final" perché, con una "remove" da parte del creatore, può divenire null.
	private String utenteCreatore;
	
	// Insieme degli username degli utenti che dispongono del diritto d'accesso in sola lettura al file
	private final HashSet<String> utentiAccessoLettura;
	
	// Insieme degli username degli utenti che dispongono del diritto d'accesso in lettura e scrittura al file
	private final HashSet<String> utentiAccessoScrittura;
	
	// Codifica dei due diritti/tipi di accesso utilizzabili per ciascun file
	enum TipoDiAccesso { SolaLettura, LetturaEScrittura; }
	
	/*
	 *  Metodo costruttore. Crea un descrittore di file dotato di contenuto, utente creatore
	 *  e insiemi vuoti per gli utenti con accesso in lettura o scrittura.
	 *  Il contenuto memorizzato nel nuovo oggetto è una deep copy del riferimento passato al costruttore (cioè esegue copy-in).
	 */
	public DescrittoreFile(E contenuto, String utenteCreatore)
	{
		Clonatore<E> cloner = new Clonatore<E>();
		this.contenuto = cloner.deepCopyConSerializzazione(contenuto);
		this.utenteCreatore = utenteCreatore;
		this.utentiAccessoLettura = new HashSet<String>();
		this.utentiAccessoScrittura = new HashSet<String>();
	}
	
	// Indica se l'utente specificato è il creatore del file attuale
	// modifies: none
	public boolean èCreatore(String username)
	{
		if (username == null) throw new NullPointerException();

		if(utenteCreatore == null) return false;
		else return utenteCreatore.equals(username);
	}
	
	// Indica se l'utente specificato dispone almeno del diritto di accesso in lettura al file attuale.
	// Restituisce true anche se dispone di un diritto di accesso più ampio, es. è il creatore o ha lettura+scrittura.
	// modifies: none
	public boolean puòLeggere(String username)
	{
		if (username == null) throw new NullPointerException();
		
		if(utenteCreatore != null && utenteCreatore.equals(username))
			return true;
		else if(utentiAccessoLettura.contains(username) || utentiAccessoScrittura.contains(username))
			return true;
		else
			return false;
	}
	
	// Indica se l'utente specificato dispone almeno del diritto di accesso in scrittura al file attuale.
	// Restituisce true anche se dispone di un diritto di accesso più ampio, cioè creatore.
	// modifies: none
	public boolean puòScrivere(String username)
	{
		if (username == null) throw new NullPointerException();
		
		if(utenteCreatore != null && utenteCreatore.equals(username))
			return true;
		else if(utentiAccessoScrittura.contains(username))
			return true;
		else
			return false;
	}
	
	// Indica se l'utente specificato è l'unico a disporre del diritto di accesso al file attuale,
	// a qualunque titolo (creatore compreso).
	// modifies: none
	public boolean unicoAdAvereAccesso(String username)
	{
		if(username == null) throw new NullPointerException();
		
		if(!èCreatore(username) && !puòLeggere(username) && !puòScrivere(username))
			throw new IllegalArgumentException("unicoAdAvereAccesso deve essere invocato su un utente che ha accesso al file");
		
		int nUtentiConAccesso = 0;
		if(utenteCreatore != null) nUtentiConAccesso++;
		nUtentiConAccesso += utentiAccessoLettura.size();
		nUtentiConAccesso += utentiAccessoScrittura.size();
		
		return(nUtentiConAccesso == 1);
	}
	
	// Aggiunge l'utente specificato tra quelli che hanno accesso al file, con il livello
	// di accesso indicato dal parametro (sola lettura o anche scrittura).
	// Se l'utente disponeva già di quel diritto d'accesso o di un diritto superiore, non esegue nulla;
	// se disponeva di un diritto inferiore, esso viene rimosso e sostituito da quello nuovo.
	// modifies: utentiAccessoLettura, utentiAccessoScrittura
	public void aggiungiCondivisione(String username, TipoDiAccesso accesso)
	{
		if(username == null) throw new NullPointerException();
			
		if(accesso == TipoDiAccesso.SolaLettura)
		{
			// Ad username viene concesso l'accesso in sola lettura
			if(!puòLeggere(username) && !puòScrivere(username))
				utentiAccessoLettura.add(username);
		}
		else if(accesso == TipoDiAccesso.LetturaEScrittura)
		{
			// Ad username viene concesso l'accesso in lettura e scrittura
			if(!puòScrivere(username))
				utentiAccessoScrittura.add(username);
			if(puòLeggere(username)) // Rimuovo permesso di lettura superfluo
				utentiAccessoLettura.remove(username);
		}
	}
	
	// Rimuove l'utente specificato da quelli che hanno accesso al file, qualunque fosse il tipo
	// di accesso di cui godeva: sola lettura, lettura+scrittura o creatore.
	// Se l'utente non ha accesso al file, non esegue nulla.
	// modifies: utenteCreatore, utentiAccessoLettura, utentiAccessoScrittura
	public void rimuoviDirittoAccesso(String username)
	{
		if(username == null) throw new NullPointerException();
		
		if(èCreatore(username))
			utenteCreatore = null;
		else if(puòLeggere(username))
			utentiAccessoLettura.remove(username);
		else if(puòScrivere(username))
			utentiAccessoScrittura.remove(username);
	}
	
	// Indica se il contenuto del file specificato per argomento è uguale al contenuto
	// del file corrente.
	// Il confronto avviene invocando il metodo equals() della classe generica E, pertanto
	// il presente metodo ha senso solo quando E ridefinisce opportunamente equals() secondo
	// il concetto di content-equality.
	// modifies: none
	public boolean contenutoUgualeA(E file)
	{
		return contenuto.equals(file);
	}
	
	// Restituisce un riferimento al contenuto del file corrente.
	// Quella che viene restituita è una deep copy o una shallow copy a seconda del livello di accesso
	// alla collezione che si desidera fornire tramite il riferimento (argomento accessibilitàCopia).
	// modifies: none
	public E getContenuto(TipoDiAccesso accessibilitàCopia)
	{
		if(accessibilitàCopia == TipoDiAccesso.SolaLettura)
		{
			// Restituisco deep copy che non permette la modifica
			Clonatore<E> cloner = new Clonatore<E>();
			return cloner.deepCopyConSerializzazione(contenuto);
		}
		else
			// Restituisco shallow copy che permette la modifica
			return contenuto;
	}
}
