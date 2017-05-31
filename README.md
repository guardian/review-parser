# Review Parser

These projects extract reviews from existing content on capi and indexes them
to CODE or PROD.

1. Get valid composer credentials.
2. Create a conf file and put in ~/.gu. (Make sure your key gives you full access)
3. Run

```
$ sbt "games/run <stage>"
$ sbt "restraunts/run <stage> <api-key> <MARINA|JAY>"
```


