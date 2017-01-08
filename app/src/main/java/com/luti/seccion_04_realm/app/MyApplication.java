package com.luti.seccion_04_realm.app;

import android.app.Application;

import com.luti.seccion_04_realm.models.Board;
import com.luti.seccion_04_realm.models.Note;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.Required;

/**
 * Created by Luti on 27/12/16.
 */
public class MyApplication extends Application{

    public static AtomicInteger BoardID = new AtomicInteger();
    public static AtomicInteger NoteID = new AtomicInteger();

    //Esto es para la configuracion, se ejecuta antes del onCreate del mainActivity
    @Override
    public void onCreate() {
        super.onCreate();
        setUpRealmConfig();
        Realm realm = Realm.getDefaultInstance();

        BoardID = getIdByTable(realm, Board.class);
        NoteID = getIdByTable(realm, Note.class);
        //Cerramos la base de datos
        realm.close();



    }

    private void setUpRealmConfig(){
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }

    //T es para decir que vamos a utilizar una clase sin decirle cual, pero que tiene que extender de
    //RealObject
    private <T extends RealmObject> AtomicInteger getIdByTable(Realm realm, Class<T> anyClass){
        RealmResults<T> results = realm.where(anyClass).findAll();
        return (results.size() > 0) ?  new AtomicInteger(results.max("id").intValue()) : new AtomicInteger();
    }
}
