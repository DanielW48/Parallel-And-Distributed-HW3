#include <thread>
#include <mutex>
#include <condition_variable>
#include <vector>
#include <queue>
#include <iostream>
#include <cassert>
#include <atomic>
using namespace std;

const int NUM_THREADS = 8;
const int MIN_TEMP = -100, MAX_TEMP = 70;

// we will use another sorted linked list and add temperatures to
// the list as each thread records them; then at the end
// of the hour we will traverse the list and take the first 5
// and last 5 and record them; I don't understand what the 10
// minute intervals of time are so those will not be included

int getRandTemp(){
    return (rand() % (MAX_TEMP - MIN_TEMP + 1)) + MIN_TEMP;
}

void doSomeStuff(){
    int currTemp = getRandTemp();
}

int main(){
    vector<thread*> threads(NUM_THREADS);
    for(int i = 0; i < NUM_THREADS; ++i){
        threads[i] = new thread(doSomeStuff);
    }

    for(int i = 0; i < NUM_THREADS; ++i){
        threads[i]->join();
    }

    return 0;
}