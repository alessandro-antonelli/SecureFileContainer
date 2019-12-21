/*
 * Università di Pisa - Corso di Programmazione II (273AA)
 * Progetto Java - Sessione estiva-autunnale A.A. 2018/2019 (appello settembre 2019)
 * Candidato Alessandro Antonelli (matricola 507264, corso A)
 */

package pr2.impl2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import pr2.SecureFileContainer;
import pr2.exceptions.*;

/* =========
 * OVERVIEW:
 * =========
 * HashSecureFileContainer<E> è una collezione mutabile illimitata di:
 * - utenze protette da password (immutabili e con duplicati non ammessi)
 * - file (mutabili e con duplicati ammessi),
 *   composti da un contenuto mutabile, serializzabile e confrontabile di tipo E,
 *   e da informazioni sui diritti d'accesso delle utenze al file.
 * 
 * Ad ogni file è associata al più una utenza creatrice,
 * un insieme di zero o più utenti con accesso in sola lettura (R), e
 * un insieme di zero o più utenti con accesso in lettura e scrittura (RW).
 * 
 * ===============================
 * INVARIANTE DI RAPPRESENTAZIONE:
 * ===============================
 * DBFile != null && DBPassword != null && DBFile non contiene valori null &&
 * DBPassword contiene solo chiavi e valori diversi da null e diversi da stringa vuota &&
 * DBPassword non contiene chiavi duplicate && in DBPassword ogni chiave è diversa dal valore associato &&
 * DBPassword non contiene valori uguali a null o a stringhe vuote &&
 * ultimoIDAssegnato >= 0 && ultimoIDAssegnato assume valori monotonicamente crescenti
 * 
 * =======================
 * FUNZIONE DI ASTRAZIONE:
 * =======================
 * FA = this rappresenta la coppia < U, F > con: 
 * U = this.DBPassword
 * F = this.DBFile 
 */
public class HashSecureFileContainer <E extends Serializable> implements SecureFileContainer<E>
{
	// Struttura in cui vengono memorizzati i riferimenti ai file e i loro diritti di accesso
	private final HashMap<Integer, DescrittoreFile<E>> DBFile;
	
	// Struttura in cui vengono memorizzate le credenziali degli utenti
	private final HashMap<String, String> DBPassword;
	
	// Indica l'ultimo numero progressivo (intero positivo) usato come chiave in DBFile. Modificato solo da generaID().
	private int ultimoIDAssegnato = 0;
	
	// Metodo costruttore: produce una nuova collezione vuota (senza utenti né file)
	public HashSecureFileContainer()
	{
		DBFile = new HashMap<Integer, DescrittoreFile<E>>();
		DBPassword = new HashMap<String, String>();
	}
	
	// Genera un intero utilizzabile come chiave nella mappa DBFile che non fosse già stato usato in precedenza.
	// Utilizzando come chiavi solo i valori restituiti da generaID(), si ha la garanzia dell'unicità delle chiavi.
	private int generaID()
	{
		ultimoIDAssegnato++;
		return ultimoIDAssegnato;
	}
	
	// Crea l'identità di un nuovo utente della collezione
	@Override
	public void createUser(String Id, String passw)
			throws NullPointerException, IllegalArgumentException, UsernameUnavailableException
	{
		if(Id == null || passw == null) throw new NullPointerException
			("Impossibile registrare un utente con username o password nulla!");
		if(Id.isEmpty() || passw.isEmpty()) throw new IllegalArgumentException
			("Impossibile registrare un utente con username o password vuota!");
		if(passw.equals(Id)) throw new IllegalArgumentException
			("Hai scelto una password uguale al tuo username: riprova scegliendone una più sicura...");
		
		if(DBPassword.containsKey(Id)) throw new UsernameUnavailableException
		("L'username che hai scelto (" + Id + ") non è disponibile perché già in uso da altro utente. Riprova scegliendone un altro...");
		
		DBPassword.put(Id, passw);
	}
	
	/*
	 *  Metodo ausiliare che esegue le operazioni di controllo delle credenziali (legalità dei valori,
	 *  esistenza dell'account, correttezza della password). In caso di esito negativo solleva le corrispondenti eccezioni.
	 */
	private void AccediAccount(String Owner, String passw)
			throws NullPointerException, NoSuchUserException, WrongPasswordException
	{
		if(Owner == null || passw == null) throw new NullPointerException
			("Impossibile accedere all'account indicando username o password nulla!");

		// Verifica esistenza account
		if(!DBPassword.containsKey(Owner)) throw new NoSuchUserException
			("Impossibile accedere: il nome utente inserito (" + Owner + ") non corrisponde ad alcun utente registrato!");
		
		// Verifica password
		String passwordCorretta = DBPassword.get(Owner);
		if(!passwordCorretta.equals(passw)) throw new WrongPasswordException
			("Accesso all'account " + Owner + " negato: la password inserita non è corretta!");
	}

	/*
	 * Restituisce il numero dei file della collezione a cui l'utente specificato ha accesso
	 * a qualunque titolo (creatore/lettore/scrittore), se vengono rispettati i controlli di identità
	 */
	@Override
	public int getSize(String Owner, String passw)
			throws NullPointerException, NoSuchUserException, WrongPasswordException
	{
		AccediAccount(Owner, passw);
		
		int conteggio = 0;
		
		for(DescrittoreFile<E> file : DBFile.values())
		{
			if(file.puòLeggere(Owner)) conteggio++;
		}
		
		return conteggio;
	}

	// Inserisce il file nella collezione, se vengono rispettati i controlli di identità
	@Override
	public boolean put(String Owner, String passw, E file)
			throws NullPointerException, NoSuchUserException, WrongPasswordException
	{
		if(file == null) throw new NullPointerException("Impossibile inserire nella collezione un file nullo!");
		AccediAccount(Owner, passw);
		
		//Aggiungo alla collezione facendo copy-in del file (se ne occupa il costruttore di DescrittoreFile)
		DescrittoreFile<E> nuovoFile = new DescrittoreFile<E>(file, Owner);
		
		DBFile.put(generaID(), nuovoFile);
		return true;
	}

	/*
	 * Restituisce un file della collezione che 1) eguaglia il file specificato 2) è accessibile dall'utente specificato,
	 * se ne esiste almeno uno e vengono rispettati i controlli di identità.
	 * In presenza di più file dotati di queste caratteristiche, ne viene restituito uno arbitrario.
	 * Se l'utente specificato è il creatore del file o ha ottenuto accesso in scrittura, eventuali modifiche al contenuto
	 * (tramite il riferimento restituito) si propagano sulla collezione;
	 * se invece ha accesso in sola lettura, eventuali modifiche al contenuto non si propagano e lasciano immutata la collezione.
	 */
	@Override
	public E get(String Owner, String passw, E file)
			throws NullPointerException, NoSuchUserException, WrongPasswordException, NoSuchFileException
	{
		if(file == null) throw new NullPointerException("Impossibile recuperare dalla collezione un file nullo!");
		AccediAccount(Owner, passw);
		
		for(DescrittoreFile<E> fileMemorizzato : DBFile.values())
			if(fileMemorizzato.contenutoUgualeA(file))
			{
				// File trovato: verifico che l'utente abbia i diritti d'accesso
				if(fileMemorizzato.puòScrivere(Owner))
					return fileMemorizzato.getContenuto(DescrittoreFile.TipoDiAccesso.LetturaEScrittura);
				else if(fileMemorizzato.puòLeggere(Owner))
					return fileMemorizzato.getContenuto(DescrittoreFile.TipoDiAccesso.SolaLettura);
			}
		
		// File non trovato, oppure l'utente non aveva i diritti
		throw new NoSuchFileException
			("Non trovato: il file indicato non esiste, o non fa parte di quelli a cui l'utente " + Owner + " ha accesso!");
	}

	/*
	 * Rimuove il diritto d'accesso al file specificato per l'utente specificato (a qualunque titolo vi avesse accesso),
	 * se vengono rispettati i controlli di identità; in presenza di duplicati, ne viene rimosso uno arbitrario
	 * tra quelli accessibili dall'utente che eguagliano il file passato per argomento.
	 * Se l'utente specificato era l'unico ad avere accesso al file, questo viene rimosso dalla collezione.
	 * Restituisce il contenuto del file rimosso; eventuali modifiche al riferimento restituito non si propagano alla collezione.
	 */
	@Override
	public E remove(String Owner, String passw, E file)
			throws NullPointerException, NoSuchUserException, WrongPasswordException, NoSuchFileException
	{
		if(file == null) throw new NullPointerException("Impossibile eliminare dalla collezione un file nullo!");
		AccediAccount(Owner, passw);
		
		for(java.util.Map.Entry<Integer, DescrittoreFile<E> > fileMemorizzato : DBFile.entrySet())
			if(fileMemorizzato.getValue().contenutoUgualeA(file) && fileMemorizzato.getValue().puòLeggere(Owner))
			{
				// Memorizzo copia del contenuto da restituire alla fine
				E retval = fileMemorizzato.getValue().getContenuto(DescrittoreFile.TipoDiAccesso.SolaLettura);
				
				if(fileMemorizzato.getValue().unicoAdAvereAccesso(Owner))
					// Owner è l'unico a poter accedere al file: rimuovo fisicamente il file dalla collezione
					DBFile.remove(fileMemorizzato.getKey());
				else
					// Ci sono altri utenti oltre ad Owner che possono accedere al file: lascio il file nella collezione rimuovendo solo il permesso
					fileMemorizzato.getValue().rimuoviDirittoAccesso(Owner);
				
				return retval;
			}
		
		throw new NoSuchFileException
			("Impossibile eliminare: il file indicato non esiste, o non fa parte di quelli a cui l'utente " + Owner + " ha accesso!");
	}

	/*
	 * Duplica il file specificato, ossia (se l'utente vi ha accesso almeno in lettura e vengono rispettati i controlli di identità)
	 * crea un nuovo file con contenuto identico al file originario, con l'utente specificato come creatore, e nessun altro
	 * utente autorizzato in lettura o lettura+scrittura. I diritti d'accesso del file originario non subiscono variazioni.
	 * In caso di pre-esistenza di doppioni, viene duplicato un file arbitrario tra quelli accessibili dall'utente
	 * che eguagliano il file passato per argomento.
	 */
	@Override
	public void copy(String Owner, String passw, E file)
			throws NullPointerException, NoSuchUserException, WrongPasswordException, NoSuchFileException
	{
		if(file == null) throw new NullPointerException("Impossibile duplicare un file nullo!");
		AccediAccount(Owner, passw);
		
		for(DescrittoreFile<E> fileMemorizzato : DBFile.values())
			if(fileMemorizzato.contenutoUgualeA(file) && fileMemorizzato.puòLeggere(Owner))
			{
				// Associo il contenuto a un nuovo file della collezione eseguendo una deep copy
				// (se ne occupa il costruttore della classe DescrittoreFile)
				E puntatoreAdOriginale = fileMemorizzato.getContenuto(DescrittoreFile.TipoDiAccesso.LetturaEScrittura); //Shallow copy
				
				DescrittoreFile<E> nuovoFile = new DescrittoreFile<E>(puntatoreAdOriginale, Owner); //Esegue la deep copy del puntatore
				DBFile.put(generaID(), nuovoFile);
				
				return; //Esco dal ciclo for-each: la copy viene effettuata su un solo file, anche se altri eguagliassero il parametro
			}
		
		throw new NoSuchFileException
			("Impossibile duplicare: il file indicato non esiste, o non fa parte di quelli a cui l'utente " + Owner + " ha accesso!");
	}

	/*
	 * Assegna all'utente Other il diritto di accesso in sola lettura per il file specificato,
	 * se l'utente Owner è il creatore del file e viene rispettato il controllo di identità;
	 * in presenza di duplicati, viene assegnato il permesso per un file arbitrario tra quelli che hanno Owner
	 * come creatore ed eguagliano il file passato per argomento.
	 */
	@Override
	public void shareR(String Owner, String passw, String Other, E file)
			throws NullPointerException, IllegalArgumentException, NoSuchUserException, WrongPasswordException, NoSuchFileException
	{
		// *** Eseguo controlli sugli input ***
		if(file == null) throw new NullPointerException("Impossibile condividere un file nullo!");
		if(Other == null) throw new NullPointerException("Impossibile condividere il file con un utente nullo!");
		
		AccediAccount(Owner, passw);
		if(Other.equals(Owner)) throw new IllegalArgumentException("Impossibile condividere un file con sé stessi!");
		
		if(!DBPassword.containsKey(Other)) throw new NoSuchUserException
		("Impossibile condividere: il nome utente del beneficiario (" + Other + ") non corrisponde ad alcun utente registrato!");
		
		// *** Eseguo la ricerca *** (solo tra i file di cui è proprietario)
		for(DescrittoreFile<E> fileMemorizzato : DBFile.values())
			if(fileMemorizzato.contenutoUgualeA(file) && fileMemorizzato.èCreatore(Owner))
			{
				// File trovato: assegno permesso
				fileMemorizzato.aggiungiCondivisione(Other, DescrittoreFile.TipoDiAccesso.SolaLettura);
				return; //Esco dal ciclo for-each: il permesso viene assegnato ad un solo file, anche se altri eguagliassero il parametro
			}
		
		// File non trovato: sollevo eccezione
		throw new NoSuchFileException
			("Impossibile condividere: il file indicato non esiste, o l'utente " + Owner + " non ne è il proprietario!");
	}

	/*
	 * Assegna all'utente Other il diritto di accesso in lettura e scrittura per il file specificato,
	 * se l'utente Owner è il creatore del file e viene rispettato il controllo di identità;
	 * in presenza di duplicati, viene assegnato il permesso per un file arbitrario tra quelli che hanno Owner
	 * come creatore ed eguagliano il file passato per argomento.
	 */
	@Override
	public void shareW(String Owner, String passw, String Other, E file)
			throws NullPointerException, IllegalArgumentException, NoSuchUserException, WrongPasswordException, NoSuchFileException
	{
		// *** Eseguo controlli sugli input ***
		if(file == null) throw new NullPointerException("Impossibile condividere un file nullo!");
		if(Other == null) throw new NullPointerException("Impossibile condividere il file con un utente nullo!");
		
		AccediAccount(Owner, passw);
		if(Other.equals(Owner)) throw new IllegalArgumentException("Impossibile condividere un file con sé stessi!");
		
		if(!DBPassword.containsKey(Other)) throw new NoSuchUserException
		("Impossibile condividere: il nome utente del beneficiario (" + Other + ") non corrisponde ad alcun utente registrato!");
		
		// *** Eseguo la ricerca *** (solo tra i file di cui è proprietario)
		for(DescrittoreFile<E> fileMemorizzato : DBFile.values())
			if(fileMemorizzato.contenutoUgualeA(file) && fileMemorizzato.èCreatore(Owner))
			{
				// File trovato: assegno permesso
				fileMemorizzato.aggiungiCondivisione(Other, DescrittoreFile.TipoDiAccesso.LetturaEScrittura);
				return; //Esco dal ciclo for-each: il permesso viene assegnato ad un solo file, anche se altri eguagliassero il parametro
			}
		
		// File non trovato: sollevo eccezione
		throw new NoSuchFileException
			("Impossibile condividere: il file indicato non esiste, o l'utente " + Owner + " non ne è il proprietario!");
	}

	/*
	 * Restituisce un iteratore (senza remove) che genera in ordine arbitrario tutti i file a cui l'utente ha accesso
	 * a qualunque titolo (creatore/lettore/scrittore), se vengono rispettati i controlli di identità.
	 * Se l'utente specificato è il creatore del file generato dall'iteratore o ha ottenuto accesso in scrittura,
	 * eventuali modifiche al contenuto (tramite il riferimento restituito) si propagano sulla collezione;
	 * se invece ha accesso in sola lettura, eventuali modifiche al contenuto non si propagano e lasciano immutata la collezione.
	 */
	@Override
	public Iterator<E> getIterator(String Owner, String passw)
			throws NullPointerException, NoSuchUserException, WrongPasswordException
	{
		AccediAccount(Owner, passw);
		
		return new IteratoreFileUtente(Owner);
	}
	
	/* 
	 * =========
	 * OVERVIEW:
	 * =========
 	 * La classe annidata IteratoreFileUtente definisce l'iteratore della
	 * sottocollezione costituita dai soli file a cui l'utente ha accesso,
	 * a qualunque titolo (creatore/lettore/scrittore).
	 * 
	 * Il suo funzionamento si basa sull'utilizzo dell'iteratore predefinito dell'insieme dei file DBFile (iteratoreTuttiFile),
	 * di cui questo iteratore si limita a scartare gli elementi della sequenza non pertinenti all'utente desiderato.
	 * 
	 * Ogni elemento viene pre-generato al passo precedente di quello in cui è restituito, in modo tale da sapere
	 * in anticipo se la sequenza dei file dell'utente è terminata o meno (visto che non si dispone di un conteggio
	 * dei file a cui l'utente ha accesso):
	 * ossia il costruttore genera il 1° elemento, la prima getNext restituisce il 1° elemento e genera il 2°, ecc.
	 * 
	 * ===============================
	 * INVARIANTE DI RAPPRESENTAZIONE:
 	 * ===============================
 	 * prossimoFileDaRestituire.puòLeggere(username) == true &&
 	 * i valori assunti da prossimoFileDaRestituire sono un sottoinsieme di quelli generati da iteratoreTuttiFile &&
 	 * prossimoFileDaRestituire == null se e solo se sono già stati generati tutti gli n file accessibili dall'utente "username"
 	 * 
 	 * =======================
 	 * FUNZIONE DI ASTRAZIONE:
 	 * =======================
 	 * FA = this rappresenta la sequenza [ o1, o2, ..., on ] tale che:
 	 * 
 	 * ogni oi è un file (tipo di dato <E>) a cui username ha diritto d'accesso almeno in lettura,
 	 * n è il numero totale dei files accessibili dall'utente (creati o condivisi con lui in lettura o scrittura),
 	 * e tutti i file con utente appartenente a ( {creatore-i} unione autorizzatiR-i unione autorizzatiRW-i ) occorrono nella sequenza
	 */
	private class IteratoreFileUtente implements Iterator<E>
	{
		// Memorizza il nome dell'utenza di cui si devono restituire i file
		private final String username;

		// Iteratore predefinito della collezione completa (comprensiva di tutti i file, anche quelli degli altri utenti), che viene usato come "base"
		private final Iterator<DescrittoreFile<E>> iteratoreTuttiFile = DBFile.values().iterator();
		
		// Memorizza il successivo elemento della sequenza non ancora generato, da restituire alla prossima invocazione di next().
		// Vale null se tutti gli elementi sono già stati generati. Viene modificato dal metodo generaProssimo().
		private DescrittoreFile<E> prossimoFileDaRestituire;
		
		
		// Metodo costruttore. Crea l'oggetto iteratore relativo all'utente indicato e pre-genera il primo elemento della sequenza.
		public IteratoreFileUtente(String username)
		{
			this.username = username;
			generaProssimo(); // Genero l'elemento da restituire alla prossima invocazione di next()
		}
		
		@Override
		public boolean hasNext()
		{
			return (prossimoFileDaRestituire != null);
		}
		
		@Override
		public E next()
		{
			if(prossimoFileDaRestituire == null)
				throw new NoSuchElementException
				("L'iteratore ha già generato tutti i file a cui l'utente ha accesso. Non ce ne sono altri da generare!");

			// Restituisco l'elemento individuato al passo precedente
			E retval;
			if(prossimoFileDaRestituire.puòScrivere(username))	//A seconda dei diritti dell'utente, restituisco copia modificabile o meno
				retval = prossimoFileDaRestituire.getContenuto(DescrittoreFile.TipoDiAccesso.LetturaEScrittura);
			else
				retval = prossimoFileDaRestituire.getContenuto(DescrittoreFile.TipoDiAccesso.SolaLettura);
				
			generaProssimo(); // Genero l'elemento da restituire al passo successivo (prossima invocazione di next)
				
			return retval;
		}
		
		/* 
		 * Genera il prossimo elemento della sequenza, da restituire alla prossima invocazione di next(),
		 * e lo memorizza in prossimoFileDaRestituire.
		 * Se gli elementi da generare sono finiti, memorizza null in prossimoFileDaRestituire.
		 */
		private void generaProssimo()
		{
			/* Entra in un ciclo in cui vengono generati nuovi elementi dell'iteratore di tutti i file,
			 * fermandosi solo quando ne trova uno a cui l'utente ha accesso,
			 * o quando finiscono gli elementi da generare.
			 */
			do
			{
				if(iteratoreTuttiFile.hasNext())
					prossimoFileDaRestituire = iteratoreTuttiFile.next();
				else
					prossimoFileDaRestituire = null;
			}
			while(prossimoFileDaRestituire != null && !prossimoFileDaRestituire.puòLeggere(username));
		}
		
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("Questo iteratore non supporta la remove");
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((DBFile == null) ? 0 : DBFile.hashCode());
		result = prime * result
				+ ((DBPassword == null) ? 0 : DBPassword.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof HashSecureFileContainer)) return false;
		
		HashSecureFileContainer<E> other = (HashSecureFileContainer<E>) obj;
		
		if (DBFile == null)
		{
			if (other.DBFile != null) return false;
		}
		else if (!DBFile.equals(other.DBFile)) return false;
		if (DBPassword == null)
		{
			if (other.DBPassword != null) return false;
		}
		else if (!DBPassword.equals(other.DBPassword)) return false;
		return true;
	}
}
