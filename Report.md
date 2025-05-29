1️⃣
🌘
the output is

Calling run()
Running in: main
Calling start()
Running in: Thread-2

why?

The first and third lines are outputs, but the second line is executing the run() method in the thread that called it, and no new thread is created. As we can see, the output shows main instead of Thread-1, meaning no new thread was created. However, in the last line, since we called the start() method, a new thread was created, and its run() method was executed.


🌗
Difference between start() and run():
When start() is called, a new thread is created, and then the run() method is executed within that new thread.
However, when the run() method is called directly, no new thread is created. Instead, it behaves like a regular function and executes the run() method of the thread in the current thread.
The main difference is that start() creates a new thread, while run() does not
2️⃣
🌘
the output is 

Main thread ends.
Daemon thread running...

why?

A daemon thread is somewhat dependent on the main threads. As long as the main threads are running, the daemon thread continues executing. That’s why, in the output, we see the message "Daemon thread running..." only once. This suggests that until the main thread terminates, the daemon thread executes and prints this message as output.


🌗
The message "Daemon thread running..." will be printed twenty times.
and i think works like .join() method.

🌖
I’m not sure if this is entirely correct, but we could say that, for example, I have an app, and one of its services is reloading data. I create a daemon thread for this service and set its boolean condition in such a way that, as long as the user is online and the main threads—such as scrolling and messaging—are running, this daemon thread keeps executing the reload process. Would something like this work?

3️⃣

🌘
Thread is running using a ...!
🌗
lambda expression
🌖
There isn’t much difference. If our thread is short, we can use this approach to improve code readability. However, if the thread is more complex—beyond just printing an output or performing a simple task—and requires inheritance, it’s better to use another method.

