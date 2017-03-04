package com.luti.seccion_04_realm.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.luti.seccion_04_realm.R;
import com.luti.seccion_04_realm.activities.NoteActivity;
import com.luti.seccion_04_realm.app.OnSwipeTouchListener;
import com.luti.seccion_04_realm.interfaces.NoteFinal;
import com.luti.seccion_04_realm.models.Note;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Luti on 10/1/17.
 */
public class NoteAdapter extends BaseAdapter  {


    //constructo y atributos
    private Context context;
    private List<Note> list;
    private int layout;
    NoteFinal notaFinal;

    public NoteAdapter(Context context, List<Note> notes, int layout){
        this.context = context;
        this.list = notes;
        this.layout = layout;
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Note getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder vh;

        //Esto es si esta vacia, si no llama a la base de datos
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(layout, null);
            vh = new ViewHolder();
            vh.description = (TextView) convertView.findViewById(R.id.textViewNoteDescription);
            vh.createdAt = (TextView) convertView.findViewById(R.id.textViewNoteCreatedAt);
            vh.icon = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(vh);
        }else {
            vh = (ViewHolder) convertView.getTag();

        }

        Note note = list.get(position);
        boolean fin = note.isFinish();

        vh.description.setText(note.getDescription());
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String date = df.format(note.getCreatedAt());
        vh.createdAt.setText(date);
 /*       vh.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notaFinal.ValidarNota(position);
            }
        });*/

        if (fin == true) {
            //convertView.setBackgroundResource(R.color.colorResult);
            vh.icon.setImageResource(R.mipmap.ic_checktrue);
            int color1 = ContextCompat.getColor(context, R.color.colorCheckTrue);
            vh.description.setTextColor(color1);
        }
        else {
            //convertView.setBackgroundResource(R.color.colorTransparent);
            vh.icon.setImageResource(R.mipmap.ic_check);
            int color = ContextCompat.getColor(context, R.color.colorAccent);
            vh.description.setTextColor(color);

        }
        return convertView;
    }

    public class ViewHolder{
        TextView description;
        TextView createdAt;
        ImageView icon;
    }
}
