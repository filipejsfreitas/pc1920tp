import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;


public class DatabaseBuddy {
    MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    MongoDatabase database;
    MongoCollection<Document> users;

    public DatabaseBuddy() {
        this.database = this.mongoClient.getDatabase("Pandemic");
        this.users = this.database.getCollection("Users");
    }

    public void insertUser(String username, String password) {
        Document document = new Document("username", username).append("password", password);
        this.users.insertOne(document);
    }

    public boolean existsUser(String username) {
        return !(this.getUser(username) == null);
    }

    public Document getUser(String username) {
        return this.users.find(new Document("username", username)).first();
    }
}


