package components;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;




public class TableWithBottomLine extends JPanel implements TableModelListener {
    
	public final static long MILS_IN_DAY = 1000 * 60 * 60 * 24;
	
//	private Connection connection;
	static Connection connection = null;
	static Statement stmt = null;
	
	static JTable table;
	static MyTableModel model;
	JLabel bottomLabel;
	JLabel bottomLabel2;		
	java.util.Date lastDate;
	

    public TableWithBottomLine() throws SQLException {
    	
    	super(new BorderLayout());
        
        try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			System.err.println("Unable to find and load driver");
			System.exit(1);
		}
        
        bottomLabel = new JLabel();
        bottomLabel2 = new JLabel();
        bottomLabel.setText(textForLabel());
   //     bottomLabel2.setText(textForLabel());
        JPanel panel = new JPanel(new GridLayout(2,2));
        panel.setFocusable(true);
        panel.add(bottomLabel);
        panel.add(bottomLabel2);
        add(panel, BorderLayout.PAGE_END);
        
          table = new JTable(new MyTableModel())      
        {
        	//Alternate color of table rows
        	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {        	
        	Component comp = super.prepareRenderer(renderer, row, column);
        /*	
        	comp.addMouseListener(
    				new MouseAdapter() {
    					public void mouseEntered(MouseEvent e) {
    						try {	
    							JComponent jc = (JComponent)comp;
    							if (isRowSelected(row))
    							jc.setBackground(row % 2 == 0 ? Color.LIGHT_GRAY : Color.LIGHT_GRAY);
    							
    						    } catch (Exception ee) {}
    				        }
    				}
    		); */
        
        	comp.setBackground(row % 2 == 0 ? Color.white : new Color(240,242,242));
        	JComponent jc = (JComponent)comp;
			if (isRowSelected(row))
			jc.setBackground(row % 2 == 0 ? Color.LIGHT_GRAY : Color.LIGHT_GRAY);
        	
        	return comp;       	
        	}        	
        };
        
        table.removeColumn(table.getColumnModel().getColumn(0));   
        table.setPreferredScrollableViewportSize(new Dimension(900, 330));
        table.setFillsViewportHeight(true);
      //  table.setModel(new MyTableModel());
        table.getModel().addTableModelListener(this);

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        //Add the scroll pane to this panel.
        add(scrollPane, BorderLayout.CENTER);
    }
    
    static Connection getConnection() {
        return connection;
    } 
    
    public ResultSet Execute_Query(String queryIn) throws SQLException {
    	ResultSet resSet = null;   		
   		try {    	   		
   			connection = TableWithBottomLine.getConnection();
   			Statement stmt = null;
   			stmt = connection.createStatement();
    	    resSet = stmt.executeQuery(queryIn);    	   			
   		} catch (SQLException e) {
   			e.printStackTrace();
   		}    	   		
   		return resSet;
   	}
    
    public String textForLabel() throws SQLException{
    	
       	List<Float> floatArrayList = new ArrayList<Float>();
     
    	   	String query ="SELECT f.Flat, f.Mobile, f.Food, f.Alcohol, f.Transport, f.Outdoor, f.Pauls_stuff, f.Stuff FROM finance.fin f WHERE f.tbl_Date >= DATE_FORMAT( NOW( ) ,  '%Y-%m-10' ) + INTERVAL IF( DAY( NOW( ) ) >10, 0 , -1 ) MONTH AND f.tbl_Date <= CURDATE( ) ";
    	   	    	   
    	    try {    	   	    
    	    	ResultSet rs = null;
    	    	rs = Execute_Query(query);    	   	
    	    	
    	    	while (rs.next()){
    	    	  floatArrayList.add(rs.getFloat("Flat"));
    	    	  floatArrayList.add(rs.getFloat("Mobile"));
    	    	  floatArrayList.add(rs.getFloat("Food"));
    	    	  floatArrayList.add(rs.getFloat("Alcohol"));
    	    	  floatArrayList.add(rs.getFloat("Transport"));
    	    	  floatArrayList.add(rs.getFloat("Outdoor"));
    	    	  floatArrayList.add(rs.getFloat("Pauls_stuff"));
    	    	  floatArrayList.add(rs.getFloat("Stuff"));
    	    	}    	    
    	   } catch(SQLException ee){
    		   ee.printStackTrace();
    	     }
    /*	    finally {
    	        stmt.close();
    	    } */
    	    Float[] array = floatArrayList.toArray(new Float[floatArrayList.size()]);
  
    	    float temp1 =0.0f;
    	    for (int i=0; i<array.length; i++){    	    	
    	    	temp1 = temp1 + array[i];    	   
    	    }
    	    float sumToSpend = 1250.0f;
    	    sumToSpend = sumToSpend - temp1;
    	    String stringForLabel = Float.toString(sumToSpend);
    	    return "This month you can spend: -"+stringForLabel+"Lt";
    //	return array;
    }  
    
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int col = e.getColumn();
        model = (MyTableModel) e.getSource();
        String stulpPav = model.getColumnName(col);
        Object data = model.getValueAt(row, col);
        Object studId = model.getValueAt(row, 0);
        System.out.println("tableChanded works");
        try {
            new ImportData(stulpPav, data, studId);
            bottomLabel.setText(textForLabel());
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }
    
 /*   public void windowOpened(WindowEvent e) {
		try {							
			Statement statement = newContentPane.connection.createStatement();
			ResultSet rs = statement.executeQuery(
					"SELECT tbl_Date as Date, Flat, Mobile, Food, Alcohol, Transport, Outdoor, Pauls_stuff, Income, Stuff FROM finance.fin");
			TableModel model = DbUtils.resultSetToTableModel(rs);						
			newContentPane.table.setModel(model);														
		} catch (SQLException ee) {}						
	} */
   /*
    static Connection getConnection() {
        return connection;
    } */
    
 /*   public class ImportData {    

    	public ImportData(String a, Object b, Object c)
    	        throws ClassNotFoundException, SQLException {
    	    Statement stmt = null;
    	    try {
    	    	connection = TableWithBottomLine.getConnection();
    	        String stulpPav = a;
    	        String duom = b.toString();
    	        String studId = c.toString();
    	        System.out.println(duom);
    	        connection.setAutoCommit(false);
    	        stmt = connection.createStatement();
    	        stmt.addBatch("update finance.fin set " + stulpPav + " = " + duom
    	                + " where ID = " + studId + ";");
    	        stmt.executeBatch();
    	        connection.commit();
    	    } catch (BatchUpdateException e) {
    	        e.printStackTrace();
    	    } catch (SQLException e) {
    	        e.printStackTrace();
    	    } finally {
    	        if (stmt != null)
    	            stmt.close();
    	        connection.setAutoCommit(true);
    	        System.out.println("Data was imported to database");
    	    }
    	}   
    } */  
    
    public class ImportData {    

    	public ImportData(String a, Object b, Object c)
    	        throws ClassNotFoundException, SQLException {
    		PreparedStatement prepStmt = null;
    	    try {
    	    	connection = TableWithBottomLine.getConnection();
    	        String stulpPav = a;
    	        String duom = b.toString();
    	        String studId = c.toString();
    	        System.out.println(duom);
    	
    	        String updateString = "update finance.fin " + "set ? = ? " + "where ID = ? "+ ";";
    	        prepStmt = connection.prepareStatement(updateString);
    	        prepStmt.setString(1, stulpPav);
    	        prepStmt.setString(2, duom);
    	        prepStmt.setString(3, studId);    	        
    	        
    	    } catch (SQLException e) {
    	        e.printStackTrace();
    	    } 
    	      finally {
    	        if (prepStmt != null)
    	            prepStmt.close();
    	        System.out.println("Data was imported to database");
    	    }  
    	}   
    }
    
    public class MyTableModel extends AbstractTableModel{
    	int rowCount;
    	Object data [][];
    	String columnNames [];

   // 	public  MyTableModel(String grupName) throws SQLException{
    	public  MyTableModel() throws SQLException{
    /*	    String query ="select Studento_id, vardas_pavarde, 1_semestras,"+
    	            " 2_semestras, egzaminas, bendras_balas "+
    	            "from pki18." + grupName; */
    		String query ="SELECT ID, tbl_Date as Date, Flat, Mobile, Food, Alcohol, Transport, Outdoor, Pauls_stuff, Income, Stuff FROM finance.fin";    	
    	    
    		ResultSet rs = null;
    	   	rs = Execute_Query(query);
    //		ResultSet rs ;
    //	   connection = TableWithBottomLine.getConnection();

    	//    Statement stmt = null;
    	//    stmt = connection.createStatement();
    	//    rs = stmt.executeQuery(query);

    	    rs.last();
    	    rowCount = rs.getRow();
    	    data = new Object[rowCount][11];
    	//    rs = stmt.executeQuery(query);
    	    rs = Execute_Query(query);
    	    for (int iEil = 0; iEil < rowCount; iEil++){
    	        rs.next();
    	        data[iEil][0] = rs.getInt("ID");
    	        data[iEil][1] = rs.getDate("Date");
    	        data[iEil][2] = rs.getFloat("Flat");
    	        data[iEil][3]  = rs.getFloat("Mobile");
    	        data[iEil][4] = rs.getFloat("Food");
    	        data[iEil][5]  = rs.getFloat("Alcohol");
    	        data[iEil][6] = rs.getFloat("Transport");
    	        data[iEil][7] = rs.getFloat("Outdoor");
    	        data[iEil][8] = rs.getFloat("Pauls_stuff");
    	        data[iEil][9] = rs.getFloat("Income");
    	        data[iEil][10] = rs.getFloat("Stuff");     	   
    	       
    	    }

            String[] columnName  = {"ID", "Date","Flat","Mobile"    	 
        //    String[] columnName  = {"Date","Flat","Mobile"
    	            ,"Food","Alcohol","Transport", "Outdoor", "Pauls_stuff", "Income", "Stuff"};
    	     columnNames = columnName;
    	}   
    	public int getColumnCount(){
    	    return columnNames.length;
    	}
    	public int getRowCount(){
    	    return data.length;
    	}
    	public String getColumnName(int col){
    	    return columnNames[col];
    	}
    	public Object getValueAt(int row, int col){
    	    return data[row][col];
    	}
    	public Class getColumnClass(int col){
    	    return getValueAt(0, col).getClass();
    	}
    	public boolean isCellEditable(int row, int col){
    	    return true;
    	}
    	public void setValueAt(Object value, int row, int col){
    	    data[row][col] = value;
    	    fireTableCellUpdated(row, col);
    	}
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     * @throws SQLException 
     */
  /*  private static java.sql.Date getCurrentDate() {
        java.util.Date today = new java.util.Date();
        return new java.sql.Date(today.getTime());
    } */
    
    private static java.sql.Date getDateForInsertion(Long date) {
      	Date tmp_date = new Date(date);  
        return new java.sql.Date(tmp_date.getTime());       
    }
    
    private static void createAndShowGUI() throws SQLException {
        //Create and set up the window.
        JFrame frame = new JFrame("TableWithBottomLine");
        TableWithBottomLine tbl = new TableWithBottomLine();        
        
        frame.addWindowListener(
				new WindowAdapter() {
					public void windowOpened(WindowEvent e) {
						try {						
							tbl.bottomLabel2.setText("Text");						
				    	    
							String query ="SELECT f.tbl_Date FROM finance.fin f ORDER BY f.tbl_Date desc";
						//	ResultSet rs ;
							ResultSet rs = null;
				    	   	rs = tbl.Execute_Query(query);
				  //  	    connection = TableWithBottomLine.getConnection();
				  //  	    Statement stmt = null;
				    //	    stmt = connection.createStatement();
				    //	    rs = stmt.executeQuery(query);
				    	    rs.first();
							tbl.lastDate = rs.getDate("tbl_Date");
						//	System.out.println(tbl.lastDate);				    	  				    	    
				    	  
				    	    long milliseconds = tbl.lastDate.getTime();
				    	    // Let's get current day
				    	    Calendar cal = Calendar.getInstance();
				    	    cal.set(Calendar.HOUR_OF_DAY, 0);
				    	    cal.set(Calendar.MINUTE, 0);
				    	    cal.set(Calendar.SECOND, 0);
				    	    cal.set(Calendar.MILLISECOND, 0);
				    	    Long currentDate = cal.getTimeInMillis();
				    	    
				    	    PreparedStatement pStmt = connection.prepareStatement("INSERT into finance.fin (tbl_Date, Flat, Mobile, Food, Alcohol, Transport, Outdoor, Pauls_stuff, Income, Stuff) values(?,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)");
				    	    
				    	    while (milliseconds < currentDate + MILS_IN_DAY) {
				    	    				    	  
				    	    	milliseconds = milliseconds + MILS_IN_DAY;
				    	    	pStmt.setDate(1, getDateForInsertion(milliseconds));
				    	    	pStmt.addBatch();
				    	    }
				    	    pStmt.executeBatch();
						} catch (Exception ee) {
							ee.printStackTrace();
						}				    							
					} 
				}
			);        
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     //   frame.setContentPane(new TableWithBottomLine());
        frame.setContentPane(tbl);        
        new TableWithBottomLine().setOpaque(true); //content panes must be opaque        
        frame.pack();
        frame.setVisible(true);
    }

  /*  class CustomActionListener implements WindowListener{
    	public void windowOpened(WindowEvent e) {
			try {							
				Statement statement = newContentPane.connection.createStatement();
				ResultSet rs = statement.executeQuery(
						"SELECT tbl_Date as Date, Flat, Mobile, Food, Alcohol, Transport, Outdoor, Pauls_stuff, Income, Stuff FROM finance.fin");
				TableModel model = DbUtils.resultSetToTableModel(rs);						
				newContentPane.table.setModel(model);														
			} catch (SQLException ee) {}						
		} */
		
    public static void connectToDB() {
		try {
			Properties prop = new Properties();
			prop.setProperty("user", "root");
			prop.setProperty("password", "");
			connection = DriverManager.getConnection("jdbc:mysql://localhost/finance", prop);
		} catch (SQLException e) {
			System.out.println("Unable to connect to database");
			System.exit(1);
		}
		
	} 
    
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
   /*     javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
					createAndShowGUI();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }); */
    	try {
		//	Properties prop = new Properties();
		//	prop.setProperty("user", "root");
		//	prop.setProperty("password", "");
		//	connection = DriverManager.getConnection("jdbc:mysql://localhost/finance", prop);
    		connectToDB();
			createAndShowGUI();
		} catch (Exception e) {
	        e.printStackTrace();
	    } /*finally {
	        if (stmt != null)
	            stmt.close();
	    } */
    }
}