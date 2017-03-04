package com.luti.seccion_04_realm.activities;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.luti.seccion_04_realm.R;
import com.luti.seccion_04_realm.adapters.NoteAdapter;
import com.luti.seccion_04_realm.app.SwipeDetector;
import com.luti.seccion_04_realm.models.Board;
import com.luti.seccion_04_realm.models.Note;
import com.luti.seccion_04_realm.util.PermissionsUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;

import static android.R.attr.width;

public class NoteActivity extends AppCompatActivity  implements RealmChangeListener<Board>//, NoteFinal {
{
    private ListView listView;
    private FloatingActionButton fab;
    private ImageView img;


    private NoteAdapter adapter;
    private RealmList<Note> notes;
    private Realm realm;
    private Board board;

    //Recogemos el id del Board para recuperar el objeto completo
    private int boardId;

    //Permisos de Calendario
    private final int CALENDAR = 100;


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

//create a swipe detector for items in the list
        final SwipeDetector swipeDetector = new SwipeDetector();
        //add a touch listener for the list view
        listView.setOnTouchListener(swipeDetector);
        //also add a click listener and use it to get position in list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TranslateAnimation anim_enter_del;
                final Note[] nota = {new Note()};
                if (swipeDetector.swipeDetected()){
                    //get the object's position in the list
                    SwipeDetector.Action ac = swipeDetector.getAction();
                    if (ac.equals(SwipeDetector.Action.LR)){
                        nota[0] = adapter.getItem(position);
                        //delete the object from DB
                        editNote2(nota[0], true);
                    }
                    else if (ac.equals(SwipeDetector.Action.RL)) {
                        final int pos = position;
                        final Note[] not = {nota[0]};

                        Log.d("SWIPE RIGHT", "SWIPE RIGHT");
                        anim_enter_del = new TranslateAnimation(- 30/100 * width, 0, 0, 0);
                        anim_enter_del.setDuration(800);
                        anim_enter_del.setFillAfter(true);
                        //del.setTag("VISIBLE");
                        listView.startAnimation(anim_enter_del);
                        //del.startAnimation(anim_enter_del);
                        anim_enter_del.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                not[0] = adapter.getItem(pos);
                                //delete the object from DB
                                editNote2(not[0], false);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                }
                    //notify user of the removal
                    //Toast.makeText(getApplicationContext(), "Deleted from " + nota[0].getDescription(), Toast.LENGTH_LONG).show();
                    //update the view without the removed object
                    //getCurrentExercisesInWorkout();
                }
                else {
                    if (!view.equals(view.findViewById(R.id.icon)) ) {

                        Note not = adapter.getItem(position);
                        if (not.isFinish())
                            editNote2(not, false);
                        else
                            editNote2(not, true);
                    }
                }
            }
        });


        //PERMISOS
        checkPermissions();
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
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        //final boolean fin = (boolean) viewInflated.findViewById(R.id.checkBoxFin);

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
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(notas.getDescription());
        //final ImageView image = (ImageView) viewInflated.findViewById(R.id.icon);
        final CheckBox fin = (CheckBox) viewInflated.findViewById(R.id.checkBoxFin);
        if (notas.isFinish() == true)
            fin.setChecked(true);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String note = input.getText().toString().trim();
                boolean finish = false;
                if (fin.isChecked()) {
                    finish = true;
                    //image.setImageResource(R.mipmap.ic_checkTrue);

                }
                else
                    finish = false;
                    //image.setImageResource(R.mipmap.ic_checkTrue);

/*                if(notas.getDescription().equals(note)){
                    Toast.makeText(getApplicationContext(), "The note is equals", Toast.LENGTH_LONG).show();
                }
                else*/

                    editNote(note, notas, finish);

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void createNewNote(String note){
        realm.beginTransaction();

        Note _note = new Note(note, false);
        realm.copyToRealm(_note);
        //Guardamos la relacion con el board al que pertenece la nota
        board.getNotes().add(_note);
        realm.commitTransaction();

    }

    private void editNote(String newNoteDescription, Note note, boolean fin){
        realm.beginTransaction();
        note.setDescription(newNoteDescription);
        note.setFinish(fin);
        realm.copyToRealmOrUpdate(note);
        realm.commitTransaction();
    }

    private void editNote2( Note note, boolean fin){
        realm.beginTransaction();
        note.setFinish(fin);
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
        String texto = board.getTitle() + "-" + notes.get(info.position).getDescription();
        switch (item.getItemId()){
            case R.id.delete_note:
                deleteNote(notes.get(info.position));
                return true;
            case R.id.edit_note:
                showAlertForEditNote("Edit Note", "Change description of the note", notes.get(info.position));
                return true;
            case R.id.add_calendar:
                //if(PermissionsUtil.checkPermissions(this, PermissionsUtil.getPermissions(this)))
                addEventToCalendar(this, texto);
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    //añadir al calendario la nota del board
    private void addEventToCalendar(Activity activity,String newNoteDescription ){
        Calendar cal = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();


        int dia = calendar.get(Calendar.DATE);
        int mes = calendar.get(Calendar.MONTH);
        int annio = calendar.get(Calendar.YEAR);

        cal.set(Calendar.DAY_OF_MONTH,dia);
        cal.set(Calendar.MONTH, mes);
        cal.set(Calendar.YEAR, annio);

        //cal.set(Calendar.HOUR_OF_DAY, 22);
        //cal.set(Calendar.MINUTE, 45);

        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.getTimeInMillis()+60*60*1000);

        intent.putExtra(CalendarContract.Events.ALL_DAY, false);
        intent.putExtra(CalendarContract.Events.RRULE , "");
        intent.putExtra(CalendarContract.Events.TITLE, newNoteDescription);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, "Descripción");
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION,"Calle ....");

        activity.startActivity(intent);
    }


    //Prueba Calendario




    //Funcionalidad para para el click de la imagen
/*    @Override
    public void ValidarNota(int position) {
        Note nota = adapter.getItem(position);
        if (nota.isFinish())
            editNote2(nota, false);
        else
            editNote2(nota, true);
    }*/

//VENTANA DE PERMISOS
private void checkPermissions(){
    PermissionsUtil.askPermissions(this);
}

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionsUtil.PERMISSION_ALL: {

                if (grantResults.length > 0) {

                    List<Integer> indexesOfPermissionsNeededToShow = new ArrayList<>();

                    for(int i = 0; i < permissions.length; ++i) {
                        if(ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                            indexesOfPermissionsNeededToShow.add(i);
                        }
                    }

                    int size = indexesOfPermissionsNeededToShow.size();
                    if(size != 0) {
                        int i = 0;
                        boolean isPermissionGranted = true;

                        while(i < size && isPermissionGranted) {
                            isPermissionGranted = grantResults[indexesOfPermissionsNeededToShow.get(i)]
                                    == PackageManager.PERMISSION_GRANTED;
                            i++;
                        }

                        if(!isPermissionGranted) {

                            showDialogNotCancelable("Permissions mandatory",
                                    "Calendar permissions is required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            checkPermissions();
                                        }
                                    });
                        }
                    }
                }
            }
        }
    }

    private void showDialogNotCancelable(String title, String message,
                                         DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setCancelable(false)
                .create()
                .show();
    }


    @Override
    public void onChange(Board element) {
        adapter.notifyDataSetChanged();
    }
}
