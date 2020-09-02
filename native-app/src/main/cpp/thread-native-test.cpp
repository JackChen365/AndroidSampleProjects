#include <jni.h>
#include <iostream>
#include <condition_variable>
#include <thread>
#include <chrono>

//--------------------------------------------------------------
//c++ notify test.
//--------------------------------------------------------------
std::condition_variable cv;
std::mutex cv_m; // This mutex is used for three purposes:
// 1) to synchronize accesses to i
// 2) to synchronize accesses to std::cerr
// 3) for the condition variable cv
int i = 0;

void waits()
{
    std::unique_lock<std::mutex> lk(cv_m);
    std::cerr << "Waiting... \n";
    cv.wait(lk, []{return i == 1;});
    std::cerr << "...finished waiting. i == 1\n";
}

void signals()
{
    std::this_thread::sleep_for(std::chrono::seconds(1));
    {
        std::lock_guard<std::mutex> lk(cv_m);
        std::cerr << "Notifying...\n";
    }
    cv.notify_all();

    std::this_thread::sleep_for(std::chrono::seconds(1));

    {
        std::lock_guard<std::mutex> lk(cv_m);
        i = 1;
        std::cerr << "Notifying again...\n";
    }
    cv.notify_all();
}

//int main()
//{
//    std::thread t1(waits), t2(waits), t3(waits), t4(signals);
//    t1.join();
//    t2.join();
//    t3.join();
//    t4.join();
//}


//--------------------------------------------------------------
//c++ consume and produce test.
//--------------------------------------------------------------
#include <condition_variable>
#include <iostream>
#include <thread>
#include <queue>

std::mutex mutex;
std::condition_variable produce_var;
std::condition_variable consume_var;
std::atomic_int counter=0;

const int MAX_BUFFER_SIZE=5;
const int MAX_SIZE=100;
std::queue<int> queue;
void consume(int id){
    while(counter < MAX_SIZE){
        std::unique_lock l(mutex);
        consume_var.wait(l,[](){
            return 0 < queue.size();
        });
        int v=queue.front();
        queue.pop();
        std::this_thread::sleep_for(std::chrono::milliseconds(200));
        l.unlock();
        printf("consume:%d num:%d\n",id,v);
        produce_var.notify_one();
    }
}

void produce(int id){
    while(counter< MAX_SIZE){
        std::unique_lock l(mutex);
        produce_var.wait(l,[](){
            return queue.size() < MAX_BUFFER_SIZE;
        });
        counter++;
        queue.push(counter.load());
        printf("produce:%d num:%d--------------\n",id,counter.load());
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
        l.unlock();
        consume_var.notify_one();
    }
}

//int main(){
//    std::thread t1(consume,0);
//    std::thread t2(consume,1);
//
//    std::thread t3(produce,0);
//    std::thread t4(produce,1);
//    std::thread t5(produce,2);
//
//    t1.join();
//    t2.join();
//    t3.join();
//    t4.join();
//    t5.join();
//    return 0;
//}

//--------------------------------------------------------------
//c++ future test.
//--------------------------------------------------------------
#include <future>

bool process(int v){
    std::this_thread::sleep_for(std::chrono::milliseconds(100));
    if(0== v%2){
        return true;
    } else {
        return false;
    }
}

int main(){
    std::future task=std::async(process,2);
    if(task.wait_for(std::chrono::milliseconds(200))==std::future_status::timeout){
        printf("future task timeout.\n");
    }
    bool result=task.get();
    printf("future task result:%i.\n",result);
    return 0;
}

