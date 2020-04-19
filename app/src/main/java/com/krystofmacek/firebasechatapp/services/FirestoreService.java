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

    private final FirebaseUser signedUser;
    private final FirebaseFirestore firestore;
    private final CollectionReference chatsReference;
    private final CollectionReference profilesReference;

    public FirestoreService() {
        signedUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        chatsReference = firestore.collection("Chats");
        profilesReference = firestore.collection("Profiles");
    }

    // get referenci na dokument Profilu, prihlaseneho uzivatele
    public DocumentReference getSignedUserDocumentRef() {
        return profilesReference
                .document(signedUser.getUid());
    }

    //Vytvoreni noveho dokumentu Chat
    public String initializeNewChat(final String otherUserId) {

        DocumentReference newChatReference = chatsReference.document();
        String newId = newChatReference.getId();
        List<String> members = new ArrayList<>();
        members.add(signedUser.getUid());
        members.add(otherUserId);
        Chat chat = new Chat(
                newId,
                members
        );
        chatsReference.document(newId).set(chat);
        // id dokumentu pridame mezi aktivni chaty prihlaseneho uzivatele
        getSignedUserDocumentRef().update("activeChats", FieldValue.arrayUnion(newId));
        return newId;
    }

    public void addChatToActive(final String chatId) {
        profilesReference.document(signedUser.getUid())
                .update("activeChats", FieldValue.arrayUnion(chatId));
    }


    // nacteni zprav serazenych dle casu odeslani
    public Query queryMessageCollection(final String chatId) {
        return chatsReference
                .document(chatId)
                .collection("Messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);
    }

    // nacteni posledni zpravy chatu
    public Query queryForLastMessage(final String chatId) {
        return chatsReference
                .document(chatId)
                .collection("Messages").orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1);
    }

    // Vtvoreni prázdného dokumentu chat
    public DocumentReference getEmptyMessageDocument(final String chatId) {
        return chatsReference
                .document(chatId)
                .collection("Messages")
                .document();
    }



    // searchUsers Query
    public Task<QuerySnapshot> searchUsersQuery(final String locationField, final String userLocation, final List<String> tags) {
        return profilesReference
                .whereEqualTo(locationField, userLocation)
                .whereArrayContainsAny("tags", tags)
                .limit(10)
                .get();
    }

    // Aktualizuje pole dokumentu
    public void updateField(final String collection, final String document, final String field, final Object value) {
        firestore.collection(collection)
                .document(document)
                .update(field, value);
    }

    // pristup ke konkretnimu dokumentu
    public DocumentReference getDocumentReference(final String collection, final String documentId) {
        return firestore.collection(collection)
                .document(documentId);
    }
    // Nacteni dokumentu, které mají pole typu Array obsahující specifikovanou hodnotu
    public Query queryByArrayContains(final String collection, final String array, final Object value) {
        return firestore.collection(collection)
                .whereArrayContains(array, value);
    }

    // Nacteni dokumentu, kde specifikované pole obsahuje jednu z hodnot ze seznamu
    public Query queryByWhereIn(final String collection, final String field, final List<String> values) {
        return firestore.collection(collection)
                .whereIn(field, values);
    }



}
