package ch.epfl.polybazaar.database.datastore.firebase;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.polybazaar.database.Model;
import ch.epfl.polybazaar.database.datastore.CollectionSnapshot;
import ch.epfl.polybazaar.database.datastore.DataSnapshot;

public class FirebaseCollectionSnapshot implements CollectionSnapshot {

    private QuerySnapshot querySnapshot;

    public FirebaseCollectionSnapshot(QuerySnapshot querySnapshot) {
        this.querySnapshot = querySnapshot;
    }

    @Override
    public List<DataSnapshot> getDocuments() {
        List<DataSnapshot> list = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            list.add(new FirebaseDataSnapshot(doc));
        }
        return list;
    }

    @Override
    public <T extends Model> List<T> toObjects(Class<T> clazz) {
        return querySnapshot.toObjects(clazz);
    }
}
