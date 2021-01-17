# System-Programming-Laboratory

## Assignment-1:
In this assignment you will write a C++ program that simulates a restaurant management system.The program will open the restaurant, assign customers to tables, make orders, provide bills to the tables, and other requests as described below. The program will get a config file as an input, which includes all required information about the restaurant opening-number of tables, number of available seats in each table, and details about the dishes in the menu. There are 4 types of customers in this restaurant, each customer type has its own way of ordering from the menu (an ordering strategy). An order may be taken from a table more than once, in such cases some customers may order a different dish. Each table in the restaurant has a limited amount of seats available (this info is provided in the config file). The restaurant can’t connect tables together, nor accommodates more customers than the number of seats available in a table. In this restaurant, it’s impossible to add new customers to an open table, but it’s possible to move customers from one table to another. A bill of a table is the total price of all dishes ordered for that table.

## Assignment-2:
In the following assignment you are required to implement a simple Micro-Service framework, which you will then use to implement an online book store with a delivery option. The Micro-Services architecture has become quite popular in recent years. In the Micro-Services architecture, complex applications are composed of small and independent services which are able to communicate with each other using messages. The Micro-Service architecture allows us to compose a large program from a collection of smaller independent parts. This assignment is composed of two main sections:
  1.Building a simple Micro-Service framework.
  2.Implementing an online books store application on top of this framework
  
 ## Assignment-3:
In this assignment you will implement a simple social network server and client. The communication between the server and the client(s) will be performed using a binary communication protocol. A registered user will be able to follow other users and post messages. Please read the entire document before starting. The implementation of the server will be based on the Thread-Per-Client (TPC) and Reactor servers taught in class. The servers, as seen in class, only support pull notifications. Any time the server receives a message from a client it can replay back to the client itself. But what if we want to send messages between clients, or broadcast an announcment to a group of clients? We would like the server to send those messages directly to the client without reciveing a request to do so. this behaviour is called push notifications. The first part of the assignment will be to replace some of the current interfaces with new interfaces that will allow such a case. Note that this part changes the servers pattern and must not know the specific protocol it is running. The current server pattern also works that way (Generics and interfaces). Once the server implementation has been extended you will have to implement an example protocol. The  BGS (Ben  Gurion  Social) Protocol will emulate a simple social network. Users need to register to the service. Once registered, they will be able to post messages and follow other users. It is a binary protocol that uses pre defined  message length for different commands. The commands are defined by an opcode, a short number at the start of each message. For each command, a different lengthof data needs to be read according  to  it’s specifications. In the following sections we will define the specifications of the commands supported by the BGS protocol. Unlike real social network you will not work with real databases. You will need to save data (Users, Passwords, Messages, ect...). You only need to save information from the time the server starts and keep it in memory until the server closes.
 
 ## Assignment-4:
 BGU needs your help to schedule courses.You are required to implement asimulator of assigning classrooms to courses. In this assignment, you will implement such a tool using Python and SQLite.
