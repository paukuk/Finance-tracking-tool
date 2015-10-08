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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;



public class TableWithBottomLine extends JPanel implements TableModelListener {
    
	public final static long MILS_IN_DAY = 1000 * 60 * 60 * 24;
		
	static JTable table;
	static MyTableModel model;
	JLabel bottomLabel;
	JLabel bottomLabel2;		
	java.util.Date lastDate;
	int quantityOfColumns = 11;
	
    public TableWithBottomLine() throws SQLException {
    	
    	super(new BorderLayout());
                
        bottomLabel = new JLabel();
        bottomLabel2 = new JLabel();
        bottomLabel.setText(textForLabel());
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
        
        for (int i=2; i<quantityOfColumns;i++){
        	table.getColumnModel().getColumn(i).setCellRenderer(new DecimalFormatRenderer());
        } 
              
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
       
    public String textForLabel() throws SQLException{
    	
       	List<Float> floatArrayList = new ArrayList<Float>();     
    	   	String query ="SELECT f.Flat, f.Mobile, f.Food, f.Alcohol, f.Transport, f.Outdoor, f.Pauls_stuff, f.Stuff FROM finance.fin f WHERE f.tbl_Date >= DATE_FORMAT( NOW( ) ,  '%Y-%m-10' ) + INTERVAL IF( DAY( NOW( ) ) >10, 0 , -1 ) MONTH AND f.tbl_Date <= CURDATE( ) ";
    	   	Connection connection = null;
       		Statement stmt = null;
    	   	ResultSet resSet = null;
    	   	try {
    	   		connection = ConnectionManager.getConnection();
       			stmt = connection.createStatement();
        	    resSet = stmt.executeQuery(query);    	   		    	    	
    	    	while (resSet.next()){
    	    	  floatArrayList.add(resSet.getFloat("Flat"));
    	    	  floatArrayList.add(resSet.getFloat("Mobile"));
    	    	  floatArrayList.add(resSet.getFloat("Food"));
    	    	  floatArrayList.add(resSet.getFloat("Alcohol"));
    	    	  floatArrayList.add(resSet.getFloat("Transport"));
    	    	  floatArrayList.add(resSet.getFloat("Outdoor"));
    	    	  floatArrayList.add(resSet.getFloat("Pauls_stuff"));
    	    	  floatArrayList.add(resSet.getFloat("Stuff"));
    	    	}    	    
    	   } catch(SQLException ee){
    		   ee.printStackTrace();
    	     } finally {
    	         closeAll(resSet, stmt, null, connection);
    	     }  
    	    Float[] array = floatArrayList.toArray(new Float[floatArrayList.size()]);  
    	    float temp1 =0.0f;
    	    for (int i=0; i<array.length; i++){    	    	
    	    	temp1 = temp1 + array[i];    	   
    	    }
    	    float sumToSpend = 800.00f;
    	    sumToSpend = sumToSpend - temp1;
    	    String stringForLabel = Float.toString(sumToSpend);
    	    return "This month you can spend: -"+stringForLabel+"Eur";
    }  
    
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int col = e.getColumn();
        model = (MyTableModel) e.getSource();
        String colName = model.getColumnName(col);
        Object cellValue = model.getValueAt(row, col);        
        Object cellId = model.getValueAt(row, 0);

        try {
            new ImportData(colName, cellValue, cellId);
            bottomLabel.setText(textForLabel());
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }   
    
    public class ImportData {    

    	public ImportData(String a, Object b, Object c)
    	        throws ClassNotFoundException, SQLException {
    		Connection connection = null;
    		PreparedStatement prepStmt = null;    		
    	    try {
    	    	connection = ConnectionManager.getConnection();
    	        String colName = a;    	        
    	        float cellValue = (float) b;
    	        int cellId = (int) c;    	        
    	        String updateString = "update finance.fin " + "set " + colName + "= ? " + "where ID = ? "+ ";";
    	        prepStmt = connection.prepareStatement(updateString);
    	        prepStmt.setFloat(1, cellValue);
    	        prepStmt.setInt(2, cellId);    	        
    	        prepStmt.executeUpdate();  
    	        
    	    } catch (SQLException e) {
    	        e.printStackTrace();
    	    } 
    	    finally {
    	   		closeAll(null, null, prepStmt, connection);
    	   	}  
    	}   
    }
    
    public class MyTableModel extends AbstractTableModel{
    	int rowCount;
    	Object data [][];
    	String columnNames [];

    	public  MyTableModel() throws SQLException{    
    		String query ="SELECT ID, tbl_Date as Date, Flat, Mobile, Food, Alcohol, Transport, Outdoor"
    				+ ", Pauls_stuff, Income, Stuff FROM finance.fin";    	
    	    
    		Connection connection = null;
       		Statement stmt = null;
    	   	ResultSet resSet = null;
    	   	try {
    	   		connection = ConnectionManager.getConnection();
       			stmt = connection.createStatement();
        	    resSet = stmt.executeQuery(query); 
   // TO-DO Maybe there is a cheaper way to get the row count than resSet.last();    
        	    resSet.last();
    	    rowCount = resSet.getRow();
    	    data = new Object[rowCount][11];
    	    resSet.beforeFirst();    	       	    
    	    for (int iEil = 0; iEil < rowCount; iEil++){
    	    	resSet.next();
    	        data[iEil][0] = resSet.getInt("ID");
    	        data[iEil][1] = resSet.getDate("Date");
    	        data[iEil][2] = resSet.getFloat("Flat");
    	        data[iEil][3]  = resSet.getFloat("Mobile");
    	        data[iEil][4] = resSet.getFloat("Food");
    	        data[iEil][5]  = resSet.getFloat("Alcohol");
    	        data[iEil][6] = resSet.getFloat("Transport");
    	        data[iEil][7] = resSet.getFloat("Outdoor");
    	        data[iEil][8] = resSet.getFloat("Pauls_stuff");
    	        data[iEil][9] = resSet.getFloat("Income");
    	        data[iEil][10] = resSet.getFloat("Stuff");     	       
    	    }
    	   	} catch(SQLException e){
    	   		e.printStackTrace();
    	   	} finally {
    	   		closeAll(resSet, stmt, null, connection);
    	   	}
            String[] columnName  = {"ID", "Date","Flat","Mobile"    	 
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
     
    private static java.sql.Date getDateForInsertion(Long date) {
      	Date tmp_date = new Date(date);  
        return new java.sql.Date(tmp_date.getTime());       
    }
    
    private static void createAndShowGUI() throws SQLException {
        JFrame frame = new JFrame("TableWithBottomLine");
        TableWithBottomLine tbl = new TableWithBottomLine();
        
        frame.addWindowListener(
				new WindowAdapter() {
					public void windowOpened(WindowEvent e) {
						Connection connection = null;
			       		Statement stmt = null;
			       		PreparedStatement prepStmt = null;
			    	   	ResultSet resSet = null;
						/* Code for future data which will come from excel DB or .json file for currency converting to Euros
			    	   	//Connection connection2 = null;			    	   	
			       		Statement stmtForEuros = null; */
			    	   	
			    	   	try {						
							tbl.bottomLabel2.setText("Text");				    	    
							String query ="SELECT f.tbl_Date FROM finance.fin f ORDER BY f.tbl_Date desc";						
							connection = ConnectionManager.getConnection();
			       			stmt = connection.createStatement();
			        	    resSet = stmt.executeQuery(query);											
							resSet.first();
							tbl.lastDate = resSet.getDate("tbl_Date");				    	  
				    	    long milliseconds = tbl.lastDate.getTime();
				    	    // Let's get current day
				    	    Calendar cal = Calendar.getInstance();
				    	    cal.set(Calendar.HOUR_OF_DAY, 0);
				    	    cal.set(Calendar.MINUTE, 0);
				    	    cal.set(Calendar.SECOND, 0);
				    	    cal.set(Calendar.MILLISECOND, 0);
				    	    Long currentDate = cal.getTimeInMillis();				    	
				    	    prepStmt = connection.prepareStatement("INSERT into finance.fin (tbl_Date, Flat, Mobile, Food, Alcohol, "
				    	    		+ "Transport, Outdoor, Pauls_stuff, Income, Stuff) values(?,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)");
				    	    
				    	    while (milliseconds < currentDate + MILS_IN_DAY) {				    	    				    	  
				    	    	milliseconds = milliseconds + MILS_IN_DAY;
				    	    	prepStmt.setDate(1, getDateForInsertion(milliseconds));
				    	    	prepStmt.addBatch();
				    	    }
				    	    prepStmt.executeBatch();				    	    
				    	   /* Code for future data which will come from excel DB or .json file for currency converting to Euros 
				    	    // Multiply all cells in rows except ID and tbl_Date by 3.4528f
				    	    stmtForEuros = connection.createStatement();
				    	    String queryForEuros = "UPDATE finance.fin f SET f.Flat = f.Flat * 3.4528, f.Mobile = f.Mobile * 3.4528, "
				    	    		+ "f.Food = f.Food * 3.4528, f.Alcohol = f.Alcohol * 3.4528, f.Transport = f.Transport * 3.4528, "
				    	    		+ "f.Outdoor = f.Outdoor * 3.4528, f.Pauls_stuff = f.Pauls_stuff * 3.4528, f.Income = f.Income * 3.4528, "
				    	    		+ "f.Stuff = f.Stuff * 3.4528";				    	    
				    	    stmtForEuros.executeUpdate(queryForEuros); */
				    	    
						} catch (SQLException ee) {
							ee.printStackTrace();
						} finally {
			    	   		closeAll(resSet, stmt, prepStmt, connection);
			    	   	} 				    							
					} 
				}
			);         
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);     
        frame.setContentPane(tbl);
        tbl.setOpaque(true);               
        frame.pack();
        frame.setVisible(true);
    }
	    
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {   
                	createAndShowGUI();                	
				} catch (Exception e) {					
					e.printStackTrace();
				}
            }
        }); 
    }
 
    private static void closeAll(ResultSet resSet, Statement stmt, PreparedStatement prepStmt, Connection connection) {
        if (resSet != null) {
          try {
        	  resSet.close();
          } catch (SQLException e) {
        	  e.printStackTrace();
          } 
        }
        if (stmt != null) {
          try {
        	  stmt.close();
          } catch (SQLException e) {
        	  e.printStackTrace();
          } 
        }
        if (prepStmt != null) {
            try {
            	prepStmt.close();
            } catch (SQLException e) {
            	e.printStackTrace();
            } 
          }
        if (connection != null) {
          try {
            connection.close();
          } catch (SQLException e) {
        	  e.printStackTrace();
          } 
        }
      }
}