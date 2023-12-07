package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.myapplication.Lib.LibSocket;
import com.example.myapplication.Lib.MyApplication;
import java.net.Socket;


public class MagasinActivity extends AppCompatActivity {

    private int currentIdArticle = 0;
    private Socket sSocket;
    private TextView textViewArticle;
    private TextView textViewPrix;
    private TextView textViewStock;
    private ImageView imageViewArticle;
    private EditText quantiteInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magasin);

        // Récupérer la socket depuis l'application
        MyApplication myApp = (MyApplication) getApplication();
        sSocket = myApp.getSocket();

        // Initialisez vos vues ici
        textViewArticle = findViewById(R.id.textViewArticle);
        textViewPrix = findViewById(R.id.textViewPrix);
        textViewStock = findViewById(R.id.textViewStock);
        imageViewArticle = findViewById(R.id.imageViewArticle);
        quantiteInput = findViewById(R.id.quantiteInput);

        new ConsultTask().execute(currentIdArticle);
    }

    public void consulterSuivant(View view) {
        if (currentIdArticle < 20) {
            currentIdArticle++;
        } else {
            currentIdArticle = 0;
        }
        new ConsultTask().execute(currentIdArticle);
    }

    public void consulterPrecedent(View view) {
        if (currentIdArticle > 0) {
            currentIdArticle--;
        } else {
            currentIdArticle = 20;
        }
        new ConsultTask().execute(currentIdArticle);
    }

    public void acheter(View view) {
        new AcheterTask().execute(String.valueOf(currentIdArticle), quantiteInput.getText().toString());
    }

    public void effectuerAchat(View v){
        Intent i = new Intent(this, PanierActivity.class);
        MyApplication myApp = (MyApplication) getApplication();
        myApp.setSocket(sSocket);
        startActivity(i);
    }

    private class ConsultTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {
            int id = params[0] + 1;
            LibSocket.send(sSocket, "CONSULT#" + id);
            return LibSocket.receive(sSocket);
        }

        @Override
        protected void onPostExecute(String response) {
            String[] mots = response.split("#");
            if (mots[0].equals("CONSULT") && !mots[1].equals("ko")) {
                // Mettez à jour vos vues avec les détails de l'article
                textViewArticle.setText(mots[2]);
                textViewPrix.setText(mots[3]);
                textViewStock.setText(mots[4]);
                String nomImage = mots[5].substring(0, mots[5].lastIndexOf('.')).toLowerCase();
                int resID = getResources().getIdentifier(nomImage, "drawable", getPackageName());
                ImageView imageViewArticle = findViewById(R.id.imageViewArticle);
                imageViewArticle.setImageResource(resID);

            } else {
                // Gérer l'erreur de consultation
            }
        }
    }

    private class AcheterTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            int idArticle = Integer.parseInt(params[0]) + 1;
            int quantite = Integer.parseInt(params[1]);
            LibSocket.send(sSocket, "ACHAT#" + idArticle + "#" + quantite);
            return LibSocket.receive(sSocket);
        }

        @Override
        protected void onPostExecute(String response) {
            String[] mots = response.split("#");
            if (mots[0].equals("ACHAT") && mots[1].equals("ok")) {
                // Mettez à jour vos vues ou effectuez d'autres actions après un achat réussi
            } else {
                // Gérer l'erreur d'achat
            }
        }
    }
}

