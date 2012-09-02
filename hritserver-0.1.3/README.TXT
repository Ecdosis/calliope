REQUIREMENTS 
couchdb must be installed. It can be found at: 
http://couchdb.apache.org/

In Debian/Ubuntu you can install it directly:

sudo apt-get install couchdb

Installation on MacOSX

1. Download the latest Erlang source. On 64 bit systems (most of them 
nowadays) configure with --enable-darwin-64bit. Otherwise it compiles in 
32 bit and it won't work with the other components, especially icu. Then 
make, make install as usual.

2. Download and install ICU (configure, make, make install)

3. Download and install couchdb. Configure, make, make install. 

SETTING UP THE DATABASE
To install the test databases:
./upload.sh

To launch couchdb if it won't start automatically:

nohup couchdb &

To install the test corcode, cortex and corform databases:

./upload.sh

RUNNING To run hritserver from the command line:

./hritserver-start.sh 

The service will run even when you log off. To stop the service, log in 
as the same user who launched it initially and type:

./hritserver-stop.sh

The service is exposed on port 8080. For it to be globally visible that 
port should be opened on the firewall. Alternatively, call it only 
locally from your web server script and serve the result via port 80.
The latest documentation for the HRIT service can be found at 
http://dhtestbed.ctsdh.luc.edu/hritinfrastructure/index.php/rest-api

USING 
Adding extra cortexs and corcodes to the database can be performed using 
curl. For hierarchical URLs use %2F instead of slash in the docid only:
curl -X PUT http://localhost:5984/cortex/english%2Fshakespeare%2Fkinglear%2Fact1%2Fscene1%2F
(Note the slashses around the database name)

Version F1 of this file can then be simply retrieved via:
curl -X GET http://localhost:8080/html/english/shakespeare/kinglear/act1/scene1/F1

This uses the default corcode and the default corform for that cortex, although you can specify others (see the API documentation)
