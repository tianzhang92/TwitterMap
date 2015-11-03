import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
public final class TweetGetAll {
    /**
     * Main entry of this application.
     *
     * @param args
     */
	// twitter API
	
	private static final String myConsumerKey = "";
	private static final String myConsumerSecret = "";
	private static final String myAccessToken = "";
	private static final String myTokenSecret = "";
	// Amazon RDS
	private static final String DB_DRIVER_CLASS_NAME = "";

	private static String DB_CONNECTION_URL = "";
	private static String DB_USER_NAME = "";
	private static String DB_PASSWORD = "";
	private static String TABLE_NAME = "";
	// count the number of rows in the table
	static int count = 0;
	// category of keywords
    static String[] sports = {"sports","Soccer","Football","Basketball","NBA","NFL","NHL","MLB","NCAA","FIBA","FIFA","UEFA","EPL","LIGUE 1","SERIE A","La Liga","Bundesliga","Tennis","Volleyball","Baseball","Skating"};
    static String[] food = {"food", "restaurant", "pizza", "burger", "noodle", "fries","cupcake", "breakfast", "brunch"};
    static String[] news = {"news","nytimes","Xinhua","新华社","共同通信社","kyodo","YNA","KCNA","ИТАР-ТАСС","Reuters","AFP","press","CNA","РИА Новости","DPA","Agencia EFE"};	
    static String[] keywordAll = concatAll(sports,food,news);//concatAll can only concat 3 String[] due to its definition
    
	private final Connection connection;
	private Statement statement;
	
	public TweetGetAll() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
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

        final TweetGetAll tweetGet = new TweetGetAll();
         
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
                    double longi = loc.getLongitude();
                    double score = 0.0;
                    String keyword = "all";
                    String text1 = status.getText();
                    String text = text1.replace("'", " ");
                    java.sql.Timestamp date = new java.sql.Timestamp(status.getCreatedAt().getTime());                 	

                    
                    if(containKeyword(text,keywordAll)){
                    	APIService apiService = APIService.getInstanceWithKey("");
                        String s=null;
    					try {
    						s = apiService.getSentiment(text1);
    					} catch (ClientProtocolException e1) {
    						// TODO Auto-generated catch block
    						e1.printStackTrace();
    					} catch (IOException e1) {
    						// TODO Auto-generated catch block
    						e1.printStackTrace();
    					}
                        System.out.println(s);
                        JsonObject jsonObject = new Gson().fromJson(s, JsonObject.class);
                        JsonObject jsonObject1 = null;
                        if(jsonObject.get("docSentiment")!=null){
                        	jsonObject1 = jsonObject.get("docSentiment").getAsJsonObject();
                            
                            //System.out.println(jsonObject1);
                            if (jsonObject1.get("score")!=null){
                            		score = jsonObject1.get("score").getAsDouble();
                            		//System.out.println(score);
                            }
                            //System.out.println(score);
                        }
                        
                        
                    	try {
                    		if (count <= 200000) {
                				// insert into table
                    			String sql3 = null;
    							if(containKeyword(text,news)){
    								sql3 = sqlStatement(TABLE_NAME, tweetID,userName,lati,longi,date,text,"news",score);
        							//System.out.println(sql3);
        							tweetGet.getStatement().executeUpdate(sql3);
        							count++;
        							System.out.println(count);
    							}
    							else if(containKeyword(text,sports)){
    								sql3 = sqlStatement(TABLE_NAME, tweetID,userName,lati,longi,date,text,"sports",score);
        							//System.out.println(sql3);
        							tweetGet.getStatement().executeUpdate(sql3);
        							count++;
        							System.out.println(count);
    							}
    							else{
    								sql3 = sqlStatement(TABLE_NAME, tweetID,userName,lati,longi,date,text,"food",score);
        							//System.out.println(sql3);
        							tweetGet.getStatement().executeUpdate(sql3);
        							count++;
        							System.out.println(count);
    							}
    						    
    						} else {
    							String sql4 = "DELETE FROM " + TABLE_NAME + "LIMIT 1";
    							tweetGet.getStatement().executeUpdate(sql4);
    							//count--;
    							String sql5 = null;
    							if(containKeyword(text,news)){
    								sql5 = sqlStatement(TABLE_NAME, tweetID,userName,lati,longi,date,text,"news",score);
        							System.out.println(sql5);
        							tweetGet.getStatement().executeUpdate(sql5);
        							//count++;
    							}
    							else if(containKeyword(text,sports)){
    								sql5 = sqlStatement(TABLE_NAME, tweetID,userName,lati,longi,date,text,"sports",score);
        							System.out.println(sql5);
        							tweetGet.getStatement().executeUpdate(sql5);
        							//count++;
    							}
    							else{
    								sql5 = sqlStatement(TABLE_NAME, tweetID,userName,lati,longi,date,text,"food",score);
        							System.out.println(sql5);
        							tweetGet.getStatement().executeUpdate(sql5);
        							//count++;
    							}
    						}
                 	               	
                    	} catch (SQLException e) {
                    		e.printStackTrace();
                    		
                    	}
                    //}//end of the situation of not having a keyword
                    }
                	
                	
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                //System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
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
        FilterQuery filter = new FilterQuery();
        double[][] locations = {{-180,-90},{180,90}}; 
        filter.locations(locations);
        twitterStream.filter(filter);
	    
    }//end of main
    
    public static String[] concatAll(String[] sports, String[] food, String[] news) {
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
		String[] arr = new String[res.size()];
		for(int i=0; i<arr.length; i++){
			arr[i] = res.get(i);
		}
		return arr;
    }
    	
    public static boolean containKeyword(String text, String[] allKeywords) {
    	boolean res = false;
		for (String str : allKeywords){
			if(text.toLowerCase().contains(str.toLowerCase())){
				return true;
			}
		}
		return res;
    }
    public static String sqlStatement(String TABLE_NAME, long tweetID,String userName,double lat,double lon,java.sql.Timestamp date,String text, String keyword,double score){
    	 return "INSERT INTO "
			     + TABLE_NAME
				 + "(tweetID,userName,Latitude,Longtitude,tweetTimeStamp,text,keyword,score)"
				 + " VALUES ('" + tweetID + "','" + userName + "','" + lat
				 + "','" + lon + "','" + date + "','" + text + "','" + keyword + "','" + score + "')";
    }
    
}
