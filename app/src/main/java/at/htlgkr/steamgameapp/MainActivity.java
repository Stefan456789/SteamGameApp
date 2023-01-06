package at.htlgkr.steamgameapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import at.htlgkr.steam.Game;
import at.htlgkr.steam.ReportType;
import at.htlgkr.steam.SteamBackend;

public class MainActivity extends AppCompatActivity {
    private static final String GAMES_CSV = "games.csv";
    private SteamBackend backend;
    private List<Game> games;
    public GameAdapter gameAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadGamesIntoListView();
        setUpReportSelection();
        setUpSearchButton();
        setUpAddGameButton();
        setUpSaveButton();
    }

    private void loadGamesIntoListView() {
        backend = new SteamBackend();
        try {

            backend.loadGames(getAssets().open(GAMES_CSV));
        } catch (IOException e) {
            e.printStackTrace();
        }
        games = new ArrayList<>();
        games.addAll(backend.getGames());
        gameAdapter = new GameAdapter(this, R.layout.game_item_layout, games);
        ((ListView)findViewById(R.id.gamesList)).setAdapter(gameAdapter);
    }

    private void setUpReportSelection() {
        List<ReportTypeSpinnerItem> reports = new ArrayList<>();
        reports.add(new ReportTypeSpinnerItem(ReportType.NONE, SteamGameAppConstants.SELECT_ONE_SPINNER_TEXT));
        reports.add(new ReportTypeSpinnerItem(ReportType.SUM_GAME_PRICES, SteamGameAppConstants.SUM_GAME_PRICES_SPINNER_TEXT));
        reports.add(new ReportTypeSpinnerItem(ReportType.AVERAGE_GAME_PRICES, SteamGameAppConstants.AVERAGE_GAME_PRICES_SPINNER_TEXT));
        reports.add(new ReportTypeSpinnerItem(ReportType.UNIQUE_GAMES, SteamGameAppConstants.UNIQUE_GAMES_SPINNER_TEXT));
        reports.add(new ReportTypeSpinnerItem(ReportType.MOST_EXPENSIVE_GAMES, SteamGameAppConstants.MOST_EXPENSIVE_GAMES_SPINNER_TEXT));
        ArrayAdapter<ReportTypeSpinnerItem> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                reports);
        Spinner spinner = findViewById(R.id.chooseReport);
        spinner.setAdapter(adapter);
        MainActivity context = this;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                ReportType type = adapter.getItem(i).getType();
                dialog.setTitle(((TextView)view).getText().toString());
                switch (type){
                    case NONE:
                        return;
                    case AVERAGE_GAME_PRICES:
                        dialog.setMessage(SteamGameAppConstants.ALL_PRICES_AVERAGE + backend.averageGamePrice());
                        break;
                    case MOST_EXPENSIVE_GAMES:
                        dialog.setMessage(SteamGameAppConstants.MOST_EXPENSIVE_GAMES + backend.selectTopNGamesDependingOnPrice(3).stream().map(x -> x + "\n").collect(Collectors.joining()));
                        break;
                    case SUM_GAME_PRICES:
                        dialog.setMessage(SteamGameAppConstants.ALL_PRICES_SUM + backend.sumGamePrices());
                        break;
                    case UNIQUE_GAMES:
                        dialog.setMessage(SteamGameAppConstants.UNIQUE_GAMES_COUNT + backend.getUniqueGames().size());
                        break;
                }

                dialog.setNeutralButton("OK", null);
                dialog.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setUpSearchButton() {
        MainActivity context = this;
        ((Button)findViewById(R.id.search)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle(SteamGameAppConstants.ENTER_SEARCH_TERM);
                EditText edittext = new EditText(context);
                edittext.setId(R.id.dialog_search_field);
                dialog.setView(edittext);
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        games.removeIf(x -> true);
                        games.addAll(backend.getGames());
                        if (!edittext.getText().toString().equals(""))
                            games.removeIf(x -> !x.getName().toLowerCase().contains(edittext.getText().toString().toLowerCase()));
                        gameAdapter.notifyDataSetChanged();
                    }
                });
                dialog.setNegativeButton("Cancle", null);
                dialog.show();
            }
        });
    }

    private void setUpAddGameButton() {
        MainActivity context = this;
        ((Button)findViewById(R.id.addGame)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle(SteamGameAppConstants.NEW_GAME_DIALOG_TITLE);
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                EditText name = new EditText(context);
                name.setHint("Name");
                name.setId(R.id.dialog_name_field);
                layout.addView(name);

                EditText date = new EditText(context);
                date.setHint("Release Date");
                date.setId(R.id.dialog_date_field);
                layout.addView(date);

                EditText price = new EditText(context);
                price.setHint("Price");
                price.setId(R.id.dialog_price_field);
                layout.addView(price);

                dialog.setView(layout);


                SimpleDateFormat dateFormat = new SimpleDateFormat(Game.DATE_FORMAT);
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        games.removeIf(x -> true);
                        try {
                            backend.addGame(new Game(name.getText().toString(), dateFormat.parse(date.getText().toString()), Double.parseDouble(price.getText().toString())));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        games.addAll(backend.getGames());

                    }
                });

                dialog.setNegativeButton("Cancle", null);
                dialog.show();
            }
        });
    }

    private void setUpSaveButton() {
        ((Button)findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    backend.store(openFileOutput(SteamGameAppConstants.SAVE_GAMES_FILENAME, MODE_PRIVATE));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
