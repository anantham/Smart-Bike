import datetime
from bs4 import BeautifulSoup
import urllib2
import MySQLdb

last_second = -1
url = "http://localhost/sample.html"
time_gap = 5
t = 0
q = 0

def getData():
    page = urllib2.urlopen(url)
    soup = BeautifulSoup(page)
    pin1 = soup.text[soup.text.find("=")+1:soup.text.find("=")+4] 
    pin2 = soup.text[soup.text.rfind("=")+1:soup.text.rfind("=")+4]
    print "pin1 has "+pin1
    print "pin2 has "+pin2
    return (pin1,pin2)

def storeData(tup):
    # Open database connection
    db = MySQLdb.connect( "localhost","root","","smartbike" )

    # prepare a cursor object using cursor() method
    cursor = db.cursor()

    value1 = int(tup[0])
    value2 = int(tup[1])

    # Prepare SQL query to INSERT a record into the database.
    sql = "INSERT INTO `smartbike`.`master` (`ID`, `Name`, `Accident`) VALUES ('"+str(value1)+"', '"+str(value2)+"', '0');"
    
    try:
       # Execute the SQL command
       cursor.execute(sql)
       print "done"
       # Commit your changes in the database
       db.commit()
    except:
       # Rollback in case there is any error
       db.rollback()
       
    # disconnect from server
    db.close()

while(True):
    # infinite loop guys! :p
    current_seconds = int(datetime.datetime.strftime(datetime.datetime.now(), '%Y-%m-%d %H:%M:%S')[17:])
    
    if( current_seconds%time_gap == 0 and last_second != current_seconds ):
        print current_seconds
        data = getData()
        storeData(data)
        last_second = current_seconds


    
