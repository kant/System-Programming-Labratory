//
// Created by eyal on 10/31/18.
//
#include "../include/Customer.h"


//Customer abstract class

Customer::Customer(std::string c_name, int c_id) : name(c_name), id(c_id){}

std::string Customer::getName() const { return  name; }

int Customer::getId() const { return id; }

//Vegetarian Customer
VegetarianCustomer::VegetarianCustomer(std::string name, int id) : Customer(name, id){}
std::vector<int> VegetarianCustomer::order(const std::vector<Dish> &menu) {}
std::string VegetarianCustomer::toString() const {return getName()+",veg";}


//Cheap Customer
CheapCustomer::CheapCustomer(std::string name, int id) : Customer(name, id){}
std::vector<int> CheapCustomer::order(const std::vector<Dish> &menu) {}
std::string CheapCustomer::toString() const {return getName()+",chp";}


//Spicy Customer
SpicyCustomer::SpicyCustomer(std::string name, int id) :Customer(name, id){}
std::vector<int> SpicyCustomer::order(const std::vector<Dish> &menu) {}
std::string SpicyCustomer::toString() const {return getName()+",spc";}

//Alcoholic Customer
AlchoholicCustomer::AlchoholicCustomer(std::string name, int id) :Customer(name, id){}
std::vector<int> AlchoholicCustomer::order(const std::vector<Dish> &menu) {}
std::string AlchoholicCustomer::toString() const {return getName()+",alc";}