package pr.foodtruckfinder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodTruckModel {

  @JsonProperty("applicant")
  private String foodTruckName;
  @JsonProperty("dayofweekstr")
  private String openDay;
  @JsonProperty("end24")
  private String closingTime;
  @JsonProperty("location")
  private String address;
  @JsonProperty("start24")
  private String openingTime;

  @JsonProperty("applicant")
  public String getFoodTruckName() {
    return foodTruckName;
  }

  @JsonProperty("dayofweekstr")
  public String getOpenDay() {
    return openDay;
  }

  @JsonProperty("end24")
  public String getClosingTime() {
    return closingTime;
  }

  @JsonProperty("location")
  public String getAddress() {
    return address;
  }

  @JsonProperty("start24")
  public String getOpeningTime() {
    return openingTime;
  }
}
