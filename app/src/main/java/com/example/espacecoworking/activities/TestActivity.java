package com.example.espacecoworking.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.espacecoworking.R;
import com.example.espacecoworking.adapters.SpaceAdapter;

import com.example.espacecoworking.models.Space;
import com.example.espacecoworking.models.SpaceImages;

import com.example.espacecoworking.repository.Repository;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class TestActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private SpaceAdapter adapter;
    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialisation des vues
        recyclerView = findViewById(R.id.recyclerViewSpaces);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 2. Initialisation du Repository
        repository = Repository.getInstance(this);

        // 3. Initialisation des données de test
        if (repository.getAllSpaces().isEmpty()) {
            initializeDefaultData();
            Toast.makeText(this, "Données de démo créées !", Toast.LENGTH_SHORT).show();
        }

        // 4. Charger et afficher les données
        loadSpaces();
    }

    private void loadSpaces() {
        List<Space> spaces = repository.getAllSpaces();
        adapter = new SpaceAdapter(this, spaces);
        recyclerView.setAdapter(adapter);
    }

    // données test
    private void initializeDefaultData() {

        // --- ESPACE 1
        Space space1 = new Space();
        space1.setName("Salle Jupiter");
        space1.setLocation("Rabat - Centre Ville");
        space1.setCapacity(10);
        space1.setDescription("Une salle moderne située au cœur de Rabat, parfaite pour les réunions d'équipe. Café illimité et Fibre optique incluse.");
        space1.setPrice(150.0);

        long id1 = repository.addSpace(space1);
        if (id1 != -1) {
            // Ajoute 3 images
            insertDrawableImage(R.drawable.img, (int) id1);
            insertDrawableImage(R.drawable.salle_mars, (int) id1);
            insertDrawableImage(R.drawable.salle_jupiter, (int) id1);
        }

        // --- ESPACE 2
        Space space2 = new Space();
        space2.setName("Espace Mars");
        space2.setLocation("Casablanca - Maarif");
        space2.setCapacity(25);
        space2.setDescription("Un grand open-space lumineux à Casablanca. Idéal pour les startups et les freelances. Accès 24/7.");
        space2.setPrice(300.50);
        long id2 = repository.addSpace(space2);
        if (id2 != -1) {
            insertDrawableImage(R.drawable.salle_mars, (int) id2);
            insertDrawableImage(R.drawable.salle_mars, (int) id2);
        }

        // --- ESPACE 3
        Space space3 = new Space();
        space3.setName("Bureau Mercure");
        space3.setLocation("Tanger - Marina");
        space3.setCapacity(4);
        space3.setDescription("Petit bureau privé avec vue sur mer. Calme absolu pour les appels importants.");
        space3.setPrice(200.0);
        long id3 = repository.addSpace(space3);
        if (id3 != -1) {
            insertDrawableImage(R.drawable.salle_jupiter, (int) id3);
        }
    }

    // --- Drawable -> Base de données
    private void insertDrawableImage(int drawableId, int spaceId) {
        try {
            // 1. Lire l'image depuis les ressources
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableId);

            // Sécurité : Si l'image n'est pas trouvée, on arrête pour éviter le crash
            if (bitmap == null) return;

            // 2. Redimensionner si nécessaire
            if (bitmap.getWidth() > 800) {
                float aspectRatio = (float) bitmap.getWidth() / bitmap.getHeight();
                int newHeight = (int) (800 / aspectRatio);
                bitmap = Bitmap.createScaledBitmap(bitmap, 800, newHeight, true);
            }

            // 3. Compresser
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            byte[] imageBytes = stream.toByteArray();

            // 4. Créer l'objet et insérer
            SpaceImages img = new SpaceImages();
            img.setSpaceId(spaceId);
            img.setImage(imageBytes);

            repository.addSpaceImage(img);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*    private static final String TAG = "TEST1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- DÉBUT DU TEST ---
        Log.d(TAG, "=== Démarrage des tests du Membre 1 ===");

        // 1. Initialiser le Repository
        Repository repo = Repository.getInstance(this);

        // 2. Test Inscription (User)
        User newUser = new User();
        newUser.setName("Test User");
        newUser.setEmail("test@email.com");
        newUser.setPassword("pass123"); // Mot de passe initial
        newUser.setRole("CLIENT");

        if (!repo.emailExists(newUser.getEmail())) {
            repo.addUser(newUser);
            Log.d(TAG, "Inscription : Succès");
        }

        // 3. Test Connexion (Hachage)
        Log.d(TAG, "--- Test Sécurité & Connexion ---");

        // Récupération de l'utilisateur pour les tests suivants
        User goodLogin = repo.authenticateUser("test@email.com", "pass123");

        if (goodLogin != null) {
            Log.d(TAG, "✅ Connexion initiale réussie.");

            // Création Client si nécessaire
            if (!repo.clientExists(goodLogin.getUserId())) {
                Client client = new Client(goodLogin.getUserId(), "Wifi, Café", null, null);
                repo.addClient(client);
            }

            // ====================================================
            // NOUVEAU TEST : CHANGEMENT DE MOT DE PASSE
            // ====================================================
            Log.d(TAG, "--- Test Changement Mot de Passe ---");

            int userId = goodLogin.getUserId();

            // SCÉNARIO A : Essai avec un MAUVAIS ancien mot de passe
            // On essaie de changer en mettant "toto" au lieu de "pass123"
            boolean failResult = repo.changePassword(userId, "toto", "newPass456");

            if (!failResult) {
                Log.d(TAG, "✅ Sécurité OK : Le changement a été refusé avec le mauvais ancien mot de passe.");
            } else {
                Log.e(TAG, "❌ GRAVE : Le mot de passe a été changé alors que l'ancien était faux !");
            }

            // SCÉNARIO B : Essai avec le BON ancien mot de passe
            // On change "pass123" -> "newPass456"
            boolean successResult = repo.changePassword(userId, "pass123", "newPass456");

            if (successResult) {
                Log.d(TAG, "✅ Update OK : Le changement de mot de passe a réussi en base.");

                // SCÉNARIO C : Vérification finale (Connexion avec le NOUVEAU pass)
                User newLogin = repo.authenticateUser("test@email.com", "newPass456");
                if (newLogin != null) {
                    Log.d(TAG, "✅ CONFIRMÉ : Connexion réussie avec le NOUVEAU mot de passe.");
                } else {
                    Log.e(TAG, "❌ ERREUR : Impossible de se connecter avec le nouveau mot de passe.");
                }

                // Vérification que l'ANCIEN ne marche plus
                if (repo.authenticateUser("test@email.com", "pass123") == null) {
                    Log.d(TAG, "✅ CONFIRMÉ : L'ancien mot de passe ne fonctionne plus.");
                }

            } else {
                Log.e(TAG, "❌ ERREUR : Le changement de mot de passe a échoué (bonnes infos pourtant).");
            }
            // ====================================================

        } else {
            Log.e(TAG, "❌ ERREUR : Connexion échouée dès le début (Impossible de tester le changement de mdp).");
        }

        // 4. Test Création Espace
        Log.d(TAG, "--- Test Espaces ---");
        // On crée un owner fictif pour respecter la Foreign Key (si nécessaire)
        // Note: Ici on suppose que l'ID 1 existe.
        Space newSpace = new Space();
        newSpace.setName("Salle Jupiter");
        newSpace.setLocation("Paris");
        newSpace.setCapacity(10);
        newSpace.setDescription("Super salle");

        long spaceId = repo.addSpace(newSpace);
        Log.d(TAG, "Espace créé avec ID : " + spaceId);

        // 5. Test Réservation & Conflits
        Log.d(TAG, "--- Test Réservations & Conflits ---");

        // Réservation A : 10h00 -> 12h00 le 2024-01-01
        Booking booking1 = new Booking();
        booking1.setSpaceId((int)spaceId);
        booking1.setClientId(goodLogin != null ? goodLogin.getUserId() : 1);
        booking1.setDate("2024-01-01");
        booking1.setStartTime("10:00");
        booking1.setEndTime("12:00");
        booking1.setStatus("CONFIRMED");

        long res1 = repo.addBooking(booking1);
        if (res1 > 0) Log.d(TAG, "✅ Réservation 1 (10h-12h) ajoutée.");
        else Log.e(TAG, "❌ Erreur ajout Réservation 1.");

        // Réservation B : 11h00 -> 13h00 (Doit échouer car chevauchement !)
        Booking booking2 = new Booking();
        booking2.setSpaceId((int)spaceId);
        booking2.setClientId(goodLogin != null ? goodLogin.getUserId() : 1);
        booking2.setDate("2024-01-01");
        booking2.setStartTime("11:00"); // Chevauche la fin de la précédente
        booking2.setEndTime("13:00");
        booking2.setStatus("PENDING");

        long res2 = repo.addBooking(booking2);
        if (res2 == -1) {
            Log.d(TAG, "✅ Conflit détecté correctement ! La réservation 2 a été bloquée.");
        } else {
            Log.e(TAG, "❌ ERREUR : Le conflit n'a pas été détecté ! ID retourné : " + res2);
        }

        // 6. Vérification finale
        List<Booking> list = repo.getAllBookings();
        Log.d(TAG, "Nombre total de réservations en base : " + list.size());

        Log.d(TAG, "=== Fin des tests ===");
    }

 */

