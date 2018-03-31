# Android application - Cardio project
The application needs to be linked with a Node.js server. To make it easier, we used Node Red technology.

# 1. Install Node.js
	* Go to: [Node.js](https://nodejs.org/en/)
	* Download the LTS version corresponding to your OS
		* For Windows / MacOS, use a simple installer (default installation)
		* For Linux, use the package manager

# 2. Install Node-red
	* Go to: [Node-red](https://nodered.org/docs/getting-started/installation)
		* For Windows, go to [Node-red Windows](https://nodered.org/docs/platforms/windows)
	* Check if Node.js is properly installed:
		* Open a "Command prompt" window
		* Type "node -v" or "node --version". You should see the version of Node.js that is installed on your computer.
	* To install Node-red:
		* Open a "Command prompt" window
		* Type the following command: "npm install -g --unsafe-perm node-red"

# 3. Run the Node-red server
	* Go to: [Running server on Windows](https://nodered.org/docs/platforms/windows#running-on-windows)
	* As said on the website, type the "node-red" command into a "Command prompt" - the server should start running
	* You must not close the command prompt in order to run the server.
	* You will see the following line displayed:
		> Server now running at http://127.0.0.1:1880/
	* Copy the link and open it in a web browser.
	* You should have something like that: ![node-red-installation](https://user-images.githubusercontent.com/23191626/38163469-000d0746-34f5-11e8-9ba4-1d2911882727.JPG)
	* Click on the hamburger menu at the top right page, then click on "import" then "clipboard": ![node-red-installation-2](https://user-images.githubusercontent.com/23191626/38163560-dc57bdfe-34f5-11e8-9073-39a8781cce72.JPG)
	* Now, copy-paste the content of the "nodeJS_noDB.txt" file into the pop-up window: ![node-red-installation-3](https://user-images.githubusercontent.com/23191626/38163566-25faf03e-34f6-11e8-97be-8d2edacac7a5.JPG)
	* Click on "import".
	* Click on the "Flow" page to paste the server schema.
	* Finally, click on the red button "Deploy" at the top right page. The server is running.

# 4. Make sure that the smartphone and the server are connected to the same wifi network !

# 5. You can run the application to interact with the server.
