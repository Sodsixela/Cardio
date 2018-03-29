package e.administrateur.cardioproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ListInfo extends BaseAdapter {
    private String titles[];
    private String infos[];
    private Drawable icons[];
    private Context context;
    ListInfo(Context context, String[] title, String[] info, Drawable[] icons)
    {
        this.titles=title;
        this.infos=info;
        this.icons=icons;
        this.context=context;
    }
    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.list_info,null);
        ImageView icon =  view.findViewById(R.id.icon);
        TextView title=  view.findViewById(R.id.title);
        TextView info=  view.findViewById(R.id.info);
        //icon.setBackgroundResource(icons[position]);
        icon.setBackground(icons[position]);
        title.setText(titles[position]);
        info.setText(infos[position]);
        if(position==1)
        {
            title.setTextColor(Color.RED);
            info.setTextColor(Color.RED);

        }
        return view;
    }
}
