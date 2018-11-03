#ifndef RESTAURANT_H_
#define RESTAURANT_H_

#include <vector>
#include <string>
#include "Dish.h"
#include "Table.h"
#include "Action.h"


class Restaurant{		
public:
	Restaurant();
    Restaurant(const std::string &configFilePath);
    void start();
    int getNumOfTables() const;
    Table* getTable(int ind);
	const std::vector<BaseAction*>& getActionsLog() const; // Return a reference to the history of actions
    std::vector<Dish>& getMenu();

    //rule of 5
    virtual ~Restaurant();
    Restaurant(Restaurant &other);
    Restaurant & operator=(Restaurant &other);
    Restaurant(Restaurant &&other);
    Restaurant &operator=(Restaurant &&other);

    //helper methods
	void execute(OpenTable &action);
	void execute(Order &action);
	void execute(MoveCustomer &action);
	void execute(PrintTableStatus &action);
	void execute(Close &action);
	void execute(CloseAll &action);
	void execute(PrintMenu &action);
	void execute(BackupRestaurant &action);
	void execute(RestoreResturant &action);
	void execute(PrintActionsLog &action);
    void clean();
    std::vector<Table*> getTables() const;


private:
    bool open;
    std::vector<Table*> tables;
    std::vector<Dish> menu;
    std::vector<BaseAction*> actionsLog;

    void readFile(const std::string &configFilePath);
    DishType convert(std::string type);
    std::string reverseCOnvert(DishType type);
    void copy(Restaurant &other);
    void steal(Restaurant &other);
    bool checkOpenValid(std::vector<std::string> tokens, Table &table);
    bool checkTable(int src, int dst);
};

#endif