# FoodTruckFinder
This Java console project displays a list of food trucks that are currently open during program execution time given a source of food truck data from the San Francisco's food truck API (http://data.sfgov.org/resource/bbb8-hzi6.json).
So if we run the program at noon on a Friday, we should see a list of all the food trucks that are open then. The output is sorted by food truck name and displayed.

The output is returned to the console in the following format:

| NAME | ADDRESS |
| ------------- |:-------------:| 
| Golden Gate Halal Food      | 979 MARKET ST
| Tacos Rodriguez | 1275 EVANS AVE
| ...             | ...

## Getting Started
- Clone this repository (git clone https://github.com/codesaiyan/FoodTruckFinder.git)
- Open the Java project as a Maven project using the pom.xml file.
- To build the project, open a terminal/command line instance within the project directory and execute the command "mvn clean install"
- Execute the FoodTruckFinder.java file to run the program and check the results in the console.



### Prerequisites
- JDK 8   
- Maven

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

### Web application implementation versus command line program
- If I was implementing this as a full blown web application, I would definitely implement location based food truck mapping. The API returns the data about all the food trucks but people are typically interested in food trucks near their location.  So, I would only return food trucks in a driving radius of 5 miles. And expose a different API in case the user wants to search over a wider radius. 
- The "optionaltext" field in the API response contains the cuisine and the menu of the food truck. This is extremely inportant information that users want to know. So, I would add that to the response and also implement filtering based on cuisine.
- Typically, food trucks have a loyal clientele thus they more or less have fixed location and hours of operations. Since, they open and close on the hour, we could run a background search at a time slice right after an hour (e.g 4 PM and 1 second) and cache the data. This will be reasonably accurate and ensure extremely fast response to the users.
