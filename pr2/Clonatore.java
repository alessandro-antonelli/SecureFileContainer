/*
 * Università di Pisa - Corso di Programmazione II (273AA)
 * Progetto Java - Sessione estiva-autunnale A.A. 2018/2019 (appello settembre 2019)
 * Candidato Alessandro Antonelli (matricola 507264, corso A)
 */

package pr2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
	
/*
 * =========
 * OVERVIEW:
 * =========
 * Classe di supporto che fornisce un servizio di "esecutore di deep copy" di oggetti
 * di tipo generico E.
 * 
 * Di fatto è come fosse una classe statica, ma java non consente di renderla tale
 * per la presenza del parametro di tipo.
 */
public class Clonatore <E extends Serializable>
{
	/*
	 * Metodo di supporto che crea una deep copy del dato passato come argomento
	 * utilizzando una serializzazione seguita da una de-serializzazione.
	 * 
	 * @param 	datoOriginale		Il dato di cui si vuole fare una copia
	 * @return 	Un nuovo oggetto di tipo E con lo stesso contenuto di datoOriginale
	 */
	@SuppressWarnings("unchecked")
	public E deepCopyConSerializzazione(E datoOriginale)
	{
		E retval = null;
        try
        {
            // Serializzazione
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(datoOriginale);
            out.flush();
            out.close();

            // Deserializzazione
            ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray())
            						);
            retval = (E) in.readObject();
        }
        catch(IOException e) { e.printStackTrace(); }
        catch(ClassNotFoundException e) { e.printStackTrace(); }
        
        return retval;
	}
}
