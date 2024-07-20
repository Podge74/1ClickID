package main.java.com.cid;

import javax.swing.*;
import javax.swing.table.*;
import java.util.Comparator;

public class TableSorterConfigurator {

    public static TableRowSorter<TableModel> createSorter(DefaultTableModel tableModel) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);

        // DAMAGE - comparator for the Damage column
        Comparator<Object> damageComparator = (o1, o2) -> {
            try {
                Double damage1 = Double.parseDouble(o1.toString());
                Double damage2 = Double.parseDouble(o2.toString());
                return damage1.compareTo(damage2);
            } catch (NumberFormatException e) {
                return 0; // Handle non-numeric values gracefully
            }
        };
        sorter.setComparator(6, damageComparator); // Set the custom comparator for the Damage column

        // FIRE RATE - comparator for the Fire Rate column
        Comparator<Object> fRateComparator = (o1, o2) -> {
            try {
                Double fRate1 = Double.parseDouble(o1.toString());
                Double fRate2 = Double.parseDouble(o2.toString());
                return fRate1.compareTo(fRate2);
            } catch (NumberFormatException e) {
                return 0; // Handle non-numeric values gracefully
            }
        };
        sorter.setComparator(7, fRateComparator); // Set the custom comparator for the Fire Rate column

        // MAG SIZE - comparator for the Mag Size column
        Comparator<Object> mSizeComparator = (o1, o2) -> {
            try {
                Double mSize1 = Double.parseDouble(o1.toString());
                Double mSize2 = Double.parseDouble(o2.toString());
                return mSize1.compareTo(mSize2);
            } catch (NumberFormatException e) {
                return 0; // Handle non-numeric values gracefully
            }
        };
        sorter.setComparator(8, mSizeComparator); // Set the custom comparator for the Mag Size column

        // DPS - comparator for the DPS column
        Comparator<Object> dpsComparator = (o1, o2) -> {
            try {
                Double dps1 = Double.parseDouble(o1.toString());
                Double dps2 = Double.parseDouble(o2.toString());
                return dps1.compareTo(dps2);
            } catch (NumberFormatException e) {
                return 0; // Handle non-numeric values gracefully
            }
        };
        sorter.setComparator(9, dpsComparator); // Set the custom comparator for the DPS column

        return sorter;
    }

    public static void main(String[] args) {
        // Example usage with gunsTableModel
        DefaultTableModel gunsTableModel = createTableModelForGuns();
        JTable gunsTable = new JTable(gunsTableModel);
        TableRowSorter<TableModel> gunsSorter = createSorter(gunsTableModel);
        gunsTable.setRowSorter(gunsSorter);

        // Example usage with legWepTableModel
        DefaultTableModel legWepTableModel = createTableModelForLegWep();
        JTable legWepTable = new JTable(legWepTableModel);
        TableRowSorter<TableModel> legWepSorter = createSorter(legWepTableModel);
        legWepTable.setRowSorter(legWepSorter);
    }

    // Dummy methods for example purposes
    private static DefaultTableModel createTableModelForGuns() {
        // Replace with actual implementation
        return new DefaultTableModel();
    }

    private static DefaultTableModel createTableModelForLegWep() {
        // Replace with actual implementation
        return new DefaultTableModel();
    }
}
