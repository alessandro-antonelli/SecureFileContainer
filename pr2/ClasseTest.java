/*
 * Università di Pisa - Corso di Programmazione II (273AA)
 * Progetto Java - Sessione estiva-autunnale A.A. 2018/2019 (appello settembre 2019)
 * Candidato Alessandro Antonelli (matricola 507264, corso A)
 */

/*	OVERVIEW:
 * Classe che contiene controlli per testare la correttezza delle implementazioni del tipo di dato astratto SecureFileContainer.
 */

package pr2;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

import pr2.exceptions.*;
import pr2.impl1.ArraySecureFileContainer;
import pr2.impl2.HashSecureFileContainer;

/*
 * =========
 * OVERVIEW:
 * =========
 * Contiene i test necessari per verificare la funzionalità delle due implementazioni,
 * il metodo main che li invoca, e le definizioni necessarie per eseguirli.
 */
public class ClasseTest
{
	private static class MioTipoRecord implements Serializable
	{
		// Overview: Classe di test che definisce un dato di tipo "record" con cui instanziare il generico E per i test
		
		private static final long serialVersionUID = 8769895548274867357L;
		
		public int numero;
		public String stringa;
		public boolean booleano;
		
		public MioTipoRecord(int n, String s, boolean b)
		{
			ModificaContenuto(n, s, b);
		}
		
		public void ModificaContenuto(int n, String s, boolean b)
		{
			if(s == null) throw new NullPointerException();
			
			numero = n;
			stringa = new String(s);
			booleano = b;
		}
		
		@Override
		public boolean equals(Object altro)
		{
			if(this == altro) return true; //Sono alias!
			if(altro == null) return false;
			if(!(altro instanceof MioTipoRecord)) return false;
			
			MioTipoRecord altroCastato = (MioTipoRecord) altro;
			
			if(altroCastato.numero == this.numero)
			{
				if	(	(altroCastato.stringa == null && this.stringa == null) ||
						(altroCastato.stringa != null && this.stringa != null && altroCastato.stringa.contentEquals(this.stringa))
					)
						if(altroCastato.booleano == this.booleano)
							return true;
			}
			
			//Se non tutti gli if hanno seguito il ramo then:
			return false;
		}
	}
	
	private static class TestFallitoException extends Exception
	{
		// Overview: Eccezione che serve a segnalare un comportamento inatteso dell'implementazione sotto test
		// (valori di ritorno errati, eccezione lanciata quando non doveva, eccezione non lanciata quando doveva, ecc)
		
		private static final long serialVersionUID = -7356691719466317982L;

		public TestFallitoException(Exception eccezioneOriginale)
		{
			super(eccezioneOriginale);
		}
		
		public TestFallitoException(String descrizione)
		{
			super(descrizione);
		}
	}
	

	public static void main(String[] args)
	{
		//Testo la prima implementazione
		System.out.println("Avvio test 1 su 6: implementazione 1 con tipo di dato predefinito immutabile (Integer)");
		System.out.flush();
		final ArraySecureFileContainer<Integer> IntContainerImpl1 = new ArraySecureFileContainer<Integer>();
		try { EseguiTestDatoImmutabile(IntContainerImpl1); }
		catch(TestFallitoException e) { System.err.println("*** Fallito! ***"); e.printStackTrace(); System.exit(-1); } 
		System.out.println("Superato!");
		System.out.flush();
		
		System.out.println("Avvio test 2 su 6: implementazione 1 con tipo di dato predefinito mutabile (Date)");
		System.out.flush();
		final ArraySecureFileContainer<Date> DateContainerImpl1 = new ArraySecureFileContainer<Date>();
		try { EseguiTestDatoMutabile(DateContainerImpl1); }
		catch(TestFallitoException e) { System.err.println("*** Fallito! ***"); e.printStackTrace(); System.exit(-1); } 
		System.out.println("Superato!");
		System.out.flush();
		
		System.out.println("Avvio test 3 su 6: implementazione 1 con tipo di dato definito dall'utente (MioTipoRecord)");
		System.out.flush();
		final ArraySecureFileContainer<MioTipoRecord> RecordContainerImpl1 = new ArraySecureFileContainer<MioTipoRecord>();
		try { EseguiTestDatoRecord(RecordContainerImpl1); }
		catch(TestFallitoException e) { System.err.println("*** Fallito! ***"); e.printStackTrace(); System.exit(-1); } 
		System.out.println("Superato!");
		System.out.flush();
		
		//Testo la seconda implementazione
		System.out.println("Avvio test 4 su 6: implementazione 2 con tipo di dato predefinito immutabile (Integer)");
		System.out.flush();
		final HashSecureFileContainer<Integer> IntContainerImpl2 = new HashSecureFileContainer<Integer>();
		try { EseguiTestDatoImmutabile(IntContainerImpl2); }
		catch(TestFallitoException e) { System.err.println("*** Fallito! ***"); e.printStackTrace(); System.exit(-1); } 
		System.out.println("Superato!");
		System.out.flush();
		
		System.out.println("Avvio test 5 su 6: implementazione 2 con tipo di dato predefinito mutabile (Date)");
		System.out.flush();
		final HashSecureFileContainer<Date> DateContainerImpl2 = new HashSecureFileContainer<Date>();
		try { EseguiTestDatoMutabile(DateContainerImpl2); }
		catch(TestFallitoException e) { System.err.println("*** Fallito! ***"); e.printStackTrace(); System.exit(-1); } 
		System.out.println("Superato!");
		System.out.flush();
		
		System.out.println("Avvio test 6 su 6: implementazione 2 con tipo di dato definito dall'utente (MioTipoRecord)");
		System.out.flush();
		final HashSecureFileContainer<MioTipoRecord> RecordContainerImpl2 = new HashSecureFileContainer<MioTipoRecord>();
		try { EseguiTestDatoRecord(RecordContainerImpl2); }
		catch(TestFallitoException e) { System.err.println("*** Fallito! ***"); e.printStackTrace(); System.exit(-1); } 
		System.out.println("Superato!");
		System.out.flush();
		
		System.out.println("Tutti i test sono stati superati! :)");
	}
	
	// Testa la struttura, a prescindere dall'implementazione utilizzata, con dati di tipo Integer (tipo predefinito immutabile) 
	private static void EseguiTestDatoImmutabile(SecureFileContainer<Integer> container) throws TestFallitoException
	{
		final String BASENAME = "UtEnTè_?*"+'\n';
		final String BASEPSW = "PaSsWòRd_?*";
		boolean nonHaSollevatoEccezione;
		
		// Uso di utenti non validi
		nonHaSollevatoEccezione = true;
		try { container.getSize("nome", "123"); }
		catch (NoSuchUserException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("Attesa eccezione mancante");
		
		nonHaSollevatoEccezione = true;
		try { container.remove(null, null, null); }
		catch (NullPointerException | NoSuchUserException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("Attesa eccezione mancante");
		
		nonHaSollevatoEccezione = true;
		try { container.getIterator("ciao", "ciaociao"); }
		catch (NoSuchUserException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("Attesa eccezione mancante");
		
		// Creazioni utenze non valide
		nonHaSollevatoEccezione = true;
		try { container.createUser(null, null); }
		catch (NullPointerException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("Attesa eccezione mancante");
		
		nonHaSollevatoEccezione = true;
		try { container.createUser("", ""); }
		catch (IllegalArgumentException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("Attesa eccezione mancante");
		
		nonHaSollevatoEccezione = true;
		try { container.createUser("nickname", "nickname"); }
		catch (IllegalArgumentException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("Attesa eccezione mancante");
		
		// Creo molte utenze
		for(int i=1; i<=100; i++)
		{
			try {
				container.createUser(BASENAME+i, BASEPSW+i);
				int retVal = container.getSize(BASENAME+i, BASEPSW+i);
				if(retVal != 0) throw new TestFallitoException("Attesa struttura vuota!");
				Iterator<Integer> retVall = container.getIterator(BASENAME+i, BASEPSW+i);
				if(retVall.hasNext()) throw new TestFallitoException("Attesa struttura vuota!");
			}
			catch (Exception e) { throw new TestFallitoException(e); }
		}
		
		// Registro nuovamente gli stessi username
		for(int i=1; i<=100; i++)
		{
			nonHaSollevatoEccezione = true;
			try { container.createUser(BASENAME+i, BASEPSW+i); }
			catch (UsernameUnavailableException e) { nonHaSollevatoEccezione = false; }
			if(nonHaSollevatoEccezione) throw new TestFallitoException("Attesa eccezione mancante");
		}
		
		// 200 put sul primo utente e 100 sul secondo, con get e getSize dopo ciascuna
		for(int i=1; i<=200; i++)
		{
			try {
				int count_pre = container.getSize(BASENAME+1, BASEPSW+1);
				
				boolean retval = container.put(BASENAME+1, BASEPSW+1, i);
				if(retval == false) throw new TestFallitoException("put ha restituito false");
				
				int retvall = container.getSize(BASENAME+1, BASEPSW+1);
				if(retvall != count_pre+1) throw new TestFallitoException("size non aumentata dopo put");
				
				Integer retvalll = container.get(BASENAME+1, BASEPSW+1, i);
				if(!retvalll.equals(i)) throw new TestFallitoException("l'elemento restituito dalla get non corrisponde a quello inserito!");
				
				if((i % 2) == 0) //Una volta sì e una no aggiungo al secondo utente
				{
					count_pre = container.getSize(BASENAME+2, BASEPSW+2);
					
					retval = container.put(BASENAME+2, BASEPSW+2, -i/2);
					if(retval == false) throw new TestFallitoException("put ha restituito false");
					
					retvall = container.getSize(BASENAME+2, BASEPSW+2);
					if(retvall != count_pre+1) throw new TestFallitoException("size non aumentata dopo put");
					
					retvalll = container.get(BASENAME+2, BASEPSW+2, -i/2);
					if(!retvalll.equals(-i/2)) throw new TestFallitoException("l'elemento restituito dalla get non corrisponde a quello inserito!");
				}
			}
			catch (Exception e) { throw new TestFallitoException(e); }
		}
		
		// Get di file inesistenti
		nonHaSollevatoEccezione = true;
		try { container.get(BASENAME+1, BASEPSW+1, 250); }
		catch (NoSuchFileException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("Attesa eccezione mancante");
		
		nonHaSollevatoEccezione = true;
		try { container.get(BASENAME+1, BASEPSW+1, Integer.MIN_VALUE); }
		catch (NoSuchFileException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("Attesa eccezione mancante");
		
		nonHaSollevatoEccezione = true;
		try { container.get(BASENAME+3, BASEPSW+3, 50); }
		catch (NoSuchFileException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("Attesa eccezione mancante");
		
		// Remove del primo elemento del primo utente
		try {
			int count_pre = container.getSize(BASENAME+1, BASEPSW+1);
			
			Integer retval = container.remove(BASENAME+1, BASEPSW+1, 1);
			if(retval != 1) throw new TestFallitoException("Elemento restituito dalla remove non corrispondente!");
			
			int retvall = container.getSize(BASENAME+1, BASEPSW+1);
			if(retvall != count_pre-1) throw new TestFallitoException("size non diminuita dopo remove");
		}
		catch (Exception e) { throw new TestFallitoException(e); }
		
		nonHaSollevatoEccezione = true;
		try { container.get(BASENAME+1, BASEPSW+1, 1); }
		catch (NoSuchFileException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("Attesa eccezione mancante");
		
		// Copy del secondo elemento del primo utente, e successiva rimozione di entrambe le copie, con getSize dopo ciascuna
		try {
			int count_pre = container.getSize(BASENAME+1, BASEPSW+1);
			
			container.copy(BASENAME+1, BASEPSW+1, 2);
			
			int retvall = container.getSize(BASENAME+1, BASEPSW+1);
			if(retvall != count_pre+1) throw new TestFallitoException("size non aumentata dopo copy");
			
			Integer retval = container.remove(BASENAME+1, BASEPSW+1, 2);
			if(retval != 2) throw new TestFallitoException("Elemento restituito dalla remove non corrispondente!");
			
			retvall = container.getSize(BASENAME+1, BASEPSW+1);
			if(retvall != count_pre) throw new TestFallitoException("size non diminuota dopo remove");
			
			retval = container.remove(BASENAME+1, BASEPSW+1, 2);
			if(retval != 2) throw new TestFallitoException("Elemento restituito dalla remove non corrispondente!");
			
			retvall = container.getSize(BASENAME+1, BASEPSW+1);
			if(retvall != count_pre - 1) throw new TestFallitoException("size non diminuita dopo remove");
		}
		catch (Exception e) { throw new TestFallitoException(e); }
		
		// L'utente 2 condivide con l'utente 1 i primi 25 file in sola lettura e i successivi 25 in lettura/scrittura, con get e getSize dopo ciascuna
		for(int i=1; i<=25; i++)
		{
			nonHaSollevatoEccezione = true;
			try { container.get(BASENAME+1, BASEPSW+1, -i); }
			catch (NoSuchFileException e) { nonHaSollevatoEccezione = false; }
			catch (Exception e) { throw new TestFallitoException(e); }
			if(nonHaSollevatoEccezione) throw new TestFallitoException("Attesa eccezione mancante");
			
			try {
				int count_pre = container.getSize(BASENAME+1, BASEPSW+1);
				
				container.shareR(BASENAME+2, BASEPSW+2, BASENAME+1, -i);
				
				int retval = container.getSize(BASENAME+1, BASEPSW+1);
				if(retval != count_pre+1) throw new TestFallitoException("size non aumentata dopo shareR");
				
				Integer retvall = container.get(BASENAME+1, BASEPSW+1, -i);
				if(!retvall.equals(-i)) throw new TestFallitoException("contenuto del file non corrispondente in seguito a shareR");
			}
			catch (Exception e) { throw new TestFallitoException(e); }
		}
		for(int i=26; i<=50; i++)
		{
			nonHaSollevatoEccezione = true;
			try { container.get(BASENAME+1, BASEPSW+1, -i); }
			catch (NoSuchFileException e) { nonHaSollevatoEccezione = false; }
			catch (Exception e) { throw new TestFallitoException(e); }
			if(nonHaSollevatoEccezione) throw new TestFallitoException("Attesa eccezione mancante");
			
			try {
				int count_pre = container.getSize(BASENAME+1, BASEPSW+1);
				
				container.shareW(BASENAME+2, BASEPSW+2, BASENAME+1, -i);
				
				int retval = container.getSize(BASENAME+1, BASEPSW+1);
				if(retval != count_pre+1) throw new TestFallitoException("size non aumentata dopo shareW");
				
				Integer retvall = container.get(BASENAME+1, BASEPSW+1, -i);
				if(!retvall.equals(-i)) throw new TestFallitoException("contenuto del file non corrispondente in seguito a shareW");
			}
			catch (Exception e) { throw new TestFallitoException(e); }
		}
		
		// Itero sull'utente 1 e controllo che restituisca 248 elementi
		try {
			if(container.getSize(BASENAME+1, BASEPSW+1) != 248) throw new TestFallitoException("size finale non corrispondente");
			
			Iterator<Integer> iteratore = container.getIterator(BASENAME+1, BASEPSW+1);
			for(int i=1; i<=248; i++)
			{
				int val = iteratore.next();
				if(val < -50 || val > 200 || val == 0 || val == 1 || val == 2)
					throw new TestFallitoException("Iteratore ha restituito un elemento estraneo (che non dovrebbe esistere)");
			}
		}
		catch (Exception e) { throw new TestFallitoException(e); }
	}
	
	// Testa la struttura, a prescindere dall'implementazione utilizzata, con dati di tipo Date (tipo predefinito mutabile)
	@SuppressWarnings("deprecation")
	private static void EseguiTestDatoMutabile(SecureFileContainer<Date> container) throws TestFallitoException
	{
		boolean nonHaSollevatoEccezione;
		
		// Registro due utenti
		try { container.createUser("A", "123"); container.createUser("B", "456"); }
		catch (Exception e) { throw new TestFallitoException(e); }
		
		// Inserisco dato e modifico il contenuto come proprietario
		final Date inserimentoOriginale = new Date(1994, 4, 30);
		try {
			boolean retval = container.put("A", "123", new Date(1994, 4, 30));
			if(retval == false) throw new TestFallitoException("put ha restituito false");
			
			if(container.getSize("A", "123") != 1) throw new TestFallitoException("size non corrispondente");
			
			Date puntatore = container.get("A", "123", inserimentoOriginale);
			if(!puntatore.equals(inserimentoOriginale)) throw new TestFallitoException("restituito oggetto diverso dall'originale!");
			
			puntatore.setYear(1850);
		}
		catch (Exception e) { throw new TestFallitoException(e); }
		final Date inserimentoModificato = new Date(1850, 4, 30);
		
		// Verifico che il valore vecchio non ci sia più
		nonHaSollevatoEccezione = true;
		try { container.get("A", "123", inserimentoOriginale); }
		catch (NoSuchFileException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("Il file originale è ancora reperibile dopo la modifica da parte del proprietario");
		
		// Verifico che quello nuovo ci sia e corrisponda
		try {
			Date puntatore = container.get("A", "123", inserimentoModificato);
			if(!puntatore.equals(inserimentoModificato)) throw new TestFallitoException("la modifica del contenuto è rimasta locale, non si è propagata nella collezione!");
			
			if(container.getSize("A", "123") != 1) throw new TestFallitoException("size non corrispondente");
		}
		catch (Exception e) { throw new TestFallitoException(e); }
		
		// Condivido il dato in sola lettura con l'altro utente
		final Date inserimentoNonPropagato = new Date(2001, 4, 30);
		try {
			if(container.getSize("B", "456") != 0) throw new TestFallitoException("Size dovrebbe essere 0!");
			container.shareR("A", "123", "B", inserimentoModificato);
			if(container.getSize("B", "456") != 1) throw new TestFallitoException("Size non aumentata dopo shareR!");
			
			Date pointer = container.get("B", "456", inserimentoModificato);
			if(!pointer.equals(inserimentoModificato)) throw new TestFallitoException("elemento condiviso non corrispondente all'originale!");
			
		// Verifico che, con i permessi di sola lettura, NON sia possibile modificarne il contenuto
			pointer.setYear(2001);
			
			pointer = container.get("B", "456", inserimentoModificato);
			if(!pointer.equals(inserimentoModificato)) throw new TestFallitoException("l'elemento condiviso in sola lettura è 'sparito' dopo una modifica");
			
			pointer = container.get("A", "123", inserimentoModificato);
			if(!pointer.equals(inserimentoModificato)) throw new TestFallitoException("l'elemento condiviso in sola lettura è 'sparito' dopo una modifica");
		}
		catch (Exception e) { throw new TestFallitoException(e); }
		
		nonHaSollevatoEccezione = true;
		try { container.get("B", "456", inserimentoNonPropagato); }
		catch (NoSuchFileException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("La modifica ad un file in sola lettura si è propagata alla collezione!");
		
		nonHaSollevatoEccezione = true;
		try { container.get("A", "123", inserimentoNonPropagato); }
		catch (NoSuchFileException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("La modifica ad un file in sola lettura si è propagata alla collezione!");
		
		// Creo un file e lo condivido in lettura+scrittura con l'utente A
		final Date secondoInserimento = new Date(1883, 10, 28);
		final Date secondoInserimentoModificato = new Date(1988, 10, 28);
		try {
			boolean retval = container.put("B", "456", new Date(1883, 10, 28));
			if(retval == false) throw new TestFallitoException("put ha restituito false");
			
			if(container.getSize("A", "123") != 1) throw new TestFallitoException("Size non corrispondente");
			container.shareW("B", "456", "A", secondoInserimento);
			if(container.getSize("A", "123") != 2) throw new TestFallitoException("Size non corrispondente");
			
		// Effettuo una modifica e verifico che, con i permessi di scrittura le modifiche si propaghino
			Date pointer = container.get("A", "123", secondoInserimento);
			pointer.setYear(1988);
		}
		catch (Exception e) { throw new TestFallitoException(e); }
		
		nonHaSollevatoEccezione = true;
		try { container.get("A", "123", secondoInserimento); }
		catch (NoSuchFileException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("La modifica al contenuto del file condiviso non si è propagata alla collezione");
		
		nonHaSollevatoEccezione = true;
		try { container.get("B", "456", secondoInserimento); }
		catch (NoSuchFileException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("La modifica al contenuto del file condiviso non si è propagata alla collezione");
		
		try {
			Date pointer = container.get("A", "123", secondoInserimentoModificato);
			if(!pointer.equals(secondoInserimentoModificato))
				throw new TestFallitoException("La modifica al contenuto del file condiviso non si è propagata alla collezione");
			
			pointer = container.get("B", "456", secondoInserimentoModificato);
			if(!pointer.equals(secondoInserimentoModificato))
				throw new TestFallitoException("La modifica al contenuto del file condiviso non si è propagata alla collezione");
		}
		catch (Exception e) { throw new TestFallitoException(e); }
		
		// Verifico il copy-in del valore passato con la get
		Date inserimentoCopyIn = new Date(2007, 1, 29);
		try {
			boolean retval = container.put("A", "123", inserimentoCopyIn);
			if(retval == false) throw new TestFallitoException("put ha restituito false!");
			
			inserimentoCopyIn.setYear(2016);
			
			container.get("A", "123", new Date(2007, 1, 29));
		}
		catch (NoSuchFileException e) { throw new TestFallitoException("La put non fa copy-in del parametro file!"); }
		catch (Exception e) { throw new TestFallitoException(e); }
		
		nonHaSollevatoEccezione = true;
		try { container.get("A", "123", new Date(2016, 1, 29)); }
		catch (Exception e) { nonHaSollevatoEccezione = false; }
		if(nonHaSollevatoEccezione) throw new TestFallitoException("La put non fa copy-in del parametro file!");
		
		// Verifico che la copy effettui davvero una deep copy
		try {
			container.put("A", "123", new Date(1943, 3, 5));
			container.copy("A", "123", new Date(1943, 3, 5));
			Date puntatore = container.get("A", "123", new Date(1943, 3, 5));
			puntatore.setYear(1998);
		}
		catch (Exception e) { throw new TestFallitoException(e); }
		
		try {
			container.get("A", "123", new Date(1998, 3, 5));
		}
		catch (NoSuchFileException e) { throw new TestFallitoException("Le modifiche ad un file di cui si è proprietari non si propagano nella collezione come dovrebbero!"); }
		catch (Exception e) { throw new TestFallitoException(e); }
		
		try {
			container.get("A", "123", new Date(1943, 3, 5));
		}
		catch (NoSuchFileException e) { throw new TestFallitoException("La copy non effettua una deep copy come dovrebbe, ma solo una shallow copy: le modifiche al file originario si propagano anche al file duplicato!"); }
		catch (Exception e) { throw new TestFallitoException(e); }
	}
	
	// Testa la struttura, a prescindere dall'implementazione utilizzata, con dati di tipo MioTipoRecord (tipo user-defined)
	private static void EseguiTestDatoRecord(SecureFileContainer<MioTipoRecord> container) throws TestFallitoException
	{
		MioTipoRecord primoInserimento = new MioTipoRecord(42, "ciao", false);
		MioTipoRecord primoInserimentoModificato = new MioTipoRecord(666, "lol", true);
		try {
			container.createUser("A", "123"); container.createUser("B", "456");
			
			// Inserisco un file e testo che il contenuto non venga distorto
			container.put("A", "123", new MioTipoRecord(42, "ciao", false));
			
			if(!container.get("A", "123", new MioTipoRecord(42, "ciao", false)).equals(new MioTipoRecord(42, "ciao", false)))
				throw new TestFallitoException("contenuto del file non corrispondente a quello inserito");
			
			// Condivido il file con B e testo che il contenuto non venga distorto
			container.shareR("A", "123", "B", primoInserimento);
			
			if(container.getSize("B", "456") != 1) throw new TestFallitoException("size non corrispondente");
			
			if(!container.get("B", "456", new MioTipoRecord(42, "ciao", false)).equals(new MioTipoRecord(42, "ciao", false)))
				throw new TestFallitoException("file condiviso non corrispondente all'originale");
			
			// A modifica il contenuto del file: controllo che la modifica sia visibile sia da A che da B
			container.get("A", "123", primoInserimento).ModificaContenuto(666, "lol", true);
			
			if(!container.get("A", "123", new MioTipoRecord(666, "lol", true)).equals(new MioTipoRecord(666, "lol", true)))
				throw new TestFallitoException("la modifica del contenuto non si è propagata alla collezione del creatore");
			
			if(!container.get("B", "456", new MioTipoRecord(666, "lol", true)).equals(new MioTipoRecord(666, "lol", true)))
				throw new TestFallitoException("la modifica del contenuto non si è propagata alla collezione dell'utente in condivisione");
			
			if(container.getSize("A", "123") != 1) throw new TestFallitoException("size non corrispondente");
			if(container.getSize("B", "456") != 1) throw new TestFallitoException("size non corrispondente");
		}
		catch (Exception e) { throw new TestFallitoException(e); }
			
		// Controllo che l'iteratore, invocato da B, restituisca una deep copy del file in sola lettura
		try {
			Iterator<MioTipoRecord> iteratore = container.getIterator("B", "456");
				
			MioTipoRecord fileInSolaLettura = iteratore.next();
			fileInSolaLettura.ModificaContenuto(1024, "modificato", false);
				
			iteratore = container.getIterator("B", "456");
			MioTipoRecord fileNellaCollezione = iteratore.next();
				
			if(fileNellaCollezione.equals(new MioTipoRecord(666, "lol", true))) ; //OK
			if(fileNellaCollezione.equals(new MioTipoRecord(1024, "modificato", false)))
					throw new TestFallitoException("Le modifiche ai file in sola lettura restituiti dall'iteratore si propagano nella collezione!");
		}
		catch (Exception e) { throw new TestFallitoException(e); }
			
		// Rimuovo la copia dal proprietario e testo che modifiche tramite il riferimento restituito
		// non siano visibili da B
		try {
			MioTipoRecord retval = container.remove("A", "123", primoInserimentoModificato);
			if(container.getSize("A", "123") != 0) throw new TestFallitoException("size non corrispondente");
			if(container.getSize("B", "456") != 1) throw new TestFallitoException("size non corrispondente");
			
			retval.ModificaContenuto(746, "RI", true);
		}
		catch (Exception e) { throw new TestFallitoException(e); }

		boolean nonHaSollevatoEccezione = true;
		try { container.get("B", "456", new MioTipoRecord(746, "RI", true)); }
		catch (NoSuchFileException e) { nonHaSollevatoEccezione = false; }
		catch (Exception e) { throw new TestFallitoException(e); }
		if(nonHaSollevatoEccezione) throw new TestFallitoException
			("Le modifiche tramite i riferimenti restituiti dalla remove si propagano nella collezione!");
		
		try {
			// Rimuovo anche l'altra copia
			container.remove("B", "456", primoInserimentoModificato);
			if(container.getSize("A", "123") != 0) throw new TestFallitoException("size non corrispondente");
			if(container.getSize("B", "456") != 0) throw new TestFallitoException("size non corrispondente");
		}
		catch (Exception e) { throw new TestFallitoException(e); }
	}
}
