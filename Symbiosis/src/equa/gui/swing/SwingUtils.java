/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.gui.swing;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author frankpeeters
 */
public class SwingUtils {

    // public static final int ROWHEIGHT = 16;
    public static final int MINWIDTH = 35;
    public static final int MAXWIDTH = 1500;

    public static void resize(JTable table) {
        Dimension d = new Dimension(table.getParent().getParent().getWidth(),
            table.getRowHeight() * table.getRowCount());
        table.setPreferredSize(d);

        final TableColumnModel columnModel = table.getColumnModel();
        int[] desiredWidthTable = new int[table.getColumnCount()];

        for (int column = 0; column < table.getColumnCount(); column++) {
            int desiredWidth = MINWIDTH;
            String columnHeader = table.getColumnName(column);
            int headerSize = columnHeader.length()*7;
            if (headerSize > desiredWidth) desiredWidth = headerSize;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                int comp_width = comp.getPreferredSize().width;
                if (comp_width > MAXWIDTH) {
                    comp_width = MAXWIDTH;
                }
                desiredWidth = Math.max(comp_width, desiredWidth);
            }
            desiredWidthTable[column] = desiredWidth;
        }

        int totalDesiredWidth = 0;
        for (int dw : desiredWidthTable) {
            totalDesiredWidth += dw;
        }
        double factor = (1.0 * table.getParent().getWidth()) / totalDesiredWidth;

        for (int column = 0; column < table.getColumnCount(); column++) {
            columnModel.getColumn(column).setPreferredWidth((int) (desiredWidthTable[column] * factor));
        }
        
        table.getParent().invalidate();
        

    }

}
