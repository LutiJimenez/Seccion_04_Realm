package com.luti.seccion_04_realm.activities;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
        //Muy importante poner esto para que me muestre el context menu al pulsar sobre el item
        registerForContextMenu(listView);

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

    //Creamos el dialogo para editar una nota

    private void showAlertForEditNote(String title, String message, final Note notas){
        AlertDialog.Builder builder = new AlertDialog.Builder(this); //instancia del builder pop up con input
        if(title != null) builder.setTitle(title);
        if(message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_note,null);
        builder.setView(viewInflated);

        //Ponemos la vista inflada que va a ser la que va a tener el EditText del popup
        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewNote);
        input.setText(notas.getDescription());

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String note = input.getText().toString().trim();
                if(notas.getDescription().equals(note)){
                    Toast.makeText(getApplicationContext(), "The note is equals", Toast.LENGTH_LONG).show();
                }
                else
                    editNote(note, notas);

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

    private void editNote(String newNoteDescription, Note note){
        realm.beginTransaction();
        note.setDescription(newNoteDescription);
        realm.copyToRealmOrUpdate(note);
        realm.commitTransaction();
    }
    private void deleteNote(Note note){
        realm.beginTransaction();
        note.deleteFromRealm();
        realm.commitTransaction();
    }

    private void deleteAll(){
        realm.beginTransaction();
        board.getNotes().deleteAllFromRealm();
        realm.commitTransaction();

    }


    /*Events*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.delete_note_All:
                deleteAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_menu_note_activity, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()){
            case R.id.delete_note:
                deleteNote(notes.get(info.position));
                return true;
            case R.id.edit_note:
                showAlertForEditNote("Edit Note", "Change description of the note", notes.get(info.position));
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    @Override
    public void onChange(Board element) {
        adapter.notifyDataSetChanged();
    }
}
