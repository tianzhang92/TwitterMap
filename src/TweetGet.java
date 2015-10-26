import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;


/**
 * <p>This is a code example of Twitter4J Streaming API - sample method support.<br>
 * Usage: java twitter4j.examples.PrintSampleStream<br>
 * </p>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class TweetGet {
    /**
     * Main entry of this application.
     *
     * @param args
     */
	// twitter API
	private static final String myConsumerKey;
	private static final String myConsumerSecret;
	private static final String myAccessToken;
	private static final String myTokenSecret;
	// Amazon RDS
	private static final String DB_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
	private static String DB_CONNECTION_URL;
	private static String DB_USER_NAME;
	private static String DB_PASSWORD;
	private static String TABLE_NAME;
	// count the number of rows in the table
	static int count = 0;
	// category of keywords
    static String[] sports = {"sports","Soccer","Football","Basketball","Tennis","Volleyball","Baseball","Skating"};
    static String[] food = {"food", "restaurant", "pizza", "burger", "noodle", "fries","cupcake", "breakfast", "brunch"};
    static String[] news = {"news","CNN","ABC","BBC","CCTV","NBC","Netflix","CBS"};	
    static String[] all = {"a"};
    
	private final Connection connection;
	private Statement statement;
	
	public TweetGet() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class.forName(DB_DRIVER_CLASS_NAME).newInstance();
		connection = DriverManager
				.getConnection(DB_CONNECTION_URL, DB_USER_NAME, DB_PASSWORD);
		statement = connection.createStatement();
		System.out.println("connect to database successfully.");
	}

	public Connection getConnection() {
		return connection;
	}

	public Statement getStatement() throws SQLException {
		return statement;
	}

	public void closeStatement() throws SQLException {
		if (statement != null) {
			statement.close();
		}
	}

	public void closeConnection() throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}
	
    public static void main(String[] args) throws TwitterException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
    	//just fill this
    	 ConfigurationBuilder cb = new ConfigurationBuilder();
         cb.setDebugEnabled(true)
           .setOAuthConsumerKey(myConsumerKey)
           .setOAuthConsumerSecret(myConsumerSecret)
           .setOAuthAccessToken(myAccessToken)
           .setOAuthAccessTokenSecret(myTokenSecret);

        final TweetGet tweetGet = new TweetGet();
         
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                //System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                GeoLocation loc = status.getGeoLocation();            	 
                // check whether has geoLocation information
                if (loc != null) {
                	String userName = status.getUser().getScreenName();
                    Long tweetID = status.getId();
                    double lati = loc.getLatitude();
                    double longti = loc.getLongitude();
                    String text = status.getText();
                    java.sql.Timestamp date = new java.sql.Timestamp(status.getCreatedAt().getTime());                 	
                	System.out.println("tweetId:" + tweetID + ", UserName:" + userName 
                			+ ", Lati:" + lati + ", Longti:" +  longti
                    		+ ", Text: " + text + ", Date:" + date);
                	String keyword = tweetGet.getKeyword(text,sports,food, news, all);
                	///*
                	try {
                		String sql1 = "SELECT count(*) FROM " + TABLE_NAME + "";
                		ResultSet resultSet = tweetGet.getStatement().executeQuery(sql1);
            	        while (resultSet.next()) {
            	        	count = resultSet.getInt(1);
            	        }  
            	        System.out.println("The number of records is: " + count);
                		if (count <= 20000) {
            				// insert into table
            				String sql = "INSERT INTO "
									+ TABLE_NAME
									+ "(tweetID,userName,Latitude,Longtitude,tweetTimeStamp,text,keyword)"
									+ " VALUES ('" + tweetID + "','" + userName + "','" + lati
									+ "','" + longti + "','" + date + "','" + text + "','" + keyword + "')";
							System.out.println(sql);
							int cRecordsInserted = tweetGet.getStatement().executeUpdate(sql);
							System.out.println("insert tweet to DB successful, this number of inserted records is:"+ cRecordsInserted);
							count++;
						} else {
							String sql = "DELETE FROM " + TABLE_NAME + "LIMIT 1";
							tweetGet.getStatement().executeUpdate(sql);
							count--;
            				sql = "INSERT INTO "
									+ TABLE_NAME
									+ "(tweetID,userName,Latitude,Longtitude,tweetTimeStamp,text,keyword)"
									+ " VALUES ('" + tweetID + "','" + userName + "','" + lati
									+ "','" + longti + "','" + date + "','" + text + "','" + keyword + "')";							
							tweetGet.getStatement().executeUpdate(sql);
							count++;
						}
             	               	
                	} catch (SQLException e) {
                		e.printStackTrace();
                	}
                	//*/
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        //twitterStream.sample();
        //provide a filter
	    FilterQuery filterquery = new FilterQuery();
	    String[] keyWords = tweetGet.concatAll(sports,food, news, all);
	    	    
	    filterquery.track(keyWords);
	    twitterStream.filter(filterquery); 
		
	    Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					tweetGet.closeStatement();
					tweetGet.closeConnection();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
    }
    
    public String[] concatAll(String[] sports, String[] food, String[] news, String[] all) {
    	ArrayList<String> res = new ArrayList<String>();
    	for(int i=0; i < sports.length; i++){
			res.add(sports[i]);
		}
		for(int i=0; i < food.length; i++){
			res.add(food[i]);
		}
		for(int i=0; i < news.length; i++){
			res.add(news[i]);
		}
		for(int i=0; i < all.length; i++){
			res.add(all[i]);
		}		
		String[] arr = new String[res.size()];
		for(int i=0; i<arr.length; i++){
			arr[i] = res.get(i);
		}
		return arr;
    }
    
    public String getKeyword(String text, String[] sports, String[] food, String[] news, String[] all) {
		String keyword = null;
		for (String str : all){
			if(text.toLowerCase().contains(str.toLowerCase())){
				keyword = "all";
				break;
			}
		}
		for (String str : sports){
			if(text.toLowerCase().contains(str.toLowerCase())){
				keyword = "sports";
				break;
			}
		}
		for (String str : food){
			if(text.toLowerCase().contains(str.toLowerCase())){
				keyword = "food";
				break;
			}
		}
		for (String str : news){
			if(text.toLowerCase().contains(str.toLowerCase())){
				keyword = "news";
				break;
			}
		}
		return keyword;   	
    }
}