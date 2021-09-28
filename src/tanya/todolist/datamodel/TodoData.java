package tanya.todolist.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

public class TodoData {
    private static TodoData instance = new TodoData();
    private static String filename = "TodoListItems.txt";

    //list of to do items
    private ObservableList<ToDoItem> todoItems;
    //Date time format so we can manipulate the date
    private DateTimeFormatter formatter;

    //public method to return the only instance of our todo class
    public static TodoData getInstance() {
        return instance;
    }

    /**
     * Private constructor
     */

    private TodoData() {
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    /**
     *
     * @return to-do list items
     */
    public ObservableList<ToDoItem> getTodoItems(){
        return todoItems;

    }
    public void addTodoItem(ToDoItem item){
        todoItems.add(item);
    }

   /** public void setTodoItems(List<ToDoItem> todoItems){
        this.todoItems = todoItems;
    }
    */
    public void loadToDoItems() throws IOException {

        todoItems = FXCollections.observableArrayList();
        Path path = Paths.get(filename);
        BufferedReader br = Files.newBufferedReader(path);

        //Will contain data for each line
        String input;


        try {
            //Creating a loop that goes through and retrieves the data
            while ((input = br.readLine()) != null) {
                String[] itemPieces = input.split("\t");

                String shortDescription = itemPieces[0];
                String details = itemPieces[1];
                String dateString = itemPieces[2];

                LocalDate date = LocalDate.parse(dateString, formatter);
                ToDoItem todoItem = new ToDoItem(shortDescription, details, date);
                todoItems.add(todoItem);
            }
           //Testing to make sure we got a valid object before we try and c
        }finally {
                if(br != null){
                    br.close();
                }
            }
        }
        public void storeToDoItems() throws IOException{
            Path path = Paths.get(filename);
            BufferedWriter bw = Files.newBufferedWriter(path);
            try {
                Iterator<ToDoItem> iterate = todoItems.iterator();
                while(iterate.hasNext()) {
                    ToDoItem item = iterate.next();
                    bw.write(String.format("%s\t%s\t%s",
                            item.getShortDescription(),
                            item.getDetails(),
                            item.getDeadline().format(formatter)
                    ));
                    bw.newLine();
                }
            } finally {
                if(bw != null) {
                    bw.close();
                }
            }
        }

        //Method to delete an item
        public void deleteToDoItem(ToDoItem item){
            todoItems.remove(item);
        }

    }

