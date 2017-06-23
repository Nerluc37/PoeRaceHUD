# Path of Exile Racing HUD

A HUD for Path of Exile. You can get league or race stats for your character.
Created for my own liking, you are welcome to try to see if its useful for you.
 
## Screenshot
 
* [Screenshot](http://imgur.com/a/PivRu)  

## Prerequisites

You will need to install sbt and the POE font set to run it as intended. You need to run POE in windowed mode to see 
the HUD.  

## Installing

* Download the project to your computer. Choose "Download ZIP" from the "Clone or download"
 dropdown you can see on the top right.

* [Install sbt](http://www.scala-sbt.org/download.html) - SBT download page

* [Install Fontin font](https://www.exljbris.com/fontin.html) - The POE font set

* Unzip the project on your hard drive

* Edit the src/main/resources/application.conf file. Fill your account-name and league-name

* Open a command line window, go to the project root directory (where you find the build.sbt file).

* Execute the application

```
sbt run
```

Remark: this will take very long (up to 10 minutes) on the first go, when sbt downloads a tons of stuff. 

## FAQ

Q: Can I modify the config file on the fly?<br> 
A: No, you have to restart the application.

Q: How often it is refreshing data?<br>
A: Every 30 seconds or as long as one API request cycle completes. This depends on your location, and your
 ladder position. From Europe I get about 200 positions every second.
 
Q: In the config file I am only filling my account name. Which character will the HUD show?<br>
A: Your highest level not dead character
  
Q: Why does it hang on Searching ... <br>
A: Check you command line window, if it has errors, then the API request fails. Example you mistyped the league or race 
name. If not, then you might not have a living character in that league's top 15000.

Q: I don't like the setup you have. Can I modify it?<br>
A: Yes to some extend. Please check the application.conf file comments. You can modify a few things.

Q: How can I shut down the application?<br>
A: Close the command line window running sbt or press Ctrl+C there.

## Feedback 

Any question, request, feedback is welcome. Please use the Issues 

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

