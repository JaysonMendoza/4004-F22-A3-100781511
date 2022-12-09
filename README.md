# 4004-F22-A3-100781511

# Getting Started Using Intelij

## Tech Overview

* This program is designed using a frontend that uses the React framework along with a variety of modules for NodeJS.
* The back end is designed using Java within the Spring Boot framework and websockets over STOMP for communication
* The tests require both the node js development server to be started and ready and the back end java spring server to be started seperatly
  - This uses two ports http://localhost:8080 for the spring server and http://localhost:3000 for the node server
  - The clients interact with http://localhost:8080 while the node server will forward all requests to http://localhost:8080 so they act like a single server
  - If this game was built and deployed it would become a single webserver that serves both front and back end. However, this would require the extra step of setting up the server to serve the static folders in the front end.
* The control flow Architecture is inspired by the FLUX design. This means that message flow from server api to viewport is uni-directional. 
  * The server API pushes STOMP messages that mutate 'data stores' and cause various componenets that rely on them to update when their subscribed data has been changed
  * When the user interacts with the UI they create 'actions' that send a message through the websocket to the server API
  * The server uses these 'actions' to make changes that will update the model and in turn potentially require the view to be updated
  * The server will send the entire new state to through the websocket to the front end updating the data stores and repeating the process
* All game logic is handled on the Java/Spring server. The Entities classes are responsible for implementing game rules in their area of responsibility
* The controllers responsibility is to coordinate all entities for the overall logic and control of the game. Turn sequences for example are the responsbility of the game controller
* The current implementation is that of a one use server. Games will not be reset and the server must be restarted to unregister players or restart the game. This can be changed without too much difficulty but was extra work that was out of scope for this assignment.

## Main Game Server Setup
1. Clone repository to given destination. If your using intelij this is best done using FILE > NEW > FROM VERSION CONTROL menu option and letting it clone for you.
2. Open the project and let Maven install the dependencies

## Node JS Development Server Setup

*This server is only used during deveopment environments because when its built it becomes one with the back end server. The test server uses a proxy to forward requests to the real server so they act like a single unit during testing*

1. Open a terminal in the same operating system and change direct to the ./frontend folder within the repository folder.
2. Run 'npm -i' or 'npm install' to automatically install all node dependencies listed in the package.json file for located in the folder
3. When you are ready to run any tests, you can run the command 'npm start' to start the development server. Make sure it's completely loaded before you run tests.

## Running Tests
1. Open a terminal and go to the ./frontend folder in the repository
2. Run 'npm start' and wait for the development server to start
3. Within intelij, run all acceptance tests as usual. It will automitcally connect to the node front end and talk to the server through the proxy. 
  - There is no need to reset the frontend server between tests. This server only acts to simulate the build environment and provide debugging information.
  - When built this would be compiled with babel and become a more traditional pool of web resources that could be served by the Spring server.
