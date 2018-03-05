import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class FoodTruckFinder {

  private final static int LIMIT = 1000;
  private final static int PAGE_SIZE = 10;
  private static final String BASE_URL = "http://data.sfgov.org/resource/bbb8-hzi6.json";
  private static Queue<FoodTruckModel> mainQueue = new LinkedList<>();
  private int offset = 0;

  public static void main(String[] args) throws IOException {
    FoodTruckFinder foodTruckFinder = new FoodTruckFinder();
    int size = foodTruckFinder.mainHandler().size();
    boolean userWantsToContinue = true;

    Scanner reader = null;
    while (size == PAGE_SIZE && userWantsToContinue) {
      reader = new Scanner(System.in);  // Reading from System.in
      System.out.println("Do you want to see more? (Y/N): ");
      String option = reader.next(); // Scans the next token of the input as a String.
      option = option.toLowerCase();
      switch (option) {
        case "y":
          size = foodTruckFinder.mainHandler().size();
          System.out.printf("%-30.30s  %-30.30s%n", "NAME", "ADDRESS");
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

  private List<FoodTruckModel> getData(int offset) throws IOException {
    StringBuilder result = new StringBuilder();
    URL url = new URL(BASE_URL + "?$limit=" + LIMIT + "&$offset=" + offset);
    System.out.println(url.toString());
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

  private List<FoodTruckModel> getFilteredList(List<FoodTruckModel> list) {
    List<FoodTruckModel> resultList = new ArrayList<>();

    LocalDateTime localDateTime = LocalDateTime.now();
    String dayOfWeek = localDateTime.getDayOfWeek().name();
    LocalTime localTime = LocalTime.now();
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    for (FoodTruckModel item : list) {
      LocalTime startTime = LocalTime.parse(item.getOpeningTime(), dateTimeFormatter);
      LocalTime endTime = LocalTime.parse(item.getClosingTime(), dateTimeFormatter);
      if (item.getOpenDay().equalsIgnoreCase(dayOfWeek) && startTime.isBefore(localTime)
          && endTime.isAfter(localTime)) {
        resultList.add(item);
      }
    }

    return resultList;
  }

  // always return list of size count
  private List<FoodTruckModel> getNextResults() throws IOException {

    List<FoodTruckModel> result = new ArrayList<>();
    List<FoodTruckModel> rawList;
    List<FoodTruckModel> filteredList;

    while (result.size() < PAGE_SIZE && offset >= 0) {
      rawList = getData(offset);
      filteredList = getFilteredList(rawList);
      result.addAll(filteredList);

      if (rawList.size() < LIMIT) {
        offset = -1;
        break;
      }
      offset += LIMIT;
    }
    return result;
  }

  private List<FoodTruckModel> mainHandler() throws IOException {

    List<FoodTruckModel> myList = new ArrayList<>();

    if (mainQueue.size() < PAGE_SIZE) {
      mainQueue.addAll(getNextResults());
    }

    int size = Math.min(PAGE_SIZE, mainQueue.size());
    for (int i = 0; i < size; i++) {
      myList.add(mainQueue.remove());
    }
    return myList;
  }
}
