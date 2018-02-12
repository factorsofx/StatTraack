StatTrack
=========

StatTrack is a Discord bot written in Java that stores and provides messaging statistics for any guild it is present in. It uses JDA as its Discord library, and Mongo is currently the only database backend available.

Features
--------

- [x] Only provides data about users who explicitly opt into stat tracking
- [x] Persists data with a database
- [ ] Customizable data display commands

How to Build
------------

StatTrack uses gradle for dependency and build management. The application plugin is used to provide an executable.

How to Run
----------

StatTrack is configured with the use of environment variables. All environment variables used are documented below.

Environment Variable | Description
-------------------- | -----------
`BOT_TOKEN`          | The bot token to use when connecting to discord. Only required environment variable.
`CMD_PREFIX`         | The prefix for commands. When not specified, defaults to `::`.
`MONGO_HOST`         | The hostname to use when connecting to mongo. When not specified, defaults to `localhost`.
`MONGO_PORT`         | The port to use when connecting to mongo. When not specified, defaults to `27017`.
`DB_NAME`            | The database name to use when connecting to mongo. When not specified, defaults to `StatTrack`.
