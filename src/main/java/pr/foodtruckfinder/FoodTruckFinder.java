package pr.foodtruckfinder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dnl.utils.text.table.TextTable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class FoodTruckFinder {

  private static final int LIMIT = 1000; // API limit is 1000 results per call
  private static final int PAGE_SIZE = 10; // Setting the user display limit of 10 results per page
  private static final String BASE_URL = "http://data.sfgov.org/resource/bbb8-hzi6.json";
  // Queue to keep track of the results to be displayed next to the user
  private static Queue<FoodTruckModel> mainQueue = new LinkedList<>();
  private static int offset = 0;

  public static void main(String[] args) throws IOException {
    run();
  }

  private static void run() throws IOException {
    List<FoodTruckModel> results = getResultsForDisplay();
    sortByFoodTruckName(results);
    displayConsoleOutput(results);
    int size = results.size();
    boolean userWantsToContinue = true;
    Scanner reader = null;
    while (size == PAGE_SIZE && userWantsToContinue) {
      reader = new Scanner(System.in);  // Reading from System.in
      System.out.println("Do you want to see more? (Y/N): ");
      String option = reader.next(); // Scans the next token of the input as a String.
      option = option.toLowerCase();
      switch (option) {
        case "y":
          results = getResultsForDisplay();
          size = results.size();
          sortByFoodTruckName(results);
          displayConsoleOutput(results);
          break;
        case "n":
          userWantsToContinue = false;
          break;
      }
    }
    if (reader != null) {
      reader.close();
    }
  }

  private static void sortByFoodTruckName(List<FoodTruckModel> foodTrucks) {
    Collections.sort(foodTrucks,
        (item1, item2) -> item1.getFoodTruckName().compareTo(item2.getFoodTruckName()));
  }

  /**
   * Display the Food Truck name and it's address in a 2 column table to the output console
   */
  private static void displayConsoleOutput(List<FoodTruckModel> results) {
    String[] columnNames = {"NAME", "ADDRESS"};
    String[][] data = new String[results.size()][2];
    int count = 0;
    for (FoodTruckModel item : results) {
      data[count][0] = item.getFoodTruckName();
      data[count][1] = item.getAddress();
      count++;
    }
    TextTable tt = new TextTable(columnNames, data);
    // this adds the numbering on the left
    tt.setAddRowNumbering(true);
    tt.printTable();
  }

  /**
   * This method calls the FoodTruck API and returns a list of FoodTruck Model objects mapped from
   * the JSON response
   */
  private static List<FoodTruckModel> getData(int offset) throws IOException {
    StringBuilder result = new StringBuilder();
    // Limit is the API response size limit and the offset is the index from which the results are returned
    URL url = new URL(BASE_URL + "?$limit=" + LIMIT + "&$offset=" + offset);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String line;
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }
    rd.close();
    return new ObjectMapper()
        .readValue(result.toString(), new TypeReference<List<FoodTruckModel>>() {
        });
  }

  /**
   * Return the filtered result list containing only the food trucks open during program execution
   * time and date
   */
  private static List<FoodTruckModel> getFilteredList(List<FoodTruckModel> list) {
    List<FoodTruckModel> resultList = new ArrayList<>();

    LocalDateTime localDateTime = LocalDateTime.now();
    String dayOfWeek = localDateTime.getDayOfWeek().name();
    LocalTime localTime = LocalTime.now();
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    for (FoodTruckModel item : list) {
      LocalTime startTime = LocalTime.parse(item.getOpeningTime(), dateTimeFormatter);
      LocalTime endTime = LocalTime.parse(item.getClosingTime(), dateTimeFormatter);
      // Filter the results to only have food trucks open right now
      if (item.getOpenDay().equalsIgnoreCase(dayOfWeek) && startTime.isBefore(localTime)
          && endTime.isAfter(localTime)) {
        resultList.add(item);
      }
    }

    return resultList;
  }

  /**
   * Return a list of Food Trucks open at the time and day of program execution of size PAGE_SIZE or
   * lower for display
   */
  private static List<FoodTruckModel> getNextResults() throws IOException {

    List<FoodTruckModel> result = new ArrayList<>(); //
    List<FoodTruckModel> rawList; // Data returned straight from the API call
    List<FoodTruckModel> filteredList; // Filtered raw list data containing only relevant food trucks open at the program execution time

    // We populate the response till it reaches the Display size limit.
    // It can happen that we get more relevant results than the page size limit in the first
    // iteration of the loop and we still populate the response list and break out of the loop
    while (result.size() < PAGE_SIZE && offset >= 0) {
      rawList = getData(offset);
      filteredList = getFilteredList(rawList);
      result.addAll(filteredList);
      // If the API call response size is lower than the API response limit, it means that there are no more food trucks to query
      if (rawList.size() < LIMIT) {
        offset = -1;
        break;
      }
      // Set the offset index for the next API call.
      // For eg. if 1000 food trucks were queried earlier for offset 0 (0-999 results), we query again from offset index 1000 (1000-1999 results) and so on
      offset += LIMIT;
    }
    return result;
  }

  /**
   * Return the result set for display. It has size <= the display page size
   */
  private static List<FoodTruckModel> getResultsForDisplay() throws IOException {

    List<FoodTruckModel> displayList = new ArrayList<>();

    if (mainQueue.size() < PAGE_SIZE) {
      mainQueue.addAll(getNextResults());
    }

    int size = Math.min(PAGE_SIZE, mainQueue.size());
    for (int i = 0; i < size; i++) {
      displayList.add(mainQueue.remove());
    }
    return displayList;
  }
}
