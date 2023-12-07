package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.myapplication.Lib.LibSocket;
import com.example.myapplication.Lib.MyApplication;
import java.net.Socket;

public class PanierActivity extends AppCompatActivity {

    private Socket sSocket;
    int idArticleAsupprimer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panier);

        // Récupérer la socket depuis l'application
        MyApplication myApp = (MyApplication) getApplication();
        sSocket = myApp.getSocket();

        idArticleAsupprimer = -1;

        viderTablePanier();
        new ActualiserPanierTask().execute();
    }



    public void supprimerArticle(View v){
        TableLayout tableLayout = findViewById(R.id.tableLayout);

        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            final TableRow tableRow = (TableRow) tableLayout.getChildAt(i);

            // Vérifiez si la ligne a au moins une vue enfant
            if (tableRow.getChildCount() > 0) {
                View firstChild = tableRow.getChildAt(0);

                // Vérifiez si la première vue est un TextView
                if (firstChild instanceof TextView) {
                    final String idValue = ((TextView) firstChild).getText().toString();
                    idArticleAsupprimer = Integer.parseInt(idValue);
                    System.out.println("ID de la ligne cliquée 2 : " + idArticleAsupprimer);

                    tableRow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // La ligne a été cliquée, et vous avez récupéré la valeur de la première colonne (id)
                            System.out.println("ID de la ligne cliquée : " + idArticleAsupprimer);
                            // Faites quelque chose avec la valeur de l'ID
                            // Par exemple, utilisez cette valeur pour effectuer une action spécifique.
                        }
                    });
                }
            }
        }

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
                //peut etre mettre un toast
            } else {
                // Gérer les erreurs en affichant un message d'erreur, si nécessaire
                //mainWindow.dialogueErreur(mots[0], "Error");
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
                viderTablePanier();
                new ActualiserPanierTask().execute();
            } else {
                // Gérer les erreurs en affichant un message d'erreur, si nécessaire
                //mainWindow.dialogueErreur(mots[0], "Error");
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
                viderTablePanier();
                //new ActualiserPanierTask().execute();
                //mainWindow.setTotal(0);
            } else {
                // Gérer les erreurs en affichant un message d'erreur, si nécessaire
                //mainWindow.dialogueErreur(mots[0], "Error");
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
                viderTablePanier();
                new ActualiserPanierTask().execute();
            } else {
                // Gérer les erreurs en affichant un message d'erreur, si nécessaire
                // Utilisez mots[0] pour obtenir le type d'erreur, et mots[2] pour le message d'erreur
                //mainWindow.dialogueErreur(mots[0], mots[2]);
            }
        }
    }



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

                        // Met à jour le total sur le thread principal
                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                panierActivity.setTotal(total);
                            }
                        });*/
                    }
                }
            } else {
                // Gère le cas où la réponse du serveur est incorrecte.
                System.out.println("La réponse du serveur est incorrecte.");
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
    }

}