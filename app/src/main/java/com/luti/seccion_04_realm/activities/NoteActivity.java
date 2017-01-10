package com.luti.seccion_04_realm.activities;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.luti.seccion_04_realm.R;
import com.luti.seccion_04_realm.adapters.NoteAdapter;
import com.luti.seccion_04_realm.models.Board;
import com.luti.seccion_04_realm.models.Note;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

public class NoteActivity extends AppCompatActivity  implements RealmChangeListener<Board>{

    private ListView listView;
    private FloatingActionButton fab;


    private NoteAdapter adapter;
    private RealmList<Note> notes;
    private Realm realm;
    private Board board;

    //Recogemos el id del Board para recuperar el objeto completo
    private int boardId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        //Nos traemos la base de datos
        realm = Realm.getDefaultInstance();

        //el boardid se encuentra en get extra del intent
        if(getIntent().getExtras() != null){
            boardId = getIntent().getExtras().getInt("id");
        }
        //Busacamos la pizarra que hemos pulsado mediante el id
        board = realm.where(Board.class).equalTo("id",boardId).findFirst();
        //Refresca el board cuando se guarda
        board.addChangeListener(this);
        notes = board.getNotes();


        this.setTitle(board.getTitle());
        fab = (FloatingActionButton) findViewById(R.id.fabAddNote);
        listView = (ListView) findViewById(R.id.listViewNote);
        adapter = new NoteAdapter(this, notes, R.layout.list_view_note_item);
        listView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertForCreatingNote("Add New Note","Type a note for " +board.getTitle() + "." );
            }
        });


    }


    /*Dialogs CREA LANOTA NUEVA*/
    private void showAlertForCreatingNote(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this); //instancia del builder pop up con input
        if(title != null) builder.setTitle(title);
        if(message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_note,null);
        builder.setView(viewInflated);

        //Ponemos la vista inflada que va a ser la que va a tener el EditText del popup
        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewNote);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String note = input.getText().toString().trim();
                if(note.length() > 0)
                    createNewNote(note);

                else
                    Toast.makeText(getApplicationContext(), "The note can't be empty", Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void createNewNote(String note){
        realm.beginTransaction();

        Note _note = new Note(note);
        realm.copyToRealm(_note);
        //Guardamos la relacion con el board al que pertenece la nota
        board.getNotes().add(_note);
        realm.commitTransaction();

    }

    @Override
    public void onChange(Board element) {
        adapter.notifyDataSetChanged();
    }
}
