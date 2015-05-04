/*
 * Copyright (C) 2015 Jeroen Berkvens
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package symbiosis.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.google.jhsheets.filtered.FilteredTableView;
import org.google.jhsheets.filtered.operators.IFilterOperator;
import org.google.jhsheets.filtered.operators.NumberOperator;
import org.google.jhsheets.filtered.tablecolumn.ColumnFilterEvent;
import org.google.jhsheets.filtered.tablecolumn.FilterableBooleanTableColumn;
import org.google.jhsheets.filtered.tablecolumn.FilterableDateTableColumn;
import org.google.jhsheets.filtered.tablecolumn.FilterableEnumTableColumn;
import org.google.jhsheets.filtered.tablecolumn.FilterableIntegerTableColumn;
import org.google.jhsheets.filtered.tablecolumn.FilterableStringTableColumn;

/**
 *
 * @author Jeroen Berkvens
 * @company EQUA
 * @project Symbiosis
 */
public class TableTest extends Application {
    
    public enum ExampleType { Option1, Option2, Option3 } ;
    
    private FilteredTableView<ExampleItem> filteredTable;
    private FilterableIntegerTableColumn<ExampleItem,Integer> idColumn;
    private FilterableStringTableColumn<ExampleItem,String> valColumn;
    private FilterableEnumTableColumn<ExampleItem,ExampleType> typeColumn;
    private FilterableDateTableColumn<ExampleItem, Date> dateColumn;
    private FilterableBooleanTableColumn<ExampleItem,Boolean> boolColumn;
    
    public static void main(String[] args) 
    {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) 
    {
        // The FilteredTableView makes it easier to receive notifications of changes to the column filters
        filteredTable = new FilteredTableView<>(createItems());
        
        // Allow an Integer or Integer range as a filter(s)
        idColumn = new FilterableIntegerTableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory("id"));
        
        // Allow for free-form text input as a filter
        valColumn = new FilterableStringTableColumn<>("Value");
        valColumn.setCellValueFactory(new PropertyValueFactory("val"));
        
        // Allow a set of array or enum values to be used selected from
        // Don't let the name fool you, you can pass in any type of object you'd like
        // The toString() method will be called, and used to display the filter menus
        typeColumn = new FilterableEnumTableColumn<>("Type", ExampleType.values());
        typeColumn.setCellValueFactory(new PropertyValueFactory("type"));
        
        // Allow a Date or Date range as a filter(s), with an optional date format
        dateColumn = new FilterableDateTableColumn<>("Date","yyyy-MM-dd HH:mm:ss");
        dateColumn.setCellValueFactory(new PropertyValueFactory("date"));
        
        // Allow a Boolean as a filter
        boolColumn = new FilterableBooleanTableColumn<>("Bool");
        boolColumn.setCellValueFactory(new PropertyValueFactory("bool"));
        
        filteredTable.getColumns().setAll(idColumn, valColumn, typeColumn, dateColumn, boolColumn);
        
        // Listen for changes to the table's filters
        filteredTable.addEventHandler(ColumnFilterEvent.FILTER_CHANGED_EVENT, new EventHandler<ColumnFilterEvent>() {
            @Override
            public void handle(ColumnFilterEvent t) {
                System.out.println("Filtered column count: " + filteredTable.getFilteredColumns().size());
                System.out.println("Filtering changed on column: " + t.sourceColumn().getText());
                System.out.println("Current filters on column " + t.sourceColumn().getText() + " are:");
                final List<IFilterOperator>filters = t.sourceColumn().getFilters();
                for (IFilterOperator filter : filters)
                {
                    System.out.println("  Type=" + filter.getType() + ", Value=" + filter.getValue() );
                }
                
                applyFilters();
            }
        });
        
        primaryStage.setScene(new Scene(filteredTable, 600, 200));
        primaryStage.show();  
    }
    
    public void applyFilters()
    {
        // Filter the data...
        final ObservableList<ExampleItem> newData = createItems();
        filterIdColumn(newData);
        filterValColumn(newData);
        filterTypeCol(newData);
        filterDateCol(newData);
        filterBoolCol(newData);
        // Display the filtered results
        filteredTable.getItems().setAll(newData);
    }
    
    public void filterIdColumn(ObservableList<ExampleItem> newData)
    {
        // Here's an example of how you could filter the ID column
        final List<ExampleItem> remove = new ArrayList<>();
        final ObservableList<NumberOperator<Integer>> filters = idColumn.getFilters();
        for (NumberOperator<Integer> filter : filters)
        {
            for (ExampleItem item : newData)
            {
                // Note: not all Type's are supported for each Operator.
                // Look at the validTypes() method to see what types are available.
                if (filter.getType() == NumberOperator.Type.EQUALS)
                {
                    if (item.getId() != filter.getValue()) remove.add(item);
                }
                else if (filter.getType() == NumberOperator.Type.NOTEQUALS)
                {
                    if (item.getId() == filter.getValue()) remove.add(item);
                }
                else if (filter.getType() == NumberOperator.Type.GREATERTHAN)
                {
                    if (item.getId() <= filter.getValue()) remove.add(item);
                }
                else if (filter.getType() == NumberOperator.Type.GREATERTHANEQUALS)
                {
                    if (item.getId() < filter.getValue()) remove.add(item);
                }
                else if (filter.getType() == NumberOperator.Type.LESSTHAN)
                {
                    if (item.getId() >= filter.getValue()) remove.add(item);
                }
                else if (filter.getType() == NumberOperator.Type.LESSTHANEQUALS)
                {
                    if (item.getId() > filter.getValue()) remove.add(item);
                }
            }
        }
        
        newData.removeAll(remove);
    }
    
    public void filterValColumn(ObservableList<ExampleItem> newData)
    {
        // ... implement
    }
    
    public void filterTypeCol(ObservableList<ExampleItem> newData)
    {
        // ... implement
    }
    
    public void filterDateCol(ObservableList<ExampleItem> newData)
    {
        // ... implement
    }
    
    public void filterBoolCol(ObservableList<ExampleItem> newData)
    {
        // ... implement
    }
    
    public ObservableList<ExampleItem> createItems()
    {
        // Create some dummy date to display
        return FXCollections.observableArrayList(
                  new ExampleItem(1,"Foo",ExampleType.Option1, new Date(113,0,20), true)
                , new ExampleItem(2,"Bar",ExampleType.Option2, new Date(112,3,11), false)
                , new ExampleItem(3,"Baz",ExampleType.Option3, new Date(113,5,29), false)
                , new ExampleItem(4,"Lorem",ExampleType.Option2, new Date(111,2,1), true)
                , new ExampleItem(5,"Ipsum",ExampleType.Option3, new Date(113,1,1), true)
                , new ExampleItem(6,"Wookie",ExampleType.Option3, new Date(112,11,31), false)
                );
    }
    
    static public class ExampleItem
    {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty val;
        private final SimpleObjectProperty<ExampleType> type;
        private final SimpleObjectProperty<Date> date;
        private final SimpleBooleanProperty bool;
        public ExampleItem(int id, String val, ExampleType type, Date date, boolean bool)
        {
            this.id = new SimpleIntegerProperty(id);
            this.val = new SimpleStringProperty(val);
            this.type = new SimpleObjectProperty<>(type);
            this.date = new SimpleObjectProperty<>(date);
            this.bool = new SimpleBooleanProperty(bool);
        }
        public int getId() { return id.get(); }
        public void setId(int id) { this.id.set(id); }
        public SimpleIntegerProperty idProperty() { return id; }
        
        public String getVal() { return val.get(); }
        public void setVal(String val) { this.val.set(val); }
        public SimpleStringProperty valProperty() { return val; }
        
        public ExampleType getType() { return type.get(); }
        public void setId(ExampleType type) { this.type.set(type); }
        public SimpleObjectProperty<ExampleType> typeProperty() { return type; }
        
        public Date getDate() { return date.get(); }
        public void setDate(Date val) { this.date.set(val); }
        public SimpleObjectProperty<Date> dateProperty() { return date; }
        
        public Boolean getBool() { return bool.get(); }
        public void setBool(boolean bool) { this.bool.set(bool); }
        public SimpleBooleanProperty boolProperty() { return bool; }
    }
}