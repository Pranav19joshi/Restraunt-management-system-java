package ui;

import model.DrinkItem;
import model.FoodItem;
import model.MenuItem;
import repository.MenuRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MenuPanel extends JPanel {
    private final MenuRepository menuRepository;
    private DefaultTableModel tableModel;
    private JTable menuTable;
    private JComboBox<String> typeCombo;
    private JTextField idField, nameField, priceField, categoryField, prepField;
    private JCheckBox vegOrAlcCheck;

    private static final Color BG = new Color(25, 25, 35), CARD = new Color(32, 32, 48);
    private static final Color ACCENT = new Color(255, 200, 60), FG = new Color(210, 210, 225);

    public MenuPanel(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(BG);

        // Title
        JLabel title = new JLabel("Menu Management", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(ACCENT);
        add(title, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Base Price", "Category", "Type", "Taxed Price"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        menuTable = new JTable(tableModel);
        styleTable(menuTable);
        JScrollPane sp = new JScrollPane(menuTable);
        sp.getViewport().setBackground(new Color(38, 38, 52));
        add(sp, BorderLayout.CENTER);

        // Bottom: form + buttons
        JPanel south = new JPanel(new BorderLayout(6, 6));
        south.setBackground(BG);
        south.add(buildForm(), BorderLayout.CENTER);
        south.add(buildButtons(), BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridLayout(4, 4, 8, 5));
        form.setBackground(CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ACCENT),
                        " Add New Item ", 0, 0, new Font("SansSerif", Font.BOLD, 12), ACCENT),
                BorderFactory.createEmptyBorder(6, 10, 8, 10)));

        typeCombo     = combo(new String[]{"Food", "Drink"});
        idField       = field(); nameField = field(); priceField = field();
        categoryField = field(); prepField = field();
        vegOrAlcCheck = new JCheckBox(); vegOrAlcCheck.setBackground(CARD);

        form.add(lbl("Type:"));     form.add(typeCombo);
        form.add(lbl("Item ID:"));  form.add(idField);
        form.add(lbl("Name:"));     form.add(nameField);
        form.add(lbl("Price:"));    form.add(priceField);
        form.add(lbl("Category:")); form.add(categoryField);
        form.add(lbl("Veg/Alc:")); form.add(vegOrAlcCheck);
        form.add(lbl("Prep(min):")); form.add(prepField);
        return form;
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        p.setBackground(BG);
        JButton addBtn = btn("Add Item", new Color(55, 170, 95));
        JButton rmBtn  = btn("Remove Selected", new Color(190, 65, 65));
        JButton refBtn = btn("Refresh", new Color(90, 90, 150));
        addBtn.addActionListener(e -> addItem());
        rmBtn.addActionListener(e  -> removeSelected());
        refBtn.addActionListener(e -> refreshTable());
        p.add(addBtn); p.add(rmBtn); p.add(refBtn);
        return p;
    }

    private void addItem() {
        try {
            String type = (String) typeCombo.getSelectedItem();
            String id = idField.getText().trim(), name = nameField.getText().trim();
            String priceStr = priceField.getText().trim(), category = categoryField.getText().trim();
            if (id.isEmpty() || name.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
                msg("Fill in all required fields.", "Validation", JOptionPane.WARNING_MESSAGE); return;
            }
            double price = Double.parseDouble(priceStr);
            MenuItem item;
            if ("Food".equals(type)) {
                int prep = prepField.getText().trim().isEmpty() ? 10 : Integer.parseInt(prepField.getText().trim());
                item = new FoodItem(id, name, category, price, vegOrAlcCheck.isSelected(), prep);
            } else {
                item = new DrinkItem(id, name, category, price, vegOrAlcCheck.isSelected());
            }
            menuRepository.add(item); refreshTable(); clearForm();
            msg("Item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            msg("Invalid price or prep time.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelected() {
        int row = menuTable.getSelectedRow();
        if (row < 0) { msg("Select an item.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Remove item " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            menuRepository.remove(id); refreshTable();
        }
    }



    public void refreshTable() {
        tableModel.setRowCount(0);
        for (MenuItem item : menuRepository.getAll()) {
            tableModel.addRow(new Object[]{item.getItemId(), item.getName(),
                    String.format("%.2f", item.getBasePrice()), item.getCategory(),
                    item.getItemType(), String.format("%.2f", item.getTaxedPrice())});
        }
    }

    private void clearForm() {
        idField.setText(""); nameField.setText(""); priceField.setText("");
        categoryField.setText(""); prepField.setText("");
        vegOrAlcCheck.setSelected(false);
    }

    // Style helpers
    private JLabel lbl(String t) { JLabel l = new JLabel(t); l.setForeground(FG); l.setFont(new Font("SansSerif", Font.PLAIN, 12)); return l; }
    private JTextField field() { JTextField f = new JTextField(); f.setBackground(new Color(45,45,60)); f.setForeground(Color.WHITE); f.setCaretColor(Color.WHITE); f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(70,70,100)), BorderFactory.createEmptyBorder(2,5,2,5))); return f; }
    private JComboBox<String> combo(String[] items) { JComboBox<String> c = new JComboBox<>(items); c.setBackground(new Color(45,45,60)); c.setForeground(Color.WHITE); return c; }
    private JButton btn(String t, Color bg) { JButton b = new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorderPainted(false); b.setFont(new Font("SansSerif", Font.BOLD, 11)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b; }
    private void styleTable(JTable t) { t.setBackground(new Color(38,38,52)); t.setForeground(FG); t.setGridColor(new Color(55,55,72)); t.getTableHeader().setBackground(new Color(45,45,62)); t.getTableHeader().setForeground(ACCENT); t.setRowHeight(24); t.setSelectionBackground(new Color(70,70,110)); }
    private void msg(String m, String title, int type) { JOptionPane.showMessageDialog(this, m, title, type); }
}
