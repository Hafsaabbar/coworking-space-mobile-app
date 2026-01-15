package com.example.espacecoworking.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;

import com.example.espacecoworking.database.*;
import com.example.espacecoworking.models.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class Repository {

    private static Repository instance;

    private UserDao userDao;
    private ClientDao clientDao;
    private SpaceDao spaceDao;
    private SpaceImagesDao spaceImagesDao;
    private BookingDao bookingDao;
    private AvailabilityDao availabilityDao;
    private SpaceOwnerDao spaceOwnerDao;

    private Repository(Context context) {
        Context appContext = context.getApplicationContext();
        this.userDao = new UserDao(appContext);
        this.clientDao = new ClientDao(appContext);
        this.spaceDao = new SpaceDao(appContext);
        this.spaceImagesDao = new SpaceImagesDao(appContext);
        this.bookingDao = new BookingDao(appContext);
        this.availabilityDao = new AvailabilityDao(appContext);
        this.spaceOwnerDao = new SpaceOwnerDao(appContext);
    }

    // Méthode d'accès statique (Singleton)
    public static synchronized Repository getInstance(Context context) {
        if (instance == null) {
            instance = new Repository(context);
        }
        return instance;
    }

    // USER
    private String hashPassword(String plainText) {
        if (plainText == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(plainText.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return plainText; // Fallback en cas d'erreur (peu probable)
        }
    }

    public long addUser(User user) {
        // Hachage du mot de passe avant de l'enregistrer dans la base
        if (user.getPassword() != null) {
            String hashedPassword = hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
        }
        return userDao.addUser(user);
    }

    public User getUserById(int userId) {
        return userDao.getUserById(userId);
    }

    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    public List<User> getUsersByRole(String role) {
        return userDao.getUsersByRole(role);
    }

    public int updateUser(User user) {
        return userDao.updateUser(user);
    }

    public int deleteUser(int userId) {
        return userDao.deleteUser(userId);
    }

    public User authenticateUser(String email, String password) {
        // On hache le mot de passe saisi pour voir s'il correspond au hash stocké
        String hashedPassword = hashPassword(password);
        return userDao.authenticateUser(email, hashedPassword);
    }

    public boolean emailExists(String email) {
        return userDao.emailExists(email);
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        if (oldPassword == null || newPassword == null) return false;

        User user = getUserById(userId);
        if (user == null) return false;

        // Vérifier l'ancien mot de passe (comparaison de hash)
        // Attention : on vérifie aussi que le mot de passe en base n'est pas null
        if (user.getPassword() == null || !user.getPassword().equals(hashPassword(oldPassword))) {
            return false;
        }

        user.setPassword(hashPassword(newPassword));
        return updateUser(user) > 0;
    }

    // CLIENT
    public long addClient(Client client) {
        return clientDao.addClient(client);
    }

    public Client getClientById(int clientId) {
        return clientDao.getClientById(clientId);
    }

    public List<Client> getAllClients() {
        return clientDao.getAllClients();
    }

    public int updateClient(Client client) {
        return clientDao.updateClient(client);
    }

    public int deleteClient(int clientId) {
        return clientDao.deleteClient(clientId);
    }

    public boolean clientExists(int clientId) {
        return clientDao.clientExists(clientId);
    }

    // SPACE
    public long addSpace(Space space) {
        return spaceDao.addSpace(space);
    }

    public Space getSpaceById(int spaceId) {
        return spaceDao.getSpaceById(spaceId);
    }

    public List<Space> getAllSpaces() {
        return spaceDao.getAllSpaces();
    }

    public List<Space> getSpacesByLocation(String location) {
        return spaceDao.getSpacesByLocation(location);
    }

    public List<Space> getSpacesByMinCapacity(int minCapacity) {
        return spaceDao.getSpacesByMinCapacity(minCapacity);
    }

    public List<Space> getSpacesByOwnerId(int ownerId) {
        return spaceDao.getSpacesByOwnerId(ownerId);
    }

    public List<Space> searchSpaces(String keyword) {
        return spaceDao.searchSpaces(keyword);
    }

    public int updateSpace(Space space) {
        return spaceDao.updateSpace(space);
    }

    public int deleteSpace(int spaceId) {
        return spaceDao.deleteSpace(spaceId);
    }

    // SPACE IMAGES
    public long addSpaceImage(SpaceImages image) {
        return spaceImagesDao.addSpaceImage(image);
    }

    public SpaceImages getImageById(int imageId) {
        return spaceImagesDao.getImageById(imageId);
    }

    public List<SpaceImages> getAllImages() {
        return spaceImagesDao.getAllImages();
    }

    public List<SpaceImages> getImagesBySpaceId(int spaceId) {
        return spaceImagesDao.getImagesBySpaceId(spaceId);
    }

    public SpaceImages getFirstImageBySpaceId(int spaceId) {
        return spaceImagesDao.getFirstImageBySpaceId(spaceId);
    }

    public int getImageCountBySpaceId(int spaceId) {
        return spaceImagesDao.getImageCountBySpaceId(spaceId);
    }

    public int updateSpaceImage(SpaceImages image) {
        return spaceImagesDao.updateSpaceImage(image);
    }

    public int deleteSpaceImage(int imageId) {
        return spaceImagesDao.deleteImage(imageId);
    }

    public int deleteImagesBySpaceId(int spaceId) {
        return spaceImagesDao.deleteImagesBySpaceId(spaceId);
    }

    // BOOKING

    /**
     * Valide que la date/heure n'est pas dans le passé
     * @return true si valide, false sinon
     */
    public boolean isValidBookingDateTime(String date, String startTime, String endTime) {
        try {
            // Format : "yyyy-MM-dd HH:mm"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            // Obtenir l'heure actuelle
            Date now = new Date();

            // Parser la date et l'heure de début
            Date bookingStart = sdf.parse(date + " " + startTime);
            Date bookingEnd = sdf.parse(date + " " + endTime);

            // Vérifications :
            // 1. La date/heure de début doit être >= à maintenant
            // 2. La date/heure de fin doit être > à la date/heure de début
            boolean isStartValid = bookingStart.getTime() >= now.getTime();
            boolean isEndValid = bookingEnd.getTime() > bookingStart.getTime();

            return isStartValid && isEndValid;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Met à jour automatiquement les réservations "confirmed" expirées en "completed"
     */
    public void updateExpiredBookingsToCompleted() {
        try {
            List<Booking> allBookings = getAllBookings();
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            for (Booking booking : allBookings) {
                // Ne traiter que les réservations "confirmed"
                if ("confirmed".equals(booking.getStatus())) {
                    try {
                        // Parser l'heure de fin de la réservation
                        Date endDateTime = sdf.parse(booking.getDate() + " " + booking.getEndTime());

                        // Si l'heure actuelle est après l'heure de fin
                        if (now.getTime() > endDateTime.getTime()) {
                            updateBookingStatus(booking.getBookingId(), "completed");
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Ajoute une réservation en vérifiant d'abord la disponibilité.
     *
     * @return ID de la réservation si succès, -1 si conflit, -3 si date/heure invalide
     */
    public long addBooking(Booking booking) {
        // Vérifier que la date/heure n'est pas dans le passé
        if (!isValidBookingDateTime(booking.getDate(), booking.getStartTime(), booking.getEndTime())) {
            return -3; // Code spécial : date/heure invalide
        }

        // Logique Métier : Vérification de conflit automatique
        boolean conflict = bookingDao.hasConflict(
                booking.getSpaceId(),
                booking.getDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                -1 // -1 car c'est une nouvelle réservation (pas d'ID à exclure)
        );

        if (conflict) {
            return -1; // Code spécial pour dire "Occupé"
        }

        return bookingDao.addBooking(booking);
    }

    public Booking getBookingById(int bookingId) {
        return bookingDao.getBookingById(bookingId);
    }

    public List<Booking> getAllBookings() {
        return bookingDao.getAllBookings();
    }

    public List<Booking> getBookingsByClientId(int clientId) {
        return bookingDao.getBookingsByClientId(clientId);
    }

    public List<Booking> getBookingsBySpaceId(int spaceId) {
        return bookingDao.getBookingsBySpaceId(spaceId);
    }

    public List<Booking> getBookingsByStatus(String status) {
        return bookingDao.getBookingsByStatus(status);
    }

    public List<Booking> getBookingsByDate(String date) {
        return bookingDao.getBookingsByDate(date);
    }

    public List<Booking> getBookingsBySpaceAndDate(int spaceId, String date) {
        return bookingDao.getBookingsBySpaceAndDate(spaceId, date);
    }

    public boolean hasConflict(int spaceId, String date, String startTime, String endTime, int excludeBookingId) {
        return bookingDao.hasConflict(spaceId, date, startTime, endTime, excludeBookingId);
    }

    public int updateBooking(Booking booking) {
        return bookingDao.updateBooking(booking);
    }

    public int updateBookingStatus(int bookingId, String status) {
        return bookingDao.updateBookingStatus(bookingId, status);
    }

    public int deleteBooking(int bookingId) {
        return bookingDao.deleteBooking(bookingId);
    }

    // AVAILABILITY
    public long addAvailability(SpaceAvailability availability) {
        return availabilityDao.addAvailability(availability);
    }

    public SpaceAvailability getAvailabilityById(int availabilityId) {
        return availabilityDao.getAvailabilityById(availabilityId);
    }

    public List<SpaceAvailability> getAllAvailabilities() {
        return availabilityDao.getAllAvailabilities();
    }

    public List<SpaceAvailability> getAvailabilitiesBySpaceId(int spaceId) {
        return availabilityDao.getAvailabilitiesBySpaceId(spaceId);
    }

    public List<SpaceAvailability> getAvailabilitiesBySpaceAndDay(int spaceId, String dayOfWeek) {
        return availabilityDao.getAvailabilitiesBySpaceAndDay(spaceId, dayOfWeek);
    }

    public List<SpaceAvailability> getAvailableSlots(int spaceId, String dayOfWeek) {
        return availabilityDao.getAvailableSlots(spaceId, dayOfWeek);
    }

    public int updateAvailability(SpaceAvailability availability) {
        return availabilityDao.updateAvailability(availability);
    }

    public int updateAvailabilityStatus(int availabilityId, boolean isAvailable) {
        return availabilityDao.updateAvailabilityStatus(availabilityId, isAvailable);
    }

    public int deleteAvailability(int availabilityId) {
        return availabilityDao.deleteAvailability(availabilityId);
    }

    public int deleteAvailabilitiesBySpaceId(int spaceId) {
        return availabilityDao.deleteAvailabilitiesBySpaceId(spaceId);
    }

    // SPACE OWNER
    public long addSpaceOwner(SpaceOwner spaceOwner) {
        return spaceOwnerDao.addSpaceOwner(spaceOwner);
    }

    public SpaceOwner getSpaceOwnerById(int spOwId) {
        return spaceOwnerDao.getSpaceOwnerById(spOwId);
    }

    public List<SpaceOwner> getAllSpaceOwners() {
        return spaceOwnerDao.getAllSpaceOwners();
    }

    // ATTENTION : Renommé pour éviter la confusion avec getSpacesByOwnerId du SpaceDao
    public List<SpaceOwner> getSpaceOwnerLinksByOwnerId(int ownerId) {
        return spaceOwnerDao.getSpacesByOwnerId(ownerId);
    }

    public List<SpaceOwner> getOwnersBySpaceId(int spaceId) {
        return spaceOwnerDao.getOwnersBySpaceId(spaceId);
    }

    public SpaceOwner getSpaceOwnerByOwnerAndSpace(int ownerId, int spaceId) {
        return spaceOwnerDao.getSpaceOwnerByOwnerAndSpace(ownerId, spaceId);
    }

    public boolean ownerOwnsSpace(int ownerId, int spaceId) {
        return spaceOwnerDao.ownerOwnsSpace(ownerId, spaceId);
    }

    public int getSpaceCountByOwnerId(int ownerId) {
        return spaceOwnerDao.getSpaceCountByOwnerId(ownerId);
    }

    public int updateSpaceOwner(SpaceOwner spaceOwner) {
        return spaceOwnerDao.updateSpaceOwner(spaceOwner);
    }

    public int deleteSpaceOwner(int spOwId) {
        return spaceOwnerDao.deleteSpaceOwner(spOwId);
    }

    public int deleteSpaceOwnerByOwnerAndSpace(int ownerId, int spaceId) {
        return spaceOwnerDao.deleteSpaceOwnerByOwnerAndSpace(ownerId, spaceId);
    }

    public int deleteSpaceOwnersBySpaceId(int spaceId) {
        return spaceOwnerDao.deleteSpaceOwnersBySpaceId(spaceId);
    }

    public int deleteSpaceOwnersByOwnerId(int ownerId) {
        return spaceOwnerDao.deleteSpaceOwnersByOwnerId(ownerId);
    }
}