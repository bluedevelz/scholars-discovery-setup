# Quick runner/wrapper for scholars-discovery project

Instructions:

1) git clone git@github.com:vivo-community/scholars-discovery.git 
2) `docker-compose up`

Assumes the scholars-discovery code is checked out into the 
`scholars-discovery` directory.  

You can change this to false after the first start up has finished
(it imports some sample data into a mariadb instance)

```yaml
middleware:
  index:
    onStartup: true
```




