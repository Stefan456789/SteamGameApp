package at.htlgkr.steam;


import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SteamBackend {

    private List<Game> games;

    public SteamBackend() {
        // Implementieren Sie diesen Konstruktor.
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void loadGames(InputStream inputStream) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(Game.DATE_FORMAT);
        // Diese methode lädt alle Games in eine Variable welche sich im Steam Backend befinden muss.

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        games = reader.lines().map(x -> {

            try {
                return new Game(
                        x.split(";")[0],
                        dateFormat.parse(x.split(";")[1]),
                        Double.parseDouble(x.split(";")[2]));

            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void store(OutputStream fileOutputStream) {
        // Diese methode schreibt alle Games in den fileOutputStream.
        SimpleDateFormat dateFormat = new SimpleDateFormat(Game.DATE_FORMAT);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
        games.forEach(x -> {
            try {
                writer.write(x.getName() + ";" + dateFormat.format(x.getReleaseDate()) + ";" + x.getPrice() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }

    public void addGame(Game newGame) {
        if (games == null)
            games = new ArrayList<>();
        games.add(newGame);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public double sumGamePrices() {
        return games.stream().mapToDouble(Game::getPrice).sum();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public double averageGamePrice() {
        return games.stream().mapToDouble(Game::getPrice).average().orElse(-1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Game> getUniqueGames() {
        return games.stream().distinct().collect(Collectors.toList());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Game> selectTopNGamesDependingOnPrice(int n) {
        return games.stream().sorted((x,y) -> Double.compare(y.getPrice(),x.getPrice())).limit(n).collect(Collectors.toList());
    }
}