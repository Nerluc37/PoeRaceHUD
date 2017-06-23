# Path of Exile Racing HUD

A HUD for Path of Exile. You can get league or race stats for your character.
Created for my own liking, you are welcome to try to see if its useful for you. 

## Prerequisites

You will need to install sbt and the POE font set to run it as intended.  

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

Q: Can I modify the config file on the fly? 
A: No, you have to restart the application. 

Q: How often it is refreshing data?
A: Every 30 seconds or as long as one API request cycle completes. This depends on your location, and your
 ladder position. From Europe I get about 200 positions every second.

## Feedback 

Any question, request, feedback is welcome. Please use the Issues 

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

