package com.krystofmacek.firebasechatapp.services;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.model.Chat;

import java.util.ArrayList;
import java.util.List;


public class FirestoreService {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private CollectionReference chatsReference;
    private CollectionReference profilesReference;

    private FirebaseUser signedUser;

    public FirestoreService() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        chatsReference = firestore.collection("Chats");
        profilesReference = firestore.collection("Profiles");

        signedUser = auth.getCurrentUser();
    }

    //Handle chats

    public String initializeNewChat(final String otherUserId) {
        // vytvoreni noveho dokumentu
        DocumentReference newChatReference = chatsReference.document();
        String newId = newChatReference.getId();
        List<String> members = new ArrayList<>();
        members.add(signedUser.getUid());
        members.add(otherUserId);
        Chat chat = new Chat(
                newId,
                members
        );
        firestore.collection("Chats").document(newId)
                .set(chat);

        // pridame ho mezi aktivni chaty prihlaseneho uzivatele
        firestore.collection("Profiles").document(signedUser.getUid())
                .update("activeChats", FieldValue.arrayUnion(newId));

        return newId;
    }

    public void addChatToActive(final String chatId) {
        profilesReference.document(signedUser.getUid())
                .update("activeChats", FieldValue.arrayUnion(chatId));
    }


    // Handle messages
    public Query queryMessageCollection(final String chatId) {
        return firestore.collection("Chats")
                .document(chatId)
                .collection("Messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);
    }

    public Query queryForLastMessage(final String chatId) {
        return firestore
                .collection("Chats")
                .document(chatId)
                .collection("Messages").orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1);
    }

    // Vraci prazdny dokument zpravy
    public DocumentReference getEmptyMessageDocument(final String chatId) {
        return firestore.collection("Chats")
                .document(chatId)
                .collection("Messages")
                .document();
    }

    public DocumentReference getSignedUserDocumentRef() {
        return firestore.collection("Profiles")
                .document(signedUser.getUid());
    }

    // searchUsers Query
    public Task<QuerySnapshot> searchUsersQuery(final String locationField, final String userLocation, final List<String> tags) {
        return firestore.collection("Profiles")
                .whereEqualTo(locationField, userLocation)
                .whereArrayContainsAny("tags", tags)
                .limit(50)
                .get();
    }

    // Aktualizuje pole dokumentu
    public void updateField(final String collection, final String document, final String field, final Object value) {
        firestore.collection(collection)
                .document(document)
                .update(field, value);
    }
    public DocumentReference getDocumentReference(final String collection, final String documentId) {
        return firestore.collection(collection)
                .document(documentId);
    }

    public Query queryByArrayContains(final String collection, final String array, final Object value) {
        return firestore.collection(collection)
                .whereArrayContains(array, value);
    }

    public Query queryByWhereIn(final String collection, final String field, final List<String> values) {
        return firestore.collection(collection)
                .whereIn(field, values);
    }

}
