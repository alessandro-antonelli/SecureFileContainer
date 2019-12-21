/*
 * Università di Pisa - Corso di Programmazione II (273AA)
 * Progetto Java - Sessione estiva-autunnale A.A. 2018/2019 (appello settembre 2019)
 * Candidato Alessandro Antonelli (matricola 507264, corso A)
 */

package pr2.impl1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import pr2.Clonatore;

/*
 * =========
 * OVERVIEW:
 * =========
 * DescrittoreUtente rappresenta un'utenza della collezione,
 * di cui memorizza le credenziali e i riferimenti ai file cui l'utente ha accesso.
 * 
 * ===============================
 * INVARIANTE DI RAPPRESENTAZIONE:
 * ===============================
 * fileCreati != null && fileRicevutiInLettura != null && fileRicevutiInScrittura != null &&
 * username != null && username != stringa vuota && password != null && password != stringa vuota && password != username
 * 
 * =======================
 * FUNZIONE DI ASTRAZIONE:
 * =======================
 * FA = this rappresenta l'utente <nome, psw> appartenente a U,
 * dove this.username = nome e this.password = psw.
 * 
 * Inoltre si ha che:
 * this.fileCreati = { file appartenenti a F : utente == file.creatore } 
 * this.fileRicevutiInLettura = { file appartenenti a F : utente appartiene a file.autorizzatiR } 
 * this.fileRicevutiInScrittura = { file appartenenti a F : utente appartiene a file.autorizzatiRW }
 */
class DescrittoreUtente<E extends Serializable> implements Iterable<E>
// Nessun modificatore per rendere la classe "package private" (visibile solo all'interno del proprio package)
{
	// Password dell'account
	private final String		password;
	
	// Lista dei riferimenti ai file creati dall'utente
	public final ArrayList<E>	fileCreati;
	
	// Lista dei riferimenti ai file a cui l'utente ha ricevuto accesso in sola lettura
	public final ArrayList<E>	fileRicevutiInLettura;
	
	// Lista dei riferimenti ai file a cui l'utente ha ricevuto accesso in lettura e scrittura
	public final ArrayList<E>	fileRicevutiInScrittura;
	
	// Username dell'account
	private final String		username;
	
	/*
	 * =========
	 * OVERVIEW:
	 * =========
 	 * La classe annidata IteratoreFileUtente definisce l'iteratore della
	 * sottocollezione costituita dai soli file a cui l'utente ha accesso,
	 * a qualunque titolo (creatore/lettore/scrittore).
	 * 
	 * ===============================
	 * INVARIANTE DI RAPPRESENTAZIONE:
 	 * ===============================
 	 * generati >= 0 && generati assume valori monotonamente crescenti && size > 0 && size ha valore costante
 	 * 
 	 * =======================
 	 * FUNZIONE DI ASTRAZIONE:
 	 * =======================
 	 * FA = this rappresenta la sequenza [ o1, o2, ..., on ] tale che:
 	 * 
 	 * ogni oi è un file (tipo di dato <E>), 
 	 * n = this.size è il numero totale dei files accessibili dall'utente (creati o condivisi con lui in lettura o scrittura),
 	 * tutti gli elementi in fileCreati, fileRicevutiInLettura e fileRicevutiInScrittura occorrono nella sequenza,
 	 * gli elementi di fileCreati occorrono prima di quelli di fileRicevutiInLettura, che a loro volta occorrono
 	 * prima di quelli di fileRicevutiInScrittura. 
	 */
	private class IteratoreFileUtente implements Iterator<E>
	{
		/*  Mantiene il conteggio del numero di elementi già generati nella corrente istanza dell'iteratore,
		 *  ovvero del numero di volte che è stata chiamata la funzione next() dalla creazione dell'oggetto. */
		int generati = 0;
		
		// Numero totale di elementi da generare, ovvero numero dei file a cui l'utente ha accesso.
		final int size = fileCreati.size() + fileRicevutiInLettura.size() + fileRicevutiInScrittura.size();
		
		// Riferimento al servizio di clonazione, richiesto per restituire copie non modificabili dei file in sola lettura
		Clonatore<E> cloner = new Clonatore<E>();

		@Override
		public boolean hasNext()
		{
			return (generati < size);
		}

		@Override
		public E next()
		{
			if(generati >= size) throw new NoSuchElementException
			("L'iteratore ha già generato tutti i " + size + " file a cui l'utente ha accesso. Non ce ne sono altri da generare!");
			
			int posizione = generati; //Memorizza l'indice dell'elemento da generare all'interno del vettore di appartenenza  
			generati++;
			
			// Genera prima gli elementi appartenenti a fileCreati
			if(posizione < fileCreati.size()) return fileCreati.get(posizione);
			else posizione -= fileCreati.size(); //Non era un indice valido del vettore creati: provo con il vettore lettura
			
			// Poi quelli appartenenti a fileRicevutiInLettura (restituisce una deep copy per evitare modifiche)
			if(posizione < fileRicevutiInLettura.size())
				return cloner.deepCopyConSerializzazione(fileRicevutiInLettura.get(posizione));
			else posizione -= fileRicevutiInLettura.size(); //Non era un indice valido del vettore lettura: provo con il vettore scrittura
			
			// Infine quelli appartenenti a fileRicevutiInScrittura
			return fileRicevutiInScrittura.get(posizione);
		}
		
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("Questo iteratore non supporta la remove");
		}
	}
	
	// Metodo costruttore: produce un descrittore con associate le credenziali ma nessun file
	public DescrittoreUtente(String password, String username)
	{
		this.password = password;
		this.username = username;
		fileCreati = new ArrayList<E>();
		fileRicevutiInLettura = new ArrayList<E>();
		fileRicevutiInScrittura = new ArrayList<E>();
	}

	// Restituisce un booleano che indica se le credenziali fornite sono corrette per l'account corrente
	public boolean passwordCorretta(String tentativo)
	{
		if(tentativo == null) throw new NullPointerException("Impossibile accedere con una password pari a null!");
		
		return tentativo.equals(password);
	}

	/*
	 * Restituisce un iteratore (senza remove) che genera tutti e soli i file a cui l'utente ha accesso.
	 * 
	 * @requires	this non deve essere modificato finché l'iteratore è in uso
	 * 
	 * @returns		Restituisce un iteratore (privo dell'operazione di remove) che produrrà tutti e soli gli
	 * 				elementi di this come oggetti di tipo E, ciascuno una sola volta.
	 * 				Verranno generati prima gli elementi di fileCreati, poi quelli di fileRicevutiInLettura
	 * 				e infine quelli di fileRicevutiInScrittura, in ordine crescente di posizione occupata
	 * 				all'interno del vettore di appartenenza.
	 */
	@Override
	public Iterator<E> iterator()
	{
		return new IteratoreFileUtente();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fileCreati == null) ? 0 : fileCreati.hashCode());
		result = prime * result + ((fileRicevutiInLettura == null) ? 0
				: fileRicevutiInLettura.hashCode());
		result = prime * result + ((fileRicevutiInScrittura == null) ? 0
				: fileRicevutiInScrittura.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof DescrittoreUtente)) return false;
		
		DescrittoreUtente<E> other = (DescrittoreUtente<E>) obj;
		
		if(other.username.equals(this.username))
		{
			if(other.password.equals(this.username))
				if(other.fileCreati.equals(this.fileCreati))
					if(other.fileRicevutiInLettura.equals(this.fileRicevutiInLettura))
						if(other.fileRicevutiInScrittura.equals(this.fileRicevutiInScrittura))
							return true;
		}
		
		return false;
	}
}
