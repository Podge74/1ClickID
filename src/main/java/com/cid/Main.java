package main.java.com.cid;
/*
hhh
 TODO:
 Last worked on: July16th '24 04:42
 * v 2.2
 * Added legendary Weapons
 * Added Apparel
 * Added pay bounties (in Miscellaneous tab)
 * Added missing boost packs

 * v 2.1
 * Guns can now be sorted by Type, Damage, DPS, Fire rate, etc.
 * Added throwables
 * Fixed formatting errors
 * UI Cleanup

 * v 2.0
 * Fixed weapon tier upgrades
 * Fixed armor tier upgrades
 *
 * v. 1.9
 * Added Research
 *
 * v. 1.8
 * Added Weapon Tier Upgrades
 * Added Weapon MODS
 * Added Armor Tier Upgrades
 * Added Armor MODS
 * Improved UI
*/

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Comparator;
import javax.swing.RowFilter;
import javax.swing.Timer;
import javax.swing.table.TableColumn;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static main.java.com.cid.KeyCodes.VK_DOWN;
import static main.java.com.cid.KeyCodes.VK_SPACE;
import static main.java.com.cid.WindowFocusAndKeyPress.pressKey;
import static main.java.com.cid.WindowFocusAndKeyPress.pressKeyOld;


public class Main {
    private static Timer popupTimer;//for "Command Copied" verification window
    private static JFrame imageFrame; // Frame to display the image
    private static Point lastImageFrameLocation = new Point(800, 0); // Store last known position of preview window
    private static String openTab;

    public static void main(String[] args) {
        popupTimer = new Timer(5000, e -> {
            JOptionPane.getRootFrame().dispose(); // Close the pop-up window after 1 second
        });
        popupTimer.setRepeats(false); // Make the timer run only once
        popupTimer.setCoalesce(true);

        // improve on look and feel
        for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
            System.out.println(lafInfo.getClassName());
        }
        try {
            //UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        // Create a JFrame (main window) for the application
        JFrame frame = new JFrame("Starfield ID Codes (Version 2.2)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600); // Increased frame size for better tab visibility

        // Create a JTabbedPane to hold the tabs
        ChangeListener tabChangeListener;
        JTabbedPane tabbedPane = new JTabbedPane();
        tabChangeListener = e -> openTab = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());// used so images only get loaded when mouse is over a new row, prevents flickering
        tabbedPane.addChangeListener(tabChangeListener);
        tabbedPane.setBackground(Color.RED); // Set background color

        ///*
        // DON NOT CLICK!!!
        DefaultTableModel dontTableModel = createTableModelForDont();
        JTable dontTable = new JTable(dontTableModel);
        dontTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseMotionListenerToTable(dontTable);// mouse move listener
        addMouseListenerToTable(dontTable);// mouse click listener
        JTextField searchFieldDont = new JTextField(20);// create search field
        searchFieldDont.setToolTipText("Search for items by name in DONT");// searchfield tooltip
        addDocumentListenerToSearchField(searchFieldDont, dontTable);
        JPanel dontPanel = new JPanel(new BorderLayout());
        dontPanel.add(searchFieldDont, BorderLayout.NORTH);
        dontPanel.add(new JScrollPane(dontTable), BorderLayout.CENTER);
        tabbedPane.addTab("DONT", dontPanel);
        //setDontTableSorter(dontTable, dontTableModel);
        //TableColumn dontTypeColumn = dontTable.getColumnModel().getColumn(0);// contains INPUT_TYPE used for calculations, should not be visible
        //hideColumn(dontTypeColumn);
        TableColumn dontImageColumn = dontTable.getColumnModel().getColumn(2);// contains Image used for calculations, should not be visible
        hideColumn(dontImageColumn);
        //TableColumn amountColumn = dontTable.getColumnModel().getColumn(3);// contains Image used for calculations, should not be visible
        //hideColumn(amountColumn);
        //*/


        // melee
        DefaultTableModel meleeTableModel = createTableModelForMelee();
        JTable meleeTable = new JTable(meleeTableModel);
        meleeTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseMotionListenerToTable(meleeTable);// mouse move listener
        addMouseListenerToTable(meleeTable);// mouse click listener
        JTextField searchFieldMelee = new JTextField(20);// create search field
        searchFieldMelee.setToolTipText("Search for items by name in MELEE");// searchfield tooltip
        addDocumentListenerToSearchField(searchFieldMelee, meleeTable);
        JPanel meleePanel = new JPanel(new BorderLayout());
        meleePanel.add(searchFieldMelee, BorderLayout.NORTH);
        meleePanel.add(new JScrollPane(meleeTable), BorderLayout.CENTER);
        tabbedPane.addTab("MELEE", meleePanel);
        setMeleeTableSorter(meleeTable, meleeTableModel);
        TableColumn meleeTypeColumn = meleeTable.getColumnModel().getColumn(0);// contains INPUT_TYPE used for calculations, should not be visible
        hideColumn(meleeTypeColumn);
        TableColumn meleeImageColumn = meleeTable.getColumnModel().getColumn(5);// contains Image used for calculations, should not be visible
        hideColumn(meleeImageColumn);
        TableColumn amountColumn = meleeTable.getColumnModel().getColumn(3);// contains Image used for calculations, should not be visible
        hideColumn(amountColumn);
        // miscellaneous
        DefaultTableModel miscellaneousTableModel = createTableModelForMiscellaneous();
        JTable miscellaneousTable = new JTable(miscellaneousTableModel);
        miscellaneousTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseListenerToTable(miscellaneousTable);
        JTextField searchFieldMiscellaneous = new JTextField(20);
        searchFieldMiscellaneous.setToolTipText("Search for items by name in MISCELLANEOUS");
        addDocumentListenerToSearchField(searchFieldMiscellaneous, miscellaneousTable);
        JPanel miscellaneousPanel = new JPanel(new BorderLayout());// misc
        miscellaneousPanel.add(searchFieldMiscellaneous, BorderLayout.NORTH);
        miscellaneousPanel.add(new JScrollPane(miscellaneousTable), BorderLayout.CENTER);
        tabbedPane.addTab("MISCELLANEOUS*", miscellaneousPanel);
        TableColumn miscTypeColumn = miscellaneousTable.getColumnModel().getColumn(0);// misc
        hideColumn(miscTypeColumn);
        miscellaneousTable.getColumnModel().getColumn(3).setMaxWidth(50);// amount
        //ammo
        DefaultTableModel ammoTableModel = createTableModelForAmmo();
        JTable ammoTable = new JTable(ammoTableModel);
        ammoTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseListenerToTable(ammoTable);
        JTextField searchFieldAmmo = new JTextField(20);
        searchFieldAmmo.setToolTipText("Search for items by name in AMMO");
        addDocumentListenerToSearchField(searchFieldAmmo, ammoTable);
        JPanel ammoPanel = new JPanel(new BorderLayout());// ammo
        ammoPanel.add(searchFieldAmmo, BorderLayout.NORTH);
        ammoPanel.add(new JScrollPane(ammoTable), BorderLayout.CENTER);
        tabbedPane.addTab("AMMO", ammoPanel);
        TableColumn ammoTypeColumn = ammoTable.getColumnModel().getColumn(0);// ammo
        hideColumn(ammoTypeColumn);
        TableColumn ammoImageColumn = ammoTable.getColumnModel().getColumn(4);// ammo
        hideColumn(ammoImageColumn);
        ammoTable.getColumnModel().getColumn(3).setMaxWidth(50);// amount
        // weapons
        DefaultTableModel gunsTableModel = createTableModelForGuns();
        JTable gunsTable = new JTable(gunsTableModel);
        gunsTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseMotionListenerToTable(gunsTable);
        addMouseListenerToTable(gunsTable);
        JTextField searchFieldGuns = new JTextField(20);
        searchFieldGuns.setToolTipText("Search for items by name in GUNS");
        addDocumentListenerToSearchField(searchFieldGuns, gunsTable);
        JPanel gunsPanel = new JPanel(new BorderLayout());// guns
        gunsPanel.add(searchFieldGuns, BorderLayout.NORTH);
        gunsPanel.add(new JScrollPane(gunsTable), BorderLayout.CENTER);
        tabbedPane.addTab("GUNS", gunsPanel);
        setGunsTableSorter(gunsTable, gunsTableModel);
        TableColumn gunsTypeColumn = gunsTable.getColumnModel().getColumn(0);// ITEM column
        hideColumn(gunsTypeColumn);
        TableColumn gunsImageColumn = gunsTable.getColumnModel().getColumn(10);// Image column
        hideColumn(gunsImageColumn);
        TableColumn gunsAmountColumn = gunsTable.getColumnModel().getColumn(3);// Amount column
        hideColumn(gunsAmountColumn);
        // helmets
        DefaultTableModel helmetsTableModel = createTableModelForHelmets();
        JTable helmetsTable = new JTable(helmetsTableModel);
        helmetsTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseMotionListenerToTable(helmetsTable);
        JTextField searchFieldHelmets = new JTextField(20);
        addMouseListenerToTable(helmetsTable);
        searchFieldHelmets.setToolTipText("Search for items by name in HELMETS");
        addDocumentListenerToSearchField(searchFieldHelmets, helmetsTable);
        JPanel helmetsPanel = new JPanel(new BorderLayout());// helmets
        helmetsPanel.add(searchFieldHelmets, BorderLayout.NORTH);
        helmetsPanel.add(new JScrollPane(helmetsTable), BorderLayout.CENTER);
        tabbedPane.addTab("HELMETS", helmetsPanel);
        TableColumn helmetsTypeColumn = helmetsTable.getColumnModel().getColumn(0);
        hideColumn(helmetsTypeColumn);
        TableColumn helmetsImageColumn = helmetsTable.getColumnModel().getColumn(4);// Image
        hideColumn(helmetsImageColumn);
        TableColumn helmetsAmountColumn = helmetsTable.getColumnModel().getColumn(3);// Image
        hideColumn(helmetsAmountColumn);
        // suits
        DefaultTableModel suitsTableModel = createTableModelForSuits();
        JTable suitsTable = new JTable(suitsTableModel);
        suitsTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseMotionListenerToTable(suitsTable);
        addMouseListenerToTable(suitsTable);
        JTextField searchFieldSuits = new JTextField(20);
        searchFieldSuits.setToolTipText("Search for items by name in SUITS");
        addDocumentListenerToSearchField(searchFieldSuits, suitsTable);
        JPanel suitsPanel = new JPanel(new BorderLayout());// suits
        suitsPanel.add(searchFieldSuits, BorderLayout.NORTH);
        suitsPanel.add(new JScrollPane(suitsTable), BorderLayout.CENTER);
        tabbedPane.addTab("SUITS", suitsPanel);
        TableColumn suitsTypeColumn = suitsTable.getColumnModel().getColumn(0);// suits
        hideColumn(suitsTypeColumn);
        TableColumn suitsImageColumn = suitsTable.getColumnModel().getColumn(5);// suits
        hideColumn(suitsImageColumn);
        TableColumn suitsAmountColumn = suitsTable.getColumnModel().getColumn(3);// Image
        hideColumn(suitsAmountColumn);
        // packs
        DefaultTableModel packsTableModel = createTableModelForPacks();
        JTable packsTable = new JTable(packsTableModel);
        packsTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseMotionListenerToTable(packsTable);
        addMouseListenerToTable(packsTable);
        JTextField searchFieldPacks = new JTextField(20);
        searchFieldPacks.setToolTipText("Search for items by name in BOOST PACKS");
        addDocumentListenerToSearchField(searchFieldPacks, packsTable);
        JPanel packsPanel = new JPanel(new BorderLayout());// packs
        packsPanel.add(searchFieldPacks, BorderLayout.NORTH);
        packsPanel.add(new JScrollPane(packsTable), BorderLayout.CENTER);
        tabbedPane.addTab("PACKS", packsPanel);
        TableColumn packsTypeColumn = packsTable.getColumnModel().getColumn(0);// TYPE
        hideColumn(packsTypeColumn);
        TableColumn packsImageColumn = packsTable.getColumnModel().getColumn(4);// Image
        hideColumn(packsImageColumn);
        TableColumn packsAmountColumn = packsTable.getColumnModel().getColumn(3);// Amount
        hideColumn(packsAmountColumn);
        // wmods
        DefaultTableModel wmodsTableModel = createTableModelForWmods();
        JTable wmodsTable = new JTable(wmodsTableModel);
        wmodsTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseListenerToTable(wmodsTable);
        JTextField searchFieldWmods = new JTextField(20);
        searchFieldWmods.setToolTipText("Search for items by name in WEAP. MODS");
        addDocumentListenerToSearchField(searchFieldWmods, wmodsTable);
        JPanel wmodsPanel = new JPanel(new BorderLayout());// weapon mods
        wmodsPanel.add(searchFieldWmods, BorderLayout.NORTH);
        wmodsPanel.add(new JScrollPane(wmodsTable), BorderLayout.CENTER);
        tabbedPane.addTab("WEAP. MODS", wmodsPanel);
        TableColumn wmodsTypeColumn = wmodsTable.getColumnModel().getColumn(0);// weapon mods
        hideColumn(wmodsTypeColumn);
        TableRowSorter<TableModel> weaponsSorter = TableSorterConfigurator.createSorter(gunsTableModel);
        //gunsTable.setRowSorter(weaponsSorter);
        // amods
        DefaultTableModel amodsTableModel = createTableModelForAmods();
        JTable amodsTable = new JTable(amodsTableModel);
        amodsTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseListenerToTable(amodsTable);
        JTextField searchFieldAmods = new JTextField(20);
        searchFieldAmods.setToolTipText("Search for items by name in ARM. MODS.");
        addDocumentListenerToSearchField(searchFieldAmods, amodsTable);
        JPanel amodsPanel = new JPanel(new BorderLayout());// armour mods
        amodsPanel.add(searchFieldAmods, BorderLayout.NORTH);
        amodsPanel.add(new JScrollPane(amodsTable), BorderLayout.CENTER);
        tabbedPane.addTab("ARM. MODS", amodsPanel);
        TableColumn amodsTypeColumn = amodsTable.getColumnModel().getColumn(0);// armour mods
        hideColumn(amodsTypeColumn);
        // mats
        DefaultTableModel matsTableModel = createTableModelForMats();
        JTable matsTable = new JTable(matsTableModel);
        matsTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseListenerToTable(matsTable);
        JTextField searchFieldMats = new JTextField(20);
        searchFieldMats.setToolTipText("Search for items by name in MATS");
        addDocumentListenerToSearchField(searchFieldMats, matsTable);
        JPanel matsPanel = new JPanel(new BorderLayout());// materials
        matsPanel.add(searchFieldMats, BorderLayout.NORTH);
        matsPanel.add(new JScrollPane(matsTable), BorderLayout.CENTER);
        tabbedPane.addTab("MATS", matsPanel);
        TableColumn matsTypeColumn = matsTable.getColumnModel().getColumn(0);// materials
        hideColumn(matsTypeColumn);
        matsTable.getColumnModel().getColumn(3).setMaxWidth(50);// amount
        // mags
        DefaultTableModel magsTableModel = createTableModelForMags();
        JTable magsTable = new JTable(magsTableModel);
        magsTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseMotionListenerToTable(magsTable);
        addMouseListenerToTable(magsTable);
        JTextField searchFieldMags = new JTextField(20);
        searchFieldMags.setToolTipText("Search for items by name in MAGS");
        addDocumentListenerToSearchField(searchFieldMags, magsTable);
        JPanel magsPanel = new JPanel(new BorderLayout());// magazines
        magsPanel.add(searchFieldMags, BorderLayout.NORTH);
        magsPanel.add(new JScrollPane(magsTable), BorderLayout.CENTER);
        tabbedPane.addTab("MAGS", magsPanel);
        TableColumn magsTypeColumn = magsTable.getColumnModel().getColumn(0);// TYPE
        hideColumn(magsTypeColumn);
        TableColumn magsImageColumn = magsTable.getColumnModel().getColumn(5);// Image
        hideColumn(magsImageColumn);
        TableColumn magsAmountColumn = magsTable.getColumnModel().getColumn(3);// Image
        hideColumn(magsAmountColumn);
        // skills
        DefaultTableModel skillsTableModel = createTableModelForSkills();
        JTable skillsTable = new JTable(skillsTableModel);
        skillsTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseListenerToTable(skillsTable);
        JTextField searchFieldSkills = new JTextField(20);
        searchFieldSkills.setToolTipText("Search for items by name in SKILLS");
        addDocumentListenerToSearchField(searchFieldSkills, skillsTable);
        JPanel skillsPanel = new JPanel(new BorderLayout());// skills
        skillsPanel.add(searchFieldSkills, BorderLayout.NORTH);
        skillsPanel.add(new JScrollPane(skillsTable), BorderLayout.CENTER);
        tabbedPane.addTab("SKILLS", skillsPanel);
        TableColumn skillTypeColumn = skillsTable.getColumnModel().getColumn(0);// skills
        hideColumn(skillTypeColumn);
        // traits
        DefaultTableModel traitsTableModel = createTableModelForTraits();
        JTable traitsTable = new JTable(traitsTableModel);
        traitsTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseListenerToTable(traitsTable);
        JTextField searchFieldTraits = new JTextField(20);
        searchFieldTraits.setToolTipText("Search for items by name in TRAITS");
        addDocumentListenerToSearchField(searchFieldTraits, traitsTable);
        JPanel traitsPanel = new JPanel(new BorderLayout());// traits
        traitsPanel.add(searchFieldTraits, BorderLayout.NORTH);
        traitsPanel.add(new JScrollPane(traitsTable), BorderLayout.CENTER);
        tabbedPane.addTab("TRAITS", traitsPanel);
        TableColumn traitsTypeColumn = traitsTable.getColumnModel().getColumn(0);// traits
        hideColumn(traitsTypeColumn);
        // background
        DefaultTableModel backgTableModel = createTableModelForBackg();
        JTable backgTable = new JTable(backgTableModel);
        backgTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseListenerToTable(backgTable);
        JTextField searchFieldBackg = new JTextField(20);
        searchFieldBackg.setToolTipText("Search for items by name in BACKGROUND");
        addDocumentListenerToSearchField(searchFieldBackg, backgTable);
        JPanel backgPanel = new JPanel(new BorderLayout());// background
        backgPanel.add(searchFieldBackg, BorderLayout.NORTH);
        backgPanel.add(new JScrollPane(backgTable), BorderLayout.CENTER);
        tabbedPane.addTab("BACKGROUND", backgPanel);
        TableColumn backgTypeColumn = backgTable.getColumnModel().getColumn(0);// background
        hideColumn(backgTypeColumn);
        // aid
        DefaultTableModel aidTableModel = createTableModelForAid();
        JTable aidTable = new JTable(aidTableModel);
        aidTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseListenerToTable(aidTable);
        JTextField searchFieldAid = new JTextField(20);
        searchFieldAid.setToolTipText("Search for items by name in AID");
        addDocumentListenerToSearchField(searchFieldAid, aidTable);
        JPanel aidPanel = new JPanel(new BorderLayout());// aid items
        aidPanel.add(searchFieldAid, BorderLayout.NORTH);
        aidPanel.add(new JScrollPane(aidTable), BorderLayout.CENTER);
        tabbedPane.addTab("AID", aidPanel);
        TableColumn aidTypeColumn = aidTable.getColumnModel().getColumn(0);// aid
        hideColumn(aidTypeColumn);
        aidTable.getColumnModel().getColumn(3).setMaxWidth(50);// amount
        // apparel
        DefaultTableModel apparelModel = createTableModelForApparel();
        JTable apparelTable = new JTable(apparelModel);
        apparelTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseMotionListenerToTable(apparelTable);
        addMouseListenerToTable(apparelTable);
        JTextField searchFieldApparel = new JTextField(20);
        searchFieldApparel.setToolTipText("Search for items by name in APPAREL");
        addDocumentListenerToSearchField(searchFieldApparel, apparelTable);
        JPanel apparelPanel = new JPanel(new BorderLayout());// apparel
        apparelPanel.add(searchFieldApparel, BorderLayout.NORTH);
        apparelPanel.add(new JScrollPane(apparelTable), BorderLayout.CENTER);
        tabbedPane.addTab("APPAREL", apparelPanel);
        TableColumn apparelTypeColumn = apparelTable.getColumnModel().getColumn(0);// TYPE
        hideColumn(apparelTypeColumn);
        TableColumn apparelImageColumn = apparelTable.getColumnModel().getColumn(4);// Image
        hideColumn(apparelImageColumn);
        TableColumn apparelAmountColumn = apparelTable.getColumnModel().getColumn(3);// Amount
        hideColumn(apparelAmountColumn);
        // powers
        DefaultTableModel powersTableModel = createTableModelForPowers();
        JTable powersTable = new JTable(powersTableModel);
        powersTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseListenerToTable(powersTable);
        JTextField searchFieldPowers = new JTextField(20);
        searchFieldPowers.setToolTipText("Search for items by name in POWERS");
        addDocumentListenerToSearchField(searchFieldPowers, powersTable);
        JPanel powersPanel = new JPanel(new BorderLayout());// powers
        powersPanel.add(searchFieldPowers, BorderLayout.NORTH);
        powersPanel.add(new JScrollPane(powersTable), BorderLayout.CENTER);
        tabbedPane.addTab("POWERS", powersPanel);
        TableColumn powersTypeColumn = powersTable.getColumnModel().getColumn(0);// powers
        hideColumn(powersTypeColumn);
        // research
        DefaultTableModel researchTableModel = createTableModelForResearch();
        JTable researchTable = new JTable(researchTableModel);
        researchTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseListenerToTable(researchTable);
        JTextField searchFieldResearch = new JTextField(20);
        searchFieldResearch.setToolTipText("Search for items by name in WEAP. UPGRD.");
        addDocumentListenerToSearchField(searchFieldResearch, researchTable);
        JPanel researchPanel = new JPanel(new BorderLayout());// research
        researchPanel.add(searchFieldResearch, BorderLayout.NORTH);
        researchPanel.add(new JScrollPane(researchTable), BorderLayout.CENTER);
        tabbedPane.addTab("RESEARCH", researchPanel);
        TableColumn researchTypeColumn = researchTable.getColumnModel().getColumn(0);// research
        hideColumn(researchTypeColumn);
        // legwep
        DefaultTableModel legWepTableModel = createTableModelForLegWep();
        JTable legWepTable = new JTable(legWepTableModel);
        legWepTable.getTableHeader().setReorderingAllowed(false);// do not allow reordering of columns
        addMouseMotionListenerToTable(legWepTable);
        addMouseListenerToTable(legWepTable);
        JTextField searchFieldLegWep = new JTextField(20);
        searchFieldLegWep.setToolTipText("Search for items by name in Legendary Weapons.");
        addDocumentListenerToSearchField(searchFieldLegWep, legWepTable);
        JPanel legWepPanel = new JPanel(new BorderLayout());
        legWepPanel.add(searchFieldLegWep, BorderLayout.NORTH);
        legWepPanel.add(new JScrollPane(legWepTable), BorderLayout.CENTER);
        tabbedPane.addTab("LEGENDARY WEAPONS", legWepPanel);
        TableColumn legWepTypeColumn = legWepTable.getColumnModel().getColumn(0);// TYPE
        hideColumn(legWepTypeColumn);
        TableColumn legWepImageColumn = legWepTable.getColumnModel().getColumn(10);// Image
        hideColumn(legWepImageColumn);
        TableColumn legWepAmountColumn = legWepTable.getColumnModel().getColumn(3);// Amount
        hideColumn(legWepAmountColumn);
        TableRowSorter<TableModel> legendaryWeaponsSorter = TableSorterConfigurator.createSorter(legWepTableModel); // Create a TableRowSorter for the guns table
        legWepTable.setRowSorter(legendaryWeaponsSorter);

        int nameWidth = 300;
        int itemIDwidth = 70;
        setColumnWidthForAllTabs(tabbedPane, "Name", nameWidth);
        setColumnWidthForAllTabs(tabbedPane, "Item ID", itemIDwidth);

        // Add the JTabbedPane to the frame
        frame.add(tabbedPane);
        frame.setVisible(true); // Set frame as visible
    }// main

    public interface User32Ex extends StdCallLibrary {
        User32Ex INSTANCE = Native.load("user32", User32Ex.class);

        boolean SetForegroundWindow(WinDef.HWND hWnd);
        WinDef.HWND FindWindow(String lpClassName, String lpWindowName);
        void keybd_event(byte bVk, byte bScan, int dwFlags, Pointer dwExtraInfo);
    }

    // ***** TABLES *****
    // DONT
    private static DefaultTableModel createTableModelForDont() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 3 && column != 4 && column != 5 && column != 6 && column != 7;// Make the specified columns read-only
                return column == -1;
            }
        };
        //region <MELEE>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        //tableModel.addColumn("Amount");// Column 2
        //tableModel.addColumn("Damage");// Column 3
        tableModel.addColumn("Image");// Column 4
        tableModel.addRow(new Object[]{"DONT", "DO NOT CLICK!!!", "", "", "", "resources/pics/melee/0026F181.png"});
        //endregion
        return tableModel;
    }// DONT

    private static DefaultTableModel createTableModelForMiscellaneous() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 2 && column != 4;// Make the specified columns read-only
                return column == 3;
            }
        };
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addColumn("Amount");// Column 3
        tableModel.addColumn("Description");// Column 4
        tableModel.addRow(new Object[]{"ITEM", "Credits", "0000000F", 999, "DOSH"});
        tableModel.addRow(new Object[]{"ITEM", "Digipicks", "0000000A", 999, "KEYS"});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"", "HANDY DRUGS", "", "", "Don't do drugs kids!"});
        tableModel.addRow(new Object[]{"ITEM", "Junk Flush", "00143CB1", 999, "Full Detox"});
        tableModel.addRow(new Object[]{"ITEM", "Panacea", "0029A859", 999, "CureAll"});//???
        tableModel.addRow(new Object[]{"ITEM", "Hippolyta", "002C5883", 999, "Persuasion Boost"});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"", "THROWABLES"});
        tableModel.addRow(new Object[]{"ITEM", "Frag Grenade", "000115EF", 999, "BOOM"});
        tableModel.addRow(new Object[]{"ITEM", "Impact Grenade", "0026D89F", 999, "BOOM"});
        tableModel.addRow(new Object[]{"ITEM", "Incendiary Grenade", "0026F180", 999, "BOOM"});
        tableModel.addRow(new Object[]{"ITEM", "Particle Grenade", "0026D89E", 999, "BOOM"});
        tableModel.addRow(new Object[]{"ITEM", "Shrapnel Grenade", "0026D89D", 999, "BOOM"});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"", "MINES", ""});
        tableModel.addRow(new Object[]{"ITEM", "Cryo Mine", "00389F34", 999, "BOOM"});
        tableModel.addRow(new Object[]{"ITEM", "Frag Mine", "0004A41A", 999, "BOOM"});
        tableModel.addRow(new Object[]{"ITEM", "Inferno Mine", "00389F33", 999, "BOOM"});
        tableModel.addRow(new Object[]{"ITEM", "Sleep Gas Mine", "003C23E3", 999, "BOOM"});
        tableModel.addRow(new Object[]{"ITEM", "Tesla Mine", "00389F37", 999, "BOOM"});
        tableModel.addRow(new Object[]{"ITEM", "Toxic Gas Mine", "003C23E4", 999, "BOOM"});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"", "BOUNTIES", ""});
        tableModel.addRow(new Object[]{"BOUNTY", "Crimson Fleet", "00010B30", 99999});
        tableModel.addRow(new Object[]{"BOUNTY", "Freestar Collective", "000638E5", 99999});
        tableModel.addRow(new Object[]{"BOUNTY", "Neon/Ryujin Industries", "0026FDEA", 99999});
        tableModel.addRow(new Object[]{"BOUNTY", "Trade Authority", "0022E53D", 99999});
        tableModel.addRow(new Object[]{"BOUNTY", "United Colonies", "0005BD93", 99999});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"WAIT", "WAIT", "showmenu sleepwaitmenu",  1});
        //endregion
        return tableModel;
    }// MISCELLANEOUS

    private static DefaultTableModel createTableModelForAmmo() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                return column == 3;
                //return column != 0 && column != 1 && column != 3 && column != 4;// Make the specified columns read-only
            }
        };
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addColumn("Amount");// Column 3
        tableModel.addColumn("Image");
        tableModel.addRow(new Object[]{"ITEM", ".27 Caliber", "002B559C", 9999, "resources/pics/zoom.png"});
        tableModel.addRow(new Object[]{"ITEM", ".43 MI Array", "002B559A", 9999, "resources/pics/hammer.png"});
        tableModel.addRow(new Object[]{"ITEM", ".43 Ultramag", "02B5599", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", ".45 Caliber ACP", "002B5598", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", ".50 Caliber Caseless", "002B5597", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", ".50 MI Array", "002B5596", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "1.5KV LZR Cartridges", "002BAE3F", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "11MM Caseless", "002B5595", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "12.5MM ST Rivet", "002B5594", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "12G Shotgun Shell", "000547A1", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "15X25 CLL Shotgun Shell", "002B4AFC", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "3KV LZR Cartridge", "0000E8EC", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "40MM XPL", "002B5592", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "6.5MM CT", "002B5590", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "6.5MM MI Array", "002B558F", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "7.5MM Whitehot", "002B558E", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "7.62x39MM", "002B558D", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "7.77MM Caseless", "0004AD3E", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "9x39MM", "002B559B", 9999,  ""});
        tableModel.addRow(new Object[]{"ITEM", "Caseless Shotgun Shell", "002B4AFB", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Heavy Particle Fuse", "002B558A", 9999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Light Particle Fuse", "002783C7", 9999, ""});
        //endregion

        return tableModel;
    }// AMMO

    private static DefaultTableModel createTableModelForGuns() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 3 && column != 4;// Make the specified columns read-only
                return column == -1;
            }
        };
        //region <GUNS>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addColumn("Amount");// Column 3
        //tableModel.addColumn("Description");// Column 4
        tableModel.addColumn("Type");// Column 5
        tableModel.addColumn("Dam. Type");// Column 6
        tableModel.addColumn("Damage");// Column 7
        tableModel.addColumn("Fire Rate");// Column 8
        tableModel.addColumn("Mag. Size");// Column 9
        tableModel.addColumn("DPS");// Column 10
        tableModel.addColumn("Image");
        tableModel.addRow(new Object[]{"ITEM", "AA-99", "002BF65B", "1", "Rifle", "PHYSICAL", "19", "112", "30", "2094", "resources/pics/weapons/002BF65B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Arc Welder", "0026D965", "1", "Heavy", "ENERGY", "16", "76", "100", "1216", "resources/pics/weapons/0026D965.png"});
        tableModel.addRow(new Object[]{"ITEM", "Auto-Rivet", "0026D964", "1", "Heavy", "PHYSICAL", "110", "15", "10", "1650", "resources/pics/weapons/0026D964.png"});
        tableModel.addRow(new Object[]{"ITEM", "Beowulf", "0004716C", "1", "Rifle", "PHYSICAL", "40", "50", "30", "1980", "resources/pics/weapons/0004716C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Big Bang", "0026D963", "1", "Shotgun", "PHYSICAL + ENERGY", "126 (32P + 94E)", "14", "8", "1764", "resources/pics/weapons/0026D963.png"});
        tableModel.addRow(new Object[]{"ITEM", "Breach", "000547A3", "1", "Shotgun", "PHYSICAL", "109", "10", "6", "1089", "resources/pics/weapons/000547A3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Bridger", "0026D96A", "1", "Heavy", "PHYSICAL", "127", "5", "4", "635", "resources/pics/weapons/0026D96A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Coachman", "0026D96B", "1", "Shotgun", "PHYSICAL", "57", "47", "2", "2688", "resources/pics/weapons/0026D96B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Cutter", "00016758", "1", "Heavy", "ENERGY", "4", "76", "0", "304", "resources/pics/weapons/00016758.png"});
        tableModel.addRow(new Object[]{"ITEM", "Drum Beat", "0018DE2C", "1", "Rifle", "PHYSICAL", "9", "160", "60", "1408", "resources/pics/weapons/0018DE2C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Discarded Sidestar", "002F413A", "1", "Pistol", "PHYSICAL", "52", "40", "17", "?", "resources/pics/weapons/002F413A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ecliptic Pistol", "0026D96E", "1", "Rifle", "PHYSICAL", "19", "112", "30", "2094", "resources/pics/weapons/0026D96E.png"});// ???
        tableModel.addRow(new Object[]{"ITEM", "Eon", "000476C4", "1", "Pistol", "PHYSICAL", "11", "50", "12", "550", "resources/pics/weapons/000476C4.png"});
        tableModel.addRow(new Object[]{"ITEM", "Equinox", "0001BC4F", "1", "Rifle", "ENERGY", "13", "50", "20", "650", "resources/pics/weapons/0001BC4F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Grendel", "00028A02", "1", "Rifle", "PHYSICAL", "3", "170", "50", "561", "resources/pics/weapons/00028A02.png"});
        tableModel.addRow(new Object[]{"ITEM", "Hard Target", "00546CC", "1", "Rifle", "PHYSICAL", "128", "25", "2", "3190", "resources/pics/weapons/00546CC.png"});
        tableModel.addRow(new Object[]{"ITEM", "Kodama", "00253A16", "1", "Rifle", "PHYSICAL", "15", "170", "30", "2465", "resources/pics/weapons/00253A16.png"});
        tableModel.addRow(new Object[]{"ITEM", "Kraken", "0021FEB4", "1", "Pistol", "PHYSICAL", "3", "150", "25", "495", "resources/pics/weapons/0021FEB4.png"});
        tableModel.addRow(new Object[]{"ITEM", "Lawgiver", "0002D7F4", "1", "Rifle", "PHYSICAL", "34", "10", "6", "341", "resources/pics/weapons/0002D7F4.png"});
        tableModel.addRow(new Object[]{"ITEM", "Maelstrom", "002984DF", "1", "Rifle", "PHYSICAL", "4", "150", "40", "660", "resources/pics/weapons/002984DF.png"});
        tableModel.addRow(new Object[]{"ITEM", "MagShear", "0002EB3C", "1", "Rifle", "PHYSICAL", "11", "300", "75", "3300", "resources/pics/weapons/0002EB3C.png"});
        tableModel.addRow(new Object[]{"ITEM", "MagPulse", "00023606", "1", "Rifle", "PHYSICAL", "78", "17", "20", "1328", "resources/pics/weapons/00023606.png"});
        tableModel.addRow(new Object[]{"ITEM", "MagShot", "0002EB42", "1", "Pistol", "PHYSICAL", "58", "25", "6", "1458", "resources/pics/weapons/0002EB42.png"});
        tableModel.addRow(new Object[]{"ITEM", "MagSniper", "0002EB45", "1", "Rifle", "PHYSICAL", "245", "17", "12", "4170", "resources/pics/weapons/0002EB45.png"});
        tableModel.addRow(new Object[]{"ITEM", "MagStorm", "0026035E", "1", "Heavy", "PHYSICAL", "11", "400", "160", "4360", "resources/pics/weapons/0026035E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Microgun", "000546CD", "1", "Shotgun", "PHYSICAL", "10", "350", "300", "3465", "resources/pics/weapons/000546CD.png"});
        tableModel.addRow(new Object[]{"ITEM", "Novablast Disputor", "0026D968", "1", "Pistol", "ELECTRO MAG", "100", "20", "5", "2000", "resources/pics/weapons/0026D968.png"});
        tableModel.addRow(new Object[]{"ITEM", "Negotiator", "0026D970", "1", "Heavy", "PHYSICAL", "165", "12", "8", "1980", "resources/pics/weapons/0026D970.png"});
        tableModel.addRow(new Object[]{"ITEM", "Novalight", "0026D967", "1", "Pistol", "PHYSICAL + ENERGY", "33 (8P + 25E)", "22", "12", "726", "resources/pics/weapons/0026D967.png"});
        tableModel.addRow(new Object[]{"ITEM", "Old Earth Assault Rifle", "0026ED2A", "1", "Rifle", "PHYSICAL", "17", "150", "30", "2475", "resources/pics/weapons/0026ED2A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Old Earth Hunting Rifle", "0021BBCD", "1", "Rifle", "PHYSICAL", "33", "40", "20", "1320", "resources/pics/weapons/0021BBCD.png"});
        tableModel.addRow(new Object[]{"ITEM", "Old Earth Pistol", "0026D96C", "1", "Pistol", "PHYSICAL", "31", "67", "9", "2064", "resources/pics/weapons/0026D96C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Old Earth Shotgun", "00278F74", "1", "Shotgun", "PHYSICAL", "80", "6", "6", "482", "resources/pics/weapons/00278F74.png"});
        tableModel.addRow(new Object[]{"ITEM", "Orion", "002773C8", "1", "Rifle", "ENERGY", "27", "33", "30", "891", "resources/pics/weapons/002773C8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pacifier", "002953F8", "1", "Shotgun", "PHYSICAL", "65", "20", "7", "1298", "resources/pics/weapons/002953F8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Rattler", "00040826", "1", "Pistol", "PHYSICAL", "11", "50", "12", "550", "resources/pics/weapons/00040826.png"});
        tableModel.addRow(new Object[]{"ITEM", "Razorback", "00000FD6", "1", "Pistol", "PHYSICAL", "56", "12", "6", "673", "resources/pics/weapons/00000FD6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Regulator", "0002CB5F", "1", "Pistol", "PHYSICAL", "39", "18", "8", "693", "resources/pics/weapons/0002CB5F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Shotty", "0026D960", "1", "Shotgun", "PHYSICAL", "55", "60", "12", "3300", "resources/pics/weapons/0026D960.png"});
        tableModel.addRow(new Object[]{"ITEM", "Sidestar", "0026D95D", "1", "Pistol", "PHYSICAL", "13", "40", "17", "528", "resources/pics/weapons/0026D95D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Solstice", "0026D961", "1", "Pistol", "ENERGY", "10", "25", "16", "250", "resources/pics/weapons/0026D961.png"});
        tableModel.addRow(new Object[]{"ITEM", "Tombstone", "0002EB36", "1", "Rifle", "PHYSICAL", "21", "90", "20", "1881", "resources/pics/weapons/0002EB36.png"});
        tableModel.addRow(new Object[]{"ITEM", "Urban Eagle", "0026D96D", "1", "Pistol", "PHYSICAL", "43", "25", "7", "1073", "resources/pics/weapons/0026D96D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Inflictor", "0026D8A0", "1", "Rifle", "PHYSICAL + ENERGY", "151 (38P + 113E)", "25", "20", "3775", "resources/pics/weapons/0026D8A0.png"});
        tableModel.addRow(new Object[]{"ITEM", "VaRuun Starshard", "0026D8A4", "1", "Pistol", "PHYSICAL + ENERGY", "106 (26P + 80E)", "12", "12", "1272", "resources/pics/weapons/0026D8A4.png"});
        tableModel.addRow(new Object[]{"ITEM", "XM-2311", "0024561C", "Pistol", "1", "PHYSICAL", "40", "67", "9", "2653", "resources/pics/weapons/0024561C.png"});
        //endregion
        return tableModel;
    }// GUNS

    private static DefaultTableModel createTableModelForMelee() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 3 && column != 4 && column != 5 && column != 6 && column != 7;// Make the specified columns read-only
                return column == -1;
            }
        };
        //region <MELEE>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addColumn("Amount");// Column 2
        tableModel.addColumn("Damage");// Column 3
        tableModel.addColumn("Image");// Column 4
        tableModel.addRow(new Object[]{"ITEM", "Barrow Knife", "0026F181", "1", "22", "resources/pics/melee/0026F181.png"});
        tableModel.addRow(new Object[]{"ITEM", "Combat Knife", "00035A48", "1", "16", "resources/pics/melee/00035A48.png"});
        tableModel.addRow(new Object[]{"ITEM", "Osmium Dagger", "0026D966", "1", "28", "resources/pics/melee/0026D966.png"});
        tableModel.addRow(new Object[]{"ITEM", "Rescue Axe", "0004F760", "1", "17", "resources/pics/melee/0004F760.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ripshank", "0026D95E", "1", "12", "resources/pics/melee/0026D95E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Tanto", "0026D8A3", "1", "40", "resources/pics/melee/0026D8A3.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Naval Cutlass", "0026D8A5", "1", "20", "resources/pics/melee/0026D8A5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'Ruun Painblade", "0026D8A2", "1", "90", "resources/pics/melee/0026D8A2.png"});
        tableModel.addRow(new Object[]{"ITEM", "Wakizashi", "0026D8A1", "1", "49", "resources/pics/melee/0026D8A1.png"});
        //endregion
        return tableModel;
    }// MELEE

    private static DefaultTableModel createTableModelForHelmets() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 3 && column != 4;// Make the specified columns read-only
                return column == -1;
            }
        };
        //region <HELMETS>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addColumn("Amount");// Column 3
        //tableModel.addColumn("Description");// Column 4
        tableModel.addColumn("Image");
        tableModel.addRow(new Object[]{"ITEM", "Black Graviplas Helmet", "001466F6", "1", "resources/pics/helmets/001466F6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Black Open Graviplas Helmet", "001466FA", "1", "resources/pics/helmets/001466FA.png"});// ???
        tableModel.addRow(new Object[]{"ITEM", "Bounty Hunter Space Helmet", "001C0F32", "1", "resources/pics/helmets/001C0F32.png"});
        tableModel.addRow(new Object[]{"ITEM", "Broken Constellation Space Helmet", "002EDE9C", "1", "resources/pics/helmets/002EDE9C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Brown Graviplas Helmet", "001466F7", "1", "resources/pics/helmets/001466F7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Brown Open Graviplas Helmet", "001466FB", "1", "resources/pics/apparel/001466FB.png"});
        tableModel.addRow(new Object[]{"ITEM", "Constellation Space Helmet", "001E2B17", "1", "resources/pics/helmets/001E2B17.png"});
        tableModel.addRow(new Object[]{"ITEM", "Cydonia Space Helmet", "0003B424", "1", "resources/pics/helmets/0003B424.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deep Mining Space Helmet", "00052792", "1", "resources/pics/helmets/00052792.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deep Recon Space Helmet", "00169F54", "1", "resources/pics/helmets/00169F54.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deepcore Space Helmet", "0006ABFF", "1", "resources/pics/helmets/0006ABFF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deepseeker Space Helmet", "0016D15C", "1", "resources/pics/helmets/0016D15C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deimos Space Helmet", "00026BF0", "1", "resources/pics/helmets/00026BF0.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ecliptic Space Helmet", "00228829", "1", "resources/pics/helmets/00228829.png"});
        tableModel.addRow(new Object[]{"ITEM", "Explorer Space Helmet", "00169F50", "1", "1", "1", "1", "resources/pics/helmets/00169F50.png"});
        tableModel.addRow(new Object[]{"ITEM", "First Solider Helmet", "0021F3F4", "1", "resources/pics/helmets/0021F3F4.png"});
        tableModel.addRow(new Object[]{"ITEM", "Generdyne Guard Helmet", "003CD812", "1", "resources/pics/helmets/003CD812.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gran-Gran's Space Helmet", "001F22BC", "1", "1", "1", "1", "1", "resources/pics/helmets/001F22BC.png"});
        tableModel.addRow(new Object[]{"ITEM", "Graviplas Merc Helmet", "000781F7", "1", "resources/pics/helmets/000781F7.png"});// ???
        tableModel.addRow(new Object[]{"ITEM", "Gray Graviplas Helmet", "001466F8", "1", "resources/pics/helmets/001466F8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gray Open Graviplas Helmet", "001466FC", "1", "resources/pics/helmets/001466FC.png"});// ???
        tableModel.addRow(new Object[]{"ITEM", "Ground Crew Space Helmet", "002392B4", "1", "resources/pics/helmets/002392B4.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC AntiXeno Space Helmet", "0010A25E", "1", "resources/pics/helmets/0010A25E.png"});// Legendary
        tableModel.addRow(new Object[]{"ITEM", "Mantis Space Helmet", "0016640A", "1", "resources/pics/helmets/0016640A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mark I Space Helmet", "0001754F", "1", "resources/pics/helmets/0001754F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mercenary Space Helmet", "0016E0B5", "1", "resources/pics/helmets/0016E0B5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mercury Space Helmet", "001D0F94", "1", "resources/pics/helmets/001D0F94.png"});
        tableModel.addRow(new Object[]{"ITEM", "Navigator Space Helmet", "00067C93", "1", "resources/pics/helmets/00067C93.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neon Security Helmet", "001F73EF", "1", "resources/pics/helmets/001F73EF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Old Earth Space Helmet", "0003084D", "1", "resources/pics/helmets/0003084D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Open Graviplas Helmet", "000781F8", "1", "resources/pics/helmets/000781F8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Operative Helmet", "0016E0C3", "1", "resources/pics/helmets/0016E0C3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Peacemaker Helmet", "0013F97B", "1", "resources/pics/helmets/0013F97B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Assault Space Helmet", "00066822", "1", "resources/pics/helmets/00066822.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Charger Space Helmet", "00066827", "1", "resources/pics/helmets/00066827.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Corsair Space Helmet", "00066829", "1", "resources/pics/helmets/00066829.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Sniper Space Helmet", "0006682B", "1", "resources/pics/helmets/0006682B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ranger Space Helmet", "001E2AC1", "1", "resources/pics/helmets/001E2AC1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Experimental Nishina Helmet", "00065926", "1", "resources/pics/helmets/00065926.png"});// Legendary
        tableModel.addRow(new Object[]{"ITEM", "Ryujin Guard Helmet", "0037A34F", "1", "resources/pics/helmets/0037A34F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Security Guard Helmet", "00165718", "1", "resources/pics/helmets/00165718.png"});
        tableModel.addRow(new Object[]{"ITEM", "Shocktroop Space Helmet", "00169F58", "1", "resources/pics/helmets/00169F58.png"});
        tableModel.addRow(new Object[]{"ITEM", "Space Trucker Space Helmet", "0016E0BD", "1", "resources/pics/helmets/0016E0BD.png"});
        tableModel.addRow(new Object[]{"ITEM", "Star Roamer Space Helmet", "00003E8F", "1", "resources/pics/helmets/00003E8F.png"});
        tableModel.addRow(new Object[]{"ITEM", "SY-920 Space Helmet", "002F4B39", "1", "resources/pics/helmets/002F4B39.png"});
        tableModel.addRow(new Object[]{"ITEM", "SY-920 Pilot Space Helmet", "002F4B39", "1", "resources/pics/apparel/002F4B39.png"});
        tableModel.addRow(new Object[]{"ITEM", "SysDef Armored Space Helmet", "00398107", "1", "resources/pics/helmets/00398107.png"});
        tableModel.addRow(new Object[]{"ITEM", "SysDef Space Helmet", "00398106", "1", "resources/pics/helmets/00398106.png"});
        tableModel.addRow(new Object[]{"ITEM", "System Def Ace Space Helmet", "002AAF45", "1", "resources/pics/helmets/002AAF45.png"});
        tableModel.addRow(new Object[]{"ITEM", "Teal Open Graviplas Helmet", "001466FD", "1", "resources/pics/helmets/001466FD.png"});// ???
        tableModel.addRow(new Object[]{"ITEM", "Trackers Alliance Space Helmet", "00166403", "1", "resources/pics/helmets/00166403.png"});
        tableModel.addRow(new Object[]{"ITEM", "Trident Guard Helmet", "0014E44E", "1", "resources/pics/helmets/0014E44E.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Ace Pilot Space Helmet", "0016640F", "1", "resources/pics/helmets/0016640F.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Armored Space Helmet", "0025780B", "1", "resources/pics/helmets/0025780B.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Marine Space Helmet", "00257806", "1", "resources/pics/helmets/00257806.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Sec Spaceriot Helmet", "000EF9B2", "1", "resources/pics/helmets/000EF9B2.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Security Helmet", "0025E8D5", "1", "resources/pics/helmets/0025E8D5.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Security Space Helmet", "000EF9B1", "1", "resources/pics/helmets/000EF9B1.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Urbanwar Space Helmet", "0021A86B", "1", "resources/pics/helmets/0021A86B.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Vanguard Space Helmet", "00248C0E", "1", "resources/pics/helmets/00248C0E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Space Helmet", "0016D3D1", "1", "resources/pics/helmets/0016D3D1.png"});
        //endregion
        return tableModel;
    }// HELMETS

    private static DefaultTableModel createTableModelForSuits() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 3 && column != 4;// Make the specified columns read-only
                return column == -1;
            }
        };
        //region <SUITS>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addColumn("Amount");// Column 3
        tableModel.addColumn("Description");// Column 4
        tableModel.addColumn("Image");// Column 5
        tableModel.addRow(new Object[]{"ITEM", "Bounty Hunter Spacesuit", "00228570", "1", "Base", "resources/pics/spacesuits/00228570.png"});
        tableModel.addRow(new Object[]{"ITEM", "Constellation Spacesuit", "001E2B18", "1", "Base", "resources/pics/spacesuits/001E2B18.png"});
        tableModel.addRow(new Object[]{"ITEM", "Cydonia Spacesuit", "0003B422", "1", "Base", "resources/pics/spacesuits/0003B422.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deep Mining Spacesuit", "0005278E", "1", "Base", "resources/pics/spacesuits/0005278E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deep Recon Spacesuit", "002265AE", "1", "Base", "resources/pics/spacesuits/002265AE.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deepcore Spacesuit", "0006AC00", "1", "Base", "resources/pics/spacesuits/0006AC00.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deepseeker Spacesuit", "0016D2C4", "1", "Base", "resources/pics/spacesuits/0016D2C4.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deimos Spacesuit", "00026BF1", "1", "Base", "resources/pics/spacesuits/00026BF1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ecliptic Spacesuit", "0022856F", "1", "Base", "resources/pics/spacesuits/0022856F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Experimental Nishina Spacesuit", "0065925", "1", "Unique", "resources/pics/spacesuits/0065925.png"});
        tableModel.addRow(new Object[]{"ITEM", "Explorer Spacesuit", "002265AF", "1", "Base", "resources/pics/spacesuits/002265AF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gran-Gran's Spacesuit", "001F22BB", "1", "Unique", "resources/pics/spacesuits/001F22BB.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ground Crew Spacesuit", "002392B5", "1", "Base", "resources/pics/spacesuits/002392B5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Experimental Nishina Spacesuit", "00065925", "1", "Legendary", "resources/pics/spacesuits/00065925.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mantis Spacesuit", "00226299", "1", "Unique", "resources/pics/spacesuits/00226299.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mark I Spacesuit", "0001754D", "1", "Base", "resources/pics/spacesuits/0001754D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mercenary Spacesuit", "00226296", "1", "Base", "resources/pics/spacesuits/00226296.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mercury Spacesuit", "001D0F96", "1", "Unique", "resources/pics/spacesuits/001D0F96.png"});
        tableModel.addRow(new Object[]{"ITEM", "Monster Costume", "00225FC9", "1", "Unique", "resources/pics/spacesuits/00225FC9.png"});
        tableModel.addRow(new Object[]{"ITEM", "Navigator Spacesuit", "00067C94", "1", "Base", "resources/pics/spacesuits/00067C94.png"});
        tableModel.addRow(new Object[]{"ITEM", "Old Earth Spacesuit", "0003084E", "1", "Base", "resources/pics/spacesuits/0003084E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Peacemaker Spacesuit", "0013F97D", "1", "Rare", "resources/pics/spacesuits/0013F97D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Assault Spacesuit", "00066821", "1", "Base", "resources/pics/spacesuits/00066821.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Charger Spacesuit", "00066826", "1", "Base", "resources/pics/spacesuits/00066826.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Corsair Spacesuit", "00066828", "1", "Base", "resources/pics/spacesuits/00066828.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Sniper Spacesuit", "0006682A", "1", "Base", "resources/pics/spacesuits/0006682A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ranger Spacesuit", "00227CA0", "1", "Base", "resources/pics/spacesuits/00227CA0.png"});
        tableModel.addRow(new Object[]{"ITEM", "Explorer Spacesuit", "0022B8F6", "1", "Epic", "resources/pics/spacesuits/0022B8F6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Security Flightsuit", "000CC4D1", "1", "Base", "resources/pics/apparel/000CC4D1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Sentinel's UC Antixeno Spacesuit", "0007B2B9", "1", "Legendary", "resources/pics/spacesuits/0007B2B9.png"});//???
        tableModel.addRow(new Object[]{"ITEM", "Shocktroop Spacesuit", "002265AD", "1", "Base", "resources/pics/spacesuits/002265AD.png"});
        tableModel.addRow(new Object[]{"ITEM", "Space Trucker Spacesuit", "0021C780", "1", "Base", "resources/pics/spacesuits/0021C780.png"});
        tableModel.addRow(new Object[]{"ITEM", "Star Roamer Spacesuit", "00004E78", "1", "Base", "resources/pics/spacesuits/00004E78.png"});
        tableModel.addRow(new Object[]{"ITEM", "Starborn Spacesuit Astra", "0012E187", "1", "Unique", "resources/pics/spacesuits/0012E187.png"});
        tableModel.addRow(new Object[]{"ITEM", "Starborn Spacesuit Avitus", "001CBA52", "1", "Unique", "resources/pics/spacesuits/001CBA52.png"});
        tableModel.addRow(new Object[]{"ITEM", "Starborn Spacesuit Bellum", "001CBA4E", "1", "Unique", "resources/pics/spacesuits/001CBA4E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Starborn Spacesuit Gravitas", "0021C77E", "1", "Unique", "resources/pics/spacesuits/0021C77E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Starborn Spacesuit Locus", "001CBA4A", "1", "Unique", "resources/pics/spacesuits/001CBA4A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Starborn Spacesuit Materia", "001CBA49", "1", "Unique", "resources/pics/spacesuits/001CBA49.png"});
        tableModel.addRow(new Object[]{"ITEM", "Starborn Spacesuit Solis", "002D7365", "1", "Unique", "resources/pics/spacesuits/002D7365.png"});
        tableModel.addRow(new Object[]{"ITEM", "Starborn Spacesuit Tempus", "002D7346", "1", "Unique", "resources/pics/spacesuits/002D7346.png"});
        tableModel.addRow(new Object[]{"ITEM", "Starborn Spacesuit Tenebris", "001CBA4D", "1", "Unique", "resources/pics/spacesuits/001CBA4D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Starborn Spacesuit Venator", "0021C77F", "1", "Unique", "resources/pics/spacesuits/0021C77F.png"});
        tableModel.addRow(new Object[]{"ITEM", "SY-920 Pilot Spacesuit", "00214769", "1", "Unique", "resources/pics/spacesuits/00214769.png"});
        tableModel.addRow(new Object[]{"ITEM", "SysDef Ace Spacesuit", "002AAF44", "1", "Base", "resources/pics/spacesuits/002AAF44.png"});
        tableModel.addRow(new Object[]{"ITEM", "SysDef Assault Spacesuit", "00398104", "1", "Base", "resources/pics/spacesuits/00398104.png"});
        tableModel.addRow(new Object[]{"ITEM", "SysDef Combat Spacesuit", "0039810A", "1", "Base", "resources/pics/spacesuits/0039810A.png"});
        tableModel.addRow(new Object[]{"ITEM", "SY-920 Pilot", "001773BD", "1", "Base", "resources/pics/apparel/001773BD.png"});
        tableModel.addRow(new Object[]{"ITEM", "SY-920 Pilot Spacesuit", "00214769", "1", "Base", "resources/pics/apparel/00214769.png"});
        tableModel.addRow(new Object[]{"ITEM", "SysDef Recon Spacesuit", "00398108", "1", "Base", "resources/pics/spacesuits/00398108.png"});
        tableModel.addRow(new Object[]{"ITEM", "SysDef Spacesuit", "00398103", "1", "Base", "resources/pics/spacesuits/00398103.png"});
        tableModel.addRow(new Object[]{"ITEM", "Trackers Alliance Spacesuit", "00166404", "1", "Base", "resources/pics/spacesuits/00166404.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Ace Spacesuit", "00166410", "1", "Base", "resources/pics/spacesuits/00166410.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC AntiXeno Spacesuit", "00206130", "1", "Base", "resources/pics/spacesuits/00206130.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Combat Spacesuit", "00257808", "1", "Base", "resources/pics/spacesuits/00257808.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Marine Spacesuit", "00257805", "1", "Base", "resources/pics/spacesuits/00257805.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Sec Combat Spacesuit", "000EF9B0", "1", "Base", "resources/pics/spacesuits/000EF9B0.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Sec Recon Spacesuit", "000EF9AF", "1", "Base", "resources/pics/spacesuits/000EF9AF.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Sec Starlaw Spacesuit", "000EF9AE", "1", "Base", "resources/pics/spacesuits/000EF9AE.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Security Spacesuit", "000EF9AD", "1", "Base", "resources/pics/spacesuits/000EF9AD.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Startroop Spacesuit", "00257809", "1", "Base", "resources/pics/spacesuits/00257809.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Urbanwar Spacesuit", "0021A86A", "1", "Base", "resources/pics/spacesuits/0021A86A.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Vanguard Spacesuit", "00248C0F", "1", "Unique", "resources/pics/spacesuits/00248C0F.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Wardog Spacesuit", "0025780A", "1", "Base", "resources/pics/spacesuits/0025780A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Utility Flightsuit", "00251F56", "1", "Base", "resources/pics/apparel/00251F56.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'Ruun Spacesuit", "00227CA3", "1", "Base", "resources/pics/spacesuits/00227CA3.png"});
        //endregion
        return tableModel;
    }// SUITS //// check formatting here

    private static DefaultTableModel createTableModelForPacks() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 3 && column != 4;// Make the specified columns read-only
                return column == -1;
            }
        };
        //region <PACKS>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addColumn("Amount");// Column 3
        //tableModel.addColumn("Description");// Column 4
        tableModel.addColumn("Image");
        tableModel.addRow(new Object[]{"ITEM", "Bounty Hunter Seek Pack", "001C0F34", "1", "resources/pics/packs/001C0F34.png"});
        tableModel.addRow(new Object[]{"ITEM", "Bounty Hunter Stalk Pack", "001C0F35", "1", "resources/pics/packs/001C0F35.png"});
        tableModel.addRow(new Object[]{"ITEM", "Bounty Hunter Track Pack", "001C0F33", "1", "resources/pics/packs/001C0F33.png"});
        tableModel.addRow(new Object[]{"ITEM", "Constellation Pack", "001E2B19", "1", "resources/pics/packs/001E2B19.png"});
        tableModel.addRow(new Object[]{"ITEM", "Cydonia Pack", "0003B423", "1", "resources/pics/packs/0003B423.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deep Mining Pack", "002EDF1F", "1", "resources/pics/packs/002EDF1F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deep Recon Pack", "00169F55", "1", "resources/pics/packs/00169F55.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deepcore Pack", "000FD333", "1", "resources/pics/packs/000FD333.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deepseeker Pack", "0016D15B", "1", "resources/pics/packs/0016D15B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deimos Pack", "00026BEF", "1", "resources/pics/packs/00026BEF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deimos Tunnel Pack", "00026BF2", "1", "resources/pics/packs/00026BF2.png"});// ??? missing pic
        tableModel.addRow(new Object[]{"ITEM", "Ecliptic Pack", "00166407", "1", "resources/pics/packs/00166407.png"});
        tableModel.addRow(new Object[]{"ITEM", "Explorer Pack", "00169F51", "1", "resources/pics/packs/00169F51.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ground Crew Pack", "002392B3", "1", "resources/pics/packs/002392B3.png"});
        tableModel.addRow(new Object[]{"ITEM", "High School Backpack", "00003A77", "1", "resources/pics/packs/00003A77.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mantis Pack", "0016640B", "1", "resources/pics/packs/0016640B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mark I Pack", "0001754E", "1", "resources/pics/packs/0001754E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mercenary Pack", "0016E0B6", "1", "resources/pics/packs/0016E0B6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mercury Pack", "001D0F95", "1", "resources/pics/packs/001D0F95.png"});
        tableModel.addRow(new Object[]{"ITEM", "Navigator Pack", "00067C95", "1", "resources/pics/packs/00067C95.png"});
        tableModel.addRow(new Object[]{"ITEM", "Old Earth Pack", "0003084C", "1", "resources/pics/packs/0003084C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Peacemarker Pack", "0013F97C", "1", "resources/pics/packs/0013F97C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Raiding Pack", "00066824", "1", "resources/pics/packs/00066824.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Survival Pack", "00066825", "1", "resources/pics/packs/00066825.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ranger Pack", "001E2AF7", "1", "resources/pics/packs/001E2AF7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Shocktrooper Pack", "00169F59", "1", "resources/pics/packs/00169F59.png"});
        tableModel.addRow(new Object[]{"ITEM", "Space Trucker Pack", "0016E0BB", "1", "resources/pics/packs/0016E0BB.png"});
        tableModel.addRow(new Object[]{"ITEM", "Star Roamer Pack", "00003E90", "1", "resources/pics/packs/00003E90.png"});
        tableModel.addRow(new Object[]{"ITEM", "SY-920 Pilot Pack", "001773BD", "1", "resources/pics/packs/001773BD.png"});
        tableModel.addRow(new Object[]{"ITEM", "SysDef Pack", "00398105", "1", "resources/pics/packs/00398105.png"});
        tableModel.addRow(new Object[]{"ITEM", "SysDef Pilot", "002AAF43", "1", "resources/pics/packs/002AAF43.png"});
        tableModel.addRow(new Object[]{"ITEM", "Tracker Alliance Pack", "00166402", "1", "resources/pics/packs/00166402.png"});
        tableModel.addRow(new Object[]{"ITEM", "Tunnel Mining Pack", "00029C7A", "1", "resources/pics/packs/00029C7A.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Ace Pilot Pack", "0016640E", "1", "resources/pics/packs/0016640E.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC AntiXeno Power Pack", "0010A25D", "1", "Legendary", "resources/pics/packs/0010A25D.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Marine Pack", "00257807", "1", "resources/pics/packs/00257807.png"});// ???
        tableModel.addRow(new Object[]{"ITEM", "UC Security Pack", "000EF9AC", "1", "resources/pics/packs/000EF9AC.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Shock Armor Skip Pack", "0021A86C", "1", "resources/pics/packs/0021A86C.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Vanguard Pilot Pack", "003E3D4F", "1", "resources/pics/packs/003E3D4F.png"});// BAD ID???
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Pack", "0016D3D0", "1", "resources/pics/packs/0016D3D0.png"});
        //endregion
        return tableModel;
    }// PACKS

    private static DefaultTableModel createTableModelForWmods() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 3 && column != 4;// Make the specified columns read-only
                return column == -1;
            }
        };
        //region <WEAP. MODS>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        //tableModel.addColumn("Amount");// Column 3
        tableModel.addColumn("Description");// Column 4
        tableModel.addRow(new Object[]{"", "*** CLICK WEAPON TO MOD FIRST!!! ***", "", "HAVE WEAPON TO MOD ALREADY SELECTED IN STARFIELD CONSOLE", "*** CLICK WEAPON TO MOD FIRST!!! ***"});
        tableModel.addRow(new Object[]{"", "", "", "IMPORTANT: DO NOT SKIP TIERS - UPGRADE IN ORDER: 2, 3, 4, 5"});
        tableModel.addRow(new Object[]{"", ""});
        tableModel.addRow(new Object[]{"", "INCREASE TIER"});
        tableModel.addRow(new Object[]{"MOD", "Tier 2", "28F442", "Calibrated"});
        tableModel.addRow(new Object[]{"MOD", "Tier 3", "28F443", "Refined"});
        tableModel.addRow(new Object[]{"MOD", "Tier 4", "28F444", "Advanced"});
        tableModel.addRow(new Object[]{"MOD", "Tier 5", "28F445", "Superior - DOES NOT APPLY TO ALL GUNS"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"", "ADD MODS"});
        //tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"", "GROUP 1", "SELECT ONLY ONE", "ADDING MORE THAN 1 MOD REPLACES PREVIOUS MOD"});
        tableModel.addRow(new Object[]{"MOD", "Anti-Personnel", "000FF442", "10% damage against humans."});
        tableModel.addRow(new Object[]{"MOD", "Bashing", "000FEA07", "Deals double damage when gun bashing."});
        tableModel.addRow(new Object[]{"MOD", "Berserker", "000F437E", "Does more damage the less armor one has."});
        tableModel.addRow(new Object[]{"MOD", "Cornered", "000F428E", "Damage increases as health decreases."});
        tableModel.addRow(new Object[]{"MOD", "Disassembler", "001625EB", "+20% damage against robots."});
        tableModel.addRow(new Object[]{"MOD", "Extended Magazine", "000FFA3B", "Doubles the base magazine capacity"});
        tableModel.addRow(new Object[]{"MOD", "Exterminator", "0015DD18", "+30% damage against aliens."});
        tableModel.addRow(new Object[]{"MOD", "Furious", "000EA117", "Each consecutive hit deals more damage."});
        tableModel.addRow(new Object[]{"MOD", "Instigating", "000F2013", "Deals double damage to targets with full health."});
        tableModel.addRow(new Object[]{"MOD", "Oxygenated", "000FAEAB", "Hold breath time when aiming with a scoped weapon is increased."});
        tableModel.addRow(new Object[]{"MOD", "Space-Adept", "000F7321", "+30% damage while in space, and -15% damage while on a planet."});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"", "GROUP 2", "SELECT ONLY ONE", "ADDING MORE THAN 1 MOD REPLACES PREVIOUS MOD"});
        tableModel.addRow(new Object[]{"MOD", "Corrosive", "0008AB47", "Randomly deals corrosive damage and reduces the targets’ armor over 6 seconds."});
        tableModel.addRow(new Object[]{"MOD", "Crippling", "000F2E39", "Deals +30% damage on the next attack after hitting a targets limbs."});
        tableModel.addRow(new Object[]{"MOD", "Handloading", "000EA0BA", "Volatile rounds that are designed to pack a bigger punch, but aren't as stable and can fail on occaission."});
        tableModel.addRow(new Object[]{"MOD", "Hitman", "00122F1C", "+15% damage while aiming."});
        tableModel.addRow(new Object[]{"MOD", "Incendiary", "007D728", "Randomly deals Incendiary damage. Burn baby, burn."});
        tableModel.addRow(new Object[]{"MOD", "Lacerate", "000FEA49", "Randomly applies a bleed effect to the target."});
        tableModel.addRow(new Object[]{"MOD", "Med Theft", "000FFA3C", "Chance that humans drop extra Med Packs on death."});
        tableModel.addRow(new Object[]{"MOD", "Poison", "00319AEC", "Randomly deals poison damage and slows the target."});
        tableModel.addRow(new Object[]{"MOD", "Radioactive", "00EA13B", "Randomly deals radioactive damage and demoralizes the target."});
        tableModel.addRow(new Object[]{"MOD", "Rapid", "000FEA04", "+25% increase in attack speed."});
        tableModel.addRow(new Object[]{"MOD", "Staggering", "000E8D64", "Small chance to stagger enemies"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"", "GROUP 3", "SELECT ONLY ONE", "ADDING MORE THAN 1 MOD REPLACES PREVIOUS MOD"});
        tableModel.addRow(new Object[]{"MOD", "Concussive", "000FBD3C", "Small chance to knock down targets."});
        tableModel.addRow(new Object[]{"MOD", "Demoralizing", "000FC884", "Small chance to demoralize a target."});
        tableModel.addRow(new Object[]{"MOD", "Elemental", "0031C0C5", "Randomly deals Corrosive, Radiation, Poison, or Incendiary damage."});
        tableModel.addRow(new Object[]{"MOD", "Explosive", "000FA8D6", "Randomly switches to explosive rounds."});
        tableModel.addRow(new Object[]{"MOD", "Frenzy", "000FC8A4", "Small chance to frenzy a target."});
        tableModel.addRow(new Object[]{"MOD", "One Inch Punch", "000F4CF0", "Rounds fire in a shotgun-like spread."});
        tableModel.addRow(new Object[]{"MOD", "Shattering", "000F4557", "Break through even the strongest armor"});
        tableModel.addRow(new Object[]{"MOD", "Skip Shot", "0031C0C4", "Every fourth shot fires two projectiles at once."});
        tableModel.addRow(new Object[]{"MOD", "Telsa", "0031C0C6", "Rounds will sometimes emit electricity where they land that damages and slows nearby targets."});
        tableModel.addRow(new Object[]{"MOD", "Titanium Build", "000FFA3D", "Premium build materials make this weapon light as a feather"});
        /*
        1 - tableModel.addRow(new Object[]{"MOD", "Anti-Personnel", "000FF442", "10% damage against humans."});
        1 - tableModel.addRow(new Object[]{"MOD", "Bashing", "000FEA07", "Deals double damage when gun bashing."});
        1 - tableModel.addRow(new Object[]{"MOD", "Berserker", "000F437E", "Does more damage the less armor one has."});
        3 - tableModel.addRow(new Object[]{"MOD", "Concussive", "000FBD3C", "Small chance to knock down targets."});
        2 - tableModel.addRow(new Object[]{"MOD", "Corrosive", "0008AB47", "Randomly deals corrosive damage and reduces the targets’ armor over 6 seconds."});
        1 - tableModel.addRow(new Object[]{"MOD", "Cornered", "000F428E", "Damage increases as health decreases."});
        2 - tableModel.addRow(new Object[]{"MOD", "Crippling", "000F2E39", "Deals +30% damage on the next attack after hitting a targets limbs."});
        3 - tableModel.addRow(new Object[]{"MOD", "Demoralizing", "000FC884", "Small chance to demoralize a target."});
        1 - tableModel.addRow(new Object[]{"MOD", "Disassembler", "001625EB", "+20% damage against robots."});
        3 - tableModel.addRow(new Object[]{"MOD", "Elemental", "0031C0C5", "Randomly deals Corrosive, Radiation, Poison, or Incendiary damage."});
        3 - tableModel.addRow(new Object[]{"MOD", "Explosive", "000FA8D6", "Randomly switches to explosive rounds."});
        1 - tableModel.addRow(new Object[]{"MOD", "Extended Magazine", "000FFA3B", "Doubles the base magazine capacity"});
        1 - tableModel.addRow(new Object[]{"MOD", "Exterminator", "0015DD18", "+30% damage against aliens."});
        3 - tableModel.addRow(new Object[]{"MOD", "Frenzy", "000FC8A4", "Small chance to frenzy a target."});
        1 - tableModel.addRow(new Object[]{"MOD", "Furious", "000EA117", "Each consecutive hit deals more damage."});
        2 - tableModel.addRow(new Object[]{"MOD", "Handloading", "000EA0BA", "Volatile rounds that are designed to pack a bigger punch, but aren't as stable and can fail on occaission."});
        2 - tableModel.addRow(new Object[]{"MOD", "Hitman", "00122F1C", "+15% damage while aiming."});
        2 - tableModel.addRow(new Object[]{"MOD", "Incendiary", "007D728", "Randomly deals Incendiary damage. Burn baby, burn."});
        1 - tableModel.addRow(new Object[]{"MOD", "Instigating", "000F2013", "Deals double damage to targets with full health."});
        2 - tableModel.addRow(new Object[]{"MOD", "Lacerate", "000FEA49", "Randomly applies a bleed effect to the target."});
        2 - tableModel.addRow(new Object[]{"MOD", "Med Theft", "000FFA3C", "Chance that humans drop extra Med Packs on death."});
        3 - tableModel.addRow(new Object[]{"MOD", "One Inch Punch", "000F4CF0", "Rounds fire in a shotgun-like spread."});
        1 - tableModel.addRow(new Object[]{"MOD", "Oxygenated", "000FAEAB", "Hold breath time when aiming with a scoped weapon is increased."});
        2 - tableModel.addRow(new Object[]{"MOD", "Poison", "00319AEC", "Randomly deals poison damage and slows the target."});
        2 - tableModel.addRow(new Object[]{"MOD", "Radioactive", "00EA13B", "Randomly deals radioactive damage and demoralizes the target."});
        2 - tableModel.addRow(new Object[]{"MOD", "Rapid", "000FEA04", "+25% increase in attack speed."});
        3 - tableModel.addRow(new Object[]{"MOD", "Shattering", "000F4557", "Break through even the strongest armor"});
        3 - tableModel.addRow(new Object[]{"MOD", "Skip Shot", "0031C0C4", "Every fourth shot fires two projectiles at once."});
        1 - tableModel.addRow(new Object[]{"MOD", "Space-Adept", "000F7321", "+30% damage while in space, and -15% damage while on a planet."});
        2 - tableModel.addRow(new Object[]{"MOD", "Staggering", "000E8D64", "Small chance to stagger enemies"});
        3 - tableModel.addRow(new Object[]{"MOD", "Telsa", "0031C0C6", "Rounds will sometimes emit electricity where they land that damages and slows nearby targets."});
        3 - tableModel.addRow(new Object[]{"MOD", "Titanium Build", "000FFA3D", "Premium build materials make this weapon light as a feather"});
        */
        //endregion
        return tableModel;
    }// WEAP. MODS

    private static DefaultTableModel createTableModelForAmods() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                return column == -1;
            }
        };
        //region <ARM. MODS>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addColumn("Description");// Column 4
        tableModel.addRow(new Object[]{"","*** CLICK ARMOUR TO MOD FIRST!!! ***", "","HAVE ARMOUR YOU WANT TO MOD ALREADY SELECTED IN STARFIELD CONSOLE", "*** CLICK ARMOUR TO MOD FIRST!!! ***"});
        tableModel.addRow(new Object[]{"", "", "", "IMPORTANT: UNLIKE WEAPONS, ARMOUR TIERS CAN BE SKIPPED WITHOUT PENALTY"});
        tableModel.addRow(new Object[]{"", "INCREASE TIER", "", ""});
        tableModel.addRow(new Object[]{"MOD", "Tier 2", "0011E2B6", "Calibrated"});
        tableModel.addRow(new Object[]{"MOD", "Tier 3", "0011E2B7", "Refined"});
        tableModel.addRow(new Object[]{"MOD", "Tier 4", "0011E2B8", "Advanced"});
        tableModel.addRow(new Object[]{"MOD", "Tier 5", "0003AF80", "Superior"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"", "ADD MODS"});
        //tableModel.addRow(new Object[]{"", "GROUP 1 - RARE, EPIC & LEGENDARY", "SELECT ONLY ONE", "ADDING MORE THAN 1, REPLACES PREVIOUS MOD"});
        tableModel.addRow(new Object[]{"", "GROUP 1", "", "ADDING MORE THAN 1 MOD REPLACES PREVIOUS MOD"});
        tableModel.addRow(new Object[]{"MOD", "Ablative", "0013369C", "-15% incoming energy damage."});
        tableModel.addRow(new Object[]{"MOD", "Anti-Ballistic", "0013369E", "-15% incoming dmg from ranged weapons."});
        tableModel.addRow(new Object[]{"MOD", "Beast Hunter", "001336BD", "-15% dmg from aliens."});
        tableModel.addRow(new Object[]{"MOD", "Bolstering", "001336C6", "Grants up to +100 energy and physical resistance, the lower your health."});
        tableModel.addRow(new Object[]{"MOD", "Chameleon", "001336C1", "Makes you invisible in sneak while not moving."});
        tableModel.addRow(new Object[]{"MOD", "Combat Veteran", "001336BE", "-15% dmg from humans."});
        tableModel.addRow(new Object[]{"MOD", "O2 Boosted", "000690B0", "+20% oxygen capacity."});
        tableModel.addRow(new Object[]{"MOD", "O2 Filter", "000690AE", "-25% oxygen consumption."});
        tableModel.addRow(new Object[]{"MOD", "Sturdy", "00133699", "-15% incoming melee dmg."});
        tableModel.addRow(new Object[]{"MOD", "Technician", "001336BC", "-15% damage from Robot enemies."});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"", "GROUP 2", "", "ADDING MORE THAN 1 MOD REPLACES PREVIOUS MOD"});
        tableModel.addRow(new Object[]{"MOD", "Acrobat", "000710FD", "Reduces fall dmg by 50%."});
        tableModel.addRow(new Object[]{"MOD", "Analyzer", "000690AF", "+10% damage to scanned targets."});
        tableModel.addRow(new Object[]{"MOD", "Antiseptic", "000710FA", "+25 airborne resistance."});
        tableModel.addRow(new Object[]{"MOD", "Auto Medic", "000C9A43", "Automatically use a med pack when hit and health is below 25%, 60s cd."});
        tableModel.addRow(new Object[]{"MOD", "Fastened", "002EDE4E", "+20 carry capacity."});
        tableModel.addRow(new Object[]{"MOD", "Galvanized", "000710F7", "+25 corrosive resistance."});
        tableModel.addRow(new Object[]{"MOD", "Hacker", "002C43DA", "+2 max auto attempts that can be banked while hacking."});
        tableModel.addRow(new Object[]{"MOD", "Leadlined", "000710F5", "+25 radiation resistance."});
        tableModel.addRow(new Object[]{"MOD", "Liquid Cooled", "000710F6", "+25 thermal resistance."});
        tableModel.addRow(new Object[]{"MOD", "Resource Hauler", "00060293", "Resources weigh 25% less."});
        tableModel.addRow(new Object[]{"MOD", "Weapon Holsters", "00060295", "Weapons weigh 50% less."});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"", "GROUP 3", "", "ADDING MORE THAN 1 MOD REPLACES PREVIOUS MOD"});
        tableModel.addRow(new Object[]{"MOD", "Armor Plated", "002EDE59", "-10% incoming Physical,Energy, and EM damage."});
        tableModel.addRow(new Object[]{"MOD", "Assisted Carry", "002EDE4F", "Drain 75% less O2 when running while encumbered."});
        tableModel.addRow(new Object[]{"MOD", "Headhunter", "002C43DC", "Deals +25% damage on the next attack after hitting a target's head."});
        tableModel.addRow(new Object[]{"MOD", "Incendiary", "00002983", "10% chance to ignite nearby attackers."});
        tableModel.addRow(new Object[]{"MOD", "Mechanized", "000BE542", "+40 carry capacity."});
        tableModel.addRow(new Object[]{"MOD", "Mirrored", "00059AE8", "4% chance to reflect attacks."});
        tableModel.addRow(new Object[]{"MOD", "Peacemaker", "002D01A2", "Rifles do 10% more damage."});
        tableModel.addRow(new Object[]{"MOD", "Reactive", "0006029F", "10% chance to stagger nearby attackers."});
        tableModel.addRow(new Object[]{"MOD", "Repulsing", "0006029D", "5% chance to disarm nearby attackers."});
        tableModel.addRow(new Object[]{"MOD", "Sensor Chip", "002C43DB", "+20% accuracy while firing on the move."});
        tableModel.addRow(new Object[]{"MOD", "Sentinel", "000BE540", "75% chance to take 50% less dmg while not moving."});
        tableModel.addRow(new Object[]{"", "", "", ""});
        /*
        //THESE DO NOT SEEM TO WORK!!!
        tableModel.addRow(new Object[]{"MOD", "Combat Resist +1", "25AF2B"});
        tableModel.addRow(new Object[]{"MOD", "Combat Resist +2", "1C5AA4"});
        tableModel.addRow(new Object[]{"MOD", "Combat Resist +3", "1C5AA6"});
        tableModel.addRow(new Object[]{"MOD", "Combat Resist +4", "1C5AAC"});
        tableModel.addRow(new Object[]{"MOD", "Combat Resist +5", "1C5AAD"});
        tableModel.addRow(new Object[]{"MOD", "Haz. Resist +1", "1C5AFA"});
        tableModel.addRow(new Object[]{"MOD", "Haz. Resist +2", "1C5AFB"});
        */
        /*
        tableModel.addRow(new Object[]{"", "ADD MOD", "ADD MOD", "ADD MOD"});
        1 - tableModel.addRow(new Object[]{"MOD", "Ablative", "0013369C", "-15% incoming energy damage."});
        1 - tableModel.addRow(new Object[]{"MOD", "Anti-Ballistic", "0013369E", "-15% incoming dmg from ranged weapons."});
        1 - tableModel.addRow(new Object[]{"MOD", "Beast Hunter", "001336BD", "-15% dmg from aliens."});
        1 - tableModel.addRow(new Object[]{"MOD", "Bolstering", "001336C6", "Grants up to +100 energy and physical resistance, the lower your health."});
        1 - tableModel.addRow(new Object[]{"MOD", "Chameleon", "001336C1", "Makes you invisible in sneak while not moving."});
        1 - tableModel.addRow(new Object[]{"MOD", "Combat Veteran", "001336BE", "-15% dmg from humans."});
        1 - tableModel.addRow(new Object[]{"MOD", "O2 Boosted", "000690B0", "+20% oxygen capacity."});
        1 - tableModel.addRow(new Object[]{"MOD", "O2 Filter", "000690AE", "-25% oxygen consumption."});
        1 - tableModel.addRow(new Object[]{"MOD", "Sturdy", "00133699", "-15% incoming melee dmg."});
        1 - tableModel.addRow(new Object[]{"MOD", "Technician", "001336BC", "-15% damage from Robot enemies."});
        //2
        2 - tableModel.addRow(new Object[]{"MOD", "Acrobat", "000710FD", "Reduces fall dmg by 50%."});
        2 - tableModel.addRow(new Object[]{"MOD", "Antiseptic", "000710FA", "+25 airborne resistance."});
        2 - tableModel.addRow(new Object[]{"MOD", "Auto Medic", "000C9A43", "Automatically use a med pack when hit and health is below 25%, 60s cd."});
        2 -tableModel.addRow(new Object[]{"MOD", "Fastened", "002EDE4E", "+20 carry capacity."});
        2 - tableModel.addRow(new Object[]{"MOD", "Galvanized", "000710F7", "+25 corrosive resistance."});
        2 - tableModel.addRow(new Object[]{"MOD", "Leadlined", "000710F5", "+25 radiation resistance."});
        2 - tableModel.addRow(new Object[]{"MOD", "Liquid Cooled", "000710F6", "+25 thermal resistance."});
        2 - tableModel.addRow(new Object[]{"MOD", "Resource Hauler", "00060293", "Resources weigh 25% less."});
        2 - tableModel.addRow(new Object[]{"MOD", "Weapon Holsters", "00060295", "Weapons weigh 50% less."});
        //3
        tableModel.addRow(new Object[]{"MOD", "Incendiary", "00002983", "10% chance to ignite nearby attackers."});
        3 - tableModel.addRow(new Object[]{"MOD", "Mechanized", "000BE542", "+40 carry capacity."});
        3 - tableModel.addRow(new Object[]{"MOD", "Mirrored", "00059AE8", "4% chance to reflect attacks."});
        3 - tableModel.addRow(new Object[]{"MOD", "Peacemaker", "002D01A2", "Rifles do 10% more damage."});
        3 - tableModel.addRow(new Object[]{"MOD", "Reactive", "0006029F", "10% chance to stagger nearby attackers."});
        3 - tableModel.addRow(new Object[]{"MOD", "Repulsing", "0006029D", "5% chance to disarm nearby attackers."});
        3 - tableModel.addRow(new Object[]{"MOD", "Sentinel", "000BE540", "75% chance to take 50% less dmg while not moving."});
        */
        /*
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"","HELMETS", "HELMETS", "HELMETS"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        */
        //endregion
        return tableModel;
    }//ARM. MODS

    private static DefaultTableModel createTableModelForMats() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 3 && column != 4;// Make the specified columns read-only
                return column == 3;
            }
        };
        //region <MATS>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addColumn("Amount");// Column 3
        tableModel.addColumn("Description");// Column 4
        tableModel.addRow(new Object[]{"ITEM", "Adaptive Frame", "246B6A", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Adhesive", "55B1", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Aldumite", "00005DEC", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Aldumite Drilling Rig", "202F5A", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Alien Genetic Material", "000C1F57", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Alkanes", "5570", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Aluminum", "557D", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Amino Acids", "55CD", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Analgesic", "55A9", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Antibiotics", "002F4436", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Antimicrobial", "55AB", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Antimony", "557B", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Argon", "5588", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Aromatic", "55B8", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Aurora", "002C5884", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Austenitic Manifold", "246B7C", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Battlestim", "002A5024", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Benzene", "5585", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Beryllium", "57D9", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Biosuppressant", "55B2", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Black Hole Heart", "00122EA8", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Caelumite", "788D6", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Caesium", "57DF", 999, ""});
        //tableModel.addRow(new Object[]{"ITEM", "Carboxylate", "???", 999, ""});// ???
        tableModel.addRow(new Object[]{"ITEM", "Carboxylic Acids", "5586", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Chlorine", "557C", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Chlorosilanes", "557E", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Cobalt", "5575", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Comm Relay", "00246B64", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Control Rod", "00246B7B", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Copper", "5576", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Cosmetic", "55A8", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "CQB-X", "0029A85E", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Drilling Rig", "20A02F", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Dwarf Star Heart", "122EB6", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Dysprosium", "5569", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Emergency Kit", "002A9DE8", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Europium", "57E1", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Fiber", "55AF", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Fiber (Root)", "00260DF0", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Fiber (Tissue)", "0024F5C3", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Fluorine", "5577", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Gold", "5579", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Hallucinogen", "0029F405", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Heart+", "0029CAD9", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Helium-3", "558E", 999, ""});
        //tableModel.addRow(new Object[]{"ITEM", "Hexatrinylaminylene", "TBD", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "High-Tensile Spidroin", "55AA", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Hippolyta", "002C5883", 999, ""});
        //tableModel.addRow(new Object[]{"ITEM", "Hydrogen Cyanide", "TBD", 999, ""});
        //tableModel.addRow(new Object[]{"ITEM", "Hydromelonic Acid", "TBD", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Hypercatalyst", "29F40D", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        //tableModel.addRow(new Object[]{"ITEM", "Illinium", "TBD", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Immunostimulant", "55B3", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Indicite", "0004BA37", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Indicite Wafer", "203EB4", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Infantry Alpha", "0029A85C", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Ionic Liquids", "557A", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Iridium", "558A", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Iron", "556E", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Isocentered Magnet", "246B77", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Isotopic Coolant", "246B76", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Lead", "5568", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Lithium", "557F", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Lubricant", "55BA", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Luxury Textile", "559E", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Mag Pressure Tank", "246B70", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Med Pack", "0000ABF9", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Membrane", "55B0", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Memory Substrate", "0000559B", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Mercury", "0027C4A1", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Metabolic Agent", "29F3FC", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Microsecond Regulator", "246B5F", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Molecular Sieve", "246B75", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Monopropellant", "246B74", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Neodymium", "5580", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Neon", "5587", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Neurologic", "0029F409", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Nickel", "5572", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Nuclear Fuel Rod", "00246B79", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Nutrient", "777FD", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Ornamental Material", "55A7", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Palladium", "5574", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Paramagnon Conductor", "00246B73", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Pigment", "29F400", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Platinum", "5573", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Plutonium", "558C", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Polymer", "55A6", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Polytextile", "246B72", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Positron Battery", "246B71", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Power Circuit", "246B5C", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Reactive Gauge", "246B6F", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Rothicite Magnet", "00203EB2", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Sealant", "55CC", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Sedative", "55AD", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Semimetal Wafer", "246B6D", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Silver", "556A", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Solvent", "55CE", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Spice", "55AC", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Squall", "002A9DE7", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Sterile Nanotubes", "246B6C", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Stimulant", "55AE", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Structural (Hide)", "00261275", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Structural Material", "55B9", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Subgiant Heart", "00122EB1", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Substrate Molecule Sieve", "202782", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Supercooled Magnet", "246B69", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Synapse Alpha", "002C5880", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Tantalum", "556F", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Tasine", "00005DED", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Tasine Superconductor", "00203EAF", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Tau Grade Rheostat", "246B68", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Tetrafluorides", "5578", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Titanium", "556D", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Toxin", "55CB", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Trauma Pack", "0029A847", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Tungsten", "556B", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Uranium", "5589", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Vanadium", "558B", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Veryl", "5DEE", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Veryl-Treated Manifold", "00203EB0", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Vytinium", "00005DEF", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Vytinium Fuel Rod", "00203EB3", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Water", "5591", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Whiteout", "00143CB2", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Xenon", "57DD", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Ytterbium", "5571", 999, ""});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Zero Wire", "246B65", 999, ""});
        tableModel.addRow(new Object[]{"ITEM", "Zero-G Gimbal", "246B66", 999, ""});
        //endregion
        return tableModel;
    }// MATS

    private static DefaultTableModel createTableModelForMags() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 3 && column != 4;// Make the specified columns read-only
                return column == -1;
            }
        };
        //region <MAGS>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addColumn("Amount");// Column 3
        tableModel.addColumn("Location");// Column 4
        tableModel.addColumn("Image");// Column 5
        tableModel.addRow(new Object[]{"ITEM", "Colonization Guide 01", "2009E3", "1", "", "resources/pics/mags/2009E3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Colonization Guide 02", "2009E2", "1", "", "resources/pics/mags/2009E3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Colonization Guide 03", "2009E1", "1", "", "resources/pics/mags/2009E3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Colonization Guide 04", "2009E0", "1", "", "resources/pics/mags/2009E3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Colonization Guide 05", "2009DF", "1", "", "resources/pics/mags/2009E3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Colonization Guide 06", "2009DE", "1", "", "resources/pics/mags/2009E3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Colonization Guide 07", "2009DD", "1", "", "resources/pics/mags/2009E3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Colonization Guide 08", "2009DC", "1", "", "resources/pics/mags/2009E3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Colonization Guide 09", "20090B", "1", "", "resources/pics/mags/2009E3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Colonization Guide 10", "2009DA", "1", "", "resources/pics/mags/2009E3.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "CombaTech Catalog 01", "212940", "1", "Syndicate Hideout, Ebbside, Neon City, Volii Alpha (Volii System) - [Further increases the range and accuracy of CombaTech weapons]", "resources/pics/mags/212940.png"});
        tableModel.addRow(new Object[]{"ITEM", "CombaTech Catalog 02", "21293E", "1", "UC Vigilance, Phobos - [Further increases the range and accuracy of CombaTech weapons]", "resources/pics/mags/212940.png"});
        tableModel.addRow(new Object[]{"ITEM", "CombaTech Catalog 03", "21293D", "1", "Hopetown, Polvo, Valo System - [Slightly increases the range and accuracy of CombaTech weapons]", "resources/pics/mags/212940.png"});
        tableModel.addRow(new Object[]{"ITEM", "CombaTech Catalog 04", "21293C", "1", "Gagarin, Abandoned Hephaestus Mine, Alpha Centauri System - [Slightly increases the range and accuracy of CombaTech weapons]", "resources/pics/mags/212940.png"});
        tableModel.addRow(new Object[]{"ITEM", "CombaTech Catalog 05", "21293B", "1", "Forgotten Mech Graveyard, Procyon III - Further increases the range and accuracy of CombaTech weapons]", "resources/pics/mags/212940.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Constellation Guide 01", "208E91", "1", "The Lodge, New Atlantis, Jemison, Alpha Centauri System - [Permanently reduces fall damage by 5%]", "resources/pics/mags/208E91.png"});
        tableModel.addRow(new Object[]{"ITEM", "Constellation Guide 02", "208E90", "1", "Abandoned Research Tower, Any Planet - [Permanently use 15% less O2 while moving over-encumbered]", "resources/pics/mags/208E91.png"});
        tableModel.addRow(new Object[]{"ITEM", "Constellation Guide 03", "208E8F", "1", "Sona's Hideout, Dauntless Crew's Crash Site, Cassiopeia I - [Permanently use 15% less 02 while moving over-encumbered]", "resources/pics/mags/208E91.png"});
        tableModel.addRow(new Object[]{"ITEM", "Constellation Guide 04", "208E8E", "1", "The Clinic, Narion System - [Healing items are permanently 5% more effective]", "resources/pics/mags/208E91.png"});
        tableModel.addRow(new Object[]{"ITEM", "Constellation Guide 05", "208E8D", "1", "Deserted Trade Authority, Prax, Valo System", "resources/pics/mags/208E91.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Cyber Runner's Cipher 01", "208D8E", "1", "Neon City Business Tower: Slayton Aerospace, Neon City, Volii Alpha, Volii System - [Laser weapons permanently do 5% more critical damage]", "resources/pics/mags/208D8E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Cyber Runner's Cipher 02", "208D8D", "1", "The Well, New Atlantis, Jemison, Alpha Centauri System - [Laser weapons permanently do 5% more critical damage]", "resources/pics/mags/208D8E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Cyber Runner's Cipher 03", "208D8C", "1", "Neuradyne Botany Laboratory, Beta Marae I, Beta Marae System - [Stealth permanently increased by 1%./(or)Laser weapons permanently do 5% more critical damage]", "resources/pics/mags/208D8E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Cyber Runner's Cipher 04", "208D8B", "1", "Hotel Volii, Neon City, Volii Alpha, Volii System - [Laser weapons permanently do 5% more critical damage]", "resources/pics/mags/208D8E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Cyber Runner's Cipher 05", "208D8A", "1", "Research and Development Lab, Infinity LTD, Jemison, New Atlantis, Alpha Centauri System - [Stealth permanently increased by 1%./(or)Laser weapons permanently do 5% more critical damage]", "resources/pics/mags/208D8E.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 1", "23E972", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 2", "23E931", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 3", "23E930", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 4", "23E92F", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 5", "23E92E", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 6", "23E92D", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 7", "23E92C", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 8", "23E92B", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 9", "23E92A", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 10", "23E929", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 11", "23E928", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 12", "23E927", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 13", "23E926", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 14", "23E925", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 15", "23E924", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 16", "23E923", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 17", "23E922", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 18", "23E921", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 19", "23E920", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 20", "23E91F", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 21", "23E91E", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 22", "23E91D", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 23", "23E91C", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 24", "23E91B", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 25", "23E91A", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 26", "23E919", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 27", "23E918", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 28", "23E917", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 29", "23E916", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dragonstar Force Vol 30", "23E915", "1", "", "resources/pics/mags/23E972.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Freestar Captain's Log 01", "201BD7", "1", "TBD", "resources/pics/mags/201BD7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Freestar Captain's Log 02", "201BD6", "1", "Deserted Mineral Plant, Any Planet - [Permanently increase carry capacity by 5kg]", "resources/pics/mags/201BD7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Freestar Captain's Log 03", "201BD5", "1", "Freestar Collective Consulate, Akila City, Akila, Cheyenne System - [Permanently increase carry capacity by 5kg]", "resources/pics/mags/201BD7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Freestar Captain's Log 04", "201BD4", "1", "Deserted Research Post, Any Planet - [Permanently do 5% more damage to robots]", "resources/pics/mags/201BD7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Freestar Captain's Log 05", "201BD3", "1", "Freestar Mech Factory, Command Center - [Permanently adds a 1% chance to disarm enemies with unarmed or melee strikes]", "resources/pics/mags/201BD7.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "GRUNT Issue 01", "21D05A", "1", "End of Red Mile, Red Mile, Porrima, Porrima System - [Ballistic weapons permanently do an additional 5% more critical damage]", "resources/pics/mags/21D05A.png"});
        tableModel.addRow(new Object[]{"ITEM", "GRUNT Issue 02", "21B050", "1", "Seokguh Syndicate Hideout, Neon City, Volii Alpha, Volii System - [Ballistic weapons permanently do an additional 5% more critical damage]", "resources/pics/mags/21D05A.png"});
        tableModel.addRow(new Object[]{"ITEM", "GRUNT Issue 03", "21B04F", "1", "Montara Luna, Waggoner Farm - [Ballistic weapons permanently do an additional 5% more critical damage]", "resources/pics/mags/21D05A.png"});
        tableModel.addRow(new Object[]{"ITEM", "GRUNT Issue 04", "21B04E", "1", "The Key Spacestation, Kryx System - [Ballistic weapons permanently do an additional 5% more critical damage]", "resources/pics/mags/21D05A.png"});
        tableModel.addRow(new Object[]{"ITEM", "GRUNT Issue 05", "21B04D", "1", "Deserted UC Garrison, Any Planet - [Ballistic weapons permanently do an additional 5% more critical damage]", "resources/pics/mags/21D05A.png"});
        tableModel.addRow(new Object[]{"ITEM", "GRUNT Issue 06", "21B04C", "1", "Deserted Ecliptic Garrison, Any Planet - [Ballistic weapons permanently do an additional 5% more critical damage]", "resources/pics/mags/21D05A.png"});
        tableModel.addRow(new Object[]{"ITEM", "GRUNT Issue 07", "21B04B", "1", "Abandoned Cryo Lab, Any Planet - [Ballistic weapons permanently do an additional 5% more critical damage]", "resources/pics/mags/21D05A.png"});
        tableModel.addRow(new Object[]{"ITEM", "GRUNT Issue 08", "21B04A", "1", "Piazzi VI-A, Retrofitted Starstation, Piazzi System - [Ballistic weapons permanently do an additional 5% more critical damage]", "resources/pics/mags/21D05A.png"});
        tableModel.addRow(new Object[]{"ITEM", "GRUNT Issue 09", "21B049", "1", "Deserted Colony War Barracks, Any Planet - [Ballistic weapons permanently do an additional 5% more critical damage]", "resources/pics/mags/21D05A.png"});
        tableModel.addRow(new Object[]{"ITEM", "GRUNT Issue 10", "21B048", "1", "Forgotten Military Base, Any Planet - [Ballistic weapons permanently do an additional 5% more critical damage]", "resources/pics/mags/21D05A.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Gunslinger's Guide 01", "20BB30", "1", "The Rock, Akila City, Akila, Cheyenne System - [Permanently reload and draw Laredo weapons 5% faster]", "resources/pics/mags/20BB30.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gunslinger's Guide 02", "20BB2F", "1", "Sinclair's Books, Akila City, Akila, Cheyenne System - [Permanently reload and draw Laredo weapons 5% faster]", "resources/pics/mags/20BB30.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gunslinger's Guide 03", "20BB2E", "1", "Ranger's Building, Hopetown, Polvo, Valo System - [Permanently reload and draw Laredo weapons 5% faster]", "resources/pics/mags/20BB30.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gunslinger's Guide 04", "20BB2D", "1", "Freestar Rangers Outpost, Neon City, Volii Alpha, Volii System - [Permanently reload and draw Laredo weapons 5% faster]", "resources/pics/mags/20BB30.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gunslinger's Guide 05", "20BB2C", "1", "Freestar Collective Embassy, New Atlantis, Jemison, Alpha Centauri System - [Permanently reload and draw Laredo weapons 5% faster]", "resources/pics/mags/20BB30.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Kryx's Journal Entry 01", "209093", "1", "Crimson Fleet Outpost, Bessel III - [Store prices are permanently reduced by 2% and you permanently sell items 2% more]", "resources/pics/mags/209093.png"});
        tableModel.addRow(new Object[]{"ITEM", "Kryx's Journal Entry 02", "209092", "1", "Vulture's Roost, Jaffa IV, Jaffa System, *Alternative: Abandoned UC Complex - [Store prices are permanently reduced by 2% and you permanently sell items 2% more]", "resources/pics/mags/209093.png"});
        tableModel.addRow(new Object[]{"ITEM", "Kryx's Journal Entry 03", "209091", "1", "Vladimir's House, Syrma VII-A, Syrma System - [Store prices are permanently reduced by 2%, and you permanently sell items for 2% more (on top of any other bonuses)]", "resources/pics/mags/209093.png"});
        tableModel.addRow(new Object[]{"ITEM", "Kryx's Journal Entry 04", "209090", "1", "The Lock, Suvorov, Kryx System", "resources/pics/mags/209093.png"});
        tableModel.addRow(new Object[]{"ITEM", "Kryx's Journal Entry 05", "20908F", "1", "Gagarin Landing - Gagarin, Alpha Centauri System - [Crew and companions permanently do 5% more weapon damage (on top of any other bonuses)]", "resources/pics/mags/209093.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Mining Monthly Issue 01", "20B4B5", "1", "Abandoned Mine (*Into The Unknown) - [Ballistic weapons permanently do 5% more critical damage]", "resources/pics/mags/20B4B5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mining Monthly Issue 02", "20B4B4", "1", "Seokguh Syndicate Hideout, Neon City, Volii Alpha, Volii System - [Ballistic weapons permanently do 5% more critical damage]", "resources/pics/mags/20B4B5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mining Monthly Issue 03", "20B4B3", "1", "Gagarin Landing Bar, Gagarin, Alpha Centauri System - [Ballistic weapons permanently do 5% more critical damage]", "resources/pics/mags/20B4B5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mining Monthly Issue 04", "20B4B2", "1", "Autonomous Dogstar Factory - [Permanently increases tool-grip weapon damage by 2%]", "resources/pics/mags/20B4B5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mining Monthly Issue 05", "20B4B1", "1", "Mining Complex, Any Planet - [Permanently increases tool-grip weapon damage by 2%]", "resources/pics/mags/20B4B5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mining Monthly Issue 06", "20B4B0", "1", "Bindi Mining Outpost, Cheyenne System - [Permanently increases tool-grip weapon damage by 2%]", "resources/pics/mags/20B4B5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mining Monthly Issue 07", "20B4AF", "1", "Residential Room, Cydonia, Mars, Sol System - [Ballistic weapons permanently do 5% more critical damage]", "resources/pics/mags/20B4B5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mining Monthly Issue 08", "20B4AE", "1", "Abandoned Mine, Any Planet - [Permanently increases tool-grip weapon damage by 2%]", "resources/pics/mags/20B4B5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mining Monthly Issue 09", "20B4AD", "1", "Ice Mines, New Homestead, Titan, Sol System - [Permanently increases tool-grip weapon damage by 2%]", "resources/pics/mags/20B4B5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mining Monthly Issue 10", "20B4AC", "1", "Abandoned Science Facility, Any Planet - [Ballistic weapons permanently do 5% more critical damage.]", "resources/pics/mags/20B4B5.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Neon Nights 01", "200A88", "1", "Ryujin Industries: Executive Floor, Neon City, Volii Alpha, Volii System - [Permanently grants the recipe for the chem BattleUp, which increases carry capacity, and physical and energy resistance for a limited time]", "resources/pics/mags/200A88.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neon Nights 02", "200A87", "1", "Trade Tower: Astral Lounge VIP Booth 1, Neon City, Volii Alpha, Volii System - [Permanently grants the recipe for the chem S.T.E.V.E., which slows down time and increases ranged damage for a limited time]", "resources/pics/mags/200A88.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neon Nights 03", "200A86", "1", "Trade Tower: Celtcorp, Neon City, Volii Alpha, Volii System - [Permanently grants the recipe for the chem 02 Shot, which increases oxygen regeneration for a limited time]", "resources/pics/mags/200A88.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neon Nights 04", "200A85", "1", "Generdyne Industries, Neon City, Volii Alpha, Volii System - [Permanently grants the recipe for the chem RedAMP, which increases physical and energy resistance, melee damage, and speed for a limited time]", "resources/pics/mags/200A88.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neon Nights 05", "200A84", "1", "Trade Tower: Xenofresh HQ, Neon CIty, Volii Alpha, Volii System - [Grants the recipe for AddJack, which removes addiction penalties and grants a bonus to researching]", "resources/pics/mags/200A88.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Nova Galactic Manual 01", "2009B6", "1", "Crew Shuttle, Cassiopeia I, Cassiopeia System - [Permanently reduces fuel needed for a grav jump by an additional 1%]", "resources/pics/mags/2009B6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Nova Galactic Manual 02", "2009B5", "1", "Nova Galactic Staryard, Luna, Sol System - [Permanently reduces fuel needed for a grav jump by an additional 1%]", "resources/pics/mags/2009B6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Nova Galactic Manual 03", "2009B4", "1", "Jemison, New Atlantis MAST Administrative Division, Alpha Centauri System - [Permanently reduces fuel needed for a grav jump by an additional 1%]", "resources/pics/mags/2009B6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Nova Galactic Manual 04", "2009B3", "1", "Deserted Freestar Collective Garrison, Any Planet - [Permanently reduces fuel needed for a grav jump by an additional 1%]", "resources/pics/mags/2009B6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Nova Galactic Manual 05", "2009B2", "1", "Abandoned Manufacturing Plant, Any Planet - [Permanently reduces fuel needed for a grav jump by an additional 1%]", "resources/pics/mags/2009B6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Nova Galactic Manual 06", "2009B1", "1", "Autonomous Staryard, Volii Psi, Volii System - [Permanently reduces fuel needed for a grav jump by an additional 1%]", "resources/pics/mags/2009B6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Nova Galactic Manual 07", "2009B0", "1", "Abandoned Maintenance Station, Any Planet - [Permanently reduces fuel needed for a grav jump by an additional 1%]", "resources/pics/mags/2009B6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Nova Galactic Manual 08", "2009AF", "1", "Abandoned Shipping Depot, Any Planet - [Permanently reduces fuel needed for a grav jump by an additional 1%]", "resources/pics/mags/2009B6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Nova Galactic Manual 09", "2009AE", "1", "Abandoned Deimos Scrapyard, Guniibuu, Gunibuu System - [Permanently reduces fuel needed for a grav jump by an additional 1%]", "resources/pics/mags/2009B6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Nova Galactic Manual 10", "2009AD", "1", "Almagest Casino, Nesoi Planet, Olympus System - [Permanently reduces fuel needed for a grav jump by an additional 1%]", "resources/pics/mags/2009B6.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Peak Performance 01", "202D9E", "1", "The Eye, Jemison, Alpha Centauri System - [Permanently increases carrying capacity by 5kg]", "resources/pics/mags/202D9E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Peak Performance 02", "202D9D", "1", "Deserted UC Listening Post, Any Planet - [Permanently increases carrying capacity by 5kg]", "resources/pics/mags/202D9E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Peak Performance 03", "202D9C", "1", "Reliant Medical Organics Lab, Beta Ternion I, Beta Ternion System - [Pharmaceuticals are permanently 2% less addictive]", "resources/pics/mags/202D9E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Peak Performance 04", "202D9B", "1", "Sonni Di Falcos Estate, Maheo I - [Pharmaceuticals are permanently 2% less addictive]", "resources/pics/mags/202D9E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Peak Performance 05", "202D9A", "1", "The Key, Suvorov, Kryx System - [Permanently increases carrying capacity by 5kg]", "resources/pics/mags/202D9E.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Solomon's Adventures 01", "208D89", "1", "Coe Estate, Akila City, Akila, Cheyenne System - [Permanently increases weapon damage at night by 5%]", "resources/pics/mags/208D89.png"});
        tableModel.addRow(new Object[]{"ITEM", "Solomon's Adventures 02", "208D88", "1", "Waggoner Farm, Montana Luna - [Permanently increases weapon damage at night by 5%]", "resources/pics/mags/208D89.png"});
        tableModel.addRow(new Object[]{"ITEM", "Solomon's Adventures 03", "208D87", "1", "Abandoned Mining Platform, Any Planet - [Permanently adds 5% to unarmed damage]", "resources/pics/mags/208D89.png"});
        tableModel.addRow(new Object[]{"ITEM", "Solomon's Adventures 04", "208D86", "1", "Safe House Gamma, Andromas II, Andromas System - [Permanently adds 5% to melee weapon critical damage]", "resources/pics/mags/208D89.png"});
        tableModel.addRow(new Object[]{"ITEM", "Solomon's Adventures 05", "208D85", "1", "Helium-3 Extraction Site, Abandoned Mineral Refinery, Eridani III, Eridani System - [Permanently increases weapon damage at night by 5%]", "resources/pics/mags/208D89.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "The New Atlantian 01", "1FF726", "1", "Tau Gourment Production Center, Tau Ceti II, Tau System - [Permanently grants the recipe for The Deep food item]", "resources/pics/mags/1FF726.png"});
        tableModel.addRow(new Object[]{"ITEM", "The New Atlantian 02", "1FF725", "1", "Starstation RE-939, Voss - [Permanently grants the recipe for the Panache food item]", "resources/pics/mags/1FF726.png"});
        tableModel.addRow(new Object[]{"ITEM", "The New Atlantian 03", "1FF724", "1", "Residential District, New Atlantis, Jemison, Alpha Centauri System - [Permanently grants the recipe for the Astral Sliders food item]", "resources/pics/mags/1FF726.png"});
        tableModel.addRow(new Object[]{"ITEM", "The New Atlantian 04", "1FF723", "1", "The Eleos Retreat, Ixyll II, Ixyll System - [Permanently grants the recipe for the Beer Brat Platter food item]", "resources/pics/mags/1FF726.png"});
        tableModel.addRow(new Object[]{"ITEM", "The New Atlantian 05", "1FF722", "1", "The Well, New Atlantis, Jemison, Alpha Centauri System - [Permanently grants the recipe for the Shepherd's Pie food item]", "resources/pics/mags/1FF726.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Trackers Primer 01", "20909B", "1", "McClure II, Victor Compound - [Permanently increases EM weapon damage by 5%]", "resources/pics/mags/20909B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Trackers Primer 02", "20909A", "1", "Indum II, Pilgrim's Rest", "resources/pics/mags/20909B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Trackers Primer 03", "209099", "1", "Porrima III, Red Mile - [Permanently increases ballistic weapon damage by 5%]", "resources/pics/mags/20909B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Trackers Primer 04", "209098", "1", "", "resources/pics/mags/20909B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Trackers Primer 05", "209097", "1", "Vectera, Argos Extractors Mining Outpost - [Permanently increases ballistic weapon damage by 5%]", "resources/pics/mags/20909B.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "UC Defense Manual 01", "209FBA", "1", "1-OF-A-KIND-SALVAGE, Niira, Narion System -[Further increases magazine size and weapon bash critical chance for all Allied Armaments guns]", "resources/pics/mags/209FBA.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Defense Manual 02", "209FB9", "1", "The Den, Chthonia ,Wolf System - [Slightly increases magazine size and weapon bash critical chance for all Allied Armament guns]", "resources/pics/mags/209FBA.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Defense Manual 03", "209FB8", "1", "Residential, Cydonia, Mars, Sol System - [Further increases magazine size and weapon bash critical chance for all Allied Armaments guns]", "resources/pics/mags/209FBA.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Defense Manual 04", "209FB7", "1", "Abandoned Weapon Station, Any Planet - [Further increases magazine size and weapon bash critical chance for all Allied Armaments guns]", "resources/pics/mags/209FBA.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Defense Manual 05", "209FB6", "1", "Abandoned Science Facility, Any Planet - [Further increases magazine size and weapon bash critical chance for all Allied Armaments guns]", "resources/pics/mags/209FBA.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Scripture 01", "20A924", "1", "UC Security Office, Jemison, New Atlantis, Alpha Centuari System - [Permanently increases sneak bonus by 1% and melee sneak attack damage by 5%]", "resources/pics/mags/20A924.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Scripture 02", "20A923", "1", "Starstation UCN-48, Any planet - [Permanently increases sneak bonus by 1% and melee sneak attack damage by 5%]", "resources/pics/mags/20A924.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Scripture 03", "20A922", "1", "The Stretch in Ruined Square Structure, Akila City, Akila, Cheyenne System - [Permanently increases sneak bonus by 1% and melee sneak attack damage by 5%]", "resources/pics/mags/20A924.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Scripture 04", "20A921", "1", "Eren's Camp, Hyla II - [Permanently increases sneak bonus by 1% and melee sneak attack damage by 5%]", "resources/pics/mags/20A924.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Scripture 05", "20A920", "1", "The Clinic, Narion System - [Permanently increases sneak bonus by 1% and melee sneak attack damage by 5%]", "resources/pics/mags/20A924.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Scripture 06", "20A91F", "1", "Va'ruun Embassy, UC Mast, New Atlantis, Jemison, Alpha Centauri System - [Permanently increases sneak bonus by 1% and melee sneak attack damage by 5%]", "resources/pics/mags/20A924.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Scripture 07", "20A91E", "1", "Ka'Zaal Sulfur Mine, Ka' Zaal, Nirah System - [Permanently increases sneak bonus by 1% and melee sneak attack damage by 5%]", "resources/pics/mags/20A924.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Scripture 08", "20A91D", "1", "Abandoned Hangar, Any Planet - [Permanently increases sneak bonus by 1% and melee sneak attack damage by 5%]", "resources/pics/mags/20A924.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Scripture 09", "20A91C", "1", "Abandoned Industrial Compound, Any Planet - [Permanently increases sneak bonus by 1% and melee sneak attack damage by 5%]", "resources/pics/mags/20A924.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Scripture 10", "20A91B", "1", "Deep Mines, Mars, Sol System - [Permanently increases sneak bonus by 1% and melee sneak attack damage by 5%]", "resources/pics/mags/20A924.png"});
        tableModel.addRow(new Object[]{"", "", "", ""});
        tableModel.addRow(new Object[]{"ITEM", "Vanguard Space Tactics 01", "2009D8", "1", "Flight Simulator, New Atlantis, Jemison, Alpha Centauri System - [Ship missiles permanently deal 5% more damage]", "resources/pics/mags/2009D8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Vanguard Space Tactics 02", "2009D7", "1", "Red Devils HQ, Mars, Cydonia, Alpha Centauri System - [Ship energy weapons permanently deal 5% more damage]", "resources/pics/mags/2009D8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Vanguard Space Tactics 03", "2009D6", "1", "The Colander, Schrodinger System - [Ship missiles permanently deal 5% more damage]", "resources/pics/mags/2009D8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Vanguard Space Tactics 04", "2009D5", "1", "Deimos Armored Transport - [Ship missiles permanently deal 5% more damage]", "resources/pics/mags/2009D8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Vanguard Space Tactics 05", "2009D4", "1", "The Sounder Spacestation, Bessel II - [Ship weapons permanently deal 5% more damage]", "resources/pics/mags/2009D8.png"});
        //endregion
        return tableModel;
    }// MAGS

    private static DefaultTableModel createTableModelForSkills() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 3 && column != 4;// Make the specified columns read-only
                return column == -1;
            }
        };
        //region <SKILLS>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        //tableModel.addColumn("Amount");// Column 3
        //tableModel.addColumn("Description");// Column 4
        tableModel.addRow(new Object[]{"", "PHYSICAL SKILLS",  ""});
        tableModel.addRow(new Object[]{"PERK", "Boxing", "002C59DF" });
        tableModel.addRow(new Object[]{"PERK", "Cellular Regeneration", "0028AE14"});
        tableModel.addRow(new Object[]{"PERK", "Concealment", "002C555E"});
        tableModel.addRow(new Object[]{"PERK", "Decontamination", "002CE2A0"});
        tableModel.addRow(new Object[]{"PERK", "Energy Weapon Dissipation", "002C59E2"});
        tableModel.addRow(new Object[]{"PERK", "Environmental Conditioning", "0028AE17"});
        tableModel.addRow(new Object[]{"PERK", "Fitness", "002CE2DD"});
        tableModel.addRow(new Object[]{"PERK", "Gymnastics", "0028AE29"});
        tableModel.addRow(new Object[]{"PERK", "Martial Arts", "002C5554"});
        tableModel.addRow(new Object[]{"PERK", "Neurostrikes", "002C53B4"});
        tableModel.addRow(new Object[]{"PERK", "Nutrition", "002CFCAD"});
        tableModel.addRow(new Object[]{"PERK", "Pain Tolerance", "002CFCAE"});
        tableModel.addRow(new Object[]{"PERK", "Rejuvenation", "0028AE13"});
        tableModel.addRow(new Object[]{"PERK", "Stealth", "002CFCB2"});
        tableModel.addRow(new Object[]{"PERK", "Weight Lifting", "002C59D9"});
        tableModel.addRow(new Object[]{"PERK", "Wellness", "002CE2E1"});
        tableModel.addRow(new Object[]{""});
        tableModel.addRow(new Object[]{"", "SOCIAL SKILLS",  ""});
        tableModel.addRow(new Object[]{"PERK", "Commerce", "002C5A8E"});
        tableModel.addRow(new Object[]{"PERK", "Deception", "002CFCAF"});
        tableModel.addRow(new Object[]{"PERK", "Diplomacy", "002C59E1"});
        tableModel.addRow(new Object[]{"PERK", "Gastronomy", "002C5A94"});
        tableModel.addRow(new Object[]{"PERK", "Instigation", "002C555D"});
        tableModel.addRow(new Object[]{"PERK", "Intimidation", "002C59DE"});
        tableModel.addRow(new Object[]{"PERK", "Isolation", "002C53AE"});
        tableModel.addRow(new Object[]{"PERK", "Leadership", "002C890D"});
        tableModel.addRow(new Object[]{"PERK", "Manipulation", "002C5555"});
        tableModel.addRow(new Object[]{"PERK", "Negotiation", "002C555F"});
        tableModel.addRow(new Object[]{"PERK", "Outpost Management", "0023826F"});
        tableModel.addRow(new Object[]{"PERK", "Persuasion", "0022EC82"});
        tableModel.addRow(new Object[]{"PERK", "Scavenging", "0028B853"});
        tableModel.addRow(new Object[]{"PERK", "Ship Command", "002C53B3"});
        tableModel.addRow(new Object[]{"PERK", "Theft", "002C555B"});
        tableModel.addRow(new Object[]{"PERK", "Xenosociology", "002C53B0"});
        tableModel.addRow(new Object[]{""});
        tableModel.addRow(new Object[]{"", "COMBAT SKILLS",  ""});
        tableModel.addRow(new Object[]{"PERK", "Armor Penetration", "0027DF94"});
        tableModel.addRow(new Object[]{"PERK", "Ballistics", "002CFCAB"});
        tableModel.addRow(new Object[]{"PERK", "Crippling", "0027CBBA"});
        tableModel.addRow(new Object[]{"PERK", "Demolitions", "002C5556"});
        tableModel.addRow(new Object[]{"PERK", "Dueling", "002CFCB0"});
        tableModel.addRow(new Object[]{"PERK", "Heavy Weapons Certification", "00147E38"});
        tableModel.addRow(new Object[]{"PERK", "Incapacitation", "0027DF96"});
        tableModel.addRow(new Object[]{"PERK", "Lasers", "002C59DD"});
        tableModel.addRow(new Object[]{"PERK", "Marksmanship", "002C890B"});
        tableModel.addRow(new Object[]{"PERK", "Particle Beams", "0027BAFD"});
        tableModel.addRow(new Object[]{"PERK", "Pistol Certification", "002080FF"});
        tableModel.addRow(new Object[]{"PERK", "Rapid Reloading", "002C555A"});
        tableModel.addRow(new Object[]{"PERK", "Rifle Certification", "002CE2E0"});
        tableModel.addRow(new Object[]{"PERK", "Sharpshooting", "002C53AF"});
        tableModel.addRow(new Object[]{"PERK", "Shotgun Certification", "0027DF97"});
        tableModel.addRow(new Object[]{"PERK", "Sniper Certification", "002C53B1"});
        tableModel.addRow(new Object[]{"PERK", "Targeting", "002C59DA"});
        tableModel.addRow(new Object[]{   ""});
        tableModel.addRow(new Object[]{"", "SCIENCE SKILLS",  ""});
        tableModel.addRow(new Object[]{"PERK", "Aneutronic Fusion", "002C2C5A"});
        tableModel.addRow(new Object[]{"PERK", "Astrodynamics", "002C5560"});
        tableModel.addRow(new Object[]{"PERK", "Astrophysics", "0027CBBB"});
        tableModel.addRow(new Object[]{"PERK", "Botany", "002C5557"});
        tableModel.addRow(new Object[]{"PERK", "Chemistry", "002CE2C0"});
        tableModel.addRow(new Object[]{"PERK", "Geology", "002CE29F"});
        tableModel.addRow(new Object[]{"PERK", "Medicine", "002CE2DF"});
        tableModel.addRow(new Object[]{"PERK", "Outpost Engineering", "002C59E0"});
        tableModel.addRow(new Object[]{"PERK", "Planetary Habitation", "0027CBC2"});
        tableModel.addRow(new Object[]{"PERK", "Research Methods", "002C555C"});
        tableModel.addRow(new Object[]{"PERK", "Scanning", "002CFCB1"});
        tableModel.addRow(new Object[]{"PERK", "Spacesuit Design", "0027CBC3"});
        tableModel.addRow(new Object[]{"PERK", "Special Projects", "0004CE2D"});
        tableModel.addRow(new Object[]{"PERK", "Surveying", "0027CBC1"});
        tableModel.addRow(new Object[]{"PERK", "Weapon Engineering", "002C890C"});
        tableModel.addRow(new Object[]{"PERK", "Zoology", "002C5552"});
        tableModel.addRow(new Object[]{   ""});
        tableModel.addRow(new Object[]{"", "TECH SKILLS",  ""});
        tableModel.addRow(new Object[]{"PERK", "Automated Weapon Systems", "0027B9ED"});
        tableModel.addRow(new Object[]{"PERK", "Ballistic Weapon Systems", "002CE2C2"});
        tableModel.addRow(new Object[]{"PERK", "Boost Assault Training", "0008C3EE"});
        tableModel.addRow(new Object[]{"PERK", "Boost Pack Training", "00146C2C"});
        tableModel.addRow(new Object[]{"PERK", "EM Weapon Systems", "002C53B2"});
        tableModel.addRow(new Object[]{"PERK", "Energy Weapon Systems", "002C59DB"});
        tableModel.addRow(new Object[]{"PERK", "Engine Systems", "002CE2DE"});
        tableModel.addRow(new Object[]{"PERK", "Missile Weapon Systems", "002C5558"});
        tableModel.addRow(new Object[]{"PERK", "Particle Beam Weapon Systems", "002C2C5B"});
        tableModel.addRow(new Object[]{"PERK", "Payloads", "00143B6B"});
        tableModel.addRow(new Object[]{"PERK", "Piloting", "002CFCAC"});
        tableModel.addRow(new Object[]{"PERK", "Robotics", "002C5553"});
        tableModel.addRow(new Object[]{"PERK", "Security", "002CE2E2"});
        tableModel.addRow(new Object[]{"PERK", "Shield Systems", "002C2C59"});
        tableModel.addRow(new Object[]{"PERK", "Starship Design", "002C59DC"});
        tableModel.addRow(new Object[]{"PERK", "Starship Engineering", "002AC953"});
        tableModel.addRow(new Object[]{"PERK", "Targeting Control Systems", "002C5559"});
        tableModel.addRow(new Object[]{   ""});
        //endregion
        return tableModel;
    }// SKILLS

    private static DefaultTableModel createTableModelForTraits() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 3 && column != 4;// Make the specified columns read-only
                return column == -1;
            }
        };
        //region <TRAITS>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        //tableModel.addColumn("Amount");// Column 3
        //tableModel.addColumn("Description");// Column 4
        tableModel.addRow(new Object[]{"PERK", "Alien DNA", "00227FDA"});
        tableModel.addRow(new Object[]{"PERK", "Dream Home", "00227FDF"});
        tableModel.addRow(new Object[]{"PERK", "Empath", "00227FD6"});
        tableModel.addRow(new Object[]{"PERK", "Extrovert", "00227FD7"});
        tableModel.addRow(new Object[]{"PERK", "Freestar Collective Settler", "00227FD5"});
        tableModel.addRow(new Object[]{"PERK", "Hero Worshipped", "00227FD9"});
        tableModel.addRow(new Object[]{"PERK", "Introvert", "00227FD8"});
        tableModel.addRow(new Object[]{"PERK", "Kid Stuff", "00227FDE"});
        tableModel.addRow(new Object[]{"PERK", "Neon Street Rat", "00227FD3"});
        tableModel.addRow(new Object[]{"PERK", "Raised Enlightened", "00227FD2"});
        tableModel.addRow(new Object[]{"PERK", "Raised Universal", "00227FD1"});
        tableModel.addRow(new Object[]{"PERK", "Serpent's Embrace", "00227FD0"});
        tableModel.addRow(new Object[]{"PERK", "Spaced", "00227FE2"});
        tableModel.addRow(new Object[]{"PERK", "Taskmaster", "00227FE0"});
        tableModel.addRow(new Object[]{"PERK", "Terra Firma", "00227FE1"});
        tableModel.addRow(new Object[]{"PERK", "United Colonies Native", "00227FD4"});
        tableModel.addRow(new Object[]{"PERK", "Wanted", "00227FDD"});
        //endregion
        return tableModel;
    }// TRAITS

    private static DefaultTableModel createTableModelForBackg() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                return column == -1;
            }
        };
        //region <BACKGROUND>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        //tableModel.addColumn("Amount");// Column 3
        tableModel.addColumn("Description");// Column 4
        tableModel.addRow(new Object[]{"PERK", "Beast Hunter", "0022EC76",  ""});
        tableModel.addRow(new Object[]{"PERK", "Bouncer", "0022EC81",  ""});
        tableModel.addRow(new Object[]{"PERK", "Bounty Hunter", "0022EC80",  ""});
        tableModel.addRow(new Object[]{"PERK", "Chef", "0022EC7F",  ""});
        tableModel.addRow(new Object[]{"PERK", "Combat Medic", "0022EC7E",  ""});
        tableModel.addRow(new Object[]{"PERK", "Cyber Runner", "0022EC7D",  ""});
        tableModel.addRow(new Object[]{"PERK", "Cyberneticist", "0022EC7C",  ""});
        tableModel.addRow(new Object[]{"PERK", "Diplomat", "0022EC7B",  ""});
        tableModel.addRow(new Object[]{"PERK", "Explorer", "0022EC79",  ""});
        tableModel.addRow(new Object[]{"PERK", "Gangster", "0022EC78",  ""});
        tableModel.addRow(new Object[]{"PERK", "Homesteader", "0022EC77",  ""});
        tableModel.addRow(new Object[]{"PERK", "Industrialist", "0022EC7A",  ""});
        tableModel.addRow(new Object[]{"PERK", "Long Hauler", "0022EC75",  ""});
        tableModel.addRow(new Object[]{"PERK", "Pilgrim", "0022EC73",  ""});
        tableModel.addRow(new Object[]{"PERK", "Professor", "0022EC72",  ""});
        tableModel.addRow(new Object[]{"PERK", "Ronin", "0022EC74",  ""});
        tableModel.addRow(new Object[]{"PERK", "Sculptor", "0022EC71",  ""});
        tableModel.addRow(new Object[]{"PERK", "Soldier", "0022EC70",  ""});
        tableModel.addRow(new Object[]{"PERK", "Space Scoundrel", "0022EC6F",  ""});
        tableModel.addRow(new Object[]{"PERK", "Xenobiologist", "0022EC6E",  ""});
        tableModel.addRow(new Object[]{"PERK", "File Not Found", "002DFD1A",  ""});
        //endregion
        return tableModel;
    }// BACKGROUND

    private static DefaultTableModel createTableModelForAid() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 3 && column != 4 && column != 5;// Make the specified columns read-only
                return column == 3;
            }
        };
        //region <AID>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addColumn("Amount");// Column 3
        tableModel.addColumn("Description");// Column 4
        tableModel.addRow(new Object[]{"ITEM", "Addichrone", "0029A851", 999, "Suppresses Addiction symptoms for 10m"});
        tableModel.addRow(new Object[]{"ITEM", "AddiJack", "001F3E85", 999, "A synthesis of addiction treatment Addichrone and cognitive enhancer NeuraJack"});
        //tableModel.addRow(new Object[]{"ITEM", "Alien Genetic Material", "000C1F57", 999, "+500 Damage & Energy Resistance for 30s"});//???
        //tableModel.addRow(new Object[]{"ITEM", "Alien Jerky", "???????", 999, "Restores 3 Health"});//??? PLACE IN FOOD//???
        tableModel.addRow(new Object[]{"ITEM", "Amp", "0029A856", 999, "+35% Movement Speed for 2m, 2x Jump Height for 2m"});
        tableModel.addRow(new Object[]{"ITEM", "Analgesic Poultice", "0003D8B2", 999, "Treats Burns, Contusions, Frostbite, Infections, Lacerations, and Puncture Wounds"});
        tableModel.addRow(new Object[]{"ITEM", "Anchored Immobilizers", "0003D3AD", 999, "+150 Damage Resistance for 5m, Treats Dislocated Limb, Fractured Limb, Fractured Skull, Sprain, and Torn Muscle."});
        tableModel.addRow(new Object[]{"ITEM", "Antibiotic Cocktail", "0003D3A9", 999, "+20% 02 Recovery for 5m, Treats Infections"});
        tableModel.addRow(new Object[]{"ITEM", "Antibiotic Injector", "0003D3AF", 999, "Combines the effects of antibiotics and injectors into one package. Used for treating afflictions"});
        tableModel.addRow(new Object[]{"ITEM", "Antibiotic Paste", "0003D3AE", 999, "Treats Burns, Frostbite, and Infections"});
        tableModel.addRow(new Object[]{"ITEM", "Antibiotic Paste", "00299590", 999, "Treats Burns, Frostbite, and Infections"});
        tableModel.addRow(new Object[]{"ITEM", "Antibiotics", "002F4436", 999, "Treats infections"});
        tableModel.addRow(new Object[]{"ITEM", "Aurora", "002C5884", 999, "A powerful and addictive hallucinogen derived from Volii Alpha's local Chasmbass population. Legal only in the city of Neon"});
        tableModel.addRow(new Object[]{"ITEM", "Bandages", "002F4459", 999, "Treats Contusions, Lacerations, and Puncture Wounds"});
        tableModel.addRow(new Object[]{"ITEM", "Battlestim", "002A5024", 999, "+250 Damage Resistance for 5m"});
        tableModel.addRow(new Object[]{"ITEM", "BattleUp", "001F3E87", 999, "A powerful and addictive hallucinogen derived from Volii Alpha's local Chasmbass population. Legal only in the city of Neon"});
        tableModel.addRow(new Object[]{"ITEM", "Black Hole Heart", "00122EA8", 999, "A strange, homemade chem concoction. Users may experience a \"slow motion\"-like sensation"});
        tableModel.addRow(new Object[]{"ITEM", "Boosted Injector", "0003D3AC", 999, "+20% 02 Recovery for 5m, Treats Brain Injury, Concussion, Heatstroke, Hernia, Hypothermia, Lung Damage, Poisoning, and Radiation Poisoning"});
        tableModel.addRow(new Object[]{"ITEM", "Boudicca", "0029959F", 999, "+30 02 for 3m, +300 Damage Resistance for 3m"});
        tableModel.addRow(new Object[]{"ITEM", "CQB-X", "0029A85E", 999, "A chem used to enhance close quarters combat effectiveness"});
        tableModel.addRow(new Object[]{"ITEM", "Dwarf Star Heart", "00299599", 999, "A strange, homemade chem concoction known for its energizing properties"});
        tableModel.addRow(new Object[]{"ITEM", "Emergency Kit", "002A9DE8", 999, "A high-intensity emergency medical kit"});
        tableModel.addRow(new Object[]{"ITEM", "Frostwolf", "002C5885", 999, "+40% Damage for 2m, +50% Movement Speed for 2m, -50% Movement Noise for 2m"});
        tableModel.addRow(new Object[]{"ITEM", "Giant Heart", "0029959A", 999, "A strange, homemade chem concoction that energizes and improves pain tolerance"});
        tableModel.addRow(new Object[]{"ITEM", "Heal Gel", "0003D3AB", 999, "+150 Damage Resistance for 5m, Treat Burns and Frostbite"});
        tableModel.addRow(new Object[]{"ITEM", "Heal Paste", "002F445C", 999, "Treats Burns and Frostbite"});
        tableModel.addRow(new Object[]{"ITEM", "Heart+", "0029CAD9", 999, "+20% Health for 2m, +200 Damage Resistance for 2m"});
        tableModel.addRow(new Object[]{"ITEM", "Hippolyta", "002C5883", 999, "A chem that lowers inhibitions and improves articulation, theoretically making the user more charming"});
        tableModel.addRow(new Object[]{"ITEM", "Hypergiant Heart", "00122E9C", 999, "A strange, homemade chem concoction that energizes and improves pain tolerance"});
        tableModel.addRow(new Object[]{"ITEM", "Immobilizer", "002F445B", 999, "Treats Dislocated Limb, Fractured Limb, Fractured Skill, Sprain, and Torn Muscle"});
        tableModel.addRow(new Object[]{"ITEM", "Infantry Alpha", "0029A85C", 999, "A chem that stabilizes aim and increases firearms damage"});//???
        tableModel.addRow(new Object[]{"ITEM", "Infantry Alpha", "0029A860", 999, "A chem that stabilizes aim and increases firearms damage"});//???
        tableModel.addRow(new Object[]{"ITEM", "Injector", "002F445A", 999, "Used to administer medication that treats afflictions"});//???
        tableModel.addRow(new Object[]{"ITEM", "Infused Bandages", "0003D3B0", 999, "Treats Burns, Contusions, Frostbite, Lacerations, and Puncture Wounds"});
        tableModel.addRow(new Object[]{"ITEM", "Junk Flush", "00143CB1", 999, "Cures all Addictions"});
        tableModel.addRow(new Object[]{"ITEM", "Med Pack", "0000ABF9", 999, "Restores 4% Health/s for 9s"});
        tableModel.addRow(new Object[]{"ITEM", "Neurajack", "0029958F", 999, "An enhancement to the chem Synapse Alpha, known to improve cognition"});//???
        tableModel.addRow(new Object[]{"ITEM", "O2 Shot", "001F3E84", 999, "An injection of hyperoxygenated serum that greatly improves circulatory function"});
        tableModel.addRow(new Object[]{"ITEM", "Panacea", "0029A859", 999, "A complete system purge, removing diseases and poisons and treating injuries of any sort. Very tiring to use"});//???
        tableModel.addRow(new Object[]{"ITEM", "Panopticon", "002A5025", 999, "An experimental synthesis of the chems Reconstim and Infantry Alpha"});
        tableModel.addRow(new Object[]{"ITEM", "Paramour", "002C5881", 999, "An enhancement to the chem Hippolyta, designed to push cognition beyond normal limits"});
        tableModel.addRow(new Object[]{"ITEM", "Penicillin X", "0029A84F", 999, "+20 02 for 2m, Treats Infections"});
        tableModel.addRow(new Object[]{"ITEM", "Pick-Me-Up", "00255DCB", 999, "+50 Carry Capacity for 15m"});
        tableModel.addRow(new Object[]{"ITEM", "Recon Stim", "0029A855", 999, "-30% Movement Noise for 10m"});
        tableModel.addRow(new Object[]{"ITEM", "Red Trench", "002C587F", 999, "+40% Melee Damage for 3m, +300 Damage Resistance for 3m"});
        tableModel.addRow(new Object[]{"ITEM", "RedAMP", "001F3E86", 999, "A fusion of the chems Red Trench and Amp, improving close combat and general athletics"});
        tableModel.addRow(new Object[]{"ITEM", "Repairing Immobilizer", "00224D61", 999, "Combines bandages and an immobilizer to treat afflictions"});
        //tableModel.addRow(new Object[]{"ITEM", "Ship Spare Part", "0003FB19", 999, ""});//???
        tableModel.addRow(new Object[]{"ITEM", "S.T.E.V.E.", "00139E4B", 999, "A cocktail made from Aurora and the chem Panopticon. Dilates perception of time and improves hand-eye coordination"});
        tableModel.addRow(new Object[]{"ITEM", "Snake Oil", "0029A850", 999, "+20% 02 Recovery 2m, Treats Brain injury. Concussion, Heatstroke, Hernia, Hypothermia, Lung Damage, Poisoning, and Radiation Poisoning"});
        tableModel.addRow(new Object[]{"ITEM", "Squall", "002A9DE7", 999, "+20% Damage for 2m, +20% Movement Speed for 2m"});
        tableModel.addRow(new Object[]{"ITEM", "Subgiant Heart", "00122EB1", 999, "A strange, homemade chem concoction known for its energizing properties"});
        tableModel.addRow(new Object[]{"ITEM", "Synapse Alpha", "002C5880", 999, "Research Project require fewer resources for 10m"});
        tableModel.addRow(new Object[]{"ITEM", "Trauma Pack", "0029A847", 999, "Restores 8% Health/s for 4s"});
        tableModel.addRow(new Object[]{"ITEM", "Unprocessed Aurora", "000C8721", 999, "An unprocessed version of the powerful hallucinogen derived from Neon's local chasmbass population"});
        tableModel.addRow(new Object[]{"ITEM", "Whiteout", "00143CB2", 999, "An unstable, refined form of the chem Squall. Dangerously addictive and associated with unchecked aggression"});
        tableModel.addRow(new Object[]{"ITEM", "Zipper Bandages", "0003D3AA", 999, "+150 Damage Resistance for 5m, Treats Contusions, Lacerations, and Puncture Wounds"});
        //endregion
        return tableModel;
    }// AID

    private static DefaultTableModel createTableModelForApparel() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                return column == -1;
            }
        };
        //region <APPAREL>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addColumn("Amount");// Column 3
        tableModel.addColumn("Image");
        tableModel.addRow(new Object[]{"ITEM", "Akila Security Hat", "0012B4B3", "1","resources/pics/apparel/0012B4B3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Akila Security Uniform", "00227720", "1", "resources/pics/apparel/00227720.png"});
        tableModel.addRow(new Object[]{"ITEM", "Amanirenas' Hat", "001E426E", "1", "resources/pics/apparel/001E426E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Amanirenas' Outfit", "00227669", "1", "resources/pics/apparel/00227669.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ambassador Suit", "003E3D51", "1", "resources/pics/apparel/003E3D51.png"});
        tableModel.addRow(new Object[]{"ITEM", "Amelia Earhart's Outfit", "0022766A", "1", "resources/pics/apparel/0022766A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Andreja's Outfit", "001341E4", "1", "resources/pics/apparel/001341E4.png"});
        tableModel.addRow(new Object[]{"ITEM", "Argos Extractor Jumpsuit", "0021BBEC", "1", "resources/pics/apparel/0021BBEC.png"});
        tableModel.addRow(new Object[]{"ITEM", "Argos Jacketed Jumpsuit", "001C84DB", "1", "resources/pics/apparel/001C84DB.png"});
        tableModel.addRow(new Object[]{"ITEM", "Astral Lounge Charmwear", "003D66F8", "1", "resources/pics/apparel/003D66F8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Astral Lounge Uniform", "003D66F9", "1", "resources/pics/apparel/003D66F9.png"});
        tableModel.addRow(new Object[]{"ITEM", "B.Morgan's Suit", "003FDBF7", "1", "resources/pics/apparel/003FDBF7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Barrett's Outfit", "0001D1DB", "1", "resources/pics/apparel/0001D1DB.png"});
        tableModel.addRow(new Object[]{"ITEM", "Batball Cap", "0019219C", "1", "resources/pics/apparel/0019219C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Bayu's Corpwear", "003E8E7F", "1", "resources/pics/apparel/003E8E7F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Black Elbow Grease Gear", "001466EE", "1", "resources/pics/apparel/001466EE.png"});
        tableModel.addRow(new Object[]{"ITEM", "Black Engineering Outfit", "001466F2", "1", "resources/pics/apparel/001466F2.png"});
        tableModel.addRow(new Object[]{"ITEM", "Black Leather Jumpsuit", "001466EA", "1", "resources/pics/apparel/001466EA.png"});
        tableModel.addRow(new Object[]{"ITEM", "Blue Collar Offwork Duds", "00246B32", "1", "resources/pics/apparel/00246B32.png"});
        tableModel.addRow(new Object[]{"ITEM", "Blue Collar Offwork Hat", "00246B30", "1", "resources/pics/apparel/00246B30.png"});
        tableModel.addRow(new Object[]{"ITEM", "Blue Elbow Grease Gear", "00077814", "1", "resources/pics/apparel/00077814.png"});
        tableModel.addRow(new Object[]{"ITEM", "Blue Lab Outfit", "003A264E", "1", "resources/pics/apparel/003A264E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Blue Labor Jumpsuit", "0029E174", "1", "resources/pics/apparel/0029E174.png"});
        tableModel.addRow(new Object[]{"ITEM", "Blue Neocity Formwear", "001B52FF", "1", "resources/pics/apparel/001B52FF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Blue Neocity Poncho", "002619F1", "1", "resources/pics/apparel/002619F1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Blue UC Leather Jumpsuit", "00077810", "1", "resources/pics/apparel/00077810.png"});
        tableModel.addRow(new Object[]{"ITEM", "Brown Elbow Grease Gear", "001466EF", "1", "resources/pics/apparel/001466EF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Brown Engineering Outfit", "001466F3", "1", "resources/pics/apparel/001466F3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Brown Leather Jumpsuit", "00062EA3", "1", "resources/pics/apparel/00062EA3.png"});//???
        tableModel.addRow(new Object[]{"ITEM", "Brown Leather Jumpsuit", "001466EB", "1", "resources/pics/apparel/00062EA3.png"});//???
        tableModel.addRow(new Object[]{"ITEM", "Brown Neocity Formwear", "001B5300", "1", "resources/pics/apparel/001B5300.png"});
        tableModel.addRow(new Object[]{"ITEM", "Casual Street Suit", "002619F6", "1", "resources/pics/apparel/002619F6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Chunks Cap", "00185A6F", "1", "resources/pics/apparel/00185A6F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Chunks Service Uniform", "001A8DDD", "1", "resources/pics/apparel/001A8DDD.png"});
        tableModel.addRow(new Object[]{"ITEM", "Clean Suit", "00235B7D", "1", "resources/pics/apparel/00235B7D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Corpo Boardroom Suit", "001A4253", "1", "resources/pics/apparel/001A4253.png"});
        tableModel.addRow(new Object[]{"ITEM", "Corpo Executive Suit", "00190D0B", "1", "resources/pics/apparel/00190D0B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Corpo Power Suit", "002265B0", "1", "resources/pics/apparel/002265B0.png"});
        tableModel.addRow(new Object[]{"ITEM", "Corpo Salary Suit", "0019F9C1", "1", "resources/pics/apparel/0019F9C1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Corpo Sleek Suit", "002265B1", "1", "resources/pics/apparel/002265B1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Cream and Blue Dress", "001F1DCE", "1", "resources/pics/apparel/001F1DCE.png"});
        tableModel.addRow(new Object[]{"ITEM", "Cyberware Streetwear", "000788AB", "1", "resources/pics/apparel/000788AB.png"});
        tableModel.addRow(new Object[]{"ITEM", "Dalton Fiennes' Suit", "00177494", "1", "resources/pics/apparel/00177494.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deimos Cap", "00185A6E", "1", "resources/pics/apparel/00185A6E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Delgado's Outfit", "0022771F", "1", "resources/pics/apparel/0022771F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deputy Hat", "002BA0E3", "1", "resources/pics/apparel/002BA0E3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Disciples Flamewear", "003C1311", "1", "resources/pics/apparel/003C1311.png"});
        tableModel.addRow(new Object[]{"ITEM", "Disciples Streetwear", "003C1312", "1", "resources/pics/apparel/003C1312.png"});
        tableModel.addRow(new Object[]{"ITEM", "Disciples Tagwear", "002262D3", "1", "resources/pics/apparel/002262D3.png"});
        tableModel.addRow(new Object[]{"ITEM", "DJ Headphone Cap", "001625DB", "1", "resources/pics/apparel/001625DB.png"});
        tableModel.addRow(new Object[]{"ITEM", "DJ Headphones", "001625DC", "1", "resources/pics/apparel/001625DC.png"});
        tableModel.addRow(new Object[]{"ITEM", "ECS Captain Actionwear", "0017A439", "1", "resources/pics/apparel/0017A439.png"});
        tableModel.addRow(new Object[]{"ITEM", "ECS Captain Uniform", "0017A436", "1", "resources/pics/apparel/0017A436.png"});
        tableModel.addRow(new Object[]{"ITEM", "ECS Officer Uniform", "0017A437", "1", "resources/pics/apparel/0017A437.png"});
        tableModel.addRow(new Object[]{"ITEM", "ECS Security Uniform", "000CC4F6", "1", "resources/pics/apparel/000CC4F6.png"});
        tableModel.addRow(new Object[]{"ITEM", "ECS Uniform", "0017A438", "1", "resources/pics/apparel/0017A438.png"});
        tableModel.addRow(new Object[]{"ITEM", "Engineering Outfit", "00077816", "1", "resources/pics/apparel/00077816.png"});
        tableModel.addRow(new Object[]{"ITEM", "Enhance Service Uniform", "001A8DE6", "1", "resources/pics/apparel/001A8DE6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Explorer Adventure Hat", "003FC344", "1", "resources/pics/apparel/003FC344.png"});
        tableModel.addRow(new Object[]{"ITEM", "Farming Hat", "00204002", "1", "resources/pics/apparel/00204002.png"});
        tableModel.addRow(new Object[]{"ITEM", "Farming Outfit", "002262D5", "1", "resources/pics/apparel/002262D5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Fashionable Suit", "00250C86", "1", "resources/pics/apparel/00250C86.png"});
        tableModel.addRow(new Object[]{"ITEM", "Faye Sengsavahn's Outfit", "0010799A", "1", "resources/pics/apparel/0010799A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Festive Neocity Poncho", "0009F0F6", "1", "resources/pics/apparel/0009F0F6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Filburn Worker's Jumpsuit", "00250BC5", "1", "resources/pics/apparel/00250BC5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Filthy Physician Uniform", "003EC02E", "1", "resources/pics/apparel/003EC02E.png"});
        tableModel.addRow(new Object[]{"ITEM", "First Mercenary Outfit", "003E5D26", "1", "resources/pics/apparel/003E5D26.png"});
        tableModel.addRow(new Object[]{"ITEM", "First Officer Hat", "0021113E", "1", "resources/pics/apparel/0021113E.png"});
        tableModel.addRow(new Object[]{"ITEM", "First Officer Outfit", "00228826", "1", "resources/pics/apparel/00228826.png"});
        tableModel.addRow(new Object[]{"ITEM", "First Soldier Outfit", "00228825", "1", "resources/pics/apparel/00228825.png"});
        tableModel.addRow(new Object[]{"ITEM", "Fishworker Mask", "0024EF42", "1", "resources/pics/apparel/0024EF42.png"});
        tableModel.addRow(new Object[]{"ITEM", "Fishworker Splashwear", "0024EF40", "1", "resources/pics/apparel/0024EF40.png"});
        tableModel.addRow(new Object[]{"ITEM", "Fishworker Wetwear", "0024EF41", "1", "resources/pics/apparel/0024EF41.png"});
        tableModel.addRow(new Object[]{"ITEM", "Flashy Leatherwear", "00009CC5", "1", "resources/pics/apparel/00009CC5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Formal Slack Suit", "000788A1", "1", "resources/pics/apparel/000788A1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Franklin Roosevelt's Outfit", "0021BBF1", "1", "resources/pics/apparel/0021BBF1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Freestar Collective Cap", "00185A6D", "1", "resources/pics/apparel/00185A6D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Freestar Dustwear", "00224FE9", "1", "resources/pics/apparel/00224FE9.png"});
        tableModel.addRow(new Object[]{"ITEM", "Freestar Militia Hat", "0021712A", "1", "resources/pics/apparel/0021712A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Freestar Militia Uniform", "00228827", "1", "resources/pics/apparel/00228827.png"});
        tableModel.addRow(new Object[]{"ITEM", "Frontier Attire", "0024EF64", "1", "resources/pics/apparel/0024EF64.png"});
        tableModel.addRow(new Object[]{"ITEM", "GalBank Service Uniform", "001CB843", "1", "resources/pics/apparel/001CB843.png"});
        tableModel.addRow(new Object[]{"ITEM", "Generdyne Lab Outfit", "003CD80F", "1", "resources/pics/apparel/003CD80F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Generdyne Security Uniform", "003CD810", "1", "resources/pics/apparel/003CD810.png"});
        tableModel.addRow(new Object[]{"ITEM", "Genevieve Monohan's Suit", "00177492", "1", "resources/pics/apparel/00177492.png"});
        tableModel.addRow(new Object[]{"ITEM", "Genghis Khan's Hat", "001D8426", "1", "resources/pics/apparel/001D8426.png"});
        tableModel.addRow(new Object[]{"ITEM", "Genghis Khan's Outfit", "002262D2", "1", "resources/pics/apparel/002262D2.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gray Elbow Grease Gear", "001466F0", "1", "resources/pics/apparel/001466F0.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gray Engineering Outfit", "001466F4", "1", "resources/pics/apparel/001466F4.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gray Labor Jumpsuit", "0029E175", "1", "resources/pics/apparel/0029E175.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gray Leather Jumpsuit", "001466EC", "1", "resources/pics/apparel/001466EC.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gray Neocity Formwear", "003AC52A", "1", "resources/pics/apparel/003AC52A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gray Pirate Captain Gear", "0004B978", "1", "resources/pics/apparel/0004B978.png"});
        tableModel.addRow(new Object[]{"ITEM", "Green Fashionable Suit", "002619EF", "1", "resources/pics/apparel/002619EF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Green Neocity Formwear", "001B5302", "1", "resources/pics/apparel/001B5302.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ground Crew Jacketwear", "0007781C", "1", "resources/pics/apparel/0007781C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Hadrian's Outfit", "003EDF98", "1", "resources/pics/apparel/003EDF98.png"});
        tableModel.addRow(new Object[]{"ITEM", "Hazmat Suit", "0029AEAE", "1", "resources/pics/apparel/0029AEAE.png"});
        tableModel.addRow(new Object[]{"ITEM", "Homesteader Scout Hat", "00273C05", "1", "resources/pics/apparel/00273C05.png"});
        tableModel.addRow(new Object[]{"ITEM", "HopeTech Cap", "00185A6C", "1", "resources/pics/apparel/00185A6C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ikande's SysDef Officer Uniform", "00108353", "1", "resources/pics/apparel/00108353.png"});
        tableModel.addRow(new Object[]{"ITEM", "Imogene Salzo's Suit", "00177493", "1", "resources/pics/apparel/00177493.png"});
        tableModel.addRow(new Object[]{"ITEM", "Inclement Weather Outfit", "0025DEF0", "1", "resources/pics/apparel/0025DEF0.png"});
        tableModel.addRow(new Object[]{"ITEM", "Jacketed Leatherwear", "000788AA", "1", "resources/pics/apparel/000788AA.png"});
        tableModel.addRow(new Object[]{"ITEM", "Leather Pocketwear", "002619FE", "1", "resources/pics/apparel/002619FE.png"});
        tableModel.addRow(new Object[]{"ITEM", "Leather Streetwear", "002619FD", "1", "resources/pics/apparel/002619FD.png"});
        tableModel.addRow(new Object[]{"ITEM", "Leatherwear", "00009CC7", "1", "resources/pics/apparel/00009CC7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Linden Calderi's Suit", "00177495", "1", "resources/pics/apparel/00177495.png"});
        tableModel.addRow(new Object[]{"ITEM", "Lucas Drexler Uniform", "00040091", "1", "resources/pics/apparel/00040091.png"});
        tableModel.addRow(new Object[]{"ITEM", "Masako Imada's Outfit", "00226298", "1", "resources/pics/apparel/00226298.png"});
        tableModel.addRow(new Object[]{"ITEM", "Matteo Khatri's Hat", "00030B4B", "1", "resources/pics/apparel/00030B4B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Matteo's Outfit", "00030B4D", "1", "resources/pics/apparel/00030B4D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Medic Uniform", "000028A5", "1", "resources/pics/apparel/000028A5.png"});
        tableModel.addRow(new Object[]{"ITEM", "MegaCorp Executive Suit", "001A52D1", "1", "resources/pics/apparel/001A52D1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Mei Devine's Outfit", "002262D1", "1", "resources/pics/apparel/002262D1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Miner Hard Hat Outfit", "0026D8AA", "1", "resources/pics/apparel/0026D8AA.png"});
        tableModel.addRow(new Object[]{"ITEM", "Miner Jacketed Jumpsuit", "0001D1D9", "1", "resources/pics/apparel/0001D1D9.png"});
        tableModel.addRow(new Object[]{"ITEM", "Miner Jumpsuit", "0001D1D7", "1", "resources/pics/apparel/0001D1D7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Miner Orebreaker Outfit", "0009B72F", "1", "resources/pics/apparel/0009B72F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Miner Utility Outfit", "0001D1E7", "1", "resources/pics/apparel/0001D1E7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Naeva's Outfit", "00225FCA", "1", "resources/pics/apparel/00225FCA.png"});
        tableModel.addRow(new Object[]{"ITEM", "NASA Lab Uniform", "000C47A0", "1", "resources/pics/apparel/000C47A0.png"});
        tableModel.addRow(new Object[]{"ITEM", "Navy Tan Dress", "001F1DC9", "1", "resources/pics/apparel/001F1DC9.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neocity Corpwear", "002266A2", "1", "resources/pics/apparel/002266A2.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neocity Hustler Outfit", "000788A4", "1", "resources/pics/apparel/000788A4.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neocity Urbanwear", "0009F0EF", "1", "resources/pics/apparel/0009F0EF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neon Businesswear", "002266A3", "1", "resources/pics/apparel/002266A3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neon Clublife Skirt", "001F1DD0", "1", "resources/pics/apparel/001F1DD0.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neon Dancer Headwear", "000C900A", "1", "resources/pics/apparel/000C900A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neon Dancer Outfit", "00225FCC", "1", "resources/pics/apparel/00225FCC.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neon Nightlife Jumpsuit", "003556E7", "1", "resources/pics/apparel/003556E7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neon Nightlife Skirt", "001F1DCF", "1", "resources/pics/apparel/001F1DCF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neon Security Uniform", "00225D9E", "1", "resources/pics/apparel/00225D9E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Neon Socialite Skirt", "00165720", "1", "resources/pics/apparel/00165720.png"});
        tableModel.addRow(new Object[]{"ITEM", "NeuroBoost Mark I", "001A2507", "1", "resources/pics/apparel/001A2507.png"});
        tableModel.addRow(new Object[]{"ITEM", "NeuroBoost Mark II", "00100517", "1", "resources/pics/apparel/00100517.png"});
        tableModel.addRow(new Object[]{"ITEM", "NeuroBoost Mark III", "00100518", "1", "resources/pics/apparel/00100518.png"});
        tableModel.addRow(new Object[]{"ITEM", "NeuroCom Mark I", "00100519", "1", "resources/pics/apparel/00100519.png"});
        tableModel.addRow(new Object[]{"ITEM", "NeuroCom Mark II", "0010051A", "1", "resources/pics/apparel/0010051A.png"});
        tableModel.addRow(new Object[]{"ITEM", "NeuroCom Mark III", "0010051B", "1", "resources/pics/apparel/0010051B.png"});
        tableModel.addRow(new Object[]{"ITEM", "NeuroTac Mark I", "0010051C", "1", "resources/pics/apparel/0010051C.png"});
        tableModel.addRow(new Object[]{"ITEM", "NeuroTac Mark II", "0010051D", "1", "resources/pics/apparel/0010051D.png"});
        tableModel.addRow(new Object[]{"ITEM", "NeuroTac Mark III", "0010051F", "1", "resources/pics/apparel/0010051F.png"});
        tableModel.addRow(new Object[]{"ITEM", "New Atlantis Sec Uniform", "0021BBF2", "1", "resources/pics/apparel/0021BBF2.png"});
        tableModel.addRow(new Object[]{"ITEM", "Nightwear", "00225DA0", "1", "resources/pics/apparel/00225DA0.png"});
        tableModel.addRow(new Object[]{"ITEM", "Noel's Outfit", "00036AFC", "1", "resources/pics/apparel/00036AFC.png"});
        tableModel.addRow(new Object[]{"ITEM", "Nova Galactic Cap", "00185A6B", "1", "resources/pics/apparel/00185A6B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Nyx's Outfit", "0018DE02", "1", "resources/pics/apparel/0018DE02.png"});
        tableModel.addRow(new Object[]{"ITEM", "Operator Leatherwear", "00009CC8", "1", "resources/pics/apparel/00009CC8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Padded Hat", "00261A03", "1", "resources/pics/apparel/00261A03.png"});
        tableModel.addRow(new Object[]{"ITEM", "Paradiso Staff Uniform", "0002FE70", "1", "resources/pics/apparel/0002FE70.png"});
        tableModel.addRow(new Object[]{"ITEM", "Patient's Clothes", "002BC183", "1", "resources/pics/apparel/002BC183.png"});
        tableModel.addRow(new Object[]{"ITEM", "Paxton's Officer Hat", "003E5D27", "1", "resources/pics/apparel/003E5D27.png"});
        tableModel.addRow(new Object[]{"ITEM", "Physician Uniform", "00226297", "1", "resources/pics/apparel/00226297.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Captain Outfit", "0022766B", "1", "resources/pics/apparel/0022766B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Crew Outfit", "0022766D", "1", "resources/pics/apparel/0022766D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Swashbuckler Gear", "0016D50B", "1", "resources/pics/apparel/0016D50B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Prison Scrubs", "002491EA", "1", "resources/pics/apparel/002491EA.png"});
        tableModel.addRow(new Object[]{"ITEM", "Prisoner Outfit", "00208E8B", "1", "resources/pics/apparel/00208E8B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Proctor Uniform", "00015C9D", "1", "resources/pics/apparel/00015C9D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pryce's Suit", "001BF2F8", "1", "resources/pics/apparel/001BF2F8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ranger Deputy Uniform", "0013730B", "1", "resources/pics/apparel/0013730B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ranger Duelwear", "0022856C", "1", "resources/pics/apparel/0022856C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ranger Hat", "002BA0E1", "1", "resources/pics/apparel/002BA0E1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Red Mile Service Uniform", "001CFA01", "1", "resources/pics/apparel/001CFA01.png"});
        tableModel.addRow(new Object[]{"ITEM", "Red Pirate Captain Gear", "0004B977", "1", "resources/pics/apparel/0004B977.png"});
        tableModel.addRow(new Object[]{"ITEM", "Reliant Medical Uniform", "000028A6", "1", "resources/pics/apparel/000028A6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Resort Wear", "00003BF6", "1", "resources/pics/apparel/00003BF6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Rokov's Officer Hat", "003EB4B4", "1", "resources/pics/apparel/003EB4B4.png"});
        tableModel.addRow(new Object[]{"ITEM", "Rokov's Uniform", "003EB4B3", "1", "resources/pics/apparel/003EB4B3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ryujin Guard Uniform", "0037A34E", "1", "resources/pics/apparel/0037A34E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ryujin Lab Outfit", "00034110", "1", "resources/pics/apparel/00034110.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ryujin Lab Worker Hood", "001823CB", "1", "resources/pics/apparel/001823CB.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ryujin Lab Worker Outfit", "001823CC", "1", "resources/pics/apparel/001823CC.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ryujin R&D Outfit", "00034114", "1", "resources/pics/apparel/00034114.png"});
        tableModel.addRow(new Object[]{"ITEM", "Sam Coe's Outfit", "0001D1DE", "1", "resources/pics/apparel/0001D1DE.png"});
        tableModel.addRow(new Object[]{"ITEM", "Sanctum Priest Headwear", "0016571D", "1", "resources/pics/apparel/0016571D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Sanctum Priest Vestment", "00225D9F", "1", "resources/pics/apparel/00225D9F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Sarah Morgan's Outfit", "00055905", "1", "resources/pics/apparel/00055905.png"});
        tableModel.addRow(new Object[]{"ITEM", "Sari Dress", "0021C786", "1", "resources/pics/apparel/0021C786.png"});
        tableModel.addRow(new Object[]{"ITEM", "Security Guard Uniform", "0021C784", "1", "resources/pics/apparel/0021C784.png"});
        tableModel.addRow(new Object[]{"ITEM", "Service Industry Uniform", "0021C783", "1", "resources/pics/apparel/0021C783.png"});
        tableModel.addRow(new Object[]{"ITEM", "Settler Adventure Hat", "0025DEEF", "1", "resources/pics/apparel/0025DEEF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Settler Casual Hat", "00258039", "1", "resources/pics/apparel/00258039.png"});
        tableModel.addRow(new Object[]{"ITEM", "Settler Casualwear", "00258038", "1", "resources/pics/apparel/00258038.png"});
        tableModel.addRow(new Object[]{"ITEM", "Settler Comfortwear", "0024F5BE", "1", "resources/pics/apparel/0024F5BE.png"});
        tableModel.addRow(new Object[]{"ITEM", "Settler Explorer Outfit", "0025DEEE", "1", "resources/pics/apparel/0025DEEE.png"});
        tableModel.addRow(new Object[]{"ITEM", "Settler Hat", "0024EF65", "1", "resources/pics/apparel/0024EF65.png"});
        tableModel.addRow(new Object[]{"ITEM", "Settler Poncho Outfit", "0025DEF1", "1", "resources/pics/apparel/0025DEF1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Settler Vested Outfit", "00261A02", "1", "resources/pics/apparel/00261A02.png"});
        tableModel.addRow(new Object[]{"ITEM", "Settler Workwear", "00261A00", "1", "resources/pics/apparel/00261A00.png"});
        tableModel.addRow(new Object[]{"ITEM", "Shielded Lab Outfit", "000753F4", "1", "resources/pics/apparel/000753F4.png"});
        tableModel.addRow(new Object[]{"ITEM", "Shinya Voss Outfit", "0021C782", "1", "resources/pics/apparel/0021C782.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ship Captain Hat", "0016E0B9", "1", "resources/pics/apparel/0016E0B9.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ship Captain's Uniform", "0021C781", "1", "resources/pics/apparel/0021C781.png"});
        tableModel.addRow(new Object[]{"ITEM", "Space Rogue Muscle Gear", "0029080F", "1", "resources/pics/apparel/0029080F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Space Rogue Outfit", "0029080E", "1", "resources/pics/apparel/0029080E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Space Trucker Bar Duds", "002456F2", "1", "resources/pics/apparel/002456F2.png"});
        tableModel.addRow(new Object[]{"ITEM", "Space Trucker Cap", "002456F3", "1", "resources/pics/apparel/002456F3.png"});
        tableModel.addRow(new Object[]{"ITEM", "Space Trucker Cargowear", "00246B31", "1", "resources/pics/apparel/00246B31.png"});
        tableModel.addRow(new Object[]{"ITEM", "Space Trucker Casualwear", "002456F4", "1", "resources/pics/apparel/002456F4.png"});
        tableModel.addRow(new Object[]{"ITEM", "Space Trucker Flannel", "002470D2", "1", "resources/pics/apparel/002470D2.png"});
        tableModel.addRow(new Object[]{"ITEM", "Space Trucker Hat", "0029080D", "1", "resources/pics/apparel/0029080D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Space Trucker Haul Wrap", "002470D0", "1", "resources/pics/apparel/002470D0.png"});
        tableModel.addRow(new Object[]{"ITEM", "Space Undersuit", "00165722", "1", "resources/pics/apparel/00165722.png"});
        tableModel.addRow(new Object[]{"ITEM", "Striker Maskwear", "002E18F6", "1", "resources/pics/apparel/002E18F6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Striker Streetwear", "00064A2E", "1", "resources/pics/apparel/00064A2E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Stroud-Eklund Cap", "00185A6A", "1", "resources/pics/apparel/00185A6A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Suit with Lapel Pin", "00027189", "1", "resources/pics/apparel/00027189.png"});
        tableModel.addRow(new Object[]{"ITEM", "Swimsuit", "00002FA4", "1", "resources/pics/apparel/00002FA4.png"});
        tableModel.addRow(new Object[]{"ITEM", "SY-920 Ensign Fatigues I", "00217127", "1", "resources/pics/apparel/00217127.png"});
        tableModel.addRow(new Object[]{"ITEM", "SY-920 Scientist Outfit", "000F4C0F", "1", "resources/pics/apparel/000F4C0F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Syndicate Boss Suit", "0011F3AD", "1", "resources/pics/apparel/0011F3AD.png"});
        tableModel.addRow(new Object[]{"ITEM", "Syndicate Capo Suit", "0011F3A8", "1", "resources/pics/apparel/0011F3A8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Syndicate Club Suit", "0011F3AC", "1", "resources/pics/apparel/0011F3AC.png"});
        tableModel.addRow(new Object[]{"ITEM", "Syndicate Pinstripes", "0011F3B0", "1", "resources/pics/apparel/0011F3B0.png"});
        tableModel.addRow(new Object[]{"ITEM", "Syndicate Thug Suit", "0011F3A7", "1", "resources/pics/apparel/0011F3A7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Synth Leatherwear", "00009CC6", "1", "resources/pics/apparel/00009CC6.png"});
        tableModel.addRow(new Object[]{"ITEM", "Synthleather Streetwear", "002619FC", "1", "resources/pics/apparel/002619FC.png"});
        tableModel.addRow(new Object[]{"ITEM", "SysDef Crew Uniform", "003329BB", "1", "resources/pics/apparel/003329BB.png"});
        tableModel.addRow(new Object[]{"ITEM", "SysDef Formal Uniform", "003329B4", "1", "resources/pics/apparel/003329B4.png"});
        tableModel.addRow(new Object[]{"ITEM", "SysDef Officer Uniform", "0021C1FB", "1", "resources/pics/apparel/0021C1FB.png"});
        tableModel.addRow(new Object[]{"ITEM", "SysDef Prison Scrubs", "000D981C", "1", "resources/pics/apparel/000D981C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Taiyo Astroneering Cap", "00185A69", "1", "resources/pics/apparel/00185A69.png"});
        tableModel.addRow(new Object[]{"ITEM", "Teal Elbow Grease Gear", "001466F1", "1", "resources/pics/apparel/001466F1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Teal Engineering Outfit", "001466F5", "1", "resources/pics/apparel/001466F5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Teal Leather Jumpsuit", "001466ED", "1", "resources/pics/apparel/001466ED.png"});
        tableModel.addRow(new Object[]{"ITEM", "TerraBrew Barista Outfit", "003CF431", "1", "resources/pics/apparel/003CF431.png"});
        tableModel.addRow(new Object[]{"ITEM", "TerraBrew Uniform", "0007F88D", "1", "resources/pics/apparel/0007F88D.png"});
        tableModel.addRow(new Object[]{"ITEM", "The Collector's Outfit", "00227CA1", "1", "resources/pics/apparel/00227CA1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Tomisar's Outfit", "000055EB", "1", "resources/pics/apparel/000055EB.png"});
        tableModel.addRow(new Object[]{"ITEM", "Trade Authority Uniform", "001CB7E7", "1", "resources/pics/apparel/001CB7E7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Trident Crew Uniform", "00164BDD", "1", "resources/pics/apparel/00164BDD.png"});
        tableModel.addRow(new Object[]{"ITEM", "Trident Guard Uniform", "000DBFDA", "1", "resources/pics/apparel/000DBFDA.png"});
        tableModel.addRow(new Object[]{"ITEM", "Trident Luxury Lines Cap", "00185A68", "1", "resources/pics/apparel/00185A68.png"});
        tableModel.addRow(new Object[]{"ITEM", "TRItek Lab Outfit", "00034111", "1", "resources/pics/apparel/00034111.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Gray Utility Jumpsuit", "00077818", "1", "resources/pics/apparel/00077818.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Navy Armored Fatigues", "002C6E7D", "1", "resources/pics/apparel/002C6E7D.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Navy Crew Hat", "0018E260", "1", "resources/pics/apparel/0018E260.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Navy Crew Uniform", "0021A86E", "1", "resources/pics/apparel/0021A86E.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Navy Duty Fatigues", "003E3ACF", "1", "resources/pics/apparel/003E3ACF.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Navy Fatigues", "002C6E7F", "1", "resources/pics/apparel/002C6E7F.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Navy Hat", "002C6E7E", "1", "resources/pics/apparel/002C6E7E.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Navy Officer Uniform", "0021A86D", "1", "resources/pics/apparel/0021A86D.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Navy Recon Fatigues", "003E3AD1", "1", "resources/pics/apparel/003E3AD1.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC Security Uniform", "0025E8D4", "1", "resources/pics/apparel/0025E8D4.png"});
        tableModel.addRow(new Object[]{"ITEM", "UC SysDef Crew Hat", "003329BC", "1", "resources/pics/apparel/003329BC.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ularu's Suit", "003B2A81", "1", "resources/pics/apparel/003B2A81.png"});
        tableModel.addRow(new Object[]{"ITEM", "United Colonies Cap", "00185A67", "1", "resources/pics/apparel/00185A67.png"});
        tableModel.addRow(new Object[]{"ITEM", "Upscale Uniform", "002265AC", "1", "resources/pics/apparel/002265AC.png"});
        tableModel.addRow(new Object[]{"ITEM", "Urban Efficiency Attire", "000788A7", "1", "resources/pics/apparel/000788A7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Urban Operator Outfit", "002619FB", "1", "resources/pics/apparel/002619FB.png"});
        tableModel.addRow(new Object[]{"ITEM", "Urban Slackwear", "000788A2", "1", "resources/pics/apparel/000788A2.png"});
        tableModel.addRow(new Object[]{"ITEM", "Utility Headphone Cap", "000781F9", "1", "resources/pics/apparel/000781F9.png"});
        tableModel.addRow(new Object[]{"ITEM", "Utility Headphones", "00252AE7", "1", "resources/pics/apparel/00252AE7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Utility Jacketwear", "0007781D", "1", "resources/pics/apparel/0007781D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Vanguard Officer Uniform", "003CAF7E", "1", "resources/pics/apparel/003CAF7E.png"});
        tableModel.addRow(new Object[]{"ITEM", "Va'ruun Ambassador Suit", "00227CA2", "1", "resources/pics/apparel/00227CA2.png"});
        tableModel.addRow(new Object[]{"ITEM", "Veena Kalra's Outfit", "0021C1FD", "1", "resources/pics/apparel/0021C1FD.png"});
        tableModel.addRow(new Object[]{"ITEM", "Vested Suit", "002619F7", "1", "resources/pics/apparel/002619F7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Vladimir Sall's Outfit", "0001D1DF", "1", "resources/pics/apparel/0001D1DF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Walter Stroud's Outfit", "0001D1E5", "1", "resources/pics/apparel/0001D1E5.png"});
        tableModel.addRow(new Object[]{"ITEM", "White Neocity Poncho", "0002B5EE", "1", "resources/pics/apparel/0002B5EE.png"});
        tableModel.addRow(new Object[]{"ITEM", "Worker's Jumpsuit", "0029E171", "1", "resources/pics/apparel/0029E171.png///"});
        tableModel.addRow(new Object[]{"ITEM", "Xenofresh Clean Suit", "0004008C", "1", "resources/pics/apparel/0004008C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Xenofresh Jumpsuit", "00208E8C", "1", "resources/pics/apparel/00208E8C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Xenofresh Tech Outfit", "001466FF", "1", "resources/pics/apparel/001466FF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Yellow Labor Jumpsuit", "0029E173", "1", "resources/pics/apparel/0029E173.png"});
        //endregion
        return tableModel;
    }// APPAREL

    private static DefaultTableModel createTableModelForPowers() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                return column == -1;
            }
        };
        //region <POWERS>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        //tableModel.addColumn("Amount");// Column 3
        tableModel.addColumn("Description");// Column 4
        //tableModel.addRow(new Object[]{"UNLOCK", "BEFORE", "UPGRADE",  ""});
        tableModel.addRow(new Object[]{"UNLOCK", "All Powers", "psb",  "Unlocks all powers.", "PSB"});
        tableModel.addRow(new Object[]{"UNLOCK", "Alien Reanimation", "2C538F",  "Resurrects a dead alien to fight alongside you for a duration."});
        tableModel.addRow(new Object[]{"UNLOCK", "Anti-Gravity Field", "2BACBA",  "Creates an area of anti-gravity that also paralyzes enemies caught in it."});
        tableModel.addRow(new Object[]{"UNLOCK", "Create Vacuum", "2C5390",  "Gut the O2 supply of targets in the area for a duration."});
        tableModel.addRow(new Object[]{"UNLOCK", "Creators' Peace", "2C538D",  "Pacifies all enemies in an area and disarms them for a duration."});
        tableModel.addRow(new Object[]{"UNLOCK", "Earthbound", "2BACB5",  "Change the gravity around you to Earth gravity levels for a duration."});
        tableModel.addRow(new Object[]{"UNLOCK", "Elemental Pull", "2C5391",  "Blasts inorganic resources in an area around you and pulls them towards you."});
        tableModel.addRow(new Object[]{"UNLOCK", "Eternal Harvest", "2BACB4",  "Regrows flora that has been harvested in a large area around you."});
        tableModel.addRow(new Object[]{"UNLOCK", "Grav Dash", "2C538C",  "Manipulate gravity to propel yourself forward, and briefly increase any damage you inflict."});
        tableModel.addRow(new Object[]{"UNLOCK", "Gravity Wave", "2BACB7",  "Launches a gravity wave in a cone ahead of you that staggers and knocks down enemies."});
        tableModel.addRow(new Object[]{"UNLOCK", "Gravity Well", "2C5A62",  "Create an area of dense gravity that pulls in and crushes everything and everyone in around it."});
        tableModel.addRow(new Object[]{"UNLOCK", "Inner Demon", "2C5399",  "Force an enemy to confront their inner demons, creating a mirror image of themselves that attacks them."});
        tableModel.addRow(new Object[]{"UNLOCK", "Life Forced", "2C538B",  "Drain the life force out of a living being and transfer it to yourself."});
        tableModel.addRow(new Object[]{"UNLOCK", "Moon Form", "2C5A4E",  "Become as strong as stone, rooting yourself in place and increasing your resistance to all damages greatly."});
        tableModel.addRow(new Object[]{"UNLOCK", "Parallel Self", "2C5A67",  "Summon another version of you from an alternate dimension for a duration."});
        tableModel.addRow(new Object[]{"UNLOCK", "Particle Beam", "2C5A66",  "Shoot a beam of pure particle energy that deals high amounts of damage to enemies in front of you."});
        tableModel.addRow(new Object[]{"UNLOCK", "Personal Atmosphere", "2C5389",  "Create a small area of unlimited oxygen around you for a duration."});
        tableModel.addRow(new Object[]{"UNLOCK", "Phased Time", "2C5A63",  "Phase through the normal flow of time and slow down the universe for a duration."});
        tableModel.addRow(new Object[]{"UNLOCK", "Precognition", "2C538A",  "Look into the multiverse and visualize the future actions of actors, conversational and otherwise."});
        tableModel.addRow(new Object[]{"UNLOCK", "Reactive Shield", "2BACB6",  "Envelop yourself in a metastable shell of antimatter that reflects projectiles and increases your resistance to attacks."});
        tableModel.addRow(new Object[]{"UNLOCK", "Sense Star Stuff", "2C5A54",  "Channel the power to detect all life around you for a duration."});
        tableModel.addRow(new Object[]{"UNLOCK", "Solar Flare", "2C5A59",  "Emit an intense burst of solar energy that damages enemies and can set them ablaze."});
        tableModel.addRow(new Object[]{"UNLOCK", "Sunless Space", "2C5388",  "Shoot a ball of ice as cold as space into an area, freezing any living being caught in the blast for a duration."});
        tableModel.addRow(new Object[]{"UNLOCK", "Supernova", "2C5387",  "Explode with the power of a supernova in an area around you, dealing massive damage."});
        tableModel.addRow(new Object[]{"UNLOCK", "Void Form", "2C5A53",  "n\tWarp the light around you, becoming nearly invisible for a duration.", "UNLOCK"});
        tableModel.addRow(new Object[]{""});
        tableModel.addRow(new Object[]{"PERK", "UPGRADE", "", "MUST BE UNLOCKED FIRST", "", ""});
        tableModel.addRow(new Object[]{"PERK", "Alien Reanimation", "25E19D",  "Life, gift of the cosmos, granted once more to a fallen alien beast, so that it may serve thankfully."});
        tableModel.addRow(new Object[]{"PERK", "Anti-Gravity Field", "25E19C",  "Generate a localized field of intense low gravity, and behold a planet's true nature."});
        tableModel.addRow(new Object[]{"PERK", "Create Vacuum", "25E19B",  "Oxygen, fuel of humanity, is withdrawn from a localized area at your command."});
        tableModel.addRow(new Object[]{"PERK", "Creators' Peace", "25E19A",  "Fill your foes with the silent calm of the universe, compelling them to temporarily abandon their weapons."});
        tableModel.addRow(new Object[]{"PERK", "Earthbound", "25E199",  "Earth, the cradle of humanity, shares its gravity in an area of your choosing."});
        tableModel.addRow(new Object[]{"PERK", "Elemental Pull", "25E198",  "Elements, the true treasure of planet and moon, are drawn to your being."});
        tableModel.addRow(new Object[]{"PERK", "Eternal Harvest", "25E197",  "What has blossomed will bloom once more, ripe for the picking."});
        tableModel.addRow(new Object[]{"PERK", "Grav Dash", "25E196",  "Manipulate gravity to propel yourself forward, and briefly increase any damage you inflict."});
        tableModel.addRow(new Object[]{"PERK", "Gravity Wave", "25E195",  "Emit a gravitational force strong enough to propel almost anything... or anyone."});
        tableModel.addRow(new Object[]{"PERK", "Gravity Well", "25E193",  "Generate a focal point of crushing gravity that draws in anyone caught in its field."});
        tableModel.addRow(new Object[]{"PERK", "Inner Demon", "25E192",  "Tap into the endless multiverse, and summon forth a friendly duplicate of an unfriendly foe."});
        tableModel.addRow(new Object[]{"PERK", "Life Forced", "25E191",  "Transfer the very lifeforce of an enemy, harming them while healing yourself."});
        tableModel.addRow(new Object[]{"PERK", "Moon Form", "25E190",  "Channel the energy of Luna, Earth's beautiful moon, to be as unmoving and resilient as stone."});
        tableModel.addRow(new Object[]{"PERK", "Parallel Self", "25E18E",  "From across the vast multiverse, a friendly version of yourself arrives, armed and ready to lend aid."});
        tableModel.addRow(new Object[]{"PERK", "Particle Beam", "25E18D",  "Emit a powerful ray of cosmic energy, dealing terrifying damage to a single target."});
        tableModel.addRow(new Object[]{"PERK", "Personal Atmosphere", "25E18C",  "Oxygen, pure and clean, brought forth to breathe deep and counteract harmful carbon dioxide."});
        tableModel.addRow(new Object[]{"PERK", "Phased Time", "25E18B",  "Time itself is yours to manipulate, as you slow the world... and all those in it."});
        tableModel.addRow(new Object[]{"PERK", "Precognition", "25E18F",  "Bend time and glimpse the future, seeing the path someone will walk and the words they may say."});
        tableModel.addRow(new Object[]{"PERK", "Reactive Shield", "25E19E",  "Form a shield of pure cosmic light that can weaken and even reflect enemy projectiles."});
        tableModel.addRow(new Object[]{"PERK", "Sense Star Stuff", "25E18A",  "Bind yourself to the particles of creation, sensing the life force of any human, alien, or Starborn."});
        tableModel.addRow(new Object[]{"PERK", "Solar Flare", "25E189",  "Tap into the power of a sun and release a directed orb of searing hot plasma."});
        tableModel.addRow(new Object[]{"PERK", "Sunless Space", "25E188",  "Introduce your enemies to the cold of space, freezing them in their tracks."});
        tableModel.addRow(new Object[]{"PERK", "Supernova", "25E187",  "Cosmic energy explodes around you, with the terrible force of a dying star."});
        tableModel.addRow(new Object[]{"PERK", "Void Form", "25E186",  "Channel the very darkness of space, rendering yourself nearly invisible to those around you."});
        //endregion
        return tableModel;
    }// POWERS

    private static DefaultTableModel createTableModelForResearch() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                return column == -1;
            }
        };
        //region <RESEARCH>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addRow(new Object[]{"","RESEARCH"});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"","PHARMACOLOGY"});
        tableModel.addRow(new Object[]{"RESEARCH", "Medical Treatment 1", "00389EEA"});
        tableModel.addRow(new Object[]{"RESEARCH", "Medical Treatment 2", "00389EEB"});
        tableModel.addRow(new Object[]{"RESEARCH", "Medical Treatment 3", "00389EEC"});
        tableModel.addRow(new Object[]{"RESEARCH", "Performance Enhancement 1", "00389EEF"});
        tableModel.addRow(new Object[]{"RESEARCH", "Performance Enhancement 2", "00389EF0"});
        tableModel.addRow(new Object[]{"RESEARCH", "Performance Enhancement 3", "00389EF1"});
        tableModel.addRow(new Object[]{"RESEARCH", "Innovative Synthesis 1", "00389EF4"});
        tableModel.addRow(new Object[]{"RESEARCH", "Innovative Synthesis 2", "00389EF5"});
        tableModel.addRow(new Object[]{"RESEARCH", "Innovative Synthesis 3", "00389EF6"});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"","FOOD AND DRINK"});
        tableModel.addRow(new Object[]{"RESEARCH", "Mixology 1", "00389EF9"});
        tableModel.addRow(new Object[]{"RESEARCH", "Mixology 2", "00389EFA"});
        tableModel.addRow(new Object[]{"RESEARCH", "Mixology 3", "00389EFB"});
        tableModel.addRow(new Object[]{"RESEARCH", "Beverage Development 1", "00389EFD"});
        tableModel.addRow(new Object[]{"RESEARCH", "Beverage Development 2", "00389EFE"});
        tableModel.addRow(new Object[]{"RESEARCH", "Beverage Development 3", "00389EFF"});
        tableModel.addRow(new Object[]{"RESEARCH", "Old Earth Cuisine 1", "00389F01"});
        tableModel.addRow(new Object[]{"RESEARCH", "Old Earth Cuisine 2", "00389F02"});
        tableModel.addRow(new Object[]{"RESEARCH", "Old Earth Cuisine 3", "00389F03"});
        tableModel.addRow(new Object[]{"RESEARCH", "Exotic Recipes 1", "00389F05"});
        tableModel.addRow(new Object[]{"RESEARCH", "Exotic Recipes 2", "00389F06"});
        tableModel.addRow(new Object[]{"RESEARCH", "Exotic Recipes 3", "00389F07"});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"","OUTPOST DEVELOPMENT"});
        tableModel.addRow(new Object[]{"RESEARCH", "Manufacturing 1", "00389F11"});
        tableModel.addRow(new Object[]{"RESEARCH", "Manufacturing 2", "00389F12"});
        tableModel.addRow(new Object[]{"RESEARCH", "Manufacturing 3", "00389F13"});
        tableModel.addRow(new Object[]{"RESEARCH", "Resource Extraction 1", "00389F0B"});
        tableModel.addRow(new Object[]{"RESEARCH", "Resource Extraction 2", "00389F0C"});
        tableModel.addRow(new Object[]{"RESEARCH", "Decoration 1", "00100B8B"});
        tableModel.addRow(new Object[]{"RESEARCH", "Decoration 2", "00100B8A"});
        tableModel.addRow(new Object[]{"RESEARCH", "Decoration 3", "00100B8C"});
        tableModel.addRow(new Object[]{"RESEARCH", "Horticulture 1", "00389F09"});
        tableModel.addRow(new Object[]{"RESEARCH", "Horticulture 2", "00389F0A"});
        tableModel.addRow(new Object[]{"RESEARCH", "Domestication 1", "003E3D4D"});
        tableModel.addRow(new Object[]{"RESEARCH", "Domestication 2", "003E3D4E"});
        tableModel.addRow(new Object[]{"RESEARCH", "Power Generation 1", "00389F14"});
        tableModel.addRow(new Object[]{"RESEARCH", "Power Generation 2", "00389F15"});
        tableModel.addRow(new Object[]{"RESEARCH", "Power Generation 3", "00389F16"});
        tableModel.addRow(new Object[]{"RESEARCH", "Robots 1", "00389F0F"});
        tableModel.addRow(new Object[]{"RESEARCH", "Robots 2", "00389F10"});
        tableModel.addRow(new Object[]{"RESEARCH", "Outpost Defense 1", "00389F0D"});
        tableModel.addRow(new Object[]{"RESEARCH", "Outpost Defense 2", "00389F0E"});
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"","EQUIPMENT"});
        tableModel.addRow(new Object[]{"RESEARCH", "Pack Mods 1", "000C11A5"});
        tableModel.addRow(new Object[]{"RESEARCH", "Pack Mods 2", "000C11A6"});
        tableModel.addRow(new Object[]{"RESEARCH", "Pack Mods 3", "000C11A7"});
        tableModel.addRow(new Object[]{"RESEARCH", "Spacesuit Mods 1", "001520B9"});
        tableModel.addRow(new Object[]{"RESEARCH", "Spacesuit Mods 2", "00178D03"});
        tableModel.addRow(new Object[]{"RESEARCH", "Spacesuit Mods 3", "00178D1A"});
        tableModel.addRow(new Object[]{"RESEARCH", "Helmet Mods 1", "00178D1B"});
        tableModel.addRow(new Object[]{"RESEARCH", "Helmet Mods 2", "00178D1C"});
        tableModel.addRow(new Object[]{"RESEARCH", "Helmet Mods 3", "00178D1D"});
        //endregion
        return tableModel;
    }// RESEARCH

    private static DefaultTableModel createTableModelForLegWep() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override// Override the isCellEditable method to make certain columns read-only
            public boolean isCellEditable(int row, int column) {
                //return column != 0 && column != 1 && column != 2 && column != 4;// Make the specified columns read-only
                return column == -1;
            }
        };
        //region <MISCELLANEOUS>
        tableModel.addColumn("INPUT_TYPE");// Column 0
        tableModel.addColumn("Name");// Column 1
        tableModel.addColumn("Item ID");// Column 2
        tableModel.addColumn("Amount");// Column 3
        //tableModel.addColumn("Description");// Column 4
        tableModel.addColumn("Type");// Column 5
        tableModel.addColumn("Dam. Type");// Column 6
        tableModel.addColumn("Damage");// Column 7
        tableModel.addColumn("Fire Rate");// Column 8
        tableModel.addColumn("Mag. Size");// Column 9
        tableModel.addColumn("DPS");// Column 10
        tableModel.addColumn("Image");
        tableModel.addRow(new Object[]{"ITEM", "Ace Sidearm", "002E073B", "1", "Pistol", "PHYSICAL", "10", "40", "17", "10", "resources/pics/legendaryweapons/002E073B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Acid Rain", "002E3E85", "1", "Rifle", "PHYSICAL", "3", "170", "50", "8.5", "resources/pics/legendaryweapons/002E3E85.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ashta Tamer", "0031FCBF", "1", "Rifle", "PHYSICAL", "127", "5", "4", "10.58", "resources/pics/legendaryweapons/0031FCBF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Avatar", "001AC6B4", "1", "Mag Sniper", "PHYSICAL", "256", "19", "20", "81", "resources/pics/legendaryweapons/001AC6B4.png"});
        tableModel.addRow(new Object[]{"ITEM", "Boom Boom", "002E0735", "1", "Shotgun", "PHYSICAL", "50", "60", "20", "50", "resources/pics/legendaryweapons/001AC6BB.png"});
        tableModel.addRow(new Object[]{"ITEM", "Boom Boom", "001AC6BB", "1", "Shotgun", "PHYSICAL", "50", "60", "20", "50", "resources/pics/legendaryweapons/001AC6BB.png"});
        //tableModel.addRow(new Object[]{"ITEM", "Brawler's Equinox", "000EC310", "1", "Laser Rifle", "", "", "180", "20"});// ???
        tableModel.addRow(new Object[]{"ITEM", "Brute Force", "001AC6E0", "1", "Shotgun", "PHYSICAL", "204", "20", "11", "22.6", "resources/pics/legendaryweapons/001AC6E0.png"});
        tableModel.addRow(new Object[]{"ITEM", "Davis Wilson's Rifle", "000367C5", "1", "Rifle", "PHYSICAL", "19", "90", "36", "28.5", "resources/pics/legendaryweapons/000367C5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Deadeye", "001BBF2A", "1", "Pistol", "PHYSICAL", "61", "12", "6", "12.2", "resources/pics/legendaryweapons/001BBF2A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Desperation", "000EC2F1", "1", "Rifle", "PHYSICAL", "17", "134", "15", "38.1", "resources/pics/legendaryweapons/000EC2F1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Despondent Assassin", "0014920B", "1", "Rifle", "PHYSICAL", "34", "40", "12", "22.6", "resources/pics/legendaryweapons/0014920B.png"});
        tableModel.addRow(new Object[]{"ITEM", "Elegance", "002E0725", "1", "Pistol", "PHYSICAL", "48", "109", "8", "8.2", "resources/pics/legendaryweapons/002E0725.png"});
        tableModel.addRow(new Object[]{"ITEM", "Ember", "002F99FF", "1", "Laser Pistol", "ENERGY", "12", "25", "16", "5", "resources/pics/legendaryweapons/002F99FF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Eternity's Gate", "00329ABB", "1", "Particle Beam Rifle", "PHYSICAL + ENERGY", "PHYS 17 + ENERGY 50", "25", "20", "27.92", "resources/pics/legendaryweapons/00329ABB.png"});
        tableModel.addRow(new Object[]{"ITEM", "Experiment A-7", "0031F257", "1", "Shotgun", "PHYSICAL", "119", "10", "6", "19.83", "resources/pics/legendaryweapons/0031F257.png"});
        tableModel.addRow(new Object[]{"ITEM", "Feather", "002E82A0", "1", "Rifle", "PHYSICAL", "19", "117", "36", "37.1", "resources/pics/legendaryweapons/002E82A0.png"});
        tableModel.addRow(new Object[]{"ITEM", "Fiscal Quarter", "000019F5", "1", "Rifle", "PHYSICAL", "5", "180", "40", "15", "resources/pics/legendaryweapons/000019F5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Fortune's Glory", "0032046E", "1", "Dagger", "PHYSICAL", "28", "", "", "", "resources/pics/legendaryweapons/00327627.png"});
        tableModel.addRow(new Object[]{"ITEM", "Fortune's Glory", "00327627", "1", "Dagger", "PHYSICAL", "28", "", "", "", "resources/pics/legendaryweapons/00327627.png"});
        tableModel.addRow(new Object[]{"ITEM", "Fury", "002E0729", "1", "Rifle", "PHYSICAL", "20", "150", "100", "50", "resources/pics/legendaryweapons/002E0729.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gallow's Reach", "0032046C", "1", "Rifle", "PHYSICAL", "18", "150", "30", "45", "resources/pics/legendaryweapons/0032046C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Gallow's Reach", "00327615", "1", "Pistol", "PHYSICAL", "18", "150", "30", "45", "resources/pics/legendaryweapons/0032046C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Haphazard Handgun", "00259DCD", "1", "Pistol", "PHYSICAL", "40", "67", "9", "44.6", "resources/pics/legendaryweapons/00259DCD.png"});
        tableModel.addRow(new Object[]{"ITEM", "Head Ranger", "002FAD3F", "1", "Rifle", "PHYSICAL", "37", "14", "6", "8.6", "resources/pics/legendaryweapons/002FAD3F.png"});
        tableModel.addRow(new Object[]{"ITEM", "Heller's Cutter", "0032046A", "1", "Cutter", "ENERGY", "4", "76", "-", "5.07", "resources/pics/legendaryweapons/0032046A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Hunterwulf", "002E3E83", "1", "Rifle", "PHYSICAL", "14", "120", "30", "28", "resources/pics/legendaryweapons/002E3E83.png"});
        //tableModel.addRow(new Object[]{"ITEM", "Item of Interest", "001D303A", "1", "", "", "", "", "", ""});//??????????????????
        tableModel.addRow(new Object[]{"ITEM", "Jake's Hangover Cure", "002BD871", "1", "Shotgun", "PHYSICAL + ENREGY", "32P + 94E", "14", "8", "29.4", "resources/pics/legendaryweapons/002BD871.png"});
        tableModel.addRow(new Object[]{"ITEM", "Justifier", "001BAA3C", "1", "Rifle", "PHYSICAL", "31", "12", "6", "6.2", "resources/pics/legendaryweapons/001BAA3C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Keelhauler", "0008D850", "1", "Pistol", "PHYSICAL", "53", "140", "6", "123.7", "resources/pics/legendaryweapons/0008D850.png"});
        tableModel.addRow(new Object[]{"ITEM", "Marathon", "002E82A2", "1", "Pistol", "PHYSICAL", "39", "33", "7", "21.45", "resources/pics/legendaryweapons/002E82A2.png"});
        tableModel.addRow(new Object[]{"ITEM", "Marksman's AA-99", "002F99FE", "1", "Rifle", "PHYSICAL", "17", "112", "30", "31.73", "resources/pics/legendaryweapons/002F99FE.png"});
        tableModel.addRow(new Object[]{"ITEM", "Memento Mori", "00316416", "1", "Pistol", "PHYSICAL", "17", "40", "17", "11.3", "resources/pics/legendaryweapons/00316416.png"});
        //tableModel.addRow(new Object[]{"ITEM", "Mind Tear", "002AC049", "1", "Pistol", "PHYSICAL", "", "", "", "", "resources/pics/legendaryweapons/002AC049.png"});//????
        tableModel.addRow(new Object[]{"ITEM", "N67 Smartgun", "001AC6BB", "1", "Heavy", "PHYSICAL", "10", "350", "300", "58.3", "resources/pics/legendaryweapons/001AC6BB.png"});
        tableModel.addRow(new Object[]{"ITEM", "Peacekeeper", "001A52E5", "1", "Rifle", "PHYSICAL", "17", "112", "50", "31.7", "resources/pics/legendaryweapons/001A52E5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Peacemaker", "000E7ED1", "1", "Rifle", "PHYSICAL", "37", "12", "6", "7.4", "resources/pics/legendaryweapons/000E7ED1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Pirate Legend", "002FAC64", "1", "Pistol", "PHYSICAL", "4", "180", "70", "12", "resources/pics/legendaryweapons/002FAC64.png"});
        tableModel.addRow(new Object[]{"ITEM", "Poisonstorm", "001AC6B8", "1", "Heavy", "PHYSICAL", "10", "400", "160", "66.7", "resources/pics/legendaryweapons/001AC6B8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Power Beat", "0022A8E1", "1", "Rifle", "PHYSICAL", "66", "39", "60", "42.9", "resources/pics/legendaryweapons/0022A8E1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Radburn", "00207711", "1", "Pistol", "PHYSICAL + ENERGY", "7P + 1E", "114", "12", "15.2", "resources/pics/legendaryweapons/00207711.png"});
        tableModel.addRow(new Object[]{"ITEM", "Rapidshot", "00162CFA", "1", "Shotgun", "PHYSICAL", "99", "18", "6", "29.7", "resources/pics/legendaryweapons/00162CFA.png"});
        tableModel.addRow(new Object[]{"ITEM", "Reckless Bombardment", "1", "002E0727", "Heavy", "PHYSICAL", "120", "12", "8", "24", "resources/pics/legendaryweapons/002E0727.png"});
        tableModel.addRow(new Object[]{"ITEM", "Reflection", "0022A90D", "1", "Rifle", "PHYSICAL + ENERGY", "41P + 137E", "29", "20", "86.5", "resources/pics/legendaryweapons/0022A90D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Revenant", "0008AF62", "1", "Rifle", "PHYSICAL", "14", "330", "150", "77", "resources/pics/legendaryweapons/0008AF62.png"});
        tableModel.addRow(new Object[]{"ITEM", "Riot Shotgun", "00162CF8", "1", "Shotgun", "ELECTRO MAG", "25", "10", "6", "19.83", "resources/pics/legendaryweapons/00162CF8.png"});
        tableModel.addRow(new Object[]{"ITEM", "Shattered Shock", "000EC2F9", "1", "Heavy", "PHYSICAL", "127", "5", "6", "20.6", "resources/pics/legendaryweapons/000EC2F9.png"});
        tableModel.addRow(new Object[]{"ITEM", "Sir Livingstone's Pistol", "001F27F5", "1", "Pistol", "PHYSICAL", "28", "67", "15", "31.3", "resources/pics/legendaryweapons/001F27F5.png"});
        tableModel.addRow(new Object[]{"ITEM", "Short Circuit", "002E7C9C", "1", "Pistol", "ENERGY", "10", "29", "22", "4.8", "resources/pics/legendaryweapons/002E7C9C.png"});
        tableModel.addRow(new Object[]{"ITEM", "Solace", "0008AF68", "1", "Pistol", "PHYSICAL", "39", "30", "7", "19.5", "resources/pics/legendaryweapons/0008AF68.png"});
        tableModel.addRow(new Object[]{"ITEM", "Speechless Fire", "002E0731", "1", "Rifle", "PHYSICAL", "33", "48", "20", "26.4", "resources/pics/legendaryweapons/002E0731.png"});
        tableModel.addRow(new Object[]{"ITEM", "Street Sweeper", "002F3B1A", "1", "Pistol", "PHYSICAL", "3", "180", "25", "9", "resources/pics/legendaryweapons/002F3B1A.png"});
        tableModel.addRow(new Object[]{"ITEM", "Tempest", "003CE4B0", "1", "Rifle", "PHYSICAL", "19", "90", "36", "28.5", "resources/pics/legendaryweapons/003CE4B0.png"});
        tableModel.addRow(new Object[]{"ITEM", "Terror Inflictor", "0022A90C", "1", "Pistol", "PHYSICAL + ENERGY", "21P + 65E", "34", "12", "48.7", "resources/pics/legendaryweapons/0022A90C.png"});
        tableModel.addRow(new Object[]{"ITEM", "The Buzzcut", "0020772C", "1", "Rifle", "PHYSICAL", "3", "212", "50", "10.6", "resources/pics/legendaryweapons/0020772C.png"});
        tableModel.addRow(new Object[]{"ITEM", "The Cursed", "001AC6E7", "1", "Shotgun", "PHYSICAL", "73", "6", "6", "7.3", "resources/pics/legendaryweapons/001AC6E7.png"});
        tableModel.addRow(new Object[]{"ITEM", "The First Shot", "002E1929", "1", "Rifle", "PHYSICAL", "46", "50", "30", "38.3", "resources/pics/legendaryweapons/002E1929.png"});
        tableModel.addRow(new Object[]{"ITEM", "The Last Breath", "0022A8E2", "1", "Rifle", "PHYSICAL", "139", "25", "5", "57.9", "resources/pics/legendaryweapons/0022A8E2.png"});
        tableModel.addRow(new Object[]{"ITEM", "The Last Priest", "0031F25A", "1", "Dagger", "PHYSICAL", "85", "", "", "", "resources/pics/legendaryweapons/0031F25A.png"});
        tableModel.addRow(new Object[]{"ITEM", "The Mutineer", "0000CAB0", "1", "Pistol", "PHYSICAL", "181", "161", "6", "586.68", "resources/pics/legendaryweapons/0000CAB0.png"});
        tableModel.addRow(new Object[]{"ITEM", "The Prime", "002DFD41", "1", "Pistol", "PHYSICAL", "61", "12", "6", "12.2", "resources/pics/legendaryweapons/002DFD41.png"});
        tableModel.addRow(new Object[]{"ITEM", "The Spacer", "001AC6C0", "1", "Pistol", "PHYSICAL + ENERGY", "9P + 30E", "25", "12", "16.25", "resources/pics/legendaryweapons/001AC6C0.png"});
        tableModel.addRow(new Object[]{"ITEM", "The Zapper", "002D7913", "1", "Pistol", "PHYSICAL", "12", "104", "28", "20.8", "resources/pics/legendaryweapons/002D7913.png"});
        tableModel.addRow(new Object[]{"ITEM", "Trickshot", "001AC6B1", "1", "Pistol", "PHYSICAL", "67", "34", "6", "38.1", "resources/pics/legendaryweapons/001AC6B1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Unfair Advantage", "003CE4B1", "1", "Pistol", "PHYSICAL", "51", "75", "6", "63.8", "resources/pics/legendaryweapons/003CE4B1.png"});
        tableModel.addRow(new Object[]{"ITEM", "Unmitigated Violence", "0011381D", "1", "Rifle", "ENERGY", "21", "33", "30", "35.6", "resources/pics/legendaryweapons/0011381D.png"});
        tableModel.addRow(new Object[]{"ITEM", "Unmitigated Violence", "001F02EF", "1", "Rifle", "ENERGY", "21", "33", "30", "35.6", "resources/pics/legendaryweapons/001F02EF.png"});
        tableModel.addRow(new Object[]{"ITEM", "Unrestrained Vengeance", "000019F7", "1", "Rifle", "ENERGY", "18", "33", "30", "9.9", "resources/pics/legendaryweapons/000019F7.png"});
        tableModel.addRow(new Object[]{"ITEM", "Vampire's Gift", "002FAC66", "1", "Rifle", "PHYSICAL", "18", "120", "66", "36", "resources/pics/legendaryweapons/002FAC66.png"});
        tableModel.addRow(new Object[]{"ITEM", "Vampire's Sidearm", "00207739", "1", "Pistol", "PHYSICAL", "28", "80", "15", "37.3", "resources/pics/legendaryweapons/00207739.png"});
        tableModel.addRow(new Object[]{"ITEM", "X-989 Microgun", "0008AF5D", "1", "Heavy", "PHYSICAL", "9", "350", "300", "52.5", "resources/pics/legendaryweapons/0008AF5D.png"});
        //endregion
        return tableModel;
    }// MISCELLANEOUS
    // ***** TABLES END *****

    private static void switchFocusToNotepad() throws InterruptedException {
        byte VK_CONTROL = 0x11; // Ctrl key
        byte VK_V = 0x56; // V key
        String windowName = "*Untitled - Notepad"; // The title of the Notepad window
        //String windowName = "Starfield"; // The title of the Notepad window
        WinDef.HWND hWnd = WindowFocusAndKeyPress.findWindow(null, windowName);

        if (hWnd != null) {
            WindowFocusAndKeyPress.setForegroundWindow(hWnd);

            // Check if the window is active
            boolean isWindowActive = false;
            for (int i = 0; i < 1000; i++) { // Try up to 10 times
                Thread.sleep(100); // Wait for a short period
                WinDef.HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
                if (hWnd.equals(foregroundWindow)) {
                    isWindowActive = true;
                    break;
                }
            }

            int delay = 0;
            if (isWindowActive) {
                pressKeyOld((byte) 0xC0);                   // tilde - open console
                System.out.println("OPEN CONSOLE");
                Thread.sleep(delay);

                pressKey(VK_CONTROL, false);        // Press Ctrl key down
                pressKey(VK_V, false);              // Press V key down
                pressKey(VK_V, true);               // Release V key
                pressKey(VK_CONTROL, true);
                System.out.println("CTRL-V");// Release Ctrl key
                Thread.sleep(delay);

                pressKeyOld(KeyCodes.VK_ENTER);             // press enter
                System.out.println("ENTER");
                Thread.sleep(200);

                pressKeyOld((byte) 0xC0);                   // tilde - close console
                //Thread.sleep(delay);
                pressKeyOld(KeyCodes.VK_ESCAPE);             // press enter
                System.out.println("ESCAPE");
                pressKeyOld(KeyCodes.VK_TAB);             // press enter
                System.out.println("TAB");
            } else {
                System.out.println("Window did not become active");
            }
        } else {
            System.out.println("Notepad window not found");
        }
    }// switchFocusToNotepad

    /*
    private static void switchFocusToNotepad() throws InterruptedException {
        byte VK_CONTROL = 0x11; // Ctrl key
        //public static final byte VK_V = 0x56; // V key
        //String windowName = "*Untitled - Notepad"; // The title of the Notepad window
        String windowName = "Starfield"; // The title of the Notepad window
        WinDef.HWND hWnd = WindowFocusAndKeyPress.findWindow(null, windowName);
        if (hWnd != null) {
            WindowFocusAndKeyPress.setForegroundWindow(hWnd);
            Thread.sleep(500);

            pressKey((byte) 0x1B, false); // Escape key
            //pressKey((byte) 0x1B, false); // Escape key
            //pressKey((byte) 0xC0, false); // '`' key (VK_OEM_3)
            // Send Ctrl+V
            //pressKey(KeyCodes.VK_CONTROL, false); // Press Ctrl key down
            //pressKey(KeyCodes.VK_V, false);       // Press V key down
            //pressKey(KeyCodes.VK_V, true);        // Release V key
            //pressKey(KeyCodes.VK_CONTROL, true);  // Release Ctrl key
            //pressKey(KeyCodes.VK_ENTER, true);  // Release Ctrl key
            //pressKey(KeyCodes.VK_ENTER, true);  // Release Ctrl key

        } else {
            System.out.println("Notepad window not found");
        }
    }
    */

    private static void addMouseListenerToTable(JTable table) {
        //String currentTab =
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            int row = table.getSelectedRow();
            int col = table.getSelectedColumn();
            if (col >= 0 && row >= 0) {// Check if the click occurred in the "Item ID" column

                /*
                //if ("Item ID".equals(table.getColumnName(col))) {
                if (col > 0) {
                    try {
                        //System.out.println("K");
                        switchFocusToNotepad();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                */

                String inputType = table.getValueAt(row, 0).toString();
                if (!inputType.isEmpty()) {
                    String consoleCommand = "";// Console Command - Check type of console command, ITEM, PERK, etc
                    if (inputType.equalsIgnoreCase("ITEM")) {
                        consoleCommand = "player.additem";
                    }
                    if (inputType.equalsIgnoreCase("PERK")) {
                        consoleCommand = "player.addperk";
                    }
                    if (inputType.equalsIgnoreCase("MOD")) {
                        consoleCommand = ".amod";
                    }
                    if (inputType.equalsIgnoreCase("WAIT")) {
                        consoleCommand = "showmenu sleepwaitmenu";
                    }
                    if (inputType.equalsIgnoreCase("UNLOCK")) {
                        consoleCommand = "player.addspell";
                    }
                    if (inputType.equalsIgnoreCase("PSB")) {
                        consoleCommand = "psb";
                    }
                    if (inputType.equalsIgnoreCase("RESEARCH")) {
                        consoleCommand = "CompleteResearch";
                    }
                    if (inputType.equalsIgnoreCase("BOUNTY")) {
                        consoleCommand = "player.paycrimegold";
                    }

                    // Amount - Append amount to ITEM type
                    String itemID = table.getValueAt(row, 2).toString();
                    String amount = "";
                    if (inputType.equalsIgnoreCase("ITEM")) {
                        amount = table.getValueAt(row, 3).toString();
                    }
                    if (inputType.equalsIgnoreCase("WAIT")) {
                        amount = table.getValueAt(row, 3).toString();
                    }
                    if (inputType.equalsIgnoreCase("BOUNTY")) {
                        amount = table.getValueAt(row, 3).toString();
                    }

                    // Construct complete console command
                    String finalInput = "";
                    if (inputType.equalsIgnoreCase("ITEM")) {
                        finalInput = consoleCommand + " " + itemID + " " + amount;
                    }
                    if (inputType.equalsIgnoreCase("PERK")) {
                        finalInput = consoleCommand + " " + itemID;
                    }
                    if (inputType.equalsIgnoreCase("MOD")) {
                        finalInput = consoleCommand + " " + itemID;
                    }
                    if (inputType.equalsIgnoreCase("WAIT")) {
                        finalInput = consoleCommand;
                    }
                    if (inputType.equalsIgnoreCase("UNLOCK")) {
                        finalInput = consoleCommand + " " + itemID;
                    }
                    if (inputType.equalsIgnoreCase("PSB")) {
                        finalInput = consoleCommand;
                    }
                    if (inputType.equalsIgnoreCase("RESEARCH")) {
                        finalInput = consoleCommand + " " + itemID;
                    }
                    if (inputType.equalsIgnoreCase("BOUNTY")) {
                        finalInput = consoleCommand + " " + amount + " " + itemID;
                    }

                    if (inputType.equalsIgnoreCase("DONT")) {
                        //finalInput = "player.placeatme 00009cee 30";// pirates
                        //finalInput = "player.placeatme 00324cfa 30";// aliens
                        //finalInput = "tcl;setgs fJumpHeightMax 900.0000";// jump very high
                        finalInput = "player.placeatme 00003A26 5000";// aliens

                        try {
                            pressKeyOld(VK_SPACE);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        //finalInput = "player.placeatme 0004A41A 30";// mines
                    }

                    // Copy the combined value to the clipboard
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    StringSelection selection = new StringSelection(finalInput);
                    clipboard.setContents(selection, selection);

                    showPopupMessage(table, table.getValueAt(row, 1) + " " + amount + "\n" + finalInput);

                    // Test output
                    System.out.println("inputType: " + inputType);
                    //System.out.println(table.getValueAt(row, 1));
                    System.out.println(finalInput);
                    System.out.println("");
                    //System.out.println("Row: " + row);
                }// if (!inputType.equals("")) {
            }// if (col >= 0 && row >= 1) {
        }// mouseClicked
        });
    }// mouse click

    private static void addMouseMotionListenerToTable(JTable table) {// mouse move
        final int[] lastCell = {-1, -1}; // Store the last hovered cell's row and column
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                // show image on mouse over if mouse is over item name
                if (col == 1 && row >= 0 && (row != lastCell[0] || col != lastCell[1])) {
                    lastCell[0] = row; // Update last hovered cell's row
                    lastCell[1] = col; // Update last hovered cell's column
                    String imagePath = table.getValueAt(row, table.getColumnCount() - 1).toString();
                    if (!imagePath.isEmpty()) {
                        showImage(imagePath);
                    }
                }
                // stop showing image if mouse is not over item name
                //if (col > 1) showImage(null);
            }
        });
    }// mouse-over

    public static void setMeleeTableSorter(JTable itemTable, TableModel itemModel) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(itemModel);
        itemTable.setRowSorter(sorter);

        // Comparator to handle sorting of string numbers
        Comparator<String> numberComparator = (s1, s2) -> {
            try {
                return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2));
            } catch (NumberFormatException e) {
                return s1.compareTo(s2);
            }
        };
        // Set custom comparators for the relevant columns
        sorter.setComparator(1, numberComparator); // Damage column
        sorter.setComparator(2, numberComparator); // Fire Rate column
        sorter.setComparator(3, numberComparator); // Mag Size column
    }// sort order of columns

    public static void setGunsTableSorter(JTable itemTable, TableModel itemModel) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(itemModel);
        itemTable.setRowSorter(sorter);

        // Comparator to handle sorting of string numbers
        Comparator<String> numberComparator = (s1, s2) -> {
            try {
                return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2));
            } catch (NumberFormatException e) {
                return s1.compareTo(s2);
            }
        };
        // Set custom comparators for the relevant columns
        sorter.setComparator(6, numberComparator); // Damage column
        sorter.setComparator(7, numberComparator); // Fire Rate column
        sorter.setComparator(8, numberComparator); // Mag Size column
        sorter.setComparator(9, numberComparator); // DPS column
    }// sort order of columns

    private static void showPopupMessage(Component parent, String message) {
        // Create the dialog
        JDialog dialog = new JDialog((Frame) null, "COMMAND COPIED");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 150);
        //dialog.setLocation(300, 300); // preventing d box from closing?
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());

        // Create and add the message label
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        dialog.add(messageLabel, BorderLayout.CENTER);

        // Create the buttons
        JButton copyButton = new JButton("COPY");
        JButton executeButton = new JButton("EXECUTE");

        // Add action listeners to the buttons
        copyButton.addActionListener(e -> {
            dialog.dispose();
        });
        executeButton.addActionListener(e -> {
            dialog.dispose();
            try {
                switchFocusToNotepad();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });

        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout()); // or BorderLayout if you prefer
        buttonPanel.add(copyButton);
        buttonPanel.add(executeButton);

        // Add the button panel to the dialog
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Display the dialog
        dialog.setVisible(true);

        // Assuming popupTimer is defined elsewhere in your code
        popupTimer.start(); // Uncomment if needed
    }// showPopupMessage

    /*
    private static void showPopupMessage(Component parent, String message) {
        JDialog dialog = new JDialog((Frame) null, "COMMAND COPIED");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        dialog.add(messageLabel, BorderLayout.CENTER);
        dialog.setVisible(true);
        popupTimer.start();
    }// showPopupMessage
    */

    private static void addDocumentListenerToSearchField(JTextField searchField, JTable table) {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTable();
            }
            private void filterTable() {
                TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel()); // Change this line
                table.setRowSorter(sorter);
                RowFilter<TableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + searchField.getText(), 1);
                sorter.setRowFilter(rowFilter);
            }
        });
    }// search bar

    private static void setColumnWidthForAllTabs(JTabbedPane tabbedPane, String columnName, int width) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            JScrollPane scrollPane = (JScrollPane) ((JPanel) tabbedPane.getComponentAt(i)).getComponent(1);
            JTable table = (JTable) scrollPane.getViewport().getView();

            // Find the column index by name
            int columnIndex = table.getColumnModel().getColumnIndex(columnName);
            if (columnIndex >= 0) {
                TableColumn column = table.getColumnModel().getColumn(columnIndex);
                column.setMaxWidth(width);
                column.setPreferredWidth(width);
            }
        }
    }// setColumnWidthForAllTabs

    private static void hideColumn(TableColumn t) {// column INPUT_TYPE is used for calculation and should not be visible
        t.setMinWidth(0);
        t.setMaxWidth(0);
        t.setPreferredWidth(0);
    }//hideColumn

    private static void showImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            if (imageFrame != null) {
                imageFrame.dispose();
                imageFrame = null; // Reset imageFrame if no image path
            }
            return; // Exit method if imagePath is empty or null
        }
        if (imageFrame != null) {
            lastImageFrameLocation = imageFrame.getLocation();
            imageFrame.dispose();
        }

        imageFrame = new JFrame("Image Viewer");
        imageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        imageFrame.setLocation(lastImageFrameLocation);

        imageFrame.setSize(230, 498);
        // change window size according to open tab
        if (("SUITS".equals(openTab))) {
            imageFrame.setSize(210, 650);
        }
        if (("PACKS".equals(openTab))) {
            imageFrame.setSize(220, 430);
        }
        if (("MAGS".equals(openTab))) {
            imageFrame.setSize(220, 300);
        }

        JLabel imageLabel = new JLabel();
        ImageIcon imageIcon = new ImageIcon(imagePath);
        imageLabel.setIcon(imageIcon);
        imageFrame.add(new JScrollPane(imageLabel));
        imageFrame.setVisible(true);
    }// show preview of item in window
}//class Main

