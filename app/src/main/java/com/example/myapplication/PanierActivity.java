package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Reseaux.LibSocket;
import com.example.myapplication.Reseaux.MyApplication;
import java.net.Socket;

public class PanierActivity extends AppCompatActivity {

    private Socket sSocket;
    int idArticleAsupprimer;
    private EditText IdArticleSupprimerInput;
    private TextView textViewTotal;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panier);

        // Récupérer la socket depuis l'application
        MyApplication myApp = (MyApplication) getApplication();
        sSocket = myApp.getSocket();

        IdArticleSupprimerInput = findViewById(R.id.IdArticleSupprimerInput);
        textViewTotal = findViewById(R.id.textViewTotal);

        idArticleAsupprimer = -1;

        viderTablePanier();
        new ActualiserPanierTask().execute();
    }



    public void supprimerArticle(View v){
        idArticleAsupprimer = Integer.parseInt(IdArticleSupprimerInput.getText().toString());

        new SupprimerArticleTask().execute();
    }



    public void viderPanier(View v){
        new ViderPanierTask().execute();
    }



    public void confirmerAchat(View v){
        new ConfirmerAchatTask().execute();
    }



    public void deconnexion(View v){
        viderTablePanier();
        new ViderPanierTask().execute();
        new deconnexionTask().execute();
        Intent i = new Intent(this, LoginActivity.class);
        MyApplication myApp = (MyApplication) getApplication();
        myApp.setSocket(null);
        startActivity(i);
    }



    // AsyncTask pour la deconnexion
    private class deconnexionTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            // Envoi de la requête pour supprimer un article
            LibSocket.send(sSocket, "LOGOUT");

            // Réponse du serveur
            return LibSocket.receive(sSocket);
        }

        @Override
        protected void onPostExecute(String reponse) {
            // Traitement de la réponse du serveur après la suppression de l'article
            String[] mots = reponse.split("#");

            if (mots[1].equals("ok")) {
                Toast.makeText(PanierActivity.this, "Deconnexion réussi", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PanierActivity.this, "Erreur de deconnexion", Toast.LENGTH_SHORT).show();
            }
        }
    }



    // AsyncTask pour supprimer un article
    private class SupprimerArticleTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            // Envoi de la requête pour supprimer un article
            LibSocket.send(sSocket, "CANCEL#" + idArticleAsupprimer);

            // Réponse du serveur
            return LibSocket.receive(sSocket);
        }

        @Override
        protected void onPostExecute(String reponse) {
            // Traitement de la réponse du serveur après la suppression de l'article
            String[] mots = reponse.split("#");

            if (mots[1].equals("ok")) {
                Toast.makeText(PanierActivity.this, "Suppression réussi", Toast.LENGTH_SHORT).show();
                viderTablePanier();
                new ActualiserPanierTask().execute();
            } else {
                Toast.makeText(PanierActivity.this, "Erreur de supprimer", Toast.LENGTH_SHORT).show();
            }
        }
    }



    // AsyncTask pour vider le panier
    private class ViderPanierTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            // Envoi de la requête pour vider le panier
            LibSocket.send(sSocket, "CANCEL_ALL");

            // Réponse du serveur
            return LibSocket.receive(sSocket);
        }

        @Override
        protected void onPostExecute(String reponse) {
            // Traitement de la réponse du serveur après le vidage du panier
            String[] mots = reponse.split("#");

            if (mots[1].equals("ok")) {
                Toast.makeText(PanierActivity.this, "Vidage du panier réussi", Toast.LENGTH_SHORT).show();
                viderTablePanier();
            } else {
                Toast.makeText(PanierActivity.this, "Erreur du vidage du panier", Toast.LENGTH_SHORT).show();
            }
        }
    }



    // AsyncTask pour confirmer l'achat
    private class ConfirmerAchatTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            // Envoi de la requête pour confirmer l'achat
            LibSocket.send(sSocket, "CONFIRMER");

            // Réponse du serveur
            return LibSocket.receive(sSocket);
        }

        @Override
        protected void onPostExecute(String reponse) {
            // Traitement de la réponse du serveur après la confirmation de l'achat
            String[] mots = reponse.split("#");

            if (mots[1].equals("ok")) {
                Toast.makeText(PanierActivity.this, "Confirmation de l'achat réussi", Toast.LENGTH_SHORT).show();
                viderTablePanier();
                new ActualiserPanierTask().execute();
            } else {
                Toast.makeText(PanierActivity.this, "Erreur confirmation achat", Toast.LENGTH_SHORT).show();
            }
        }
    }



    // AsyncTask pour actualiser le panier
    private class ActualiserPanierTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            // Envoie la requête au serveur pour récupérer le contenu du caddie
            LibSocket.send(sSocket, "CADDIE");

            // Reçoit la réponse du serveur
            return LibSocket.receive(sSocket);
        }

        @Override
        protected void onPostExecute(String reponse) {
            // Analyse la réponse
            String[] mots = reponse.split("#");

            // Vérifie si la réponse est conforme
            if (mots[0].equals("CADDIE") && mots[1].equals("ok")) {
                String contenu;

                // Vérifie si la réponse contient des données
                if (mots.length >= 4) {
                    contenu = mots[3];

                    // Vérifie si le caddie est vide
                    if (contenu.isEmpty()) {
                        System.out.println("Le caddie est vide.");
                    } else {
                        String[] articles = contenu.split(";");

                        float total = 0;

                        // Parcourt les articles dans la réponse
                        for (String article : articles) {
                            String[] attributs = article.split(",");
                            if (attributs.length == 4) {
                                final int idArticle = Integer.parseInt(attributs[0]);
                                final String intitule = attributs[1];
                                final int quantite = Integer.parseInt(attributs[2]);
                                final float prix = Float.parseFloat(attributs[3]);

                                total += prix * quantite;
                                System.out.println("ID: " + idArticle + ", Intitule: " + intitule + ", Quantite: " + quantite + ", Prix: " + prix);

                                // Ajoute l'article au tableau du panier sur le thread principal
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ajouteArticleTablePanier(idArticle, intitule, prix, quantite);
                                    }
                                });
                            }
                        }

                        // Mettre à jour le total sur le thread principal
                        float finalTotal = total;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewTotal.setText(String.format("%.2f", finalTotal));
                            }
                        });
                    }
                }
            } else {
                // Gère le cas où la réponse du serveur est incorrecte.
                System.out.println("La réponse du serveur est incorrecte");
            }
        }
    }



    private void ajouteArticleTablePanier(final int idArticle, final String intitule, final float prix, final int quantite) {
        // Crée une nouvelle rangée pour l'article
        TableRow row = new TableRow(this);

        // Ajoute les colonnes (idArticle, intitule, prix, quantite)
        TextView textViewId = createTextView(String.valueOf(idArticle));
        TextView textViewIntitule = createTextView(intitule);
        TextView textViewPrix = createTextView(String.valueOf(prix));
        TextView textViewQuantite = createTextView(String.valueOf(quantite));

        // Ajoute les colonnes à la rangée
        row.addView(textViewId);
        row.addView(textViewIntitule);
        row.addView(textViewPrix);
        row.addView(textViewQuantite);

        // Ajoute la rangée à la table du panier
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        tableLayout.addView(row);
    }



    // Utilitaire pour créer un TextView avec une valeur spécifiée
    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(23);
        textView.setPadding(16, 30, 16, 30);
        return textView;
    }



    private void viderTablePanier() {
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        tableLayout.removeAllViews();
        textViewTotal.setText(Float.toString(0F));
    }

}