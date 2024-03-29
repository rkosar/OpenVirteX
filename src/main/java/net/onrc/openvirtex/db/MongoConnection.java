package net.onrc.openvirtex.db;

import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class MongoConnection implements DBConnection {
	private static final String DB_NAME = "OVX";
	private MongoClient mongo;

	private static Logger log = LogManager.getLogger(MongoConnection.class
			.getName());

	@Override
	public void connect(String host, Integer port) {
		MongoConnection.log.info("Connecting to MongoDB at {}:{}", host, port);
		try {
			this.mongo  = new MongoClient(host, port);
			this.mongo.getConnector().getDBPortPool(mongo.getAddress()).get().ensureOpen();
		} catch (UnknownHostException e) {
			MongoConnection.log.error("Invalid MongoDB host");
		} catch (Exception e) {
			MongoConnection.log.error("Error connecting to database");
		}
	}

	@Override
	public void disconnect() {
		this.mongo.close();
	}

	public DB getDB() {
		return mongo.getDB(MongoConnection.DB_NAME);
	}

}
