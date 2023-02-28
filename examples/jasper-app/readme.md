Uses the rally-domain from gorm-tools with some report examples. 
`gw jasper-app:bootRun` then go to http://localhost:8080/reports

## Jasper Studio

### DataSource reports
To do some example reports against the database, 
run `gw -Dgrails.env=prod jasper-app:bootRun` to generate the `rallyDb.mv.db`, bootstrap generates a bunch of data into it. 

Then the RallyDataAdapter.jrdax can be used to play with reports that are sql based. 
