import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import com.google.gson.*;

public class DBManager {
	// Amazon RDS
	private static final String DB_DRIVER_CLASS_NAME = "";
	private static String DB_CONNECTION_URL = "";
	private static String DB_USER_NAME = "";
	private static String DB_PASSWORD = "";
	
	public Connection connection = null;
	
	public void connect()  {

		try {
			Class.forName(DB_DRIVER_CLASS_NAME).newInstance();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			connection = DriverManager
					.getConnection(DB_CONNECTION_URL, DB_USER_NAME, DB_PASSWORD);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("connected to database");
	}
	
	public void close(){
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//public ArrayList<String> getData(int interval){
	public ArrayList<String> getData(){
		int count=0;
		//TimeZone zone = TimeZone.getTimeZone("UTC");
		//Calendar curTime = Calendar.getInstance(zone);
		////Calendar curTime = Calendar.getInstance();
		//curTime.add(Calendar.SECOND, -interval);
		//java.sql.Timestamp cur = new java.sql.Timestamp(curTime.getTimeInMillis());
		ArrayList<String> data = new ArrayList<String>();
		PreparedStatement ps = null;
		ResultSet rs = null;	
		
		//String query = "select Latitude, Longtitude, keyword, score from tweet where tweetTimeStamp > ? and keyword IS NOT NULL";
		String query = "select Latitude, Longtitude, keyword, score from tweet where ID>50";

		System.out.println(query);
		try {
			ps = connection.prepareStatement(query);
			//ps.setTimestamp(1, cur);
			//System.out.println(cur);
			rs = ps.executeQuery();
			while(rs.next()){
				Double lati = rs.getDouble(1);
				Double longti = rs.getDouble(2);
				String keyword = rs.getString(3);
				Double score = rs.getDouble(4);
				Gson gson = new Gson();
				TweetInfo geoData = new TweetInfo(lati, longti, keyword,score);
				String entry = gson.toJson(geoData);
				data.add(entry);
				TweetInfo geoDataSendAll = new TweetInfo(lati, longti,"all",score);
				entry = gson.toJson(geoDataSendAll);
				data.add(entry);
				count++;
				if(count==50){
					break;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();		
		}		
		return data;
	}	
}

class TweetInfo {
	double lat;
	double lon;
	String kw;
	double senti;
	public TweetInfo(double lati, double longti, String keyword,double senti){
		this.lat = lati;
		this.lon = longti;
		this.kw = keyword;
		this.senti = senti;
	}	
}
