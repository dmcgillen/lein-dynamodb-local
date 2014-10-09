lein-dynamodb-local
===================

A Leiningen 2 plugin providing a local DynamoDB instance to run tests against.

This starts an instance of DynamoDB Local (http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.DynamoDBLocal.html) provided by Amazon and shuts it down on completion. This is packaged with the plugin and does not need to be downloaded separately. The plugin puts the DynamoDB Local libraries into your temp directory.

## Usage

Add `[lein-dynamodb-local "0.1.0"]` to your project.clj `:plugins` vector.

You can start DynamoDB Local, followed by any other tasks you may want to run, using the following:

    $ lein dynamodb-local other-tasks...

The plugin will shutdown DynamoDB Local upon completion.

DynamoDB Local can be started without running any other tasks if you so wish:

    $ lein dynamodb-local

Killing the process (e.g. with <kbd>Ctrl</kbd>+<kbd>C</kbd>) will shutdown DynamoDB Local.

### Configuration

You can set some parameters by adding the `:dynamodb-local` keyword to your project.clj map.

#### :port

Set the port that you want DynamoDB Local to run on (defaults to 8080).

```clojure
:dynamodb-local {:port 12345}
```

#### :in-memory?

Set if you want DynamoDB Local to be run in memory (defaults to false).

Note: This cannot be used in conjunction with the `:db-path` parameter.

```clojure
:dynamodb-local {:in-memory? true}
```

#### :db-path

Set the path that DynamoDB Local will write its database file to (defaults to the current directory).

Note: This cannot be used in conjunction with the `:in-memory?` parameter.

```clojure
:dynamodb-local {:db-path "/path/to/desired/db/output/"}
```

## License

Copyright © 2014 Donovan McGillen

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure.
