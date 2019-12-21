/*
 * Università di Pisa - Corso di Programmazione II (273AA)
 * Progetto Java - Sessione estiva-autunnale A.A. 2018/2019 (appello settembre 2019)
 * Candidato Alessandro Antonelli (matricola 507264, corso A)
 */

package pr2.impl1;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import pr2.impl1.DescrittoreUtente;
import pr2.Clonatore;
import pr2.SecureFileContainer;
import pr2.exceptions.*;

/* =========
 * OVERVIEW:
 * =========
 * ArraySecureFileContainer<E> è una collezione mutabile illimitata di:
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
 * DBUtenti != null && DBUtenti.size >= 0 && DBUtenti.KeySet non contiene NULL né duplicati né stringhe vuote &&
 * DBUtenti.values non contiene NULL
 * 
 * Per ogni v appartenente a DBUtenti.values:
 * v.fileCreati != null && v.fileRicevutiInLettura != null && v.fileRicevutiInScrittura != null &&
 * v.username != null && v.username != stringa vuota && v.password != null && v.password != stringa vuota && v.password != v.username 
 * 
 * =======================
 * FUNZIONE DI ASTRAZIONE:
 * =======================
 * FA = this rappresenta la coppia < U, F > con:
 * 
 * U = { < nome, psw > | nome appartiene a DBUtenti.KeySet && psw == DBUtenti.values().password }
 * 
 * F = il vettore costituito da tanti elementi (file) quanti ne ha l'unione disgiunta (nel senso della pointer-equality)
 *     degli insiemi DBUtenti.values().fileCreati, DBUtenti.values().fileRicevutiInLettura e
 *     e DBUtenti.values().fileRicevutiInScrittura di tutti gli utenti.
 *     Per ciascun file:
 *     - il campo contenuto è costituito dal riferimento memorizzato in uno degli insiemi sopracitati;
 *     - l'utente creatore è colui che memorizza il riferimento al dato in DBUtenti.values().fileCreati;
 *     - gli utenti appartenenti ad autorizzatiR sono quelli che memorizzano un riferimento al dato in DBUtenti.values().fileRicevutiInLettura;
 *     - gli utenti appartenenti ad autorizzatiRW sono quelli che memorizzano un riferimento al dato in DBUtenti.values().fileRicevutiInScrittura.
 */
public class ArraySecureFileContainer<E extends Serializable> implements SecureFileContainer<E>
{
	// Struttura in cui vengono memorizzati sia gli utenti che i riferimenti ai file
	private final HashMap<String, DescrittoreUtente<E> > DBUtenti;
	
	// Metodo costruttore: produce una nuova collezione vuota (senza utenti né file)
	public ArraySecureFileContainer()
	{
		DBUtenti = new HashMap<String, DescrittoreUtente<E>>();
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
		
		if(DBUtenti.containsKey(Id)) throw new UsernameUnavailableException
		("L'username che hai scelto (" + Id + ") non è disponibile perché già in uso da altro utente. Riprova scegliendone un altro...");
		
		DescrittoreUtente<E> nuovoDescrittore = new DescrittoreUtente<E>(passw, Id);
		DBUtenti.put(Id, nuovoDescrittore);
	}
	
	
	/*
	 *  Metodo ausiliare che esegue le operazioni di controllo delle credenziali (legalità dei valori,
	 *  esistenza dell'account, correttezza della password). In caso di esito negativo solleva le corrispondenti eccezioni.
	 *  Altrimenti restituisce un riferimento al descrittore dell'utente.
	 */
	private DescrittoreUtente<E> AccediAccount(String Owner, String passw)
			throws NullPointerException, NoSuchUserException, WrongPasswordException
	{
		if(Owner == null || passw == null) throw new NullPointerException
			("Impossibile accedere all'account indicando username o password nulla!");
		
		// Verifica esistenza account
		DescrittoreUtente<E> descrittore = DBUtenti.get(Owner);
		
		if(descrittore == null) //Nella map non è stata trovata un'associazione per la chiave (username)
			throw new NoSuchUserException
			("Impossibile accedere: il nome utente inserito (" + Owner + ") non corrisponde ad alcun utente registrato!");
		
		// Verifica password
		if(!descrittore.passwordCorretta(passw)) throw new WrongPasswordException
			("Accesso all'account " + Owner + " negato: la password inserita non è corretta!");
		
		return descrittore;
	}
	
	/*
	 * Restituisce il numero dei file della collezione a cui l'utente specificato ha accesso
	 * a qualunque titolo (creatore/lettore/scrittore), se vengono rispettati i controlli di identità
	 */
	@Override
	public int getSize(String Owner, String passw)
			throws NullPointerException, NoSuchUserException, WrongPasswordException
	{
		DescrittoreUtente<E> descrittore = AccediAccount(Owner, passw);
		
		int conteggio = 0;
		conteggio += descrittore.fileCreati.size();
		conteggio += descrittore.fileRicevutiInLettura.size();
		conteggio += descrittore.fileRicevutiInScrittura.size();
		return conteggio;
	}

	// Inserisce il file nella collezione, se vengono rispettati i controlli di identità
	@Override
	public boolean put(String Owner, String passw, E file)
			throws NullPointerException, NoSuchUserException, WrongPasswordException
	{
		if(file == null) throw new NullPointerException("Impossibile inserire nella collezione un file nullo!");
		DescrittoreUtente<E> descrittore = AccediAccount(Owner, passw);
		
		//Aggiungo alla collezione facendo copy-in del file
		Clonatore<E> cloner = new Clonatore<E>();
		descrittore.fileCreati.add(cloner.deepCopyConSerializzazione(file));
		return true;
	}

	/*
	 * Restituisce un file della collezione che 1) eguaglia il file specificato 2) è accessibile dall'utente specificato,
	 * se ne esiste almeno uno e vengono rispettati i controlli di identità.
	 * In presenza di più file dotati di queste caratteristiche, ne viene restituito uno arbitrario, dando la priorità prima ai
	 * file creati e poi a quelli di cui si ha avuto accesso in sola lettura.
	 * Se l'utente specificato è il creatore del file o ha ottenuto accesso in scrittura, eventuali modifiche al contenuto
	 * (tramite il riferimento restituito) si propagano sulla collezione;
	 * se invece ha accesso in sola lettura, eventuali modifiche al contenuto non si propagano e lasciano immutata la collezione.
	 */
	@Override
	public E get(String Owner, String passw, E file)
			throws NullPointerException, NoSuchUserException, WrongPasswordException, NoSuchFileException
	{
		if(file == null) throw new NullPointerException("Impossibile recuperare dalla collezione un file nullo!");
		DescrittoreUtente<E> descrittore = AccediAccount(Owner, passw);
		int pos;
		
		if((pos = descrittore.fileCreati.indexOf(file)) != -1)
			// Get di un file in lettura e scrittura: restituisco shallow copy
			return descrittore.fileCreati.get(pos);
		else if((pos = descrittore.fileRicevutiInLettura.indexOf(file)) != -1)
		{
			// Get di un file in sola lettura: restituisco una deep copy
			Clonatore<E> cloner = new Clonatore<E>();
			return cloner.deepCopyConSerializzazione(descrittore.fileRicevutiInLettura.get(pos));
		}
		else if((pos = descrittore.fileRicevutiInScrittura.indexOf(file)) != -1)
			// Get di un file in lettura e scrittura: restituisco shallow copy
			return descrittore.fileRicevutiInScrittura.get(pos);
		else
			throw new NoSuchFileException
			("Non trovato: il file indicato non esiste, o non fa parte di quelli a cui l'utente " + Owner + " ha accesso!");
	}

	/*
	 * Rimuove il diritto d'accesso al file specificato per l'utente specificato (a qualunque titolo vi avesse accesso),
	 * se vengono rispettati i controlli di identità; in presenza di duplicati, ne viene rimosso uno arbitrario
	 * tra quelli accessibili dall'utente che eguagliano il file passato per argomento, dando la priorità prima ai
	 * file creati e poi a quelli di cui si ha avuto accesso in sola lettura.
	 * Se l'utente specificato era l'unico ad avere accesso al file, questo viene rimosso dalla collezione.
	 * Restituisce il contenuto del file rimosso; eventuali modifiche al riferimento restituito non si propagano alla collezione.
	 */
	@Override
	public E remove(String Owner, String passw, E file)
			throws NullPointerException, NoSuchUserException, WrongPasswordException, NoSuchFileException
	{
		if(file == null) throw new NullPointerException("Impossibile eliminare dalla collezione un file nullo!");
		DescrittoreUtente<E> descrittore = AccediAccount(Owner, passw);
		
		Clonatore<E> cloner = new Clonatore<E>();
		int pos;
		
		if((pos = descrittore.fileCreati.indexOf(file)) != -1)
			// Trovato tra i file creati
			return cloner.deepCopyConSerializzazione(descrittore.fileCreati.remove(pos));
		else if((pos = descrittore.fileRicevutiInLettura.indexOf(file)) != -1)
			// Trovato tra i file ricevuti in lettura
			return cloner.deepCopyConSerializzazione(descrittore.fileRicevutiInLettura.remove(pos));
		else if((pos = descrittore.fileRicevutiInScrittura.indexOf(file)) != -1)
			// Trovato tra i file ricevuti in lettura+scrittura
			return cloner.deepCopyConSerializzazione(descrittore.fileRicevutiInScrittura.remove(pos));
		else
			// Non trovato
			throw new NoSuchFileException
			("Impossibile eliminare: il file indicato non esiste, o non fa parte di quelli a cui l'utente " + Owner + " ha accesso!");
	}

	/*
	 * Duplica il file specificato, ossia (se l'utente vi ha accesso almeno in lettura e vengono rispettati i controlli di identità)
	 * crea un nuovo file con contenuto identico al file originario, con l'utente specificato come creatore, e nessun altro
	 * utente autorizzato in lettura o lettura+scrittura. I diritti d'accesso del file originario non subiscono variazioni.
	 * In caso di pre-esistenza di doppioni, viene duplicato un file arbitrario tra quelli accessibili dall'utente
	 * che eguagliano il file passato per argomento, dando la priorità ai file di cui è proprietario e poi ai file di cui ha accesso in sola lettura.
	 */
	@Override
	public void copy(String Owner, String passw, E file)
			throws NullPointerException, NoSuchUserException, WrongPasswordException, NoSuchFileException
	{
		if(file == null) throw new NullPointerException("Impossibile duplicare un file nullo!");
		DescrittoreUtente<E> descrittore = AccediAccount(Owner, passw);
		
		// Individuo il file originale da copiare
		E puntatoreAllOriginale;
		int pos;
		
		if((pos = descrittore.fileCreati.indexOf(file)) != -1)
			// Trovato tra i file creati
			puntatoreAllOriginale = descrittore.fileCreati.get(pos);
		else if((pos = descrittore.fileRicevutiInLettura.indexOf(file)) != -1)
			// Trovato tra i file ricevuti in lettura
			puntatoreAllOriginale = descrittore.fileRicevutiInLettura.get(pos);
		else if((pos = descrittore.fileRicevutiInScrittura.indexOf(file)) != -1)
			// Trovato tra i file ricevuti in lettura+scrittura
			puntatoreAllOriginale = descrittore.fileRicevutiInScrittura.get(pos);
		else
			// Non trovato
			throw new NoSuchFileException
			("Impossibile duplicare: il file indicato non esiste, o non fa parte di quelli a cui l'utente " + Owner + " ha accesso!");
		
		// Eseguo la copia, inserendola tra i file di cui è proprietario
		Clonatore<E> cloner = new Clonatore<E>();
		E clonato = cloner.deepCopyConSerializzazione(puntatoreAllOriginale);
		descrittore.fileCreati.add(clonato);
	}

	/*
	 * Assegna all'utente Other il diritto di accesso in sola lettura per il file specificato,
	 * se l'utente Owner è il creatore del file e viene rispettato il controllo di identità;
	 * in presenza di duplicati, viene assegnato il permesso per un file arbitrario tra quelli che hanno Owner
	 * come creatore ed eguagliano il file passato per argomento, dando la priorità prima ai
	 * file creati e poi a quelli di cui si ha avuto accesso in sola lettura.
	 */
	@Override
	public void shareR(String Owner, String passw, String Other, E file)
			throws NullPointerException, IllegalArgumentException, NoSuchUserException, WrongPasswordException, NoSuchFileException
	{
		// *** Eseguo controlli sugli input ***
		if(file == null) throw new NullPointerException("Impossibile condividere un file nullo!");
		if(Other == null) throw new NullPointerException("Impossibile condividere il file con un utente nullo!");
		
		DescrittoreUtente<E> descrProprietario = AccediAccount(Owner, passw);
		if(Other.equals(Owner)) throw new IllegalArgumentException("Impossibile condividere un file con sé stessi!");
		
		DescrittoreUtente<E> descrBeneficiario = DBUtenti.get(Other);
		
		if(descrBeneficiario == null) // Utente beneficiario inesistente: eccezione
			throw new NoSuchUserException
			("Impossibile condividere: il nome utente del beneficiario (" + Other + ") non corrisponde ad alcun utente registrato!");
		
		// *** Eseguo la ricerca *** (solo nella lista dei file di cui è proprietario)
		int pos = descrProprietario.fileCreati.indexOf(file);
		
		if(pos == -1)	// File non trovato: eccezione
			throw new NoSuchFileException
			("Impossibile condividere: il file indicato non esiste, o l'utente " + Owner + " non ne è il proprietario!");
		else			// File trovato: se non c'era già, lo aggiungo alla lista del beneficiario
			if(!descrBeneficiario.fileRicevutiInLettura.contains(descrProprietario.fileCreati.get(pos)))
				descrBeneficiario.fileRicevutiInLettura.add(descrProprietario.fileCreati.get(pos));
	}

	/*
	 * Assegna all'utente Other il diritto di accesso in lettura e scrittura per il file specificato,
	 * se l'utente Owner è il creatore del file e viene rispettato il controllo di identità;
	 * in presenza di duplicati, viene assegnato il permesso per un file arbitrario tra quelli che hanno Owner
	 * come creatore ed eguagliano il file passato per argomento, dando la priorità prima ai
	 * file creati e poi a quelli di cui si ha avuto accesso in sola lettura.
	 */
	@Override
	public void shareW(String Owner, String passw, String Other, E file)
			throws NullPointerException, IllegalArgumentException, NoSuchUserException, WrongPasswordException, NoSuchFileException
	{
		// *** Eseguo controlli sugli input ***
		if(file == null) throw new NullPointerException("Impossibile condividere un file nullo!");
		if(Other == null) throw new NullPointerException("Impossibile condividere il file con un utente nullo!");
		
		DescrittoreUtente<E> descrProprietario = AccediAccount(Owner, passw);
		if(Other.equals(Owner)) throw new IllegalArgumentException("Impossibile condividere un file con sé stessi!");
		
		DescrittoreUtente<E> descrBeneficiario = DBUtenti.get(Other);
		
		if(descrBeneficiario == null) // Utente beneficiario inesistente: eccezione
			throw new NoSuchUserException
			("Impossibile condividere: il nome utente del beneficiario (" + Other + ") non corrisponde ad alcun utente registrato!");
		
		// *** Eseguo la ricerca *** (solo nella lista dei file di cui è proprietario)
		int pos = descrProprietario.fileCreati.indexOf(file);
		
		if(pos == -1)	// File non trovato: eccezione
			throw new NoSuchFileException
			("Impossibile condividere: il file indicato non esiste, o l'utente " + Owner + " non ne è il proprietario!");
		else			// File trovato: se non c'era già, lo aggiungo alla lista del beneficiario
			if(!descrBeneficiario.fileRicevutiInScrittura.contains(descrProprietario.fileCreati.get(pos)))
				descrBeneficiario.fileRicevutiInScrittura.add(descrProprietario.fileCreati.get(pos));
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
		DescrittoreUtente<E> descrittore = AccediAccount(Owner, passw);
		
		return descrittore.iterator();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((DBUtenti == null) ? 0 : DBUtenti.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ArraySecureFileContainer)) return false;
		
		ArraySecureFileContainer<E> other = (ArraySecureFileContainer<E>) obj;
		
		if(other.DBUtenti != null && this.DBUtenti != null)
			return other.DBUtenti.equals(this.DBUtenti);
		else return false;
	}
}
