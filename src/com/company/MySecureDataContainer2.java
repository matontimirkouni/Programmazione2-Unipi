package com.company;
import java.util.*;
import com.company.Exception.*;

import javax.xml.crypto.Data;

public class MySecureDataContainer2 <E> implements SecureDataContainer<E>{
    //FA <users,datacollection> dove
    //users={users.get(0).......users.get(users.size-1)}
    //datacollec={datacollec.get(0).....datacollec.get(datacollec.size - 1)}

    //IR: users != null && datacollection !=  null
    //forall i. 0<=i < users.size() => users.get(i) != null
    //forall i,j. 0<=i,j< users.size() i!= j => users.get(i) != users.get(j)
    //forall i. 0<=i < datacollec.size() => datacollec.get(i) != null

    private Hashtable<String,List<DataStruct2<E>>> datacollection;
    private Hashtable<String,String> users;

    public MySecureDataContainer2()
    {
        users= new Hashtable<>();
        datacollection=new Hashtable<>();
    }

    // Crea l’identità un nuovo utente della collezione
    public void createUser(String Id, String passw) throws NullPointerException,DuplicateUserException{
        if((Id == null) || (passw == null)) throw new NullPointerException();
        if(users.containsKey(Id)) throw  new DuplicateUserException("Duplicated user");

        users.put(Id,passw);
    }
    /**
     @REQUIRES: Id != null && passw != null
     @MODIFIES: this.users
     @EFFECTS: Crea un nuovo utente nella collezione
     @THROWS: NullPointerException se id == null || passw == null
              DuplicateUserException se user.id già presente
     **/



    // Rimuove l’utente dalla collezione
    public void RemoveUser(String Id, String passw) throws NullPointerException,UserNotFoundException,WrongPasswordException{
        if((Id == null) || (passw == null)) throw new NullPointerException();
        if(!users.containsKey(Id)) throw  new UserNotFoundException("User not found");
        if(!checkUserPassword(Id,passw)) throw new WrongPasswordException("Wrong password");


        //Rimuovo l'utente dalle autorizzazioni in altri file
        for(String s:datacollection.keySet())
            for(DataStruct2 d:datacollection.get(s))
                if(d.getShares().contains(Id))
                    d.getShares().remove(Id);

        datacollection.remove(Id);
        users.remove(Id);
    }
    /**
     @REQUIRES: Id != null && passw != null
     @MODIFIES: this.users && this.datacollec
     @EFFECTS: Se l'utente è presente nella collezione rimuove i dati a lui associati ovvero quelli di cui è proprietario
               ed i riferimenti a lui nei file condivisi da altri utenti, per finire elimina l'utente dalla lista users
     @THROWS: NullPointerException se id == null || passw == null
              UserNotFoundException (checked) se l'utente non è presente (checkUserExitence(Id)=False)
              WrongPasswordException (checked) se non vengono rispettati i controlli di identità (checkPassword(passw)=False)
     **/

    // Restituisce il numero degli elementi di un utente presenti nella
    // collezione
    public int getSize(String Owner, String passw) throws NullPointerException,UserNotFoundException ,WrongPasswordException{
        if((Owner == null) || (passw == null)) throw new NullPointerException();
        if(!users.containsKey(Owner)) throw  new UserNotFoundException("User not found");
        if(!checkUserPassword(Owner,passw)) throw new WrongPasswordException("Wrong password");

        int sz=0;
        if(datacollection.get(Owner)!= null)
            sz=datacollection.get(Owner).size();
        return sz;
    }
    /**
     @REQUIRES: Owner != null && passw != null
     @EFFECTS: Superati i controlli di identità,restituisce size ovvero il numero degli elementi di un utente presenti
               nella collezione (solo quelli di cui è proprietario)
     @THROWS: NullPointerException se Owner == null || passw == null
              UserNotFoundException (checked) se l'utente non è presente (checkUserExitence(Id)=False)
              WrongPasswordException se i controlli di indentità non sono rispettati
     **/

    // Inserisce il valore del dato nella collezione
    // se vengono rispettati i controlli di identità
    public boolean put(String Owner, String passw, E data) throws NullPointerException,UserNotFoundException,WrongPasswordException{
        if((Owner == null) || (passw == null) ||  (data == null)) throw new NullPointerException();
        if(!users.containsKey(Owner)) throw  new UserNotFoundException("User not found");
        if(!checkUserPassword(Owner,passw)) throw new WrongPasswordException("Wrong password");

        List<DataStruct2<E>> dt= datacollection.get(Owner);
        if (dt==null)
        {
            dt=new ArrayList<>();
            datacollection.put(Owner,dt);
        }
        DataStruct2<E> tmp= new DataStruct2<>(data);
        return dt.add(tmp);
    }
    /**
     @REQUIRES: Owner != null && passw != null && data != null
     @MODIFIES: this.datacollec
     @EFFECTS: Superati i controlli di identità, 'data'  viene inserito ed attribuito ad Owner, la funzione restituisce : true
                se l'operazione di inserimento va a buon fine

     @THROWS: NullPointerException se owner == null || passw == null || data== null
              UserNotFoundException (checked) se Owner non è presente
              WrongPasswordException (checked) se non vengono rispettati i controlli di identità
     **/


    // Ottiene una copia del valore del dato nella collezione
    // se vengono rispettati i controlli di identità
    public E get(String Owner, String passw, E data) throws NullPointerException,UserNotFoundException ,WrongPasswordException{
        if((Owner == null) || (passw == null) ||  (data == null)) throw new NullPointerException();
        if(!users.containsKey(Owner)) throw  new UserNotFoundException("User not found");
        if(!checkUserPassword(Owner,passw)) throw new WrongPasswordException("Wrong password");

        E tmp = null;
        for(String s:datacollection.keySet())
            for(DataStruct2 d:datacollection.get(s))
                if(d.equals(data) && (s.equals(Owner) || d.getShares().contains(Owner)))
                    tmp=(E) d.getData();

        return tmp;
    }
    /**
     @REQUIRES: Owner != null && passw != null && data != null
     @EFFECTS: Superati i controlli di identità restituisce il riferimento a'data'
               Se 'data' è presente in molteplice copia restituisce la prima trovata
               Se 'data' non è presente ritorna null
     @THROWS: NullPointerException se owner == null || passw == null || data== null
              UserNotFoundException (checked) se Owner non è presente
              WrongPasswordException (checked) se non vengono rispettati i controlli di identità
     **/

    // Condivide il dato nella collezione con un altro utente
    // se vengono rispettati i controlli di identità
    public void share(String Owner, String passw, String Other, E data) throws NullPointerException,UserNotFoundException,WrongPasswordException {
        if((Owner == null) || (passw == null) ||(Other== null) || (data==null)) throw new NullPointerException();
        if(!users.containsKey(Owner)) throw  new UserNotFoundException("User not found");
        if(!users.containsKey(Other)) throw  new UserNotFoundException("User not found");
        if(!checkUserPassword(Owner,passw)) throw new WrongPasswordException("Wrong password");

        for(DataStruct2 d:datacollection.get(Owner))
            if(d.getData().equals(data) && !d.getShares().contains(Other))
                d.addShare(Other);
    }
    /**
     @REQUIRES: Owner != null && passw != null &&  Other != null && data != null
     @MODIFIES: this.datacollec
     @EFFECTS: Superati i controlli di identità condivide un dato della collezione ovvero aggiunge 'data' in Hash(Owner)
     @THROWS: NullPointerException se owner == null || passw == null || data== null || Other == nul
              UserNotFoundException (checked) se Owner oppure Other non sono presente
              WrongPasswordException (checked) se non vengono rispettati i controlli di identità
              DuplicateUserException se Other è già autorizzato alla visione di data
     **/


    // Rimuove il dato nella collezione
    // se vengono rispettati i controlli di identità
    public E remove(String Owner, String passw, E data) throws NullPointerException,UserNotFoundException,WrongPasswordException{
        if((Owner == null) || (passw == null) ||(data==null)) throw new NullPointerException();
        if(!users.containsKey(Owner)) throw  new UserNotFoundException("User not found");
        if(!checkUserPassword(Owner,passw)) throw new WrongPasswordException("Wrong password");

        E tmp= null;
        List<DataStruct2<E>> ls = datacollection.get(Owner);
        for(int i=0;i < ls.size();i++)
        {
            if(ls.get(i).getData().equals(data)) {
                tmp= (E) ls.get(i);
                ls.remove(i);
                i--;
            }
        }
        return tmp;
    }
    /**
     @REQUIRES: Owner != null && passw != null && data != null
     @MODIFIES: this.datacollec
     @EFFECTS: Se il dato non è presente ritorna Null
               Se il dato è presente viene conservato in 'tmp' prima di essere rimosso dalla collezione e
               ritornato al chiamante
               Se il dato è presente im molteplice copia verranno rimosse tutte e la funzione restituisce l'ultima rimossa
               NOTA 'data' viene rimosso solo se Owner è il proprietario
     @THROWS: NullPointerException se owner == null || passw == null || data== null
              UserNotFoundException (checked) se Owner non è presente
              WrongPasswordException (checked) se non vengono rispettati i controlli di identità
     **/

    // Crea una copia del dato nella collezione
    // se vengono rispettati i controlli di identità
    public void copy(String Owner, String passw, E data) throws NullPointerException,UserNotFoundException,WrongPasswordException{
        if((Owner == null) || (passw == null) ||(data==null)) throw new NullPointerException();
        if(!users.containsKey(Owner)) throw  new UserNotFoundException("User not found");
        if(!checkUserPassword(Owner,passw)) throw new WrongPasswordException("Wrong password");

        this.put(Owner,passw,data);
    }
    /**
     @REQUIRES: Owner != null && passw != null && data != null
     @MODIFIES: this
     @EFFECTS: Crea una copia di 'data' nella collezione
     @THROWS: NullPointerException se owner == null || passw == null || data== null
              UserNotFoundException (checked) se Owner non è presente
              WrongPasswordException (checked) se non vengono rispettati i controlli di identità
     **/

    // Restituisce un iteratore (senza remove) che genera tutti i dati
    // dell’utente in ordine arbitrario
    // se vengono rispettati i controlli di identità
    public Iterator<E> getIterator(String Owner, String passw) throws NullPointerException,UserNotFoundException,WrongPasswordException {
        if((Owner == null) || (passw == null)) throw new NullPointerException();
        if(!users.containsKey(Owner)) throw  new UserNotFoundException("User not found");
        if(!checkUserPassword(Owner,passw)) throw new WrongPasswordException("Wrong password");

        List<E> tmp = new ArrayList<>();
        for(String s:datacollection.keySet())
            for(DataStruct2 d:datacollection.get(s))
                if(s.equals(Owner) || d.getShares().contains(Owner))
                    tmp.add((E)d.getData());

        return tmp.iterator();
    }
    /**
     @REQUIRES: Owner != null && passw != null
     @EFFECTS: Superati i controlli di identità restituisce un iteratore contenente i dati dell utente 'Owner'
     @THROWS: NullPointerException se owner == null || passw == null
              UserNotFoundException (checked) se Owner non è presente
              WrongPasswordException (checked) se non vengono rispettati i controlli di identità

     **/
    private boolean checkUserPassword(String Id,String Passw)
    {
        String pass= users.get(Id);
        if (pass.equals(Passw))
            return true;
        return false;
    }
}
