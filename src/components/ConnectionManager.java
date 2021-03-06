package components;

import java.sql.Connection; 
import java.sql.SQLException;
import javax.sql.DataSource;
import com.mchange.v2.c3p0.DataSources;
 
public class ConnectionManager {
    private static String url = "jdbc:mysql://localhost/finance";    
    private static String driverName = "com.mysql.jdbc.Driver";   
    private static String username = "root";   
    private static String password = "";
    private static Connection con;

    public static Connection getConnection() {
    	try {
            Class.forName(driverName);
            try {            	
            	DataSource unpooled = DataSources.unpooledDataSource(url,username,password);
        		DataSource pooled = DataSources.pooledDataSource(unpooled);
            	con = pooled.getConnection();            	
            } catch (SQLException ex) {                
                System.out.println("Failed to create the database connection."); 
            }
        } catch (ClassNotFoundException ex) {            
            System.out.println("Driver not found."); 
        }
        return con;
    }
} 