package tanya.todolist;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import tanya.todolist.datamodel.ToDoItem;
import javafx.fxml.FXML;
import tanya.todolist.datamodel.TodoData;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {
    @FXML
    private ToggleButton filterToggleButton;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private List<ToDoItem> todoItems;
    @FXML
    private Label deadlineLabel;

    @FXML
    private ListView<ToDoItem> todoListView;

    @FXML
    private TextArea itemDetailsTextArea;

    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private FilteredList<ToDoItem> filteredList;
    private Predicate<ToDoItem> wantAllItems;
    private Predicate<ToDoItem> wantTodaysItems;

    public void initialize() {
        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle (ActionEvent event){
                ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);

            }
        });

        listContextMenu.getItems().addAll(deleteMenuItem);

        /**  ToDoItem item1 = new ToDoItem("Mail birthday card", "Buy a card for Bri", LocalDate.of(2021, Month.SEPTEMBER, 15));
        ToDoItem item2 = new ToDoItem("Doctor appointment", "At SLU location at 1pm", LocalDate.of(2021, Month.SEPTEMBER, 29));
        ToDoItem item3 = new ToDoItem("Session with Beth", "Zoom session at 10am", LocalDate.of(2021, Month.OCTOBER, 1));
        ToDoItem item4 = new ToDoItem("Run 5K", "Morning run planned at Green Lake", LocalDate.of(2021, Month.OCTOBER, 2));
        ToDoItem item5 = new ToDoItem("Halloween", "Dress up dog & eat candy", LocalDate.of(2021, Month.OCTOBER, 31));


        todoItems = new ArrayList<ToDoItem>();
        todoItems.add(item1);
        todoItems.add(item2);
        todoItems.add(item3);
        todoItems.add(item4);
        todoItems.add(item5);

        TodoData.getInstance().setTodoItems(todoItems);
    */
        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ToDoItem>() {
            @Override
            public void changed(ObservableValue<? extends ToDoItem> observable, ToDoItem oldValue, ToDoItem newValue) {
                if (newValue != null) {
                    ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
                    itemDetailsTextArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy"); // d M yy
                    deadlineLabel.setText(df.format(item.getDeadline()));
                }
            }
        });

        wantAllItems= new Predicate<ToDoItem>(){

            @Override
            public boolean test(ToDoItem toDoItem) {
                return true;
            }
        };

        wantTodaysItems= new Predicate<ToDoItem>(){

            @Override
            public boolean test(ToDoItem toDoItem) {
                return (toDoItem.getDeadline().equals(LocalDate.now()));
            }
        };

        filteredList = new FilteredList<ToDoItem>(TodoData.getInstance().getTodoItems(), wantAllItems);

        SortedList<ToDoItem> sortedList = new SortedList<ToDoItem>(filteredList,
                new Comparator<ToDoItem>(){
                    @Override
                    public int compare(ToDoItem o1, ToDoItem o2) {
                        return o1.getDeadline().compareTo(o2.getDeadline());
                    }


                });


        todoListView.setItems(TodoData.getInstance().getTodoItems());
        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();

        todoListView.setCellFactory(new Callback<ListView<ToDoItem>, ListCell<ToDoItem>>(){
            @Override
            public ListCell<ToDoItem> call(ListView<ToDoItem> param){
                ListCell<ToDoItem> cell = new ListCell<ToDoItem>(){

                    @Override
                    protected void updateItem(ToDoItem item, boolean empty){
                        super.updateItem(item, empty);
                        if(empty) {
                            setText(null);
                        } else {
                            setText(item.getShortDescription());
                            if(item.getDeadline().isBefore(LocalDate.now().plusDays(1))){
                                setTextFill(Color.PURPLE);
                            } else if(item.getDeadline().equals(LocalDate.now().plusDays(1))){
                                setTextFill(Color.GREEN);

                            }
                        }
                    }
                };

                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if (isNowEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }
                        });

                return cell;
            }
        });
    }
    @FXML
    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add new to-do item");
        dialog.setHeaderText("Use this dialog to create a new to-do item");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DialogController controller = fxmlLoader.getController();
            ToDoItem newItem = controller.processResults();

            todoListView.getSelectionModel().select(newItem);
        }
    }

    @FXML
    public void handleKeyPressed(KeyEvent keyEvent){
        ToDoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if(keyEvent.getCode().equals(KeyCode.DELETE)){
                deleteItem(selectedItem);
            }
        }
    }
    @FXML
    public void handleClickListView(){
       ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
       itemDetailsTextArea.setText(item.getDetails());
       deadlineLabel.setText(item.getDeadline().toString());
      // System.out.println("The selected item is " + item);
      /**  StringBuilder sb = new StringBuilder(item.getDetails());
        sb.append("\n\n\n\n");
        sb.append("Due: ");
        sb.append(item.getDeadline().toString());
        itemDetailsTextArea.setText(sb.toString());
    */
       }

       public void deleteItem(ToDoItem item){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete To-Do Item");
        alert.setHeaderText("Delete item: " + item.getShortDescription());
        alert.setContentText("Are you sure? Press OK to confirm, or cancel to back out.");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && (result.get() == ButtonType.OK)) {
            TodoData.getInstance().deleteToDoItem(item);
            }
        }

       @FXML
        public void handleFilterButton(){
        ToDoItem selectedItem= todoListView.getSelectionModel().getSelectedItem();
         if(filterToggleButton.isSelected()) {
             filteredList.setPredicate(wantTodaysItems);
             if (filteredList.isEmpty()) {
                 itemDetailsTextArea.clear();
                 deadlineLabel.setText("");
             } else if (filteredList.contains(selectedItem)) {
                 todoListView.getSelectionModel().select(selectedItem);
                }
            } else {
                filteredList.setPredicate(wantAllItems);
                todoListView.getSelectionModel().select(selectedItem);
            }
    }

        @FXML
        public void handleExit(){
            Platform.exit();
        }
}