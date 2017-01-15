package com.luti.seccion_04_realm.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.luti.seccion_04_realm.R;
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
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder vh;

        //Esto es si esta vacia, si no llama a la base de datos
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(layout, null);
            vh = new ViewHolder();
            vh.description = (TextView) convertView.findViewById(R.id.textViewNoteDescription);
            vh.createdAt = (TextView) convertView.findViewById(R.id.textViewNoteCreatedAt);
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

        if (fin == true)
            convertView.setBackgroundResource(R.color.colorResult);
        else
            convertView.setBackgroundResource(R.color.colorTransparent);

        return convertView;
    }

    public class ViewHolder{
        TextView description;
        TextView createdAt;
    }
}
