# Review Parser

These projects extract reviews from existing content on capi and indexes them
to CODE or PROD.

1. Get valid composer credentials.
2. Create a conf file and put in ~/.gu. (Make sure your key gives you full access)
3. Run

```
$ sbt "games/run <stage> [<content-id>]"
$ sbt "films/run <stage> <omdb api-key> [<content-id>]"
$ sbt "restraunts/run <stage> <google maps api-key> <MARINA|JAY>"
```
For games and films, leave out the optional `content-id` param for a full reindex.

