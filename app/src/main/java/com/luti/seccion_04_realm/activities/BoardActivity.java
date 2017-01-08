package com.luti.seccion_04_realm.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.luti.seccion_04_realm.R;
import com.luti.seccion_04_realm.adapters.BoardAdapter;
import com.luti.seccion_04_realm.models.Board;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class BoardActivity extends AppCompatActivity implements RealmChangeListener<RealmResults<Board>>, AdapterView.OnItemClickListener {

    private FloatingActionButton fab;
    private Realm realm;
    private ListView listView;
    private BoardAdapter adapter;

    //Resultados de la query contra la query de boards
    private RealmResults<Board> boards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        //DB Realm
        realm = Realm.getDefaultInstance();
        boards = realm.where(Board.class).findAll();

        //se añade listener para que capture los cambios en la lista de resultados.
        boards.addChangeListener(this);

        adapter = new BoardAdapter(this, boards, R.layout.list_view_board_item);
        listView = (ListView) findViewById(R.id.listViewBoard);
        listView.setAdapter(adapter);

        fab = (FloatingActionButton) findViewById(R.id.fabAddBoard);
       fab.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               showAlertForCreatingBoard("Add new board", "Type a name for your new board");
           }
       });
    }


    /*CRUD Actions*/
    private void createNewBoard(String boardName) {
        realm.beginTransaction();
        Board board = new Board(boardName);
        realm.copyToRealm(board);
        realm.commitTransaction();

    }



    /*Dialogs*/
    private void showAlertForCreatingBoard(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this); //instancia del builder pop up con input
        if(title != null) builder.setTitle(title);
        if(message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_board,null);
        builder.setView(viewInflated);

        //Ponemos la vista inflada que va a ser la que va a tener el EditText del popup
        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewBoard);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String boardName = input.getText().toString().trim();
                if(boardName.length() > 0)
                    createNewBoard(boardName);

                else
                    Toast.makeText(getApplicationContext(), "The name is required to create a new Board", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onChange(RealmResults<Board> element) {
        adapter.notifyDataSetChanged();
    }

    //Esto es para pasar a la siguiente activity pasandole el id del item del board que hemos pulsado
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(BoardActivity.this, NoteActivity.class);
        intent.putExtra("id", boards.get(position).getId());
        startActivity(intent);

    }
}
