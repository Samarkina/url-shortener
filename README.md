# URL Shortener
Link shortener website using Redis database. 

In order to use this service you need to install Redis and run `src/main/scala/Main.scala` file.

### How to install Redis:
```
brew install redis
```

Starting and stopping Redis in the foreground

To test your Redis installation, you can run the redis-server executable from the command line:
```
redis-server
```

If successful, you'll see the startup logs for Redis, and Redis will be running in the foreground.

**To stop Redis, enter Ctrl-C.**

Starting and stopping Redis using launchd

As an alternative to **running** Redis in the foreground, you can also use launchd to start the process in the background:
```
brew services start redis
```

To **stop** the service, run:
```
brew services stop redis
```

