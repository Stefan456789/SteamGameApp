package at.htlgkr.steamgameapp;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import at.htlgkr.steam.Game;

public class GameAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private int layoutId;
    private List<Game> games;

    public GameAdapter(Context context, int listViewItemLayoutId, List<Game> games) {
        this.games = games;
        this.layoutId = listViewItemLayoutId;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return games.size();
    }

    @Override
    public Object getItem(int position) {
        return games.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View givenView, ViewGroup parent) {
        Game game = games.get(position);
        View listItem = givenView == null ? inflater.inflate(layoutId, null) : givenView;
        ((TextView) listItem.findViewById(R.id.name)).setText(game.getName());
        ((TextView) listItem.findViewById(R.id.date)).setText(Double.toString(game.getPrice()));
        SimpleDateFormat dateFormat = new SimpleDateFormat(Game.DATE_FORMAT);
        ((TextView) listItem.findViewById(R.id.price)).setText(dateFormat.format(game.getReleaseDate()));
        return listItem;
    }
}
