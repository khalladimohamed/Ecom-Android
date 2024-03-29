package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.example.myapplication.Reseaux.LibSocket;
import com.example.myapplication.Reseaux.MyApplication;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {

    private Socket sSocket;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }



    public void login(View v) {
        new ConnectToServerTask().execute();
    }



    private class ConnectToServerTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                sSocket = new Socket("192.168.109.130", 50000);
                String user = ((TextView) findViewById(R.id.loginInput)).getText().toString();
                String mdp = ((TextView) findViewById(R.id.mdpInput)).getText().toString();
                int nouveauClient = isNouveauClientChecked();

                // Envoi de la requête
                LibSocket.send(sSocket, "LOGIN#" + user + "#" + mdp + "#" + nouveauClient);

                // Réponse du Serveur
                return LibSocket.receive(sSocket);

            } catch (IOException ex) {
                ex.printStackTrace();
                return "Erreur de connexion";
            }
        }

        @Override
        protected void onPostExecute(String reponse) {
            String[] mots = reponse.split("#");

            if (mots[0].equals("LOGIN")) {
                if (mots[1].equals("ok")) {
                    // Passer la socket à l'activité suivante
                    Toast.makeText(LoginActivity.this, "Login réussi", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(LoginActivity.this, MagasinActivity.class);
                    MyApplication myApp = (MyApplication) getApplication();
                    myApp.setSocket(sSocket);
                    startActivity(i);
                } else if (mots[1].equals("ko")) {
                    // Gérer l'échec de la connexion
                    Toast.makeText(LoginActivity.this, mots[2], Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    public int isNouveauClientChecked() {
        Switch switchNewClient = findViewById(R.id.switchNewClient);

        if (switchNewClient.isChecked()) {
            return 1;
        } else {
            return 0;
        }
    }
}
