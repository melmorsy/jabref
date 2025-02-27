package org.jabref.gui.menus;

import java.util.Collection;
import java.util.List;

import javax.swing.undo.UndoManager;

import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;

import org.jabref.gui.EntryTypeView;
import org.jabref.gui.Globals;
import org.jabref.gui.undo.CountingUndoManager;
import org.jabref.gui.undo.NamedCompound;
import org.jabref.gui.undo.UndoableChangeType;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.database.BibDatabaseMode;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.BibEntryType;
import org.jabref.model.entry.types.BibtexEntryTypeDefinitions;
import org.jabref.model.entry.types.EntryType;
import org.jabref.model.entry.types.IEEETranEntryTypeDefinitions;
import org.jabref.model.strings.StringUtil;

public class ChangeEntryTypeMenu {

    public ChangeEntryTypeMenu() {

    }

    public static MenuItem createMenuItem(EntryType type, List<BibEntry> entries, UndoManager undoManager) {
        CustomMenuItem menuItem = new CustomMenuItem(new Label(type.getDisplayName()));
        menuItem.setOnAction(event -> {
            NamedCompound compound = new NamedCompound(Localization.lang("Change entry type"));
            entries.forEach(e -> e.setType(type)
                 .ifPresent(change -> compound.addEdit(new UndoableChangeType(change))));
            undoManager.addEdit(compound);
        });
        String description = EntryTypeView.getDescription(type);
        if (StringUtil.isNotBlank(description)) {
            Tooltip tooltip = new Tooltip(description);
            Tooltip.install(menuItem.getContent(), tooltip);
        }
        return menuItem;
    }

    public ContextMenu getChangeEntryTypePopupMenu(List<BibEntry> entries, BibDatabaseContext bibDatabaseContext, CountingUndoManager undoManager) {
        ContextMenu menu = new ContextMenu();
        populateComplete(menu.getItems(), entries, bibDatabaseContext, undoManager);
        return menu;
    }

    public Menu getChangeEntryTypeMenu(List<BibEntry> entries, BibDatabaseContext bibDatabaseContext, CountingUndoManager undoManager) {
        Menu menu = new Menu();
        menu.setText(Localization.lang("Change entry type"));
        populateComplete(menu.getItems(), entries, bibDatabaseContext, undoManager);
        return menu;
    }

    private void populateComplete(ObservableList<MenuItem> items, List<BibEntry> entries, BibDatabaseContext bibDatabaseContext, CountingUndoManager undoManager) {
        if (bibDatabaseContext.isBiblatexMode()) {
            // Default BibLaTeX
            populate(items, Globals.entryTypesManager.getAllTypes(BibDatabaseMode.BIBLATEX), entries, undoManager);

            // Custom types
            populateSubMenu(items, Localization.lang("Custom"), Globals.entryTypesManager.getAllCustomTypes(BibDatabaseMode.BIBLATEX), entries, undoManager);
        } else {
            // Default BibTeX
            populateSubMenu(items, BibDatabaseMode.BIBTEX.getFormattedName(), BibtexEntryTypeDefinitions.ALL, entries, undoManager);
            items.remove(0); // Remove separator

            // IEEETran
            populateSubMenu(items, "IEEETran", IEEETranEntryTypeDefinitions.ALL, entries, undoManager);

            // Custom types
            populateSubMenu(items, Localization.lang("Custom"), Globals.entryTypesManager.getAllCustomTypes(BibDatabaseMode.BIBTEX), entries, undoManager);
        }
    }

    private void populateSubMenu(ObservableList<MenuItem> items, String text, List<BibEntryType> entryTypes, List<BibEntry> entries, CountingUndoManager undoManager) {
        if (!entryTypes.isEmpty()) {
            items.add(new SeparatorMenuItem());
            Menu custom = new Menu(text);
            populate(custom, entryTypes, entries, undoManager);
            items.add(custom);
        }
    }

    private void populate(ObservableList<MenuItem> items, Collection<BibEntryType> types, List<BibEntry> entries, UndoManager undoManager) {
        for (BibEntryType type : types) {
            items.add(createMenuItem(type.getType(), entries, undoManager));
        }
    }

    private void populate(Menu menu, Collection<BibEntryType> types, List<BibEntry> entries, UndoManager undoManager) {
        populate(menu.getItems(), types, entries, undoManager);
    }
}
