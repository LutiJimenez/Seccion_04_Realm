package com.luti.seccion_04_realm.models;

import com.luti.seccion_04_realm.app.MyApplication;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Luti on 27/12/16.
 */
public class Note extends RealmObject {

    @PrimaryKey
    private int id;
    @Required
    private String description;
    @Required
    private Date createdAt;

    //COnstructor vacio que necesita REALM
    public Note (){}

    public Note(String description){
        this.id = MyApplication.NoteID.incrementAndGet();
        this.description = description;
        this.createdAt = new Date();
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }


    public void setDescription(String description) {
        this.description = description;
    }

}
