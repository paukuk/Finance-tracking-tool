package components;

import java.awt.Component;
import java.text.DecimalFormat;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DecimalFormatRenderer extends DefaultTableCellRenderer {
    private static final DecimalFormat formatter = new DecimalFormat( "0.00" );

    public Component getTableCellRendererComponent(
       JTable table, Object value, boolean isSelected,
       boolean hasFocus, int row, int column) {
       value = formatter.format((Number)value);

       return super.getTableCellRendererComponent(
          table, value, isSelected, hasFocus, row, column );
    }
 }