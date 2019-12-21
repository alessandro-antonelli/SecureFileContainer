/*
 * Università di Pisa - Corso di Programmazione II (273AA)
 * Progetto Java - Sessione estiva-autunnale A.A. 2018/2019 (appello settembre 2019)
 * Candidato Alessandro Antonelli (matricola 507264, corso A)
 */

package pr2;

import java.io.Serializable;
import java.util.Iterator;
import pr2.exceptions.*;

/* =========
 * OVERVIEW:
 * =========
 * SecureFileContainer<E> è una collezione mutabile illimitata di:
 * - utenze protette da password (immutabili e con duplicati non ammessi)
 * - file (mutabili e con duplicati ammessi),
 *   composti da un contenuto mutabile, serializzabile e confrontabile di tipo E,
 *   e da informazioni sui diritti d'accesso delle utenze al file.
 * 
 * Ad ogni file è associata al più una utenza creatrice,
 * un insieme di zero o più utenti con accesso in sola lettura (R), e
 * un insieme di zero o più utenti con accesso in lettura e scrittura (RW).
 * 
 * ================
 * TYPICAL ELEMENT:
 * ================
 * L'elemento tipico è una coppia < U, F > dove:
 * - U = {utente-1, ... , utente-n } è un insieme di n elementi
 *   con utente-i = coppia <username-i, password-i>
 * - F = [ file-1, ... , file-m ] è un vettore di m elementi
 *   con file-i = quadrupla <contenuto-i, creatore-i, autorizzatiR-i, autorizzatiRW-i>
 * 
 * 			dove:
 * (1) username-i e password-i sono non vuoti
 *     (per ogni i tale che 0 < i <= n)
 * (2) username-i != username-j
 *     (per ogni i, j tali che 0 < (i, j) <= n e i != j)
 * (3) contenuto-i è un oggetto di tipo E
 *     (per ogni i tale che 0 < i <= m)
 * (4) creatore-i è un utente appartenente a { username-j | 1 <= j <= n } oppure è vuoto
 *     (per ogni i tale che 0 < i <= m)
 * (5) autorizzatiR-i e autorizzatiRW-i sono sottoinsiemi di { username-j | 1 <= j <= n }
 *     (per ogni i tale che 0 < i <= m)
 * (6) autorizzatiR-i, autorizzatiRW-i e {creatore-i} sono insiemi disgiunti a due a due
 *     (per ogni i tale che 0 < i <= m)
 */
public interface SecureFileContainer<E extends Serializable>
{ 
	/**
	 * Crea l'identità di un nuovo utente della collezione
	 * 
	 * @param		Id		Username dell'utenza da creare
	 * @param		passw	Password dell'utenza da creare
	 * 
	 * @requires	passw != Id && Id != null && passw != null && Id != "" && passw != "" &&
	 * 				Id != username-i  != null && passw != null && Id != "" && passw != "" &&
	 * 				Id != username-i per ogni utente-i appartenente a U
	 * @effects		this.U(post) = this.U(pre) unione { <Id, passw> }
	 * @modifies	this.U
	 * @return		none
	 * 
	 * @throws	NullPointerException (unchecked)		se Id == null || passw == null
	 * @throws	IllegalArgumentException (unchecked)	se Id == "" || passw == "" || passw == Id
	 * @throws	UsernameUnavailableException (checked)	se esiste utente-i appartenente a U tale che username-i == Id
	 */
	public void createUser(String Id, String passw)
			throws NullPointerException, IllegalArgumentException, UsernameUnavailableException;

	
	/**
	 * Restituisce il numero dei file della collezione a cui l'utente specificato ha accesso
	 * a qualunque titolo (creatore/lettore/scrittore), se vengono rispettati i controlli di identità
	 * 
	 * @param		Owner	Username dell'utente di cui si vuole conoscere il conteggio
	 * @param		passw	Password dell'utente di cui si vuole conoscere il conteggio
	 * 
	 * @requires	Owner != null && passw != null &&
	 * 				esiste utente-i appartenente a U tale che (username-i == Owner && password-i == passw)
	 * @effects		this(post) == this(pre)
	 * @modifies	none
	 * @return		cardinalità di { file-i : Owner  == creatore-i || Owner appartiene a autorizzatiR-i || Owner appartiene a autorizzatiRW-i }
	 * 
	 * @throws		NullPointerException (unchecked)	se Owner == null || passw == null
	 * @throws		NoSuchUserException (checked)		se username-i != Owner (per ogni utente-i appartenente a U)
	 * @throws		WrongPasswordException (checked)	se password-i != passw (con i tale che username-i == Owner)
	 */
	public int getSize(String Owner, String passw)
			throws NullPointerException, NoSuchUserException, WrongPasswordException;
	
	
	/**
	 * Inserisce il file nella collezione, se vengono rispettati i controlli di identità
	 * 
	 * @param	Owner	Username dell'utente che risulterà creatore del file
	 * @param	passw	Password dell'utente che risulterà creatore del file
	 * @param	file	File da inserire nella collezione
	 * 
	 * @requires	Owner != null && passw != null && file != null &&
	 * 				esiste utente-i appartenente a U tale che (username-i == Owner && password-i == passw)
	 * @effects		this.F(post) = this.F(pre) unione { <file, Owner, insieme vuoto, insieme vuoto> }
	 * @modifies	this.F
	 * @return		true
	 * 
	 * @throws	NullPointerException (unchecked)	se Owner == null || passw == null || file == null
	 * @throws	NoSuchUserException (checked)		se username-i != Owner (per ogni utente-i appartenente a U)
	 * @throws	WrongPasswordException (checked)	se password-i != passw (con i tale che username-i == Owner)
	 */
	public boolean put(String Owner, String passw, E file)
			throws NullPointerException, NoSuchUserException, WrongPasswordException;
	
	
	/**
	 * Restituisce un file della collezione che 1) eguaglia il file specificato 2) è accessibile dall'utente specificato,
	 * se ne esiste almeno uno e vengono rispettati i controlli di identità.
	 * In presenza di più file dotati di queste caratteristiche, ne viene restituito uno arbitrario.
	 * Se l'utente specificato è il creatore del file o ha ottenuto accesso in scrittura, eventuali modifiche al contenuto
	 * (tramite il riferimento restituito) si propagano sulla collezione;
	 * se invece ha accesso in sola lettura, eventuali modifiche al contenuto non si propagano e lasciano immutata la collezione.
	 * 
	 * @param	Owner	Username dell'utente che ha accesso al file
	 * @param	passw	Password dell'utente che ha accesso al file
	 * @param	file	File che si vuole recuperare
	 * 
	 * @requires	Owner != null && passw != null && file != null &&
	 * 				esiste utente-i appartenente a U tale che (username-i == Owner && password-i == passw) &&
	 * 				esiste file-i appartenente a F tale che file.equals(contenuto-i) e
	 * 				e Owner appartiene a ( {creatore-i} unione autorizzatiR-i unione autorizzatiRW-i )
	 * @effects		this(post) == this(pre) &&
	 * 				se (Owner appartiene a autorizzatiR-i && valore_restituito_dal_metodo viene modificato) allora this non cambia
	 * @modifies	none
	 * @return		contenuto-i
	 * 				[ con i tale che Owner appartiene a ( {creatore-i} unione autorizzatiR-i unione autorizzatiRW-i ) e
	 * 				  e file.equals(contenuto-i) ]
	 * 				in versione tale che le modifiche al valore restituito:
	 * 				NON si propagano a contenuto-i		se Owner appartiene ad autorizzatiR-i
	 * 				SI propagano a contenuto-i			altrimenti
	 * 
	 * @throws	NullPointerException (unchecked)	se Owner == null || passw == null || file == null
	 * @throws	NoSuchUserException (checked)		se username-i != Owner (per ogni utente-i appartenente a U)
	 * @throws	WrongPasswordException (checked)	se password-i != passw (con i tale che username-i == Owner)
	 * @throws	NoSuchFileException (checked)		se non esiste file-i appartenente a F tale che file.equals(contenuto-i) e
	 * 												e Owner appartiene a ( {creatore-i} unione autorizzatiR-i unione autorizzatiRW-i )
	 */
	public E get(String Owner, String passw, E file)
			throws NullPointerException, NoSuchUserException, WrongPasswordException, NoSuchFileException;
	
	
	/**
	 * Rimuove il diritto d'accesso al file specificato per l'utente specificato (a qualunque titolo vi avesse accesso),
	 * se vengono rispettati i controlli di identità; in presenza di duplicati, ne viene rimosso uno arbitrario
	 * tra quelli accessibili dall'utente che eguagliano il file passato per argomento.
	 * Se l'utente specificato era l'unico ad avere accesso al file, questo viene rimosso dalla collezione.
	 * Restituisce il contenuto del file rimosso; eventuali modifiche al riferimento restituito non si propagano alla collezione.
	 * 
	 * @param	Owner	Username dell'utente che ha accesso al file
	 * @param	passw	Password dell'utente che ha accesso al file
	 * @param	file	File che si desidera rimuovere
	 * 
	 * @requires	Owner != null && passw != null && file != null
	 * 				esiste utente-i appartenente a U tale che (username-i == Owner && password-i == passw) &&
	 * 				esiste file-i appartenente a F tale che file.equals(contenuto-i) e
	 * 				e Owner appartiene a ( {creatore-i} unione autorizzatiR-i unione autorizzatiRW-i )
	 * 
	 * @effects		se cardinalità( {creatore-i} unione autorizzatiR-i unione autorizzatiRW-i ) == 1
	 * 					allora this.F(post) = this.F(pre) \ {x}	
	 * 				altrimenti:
	 *		 				se Owner appartiene ad autorizzatiR-i
	 *		 					allora autorizzatiR-i(post) = autorizzatiR-i(pre) \ {Owner}
	 *		 				se Owner appartiene ad autorizzatiRW-i
	 *		 					allora autorizzatiRW-i(post) = autorizzatiRW-i(pre) \ {Owner}
	 *		 				se Owner == creatore-i
	 *		 					allora creatore-i(post) = null

	 *				dove x = file-i appartenente a F tale che file.equals(contenuto-i) e
	 * 				e Owner appartiene a ( {creatore-i} unione autorizzatiR-i unione autorizzatiRW-i )
	 * 
	 * @modifies	this.F
	 * 
	 * @return		contenuto-i
	 * 
	 * @throws	NullPointerException (unchecked)	se Owner == null || passw == null || file == null
	 * @throws	NoSuchUserException (checked)		se username-i != Owner (per ogni utente-i appartenente a U)
	 * @throws	WrongPasswordException (checked)	se password-i != passw (con i tale che username-i == Owner)
	 * @throws	NoSuchFileException (checked)		se non esiste file-i appartenente a F tale che file.equals(contenuto-i) e
	 * 												e Owner appartiene a ( {creatore-i} unione autorizzatiR-i unione autorizzatiRW-i )
	 */
	public E remove(String Owner, String passw, E file)
			throws NullPointerException, NoSuchUserException, WrongPasswordException, NoSuchFileException;
	
	
	/**
	 * Duplica il file specificato, ossia (se l'utente vi ha accesso almeno in lettura e vengono rispettati i controlli di identità)
	 * crea un nuovo file con contenuto identico al file originario, con l'utente specificato come creatore, e nessun altro
	 * utente autorizzato in lettura o lettura+scrittura. I diritti d'accesso del file originario non subiscono variazioni.
	 * In caso di pre-esistenza di doppioni, viene duplicato un file arbitrario tra quelli accessibili dall'utente
	 * che eguagliano il file passato per argomento.
	 * 
	 * @param	Owner	Username dell'utente che ha accesso al file
	 * @param	passw	Password dell'utente che ha accesso al file
	 * @param	file	File che si desidera duplicare
	 * 
	 * @requires	Owner != null && passw != null && file != null &&
	 * 				esiste utente-i appartenente a U tale che (username-i == Owner && password-i == passw) &&
	 * 				esiste file-i appartenente a F tale che file.equals(contenuto-i) e
	 * 				e Owner appartiene a ( {creatore-i} unione autorizzatiR-i unione autorizzatiRW-i )
	 * 
	 * @effects		this.F(post) = this.F(pre) unione { <x, Owner, insieme vuoto, insieme vuoto> }
	 * 				
	 * 				dove x = deep copy di contenuto-i,
	 * 				con i tale che file.equals(contenuto-i) e
	 * 				e Owner appartiene a ( {creatore-i} unione autorizzatiR-i unione autorizzatiRW-i )
	 * 
	 * @modifies	this.F
	 * @return		none
	 * 
	 * @throws	NullPointerException (unchecked)	se Owner == null || passw == null || file == null
	 * @throws	NoSuchUserException (checked)		se username-i != Owner (per ogni utente-i appartenente a U)
	 * @throws	WrongPasswordException (checked)	se password-i != passw (con i tale che username-i == Owner)
	 * @throws	NoSuchFileException (checked)		se non esiste file-i appartenente a F tale che file.equals(contenuto-i) e
	 * 												e Owner appartiene a ( {creatore-i} unione autorizzatiR-i unione autorizzatiRW-i )
	 */
	public void copy(String Owner, String passw, E file)
			throws NullPointerException, NoSuchUserException, WrongPasswordException, NoSuchFileException;
	
	
	/**
	 * Assegna all'utente Other il diritto di accesso in sola lettura per il file specificato,
	 * se l'utente Owner è il creatore del file e viene rispettato il controllo di identità;
	 * in presenza di duplicati, viene assegnato il permesso per un file arbitrario tra quelli che hanno Owner
	 * come creatore ed eguagliano il file passato per argomento.
	 * 
	 * @param	Owner	Username dell'utente creatore del file
	 * @param	passw	Password dell'utente creatore del file
	 * @param	Other	Username dell'utente con il quale si desidera condividere il file
	 * @param	file	File che si desidera condividere
	 * 
	 * @requires	Owner != null && passw != null && Other != null && file != null && Other != Owner &&
	 * 				esiste utente-i appartenente a U tale che (username-i == Owner && password-i == passw) &&
	 * 				esiste utente-i appartenente a U tale che (username-i == Other) &&
	 * 				esiste file-i appartenente a F tale che file.equals(contenuto-i) e Owner == creatore-i
	 * @effects		autorizzatiR-i(post) = autorizzatiR-i(pre) unione {Other}		se Other non appartiene a autorizzatiR-i(pre)
	 * 				none		se Other appartiene a autorizzatiR-i(pre)
	 * 				(con i tale che file.equals(contenuto-i) e Owner == creatore-i)
	 * @modifies	this.F.autorizzatiR-i	(con i tale che file.equals(contenuto-i) e Owner == creatore-i)
	 * @return		none
	 * 
	 * @throws	NullPointerException (unchecked)		se Owner == null || passw == null || Other == null || file == null
	 * @throws	IllegalArgumentException (unchecked)	se Other == Owner
	 * @throws	NoSuchUserException (checked)			se	username-i != Owner (per ogni utente-i appartenente a U) ||
	 * 													username-j != Other (per ogni utente-j appartenente a U)
	 * @throws	WrongPasswordException (checked)		se password-i != passw (con i tale che username-i == Owner)
	 * @throws	NoSuchFileException (checked)			se non esiste file-i appartenente a F tale che file.equals(contenuto-i) e Owner == creatore-i
	 */
	public void shareR(String Owner, String passw, String Other, E file)
			throws NullPointerException, IllegalArgumentException, NoSuchUserException, WrongPasswordException, NoSuchFileException;
	
	
	/**
	 * Assegna all'utente Other il diritto di accesso in lettura e scrittura per il file specificato,
	 * se l'utente Owner è il creatore del file e viene rispettato il controllo di identità;
	 * in presenza di duplicati, viene assegnato il permesso per un file arbitrario tra quelli che hanno Owner
	 * come creatore ed eguagliano il file passato per argomento.
	 * 
	 * @param	Owner	Username dell'utente creatore del file
	 * @param	passw	Password dell'utente creatore del file
	 * @param	Other	Username dell'utente con il quale si desidera condividere il file 
	 * @param	file	File che si desidera condividere
	 * 
	 * @requires	Owner != null && passw != null && Other != null && file != null && Other != Owner &&
	 * 				esiste utente-i appartenente a U tale che (username-i == Owner && password-i == passw) &&
	 * 				esiste utente-i appartenente a U tale che (username-i == Other) &&
	 * 				esiste file-i appartenente a F tale che file.equals(contenuto-i) e Owner == creatore-i
	 * @effects		autorizzatiRW-i(post) = autorizzatiRW-i(pre) unione {Other}		se Other non appartiene a autorizzatiRW-i(pre)
	 * 				none		se Other appartiene a autorizzatiRW-i(pre)
	 * 				(con i tale che file.equals(contenuto-i) e Owner == creatore-i)
	 * @modifies	this.F.autorizzatiRW-i	(con i tale che file.equals(contenuto-i) e Owner == creatore-i)
	 * @return		none
	 * 
	 * @throws	NullPointerException (unchecked)		se Owner == null || passw == null || Other == null || file == null
	 * @throws	IllegalArgumentException (unchecked)	se Other == Owner
	 * @throws	NoSuchUserException (checked)			se	username-i != Owner (per ogni utente-i appartenente a U) ||
	 * 													username-j != Other (per ogni utente-j appartenente a U)
	 * @throws	WrongPasswordException (checked)		se password-i != passw (con i tale che username-i == Owner)
	 * @throws	NoSuchFileException (checked)			se non esiste file-i appartenente a F tale che file.equals(contenuto-i) e Owner == creatore-i
	 */
	public void shareW(String Owner, String passw, String Other, E file)
			throws NullPointerException, IllegalArgumentException, NoSuchUserException, WrongPasswordException, NoSuchFileException;
	
	
	/**
	 * Restituisce un iteratore (senza remove) che genera in ordine arbitrario tutti i file a cui l'utente ha accesso
	 * a qualunque titolo (creatore/lettore/scrittore), se vengono rispettati i controlli di identità.
	 * Se l'utente specificato è il creatore del file generato dall'iteratore o ha ottenuto accesso in scrittura,
	 * eventuali modifiche al contenuto (tramite il riferimento restituito) si propagano sulla collezione;
	 * se invece ha accesso in sola lettura, eventuali modifiche al contenuto non si propagano e lasciano immutata la collezione.
	 * 
	 * @param	Owner	Username dell'utente dei cui file si vuole ottenere l'iteratore
	 * @param	passw	Password dell'utente dei cui file si vuole ottenere l'iteratore
	 * 
	 * @requires	Owner != null && passw != null &&
	 * 				esiste utente-i appartenente a U tale che (username-i == Owner && password-i == passw) &&
	 * 				this.F non deve essere modificato finché l'iteratore è in uso
	 * @effects		this(post) == this(pre) &&
	 * 				se (Owner appartiene a autorizzatiR-i && valore_restituito_dall'iteratore viene modificato) allora this non cambia
	 * @modifies	none
	 * @return		un iteratore privo di operazione remove che produrrà tutti e soli gli elementi di this.F
	 * 				tali che Owner appartiene a ( {creatore-i} unione autorizzatiR-i unione autorizzatiRW-i ),
	 * 				sotto forma di oggetti E, ciascuno una sola volta, in ordine arbitrario,
	 * 				e in versione tale che le modifiche all'elemento generato:
	 * 				NON si propagano a contenuto-i	se Owner appartiene a autorizzatiR-i (con i tale che file-i == elemento)
	 * 				SI propagano a contenuto-i		altrimenti
	 * 
	 * @throws	NullPointerException (unchecked)	se Owner == null || passw == null
	 * @throws	NoSuchUserException (checked)		se username-i != Owner (per ogni utente-i appartenente a U)
	 * @throws	WrongPasswordException (checked)	se password-i != passw (con i tale che username-i == Owner)
	 */
	public Iterator<E> getIterator(String Owner, String passw)
			throws NullPointerException, NoSuchUserException, WrongPasswordException;
}
